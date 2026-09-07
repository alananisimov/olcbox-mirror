package org.olcbox.app.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

/** Only complete downloads are made available to the package installer. */
internal class UpdateDownloadCache(private val directory: File) {
    private val downloadMutex = Mutex()

    fun downloadedFile(asset: AppUpdateAsset): File? = targetFile(asset).takeIf { file ->
        file.isFile && file.length() > 0L &&
            (asset.sizeBytes == null || file.length() == asset.sizeBytes)
    }

    suspend fun download(
        asset: AppUpdateAsset,
        proxy: Proxy = Proxy.NO_PROXY,
        onProgress: suspend (Float) -> Unit = {}
    ): File = downloadMutex.withLock {
        withContext(Dispatchers.IO) {
            downloadedFile(asset)?.let { return@withContext it }
            check(directory.isDirectory || directory.mkdirs()) { "Could not create update directory" }
            val target = targetFile(asset)
            val partial = File(directory, "${target.name}.part")
            val connection = URL(asset.downloadUrl).openConnection(proxy) as HttpURLConnection
            try {
                connection.connectTimeout = 10_000
                connection.readTimeout = 60_000
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("Update download returned HTTP ${connection.responseCode}")
                }
                val contentLength = connection.contentLengthLong.takeIf { it > 0L }
                val total = asset.sizeBytes ?: contentLength
                var copied = 0L
                connection.inputStream.use { input ->
                    partial.outputStream().use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        while (true) {
                            coroutineContext.ensureActive()
                            val read = input.read(buffer)
                            if (read < 0) break
                            output.write(buffer, 0, read)
                            copied += read
                            if (total != null && total > 0L) {
                                onProgress((copied.toDouble() / total).toFloat().coerceIn(0f, 1f))
                            }
                        }
                    }
                }
                if (copied == 0L ||
                    (contentLength != null && copied != contentLength) ||
                    (asset.sizeBytes != null && copied != asset.sizeBytes)
                ) {
                    throw IOException("Update download is incomplete")
                }
                coroutineContext.ensureActive()
                if (!partial.renameTo(target)) throw IOException("Could not save downloaded update")
                onProgress(1f)
                target
            } finally {
                connection.disconnect()
                partial.delete()
            }
        }
    }

    private fun targetFile(asset: AppUpdateAsset): File {
        val identity = listOf(asset.downloadUrl, asset.updatedAt, asset.sizeBytes).joinToString("|")
        val hash = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray())
            .joinToString("") { "%02x".format(it) }
        val name = asset.name.substringAfterLast('/').substringAfterLast('\\')
            .ifBlank { "olcbox-update.apk" }
        return File(directory, "$hash-$name")
    }
}
