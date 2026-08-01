package com.offline.saveeditor.edit

import com.offline.saveeditor.model.SaveDocument
import com.offline.saveeditor.parser.SaveXmlEditor

data class ModulePreflight(
    val rule: EditRule,
    val variableExists: Boolean,
    val currentValue: String?,
    val writable: Boolean,
    val reason: String,
)

object ModulePreflightEngine {
    fun inspect(document: SaveDocument?, rules: List<EditRule>): List<ModulePreflight> {
        val vars = document?.let { SaveXmlEditor.listVars(it.xml).associateBy { entry -> entry.name.lowercase() } }.orEmpty()
        return rules.map { rule ->
            val entry = vars[rule.variableName.lowercase()]
            val verified = rule.confidence == EditConfidence.VERIFIED
            ModulePreflight(
                rule = rule,
                variableExists = entry != null,
                currentValue = entry?.value,
                writable = document != null && document.canEncode && verified && entry != null,
                reason = when {
                    document == null -> "Chưa mở save"
                    !document.canEncode -> "Nguồn XML chỉ đọc; cần container nhị phân để encode"
                    !verified -> "Rule chưa được xác minh"
                    entry == null -> "Không tìm thấy biến ${rule.variableName} trong save"
                    else -> "Đủ điều kiện tạo save đã chỉnh"
                },
            )
        }
    }
}
