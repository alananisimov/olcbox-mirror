package org.olcbox.app.vpn

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlin.time.TimeSource

/** Poll the blocking mobile API so a cancelled connection releases the startup lock promptly. */
internal suspend fun awaitRuntimeReady(timeoutMillis: Long, waitReady: (Long) -> Unit) {
    val started = TimeSource.Monotonic.markNow()
    while (true) {
        coroutineContext.ensureActive()
        val remaining = timeoutMillis - started.elapsedNow().inWholeMilliseconds
        if (remaining <= 0) error("olcRTC runtime readiness timed out")
        try {
            waitReady(minOf(remaining, 200L))
            coroutineContext.ensureActive()
            return
        } catch (error: Exception) {
            coroutineContext.ensureActive()
            if (error.message != "olcRTC runtime readiness timed out") throw error
        }
    }
}
