package com.offline.saveeditor.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaveMemoryProfileTest {
    @Test fun reportsCombinedRetainedBytes() {
        val profile = SaveMemoryProfile(10, 20)
        assertEquals(30, profile.retainedBytes)
        assertFalse(profile.duplicatesLargePayload)
    }

    @Test fun flagsTwoLargeBuffers() {
        val size = 8 * 1024 * 1024
        assertTrue(SaveMemoryProfile(size, size).duplicatesLargePayload)
    }
}
