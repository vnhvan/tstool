package com.offline.saveeditor.diagnostics

import com.offline.saveeditor.analysis.KnowledgeBase
import com.offline.saveeditor.analysis.SaveAnalyzer
import com.offline.saveeditor.model.SaveDocument
import com.offline.saveeditor.module.ModuleRegistry
import com.offline.saveeditor.validation.ContainerInspector
import com.offline.saveeditor.validation.SaveValidator

enum class DiagnosticSeverity { INFO, WARNING, ERROR }

data class DiagnosticItem(val severity: DiagnosticSeverity, val code: String, val message: String)

data class DiagnosticsReport(val items: List<DiagnosticItem>) {
    val errorCount get() = items.count { it.severity == DiagnosticSeverity.ERROR }
    val warningCount get() = items.count { it.severity == DiagnosticSeverity.WARNING }
    val infoCount get() = items.count { it.severity == DiagnosticSeverity.INFO }
    val safeForVerifiedEdits get() = errorCount == 0

    fun toPlainText(): String = buildString {
        appendLine("Township Offline Save Editor - Diagnostics")
        appendLine("ERROR=$errorCount | WARN=$warningCount | INFO=$infoCount")
        appendLine("Safe for verified edits: $safeForVerifiedEdits")
        items.forEach { appendLine("${it.severity} [${it.code}] ${it.message}") }
    }
}

object DiagnosticsEngine {
    fun inspect(document: SaveDocument): DiagnosticsReport {
        val items = mutableListOf<DiagnosticItem>()
        val container = ContainerInspector.inspect(document.container)
        if (!container.validHeader || container.isTruncated) {
            items += DiagnosticItem(DiagnosticSeverity.ERROR, "CONTAINER", container.message)
        } else items += DiagnosticItem(DiagnosticSeverity.INFO, "CONTAINER", container.message)

        val validation = SaveValidator.validate(document)
        validation.issues.forEach {
            val severity = when (it.severity.name) {
                "ERROR" -> DiagnosticSeverity.ERROR
                "WARNING" -> DiagnosticSeverity.WARNING
                else -> DiagnosticSeverity.INFO
            }
            items += DiagnosticItem(severity, "STRUCTURE", it.message)
        }
        if (validation.issues.isEmpty()) items += DiagnosticItem(DiagnosticSeverity.INFO, "STRUCTURE", "Không phát hiện lỗi cấu trúc")

        val snapshot = SaveAnalyzer.snapshot(document.xml)
        items += DiagnosticItem(DiagnosticSeverity.INFO, "COUNTS", "${snapshot.vars.size} Var, ${snapshot.objects.size} Object")
        if (snapshot.duplicateVars.isNotEmpty()) {
            items += DiagnosticItem(DiagnosticSeverity.WARNING, "DUPLICATE_VAR", "Biến trùng: ${snapshot.duplicateVars.take(20).joinToString()}")
        }

        KnowledgeBase.knownVars.forEach { item ->
            val value = snapshot.vars[item.key]
            if (value != null) items += DiagnosticItem(DiagnosticSeverity.INFO, "KNOWN_VAR", "${item.label} (${item.key}) = $value · ${item.confidence}")
        }
        ModuleRegistry.modules.forEach { module ->
            items += DiagnosticItem(DiagnosticSeverity.INFO, "MODULE", "${module.title}: ${module.status}")
        }
        if (!document.canEncode) items += DiagnosticItem(DiagnosticSeverity.WARNING, "READ_ONLY", "Nguồn là XML đã giải mã; không thể tạo container mới")
        return DiagnosticsReport(items)
    }
}
