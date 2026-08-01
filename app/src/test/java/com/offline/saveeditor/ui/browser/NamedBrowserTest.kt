package com.offline.saveeditor.ui.browser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NamedBrowserTest {
    @Test fun filtersAndClampsPage() {
        val source = (1..55).map { "backup_$it" }
        val result = NamedBrowser.browse(source, "backup_", 99, 20) { it }
        assertEquals(3, result.totalPages)
        assertEquals(2, result.page)
        assertEquals(15, result.items.size)
        assertTrue(result.canPrevious)
        assertFalse(result.canNext)
    }

    @Test fun emptyResultStillHasStablePage() {
        val result = NamedBrowser.browse(listOf("a"), "missing", 4, 20) { it }
        assertEquals(0, result.items.size)
        assertEquals(0, result.page)
        assertEquals(1, result.totalPages)
    }
}
