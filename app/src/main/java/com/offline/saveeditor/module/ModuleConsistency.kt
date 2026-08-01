package com.offline.saveeditor.module

import com.offline.saveeditor.edit.EditConfidence
import com.offline.saveeditor.edit.EditRuleProvider

data class ModuleConsistencyIssue(val moduleId: String, val message: String)

data class ModuleConsistencyReport(val issues: List<ModuleConsistencyIssue>) {
    val isValid: Boolean get() = issues.isEmpty()
    fun requireValid() {
        require(isValid) { issues.joinToString("; ") { "${it.moduleId}: ${it.message}" } }
    }
}

/** Validates that UI module status and executable edit rules cannot silently diverge. */
object ModuleConsistency {
    fun inspect(catalog: ModuleCatalog, rules: EditRuleProvider): ModuleConsistencyReport {
        val issues = mutableListOf<ModuleConsistencyIssue>()
        catalog.modules.forEach { module ->
            val rule = rules.find(module.id)
            when (module.status) {
                ModuleStatus.VERIFIED -> {
                    if (rule == null) issues += ModuleConsistencyIssue(module.id, "VERIFIED nhưng thiếu edit rule")
                    else if (rule.confidence != EditConfidence.VERIFIED) {
                        issues += ModuleConsistencyIssue(module.id, "VERIFIED nhưng rule không VERIFIED")
                    }
                }
                ModuleStatus.READ_ONLY, ModuleStatus.EXPERIMENTAL -> {
                    if (rule?.confidence == EditConfidence.VERIFIED) {
                        issues += ModuleConsistencyIssue(module.id, "Module chưa VERIFIED nhưng rule đã cho phép ghi")
                    }
                }
                ModuleStatus.BLOCKED -> {
                    if (rule != null && rule.confidence != EditConfidence.BLOCKED) {
                        issues += ModuleConsistencyIssue(module.id, "BLOCKED nhưng rule chưa bị khóa")
                    }
                }
            }
        }
        rules.all().forEach { rule ->
            if (catalog.find(rule.id) == null) issues += ModuleConsistencyIssue(rule.id, "Có edit rule nhưng thiếu module metadata")
        }
        return ModuleConsistencyReport(issues)
    }
}
