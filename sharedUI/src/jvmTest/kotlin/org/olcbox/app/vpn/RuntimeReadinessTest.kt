package org.olcbox.app.vpn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RuntimeReadinessTest {
    @Test
    fun cancellingAStuckStartDoesNotWaitForTheConnectionTimeout() = runBlocking {
        val entered = CountDownLatch(1)
        val job = launch(Dispatchers.IO) {
            awaitRuntimeReady(60_000) { timeout ->
                entered.countDown()
                Thread.sleep(timeout)
                error("olcRTC runtime readiness timed out")
            }
        }
        assertTrue(entered.await(2, TimeUnit.SECONDS))
        withTimeout(1_000) { job.cancelAndJoin() }
    }

    @Test
    fun reportsCoreFailureWithoutRetryingUntilTheDeadline() = runBlocking {
        var calls = 0
        val error = assertFailsWith<IllegalStateException> {
            awaitRuntimeReady(60_000) {
                calls++
                error("handshake failed")
            }
        }
        assertEquals("handshake failed", error.message)
        assertEquals(1, calls)
    }
}
