package com.offline.saveeditor.validation

import com.offline.saveeditor.model.SaveDocument
import com.offline.saveeditor.model.SaveSourceKind

data class ValidationIssue(val severity: Severity, val message: String)
enum class Severity { ERROR, WARNING, INFO }

data class ValidationResult(val issues: List<ValidationIssue>) {
    val canExport: Boolean get() = issues.none { it.severity == Severity.ERROR }
}

object SaveValidator {
    fun validate(document: SaveDocument): ValidationResult {
        val issues = buildList {
            if (document.containerSize < 8) add(ValidationIssue(Severity.ERROR, "Container quá ngắn"))
            if (document.sourceKind == SaveSourceKind.BINARY_CONTAINER && document.containerSize != 300_008) add(ValidationIssue(Severity.WARNING, "Kích thước khác mẫu đã xác minh 300.008 byte"))
            if (document.sourceKind == SaveSourceKind.DECODED_XML) add(ValidationIssue(Severity.INFO, "Đang mở XML đã giải mã: chỉ đọc, không thể tạo container save"))
            if (!document.xml.toString(Charsets.UTF_8).startsWith("<root>")) add(ValidationIssue(Severity.ERROR, "XML không bắt đầu bằng <root>"))
            if (!document.xml.toString(Charsets.UTF_8).endsWith("</root>")) add(ValidationIssue(Severity.ERROR, "XML không kết thúc bằng </root>"))
            if (document.fields.soundVolume != null && document.fields.soundVolume !in 0..100) add(ValidationIssue(Severity.WARNING, "Sound Volume ngoài khoảng 0..100"))
            if (document.fields.coin == null) add(ValidationIssue(Severity.INFO, "Không tìm thấy Coin"))
            if (document.fields.tcash == null) add(ValidationIssue(Severity.INFO, "Không tìm thấy TCash"))
        }
        return ValidationResult(issues)
    }
}
