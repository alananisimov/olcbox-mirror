package org.olcbox.app.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TrafficQuotaTest {
    @Test
    fun parsesUsedTotalAndExplicitAvailable() {
        val quota = assertNotNull(parseTrafficQuota("500mb/10gb", "9.5gb"))

        assertEquals("500mb", quota.usedLabel)
        assertEquals("9.5gb", quota.availableLabel)
        assertEquals(0.95f, quota.remainingFraction, absoluteTolerance = 0.001f)
    }

    @Test
    fun derivesAvailableFromUsedAndTotal() {
        val quota = assertNotNull(parseTrafficQuota("2 GB / 8 GB", null))

        assertEquals("6 GB", quota.availableLabel)
        assertEquals(0.75f, quota.remainingFraction, absoluteTolerance = 0.001f)
    }

    @Test
    fun derivesTotalFromSeparateUsedAndAvailable() {
        val quota = assertNotNull(parseTrafficQuota("1gb", "3gb"))

        assertEquals(0.75f, quota.remainingFraction, absoluteTolerance = 0.001f)
    }

    @Test
    fun rejectsQuotaWithoutEnoughInformation() {
        assertNull(parseTrafficQuota(null, "3gb"))
        assertNull(parseTrafficQuota("unlimited", "3gb"))
    }

    @Test
    fun descriptionTakesPriorityAndCommentRemainsCompatible() {
        assertEquals(
            "Preferred description",
            SubscriptionMetadata(
                description = "Preferred description",
                comment = "Legacy comment"
            ).displayDescription()
        )
        assertEquals(
            "Legacy comment",
            LocationMetadata(comment = "Legacy comment").displayDescription()
        )
    }
}
