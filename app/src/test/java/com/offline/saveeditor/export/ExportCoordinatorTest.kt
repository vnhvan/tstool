package com.offline.saveeditor.export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExportCoordinatorTest {
    @Test fun sanitizesName() {
        val p = ExportPayload(byteArrayOf(1), "../bad name?.txt", ExportKind.TEXT)
        assertEquals("bad_name_.txt", p.safeName)
    }

    @Test fun rejectsEmptyOutput() {
        assertFailsWith<IllegalArgumentException> {
            ExportPayload(byteArrayOf(), "x.txt", ExportKind.TEXT)
        }
    }
}
