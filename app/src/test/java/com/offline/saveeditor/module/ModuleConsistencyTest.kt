package com.offline.saveeditor.module

import com.offline.saveeditor.edit.DefaultEditRuleProvider
import com.offline.saveeditor.edit.EditConfidence
import com.offline.saveeditor.edit.EditRule
import com.offline.saveeditor.edit.EditRuleProvider
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModuleConsistencyTest {
    @Test fun defaultCatalogAndRulesAreConsistent() {
        val report = ModuleConsistency.inspect(ModuleRegistry.catalog, DefaultEditRuleProvider.instance)
        assertTrue(report.isValid, report.issues.joinToString())
    }

    @Test fun verifiedModuleWithoutRuleIsRejected() {
        val catalog = ModuleCatalog.Builder()
            .register(EditorModule("x", "X", ModuleStatus.VERIFIED, "x"))
            .build()
        val rules = EditRuleProvider.Builder().build()
        assertFailsWith<IllegalArgumentException> { ModuleConsistency.inspect(catalog, rules).requireValid() }
    }

    @Test fun invalidRangeIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            EditRuleProvider.Builder().register(EditRule("x", "X", "x", EditConfidence.VERIFIED, 10, 1))
        }
    }
}
