package com.offline.saveeditor.edit

import com.offline.saveeditor.analysis.SaveAnalyzer
import com.offline.saveeditor.analysis.SaveDiff
import com.offline.saveeditor.analysis.SaveDiffEngine
import com.offline.saveeditor.model.SaveDocument
import com.offline.saveeditor.model.SaveRepository
import com.offline.saveeditor.parser.SaveXmlEditor
import com.offline.saveeditor.util.sha256

object SafeEditEngine {
    fun prepare(document: SaveDocument, rule: EditRule, newValue: Long, allowCandidate: Boolean = false): PreparedEdit {
        require(document.canEncode) { "Cần mở mGameInfo nhị phân để tạo save mới" }
        require(rule.confidence != EditConfidence.BLOCKED) { "Module ${rule.title} đang bị khóa" }
        require(rule.confidence == EditConfidence.VERIFIED || allowCandidate) {
            "Module ${rule.title} chưa được xác minh; chỉ cho phép xem và phân tích"
        }
        rule.minValue?.let { require(newValue >= it) { "${rule.title} phải >= $it" } }
        rule.maxValue?.let { require(newValue <= it) { "${rule.title} phải <= $it" } }

        val before = SaveAnalyzer.snapshot(document.xml)
        val old = before.vars[rule.variableName] ?: error("Không tìm thấy biến ${rule.variableName}")
        val edited = SaveXmlEditor.setVar(document.xml, rule.variableName, newValue.toString())
        val after = SaveAnalyzer.snapshot(edited)
        val diff = SaveDiffEngine.between(before, after)
        require(diff.vars.size == 1 && diff.objects.isEmpty()) {
            "Quy tắc chỉnh sửa không an toàn: dự kiến 1 Var thay đổi nhưng có ${diff.vars.size} Var và ${diff.objects.size} Object"
        }
        val only = diff.vars.single()
        require(only.name.equals(rule.variableName, ignoreCase = true)) {
            "Quy tắc chỉnh sửa tác động sai biến: ${only.name}"
        }
        return PreparedEdit(rule, old, newValue.toString(), edited, diff)
    }

    fun encodeAndVerify(document: SaveDocument, prepared: PreparedEdit): EncodedEdit {
        val output = SaveRepository.encodeAndVerify(document, prepared.editedXml)
        val reopened = SaveRepository.open(output)
        val actual = SaveAnalyzer.snapshot(reopened.xml).vars[prepared.rule.variableName]
        require(actual == prepared.newValue) { "Giá trị sau encode/đọc lại không khớp: $actual" }
        return EncodedEdit(prepared, output, sha256(output))
    }
}
