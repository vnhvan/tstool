package com.offline.saveeditor.module

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ModuleCatalogTest {
    @Test fun verifiedModulesAreWritable() {
        assertEquals(listOf("sound"), ModuleRegistry.catalog.writable().map { it.id })
    }
    @Test fun duplicateIdsAreRejected() {
        val b = ModuleCatalog.Builder().register(EditorModule("x", "X", ModuleStatus.BLOCKED, "x"))
        assertFailsWith<IllegalArgumentException> { b.register(EditorModule("x", "X2", ModuleStatus.BLOCKED, "x")) }
    }
}
