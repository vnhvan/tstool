package com.offline.saveeditor.io

import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class LimitedInputTest {
    @Test fun readsWithinLimit() {
        val data = ByteArray(4097) { (it % 251).toByte() }
        assertContentEquals(data, LimitedInput.read(ByteArrayInputStream(data), maxBytes = 5000, bufferSize = 1024))
    }

    @Test fun rejectsOversizedAndEmptyInput() {
        assertFailsWith<IllegalArgumentException> { LimitedInput.read(ByteArrayInputStream(ByteArray(11)), maxBytes = 10) }
        assertFailsWith<IllegalArgumentException> { LimitedInput.read(ByteArrayInputStream(ByteArray(0)), maxBytes = 10) }
    }
}
