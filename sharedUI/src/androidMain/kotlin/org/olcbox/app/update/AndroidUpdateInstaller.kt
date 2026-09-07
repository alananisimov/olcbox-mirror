package org.olcbox.app.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.olcbox.app.data.datasource.withProxyAuthentication
import org.olcbox.app.data.repository.SubscriptionFetchProxy
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy

class AndroidUpdateInstaller(
    context: Context,
    private val proxyProvider: () -> SubscriptionFetchProxy? = { null }
) {
    private val appContext = context.applicationContext
    private val downloads = UpdateDownloadCache(File(appContext.cacheDir, "updates"))

    fun downloadedFile(asset: AppUpdateAsset): File? = downloads.downloadedFile(asset)

    fun canRequestPackageInstalls(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                appContext.packageManager.canRequestPackageInstalls()
    }

    fun unknownSourcesSettingsIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${appContext.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun openUnknownSourcesSettings(): Result<Unit> = runCatching {
        appContext.startActivity(unknownSourcesSettingsIntent())
    }

    suspend fun downloadAndOpen(
        asset: AppUpdateAsset,
        onProgress: (Float) -> Unit = {}
    ): Result<String> {
        return runCatching {
            if (!canRequestPackageInstalls()) {
                openUnknownSourcesSettings().getOrThrow()
                return@runCatching "Allow Olcbox to install updates, then tap Download again"
            }

            val file = download(asset, onProgress).getOrThrow()
            val installIntent = installIntent(file)
            try {
                appContext.startActivity(installIntent)
            } catch (error: ActivityNotFoundException) {
                val uri = uriFor(file)
                appContext.startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType(file.name))
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                )
            }
            "Installing ${asset.name}"
        }
    }

    fun installIntent(file: File): Intent {
        return installIntent(uriFor(file), file.name)
    }

    fun relaunchIntent(): Intent? {
        return appContext.packageManager
            .getLaunchIntentForPackage(appContext.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }

    private fun installIntent(uri: Uri, name: String): Intent {
        return Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            data = uri
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
            putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            setDataAndType(uri, mimeType(name))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    private fun uriFor(file: File): Uri {
        return FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
    }

    suspend fun download(asset: AppUpdateAsset, onProgress: (Float) -> Unit): Result<File> {
        return try {
            val proxy = proxyProvider()
            Result.success(withProxyAuthentication(proxy) {
                val connectionProxy = proxy?.let {
                    Proxy(Proxy.Type.SOCKS, InetSocketAddress(it.host, it.port))
                } ?: Proxy.NO_PROXY
                downloads.download(asset, connectionProxy) { reportProgress(it, onProgress) }
            })
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun reportProgress(progress: Float, onProgress: (Float) -> Unit) {
        withContext(Dispatchers.Main.immediate) {
            onProgress(progress)
        }
    }

    private fun mimeType(name: String): String {
        return when {
            name.endsWith(".apk", ignoreCase = true) -> "application/vnd.android.package-archive"
            else -> "application/octet-stream"
        }
    }
}
