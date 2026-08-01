package com.offline.saveeditor.analysis

import com.offline.saveeditor.ui.Paging
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PagingTest {
    @Test fun clampsPagesAndPreservesCounts() {
        val values = (1..53).toList()
        val first = Paging.page(values, 0, 20)
        assertEquals(20, first.items.size)
        assertEquals(3, first.totalPages)
        assertFalse(first.canPrevious)
        assertTrue(first.canNext)
        val last = Paging.page(values, 99, 20)
        assertEquals(13, last.items.size)
        assertEquals(2, last.page)
        assertFalse(last.canNext)
    }
}
