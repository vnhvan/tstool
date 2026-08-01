package com.offline.saveeditor.util

import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class HashingStreamTest {
    @Test fun streamAndByteArrayDigestsMatch() {
        val bytes = ByteArray(200_000) { (it % 251).toByte() }
        assertEquals(sha256(bytes), sha256(ByteArrayInputStream(bytes), 4096))
    }
}
