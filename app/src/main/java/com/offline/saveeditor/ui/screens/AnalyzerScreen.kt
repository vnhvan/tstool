package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.analysis.AnalysisReporter
import com.offline.saveeditor.analysis.SaveAnalyzer
import com.offline.saveeditor.export.ExportKind
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel

@Composable
fun AnalyzerScreen(session: EditorSession, viewModel: EditorViewModel, onCompare: () -> Unit, onExport: (ExportPayload) -> Unit) {
    val document = session.document
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (document == null) { Text("Hãy mở save ở màn hình Editor trước."); return@Column }
        val snapshot = SaveAnalyzer.snapshot(document.xml)
        val summary = SaveAnalyzer.summarize(snapshot)
        Text("Phân tích", style = MaterialTheme.typography.titleLarge)
        Text("${summary.varCount} Var · ${summary.objectCount} Object · ${summary.duplicateVarCount} biến trùng")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = !session.busy, onClick = viewModel::refreshDiagnostics) { Text("Diagnostics") }
            Button(enabled = !session.busy, onClick = onCompare) { Text("So sánh save") }
        }
        Button(onClick = {
            val text = AnalysisReporter.create(snapshot).toPlainText().toByteArray()
            onExport(ExportPayload(text, "save_analysis.txt", ExportKind.TEXT))
        }) { Text("Xuất báo cáo phân tích") }
        session.diagnostics?.let { diag ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text("ERROR=${diag.errorCount} · WARN=${diag.warningCount} · INFO=${diag.infoCount}")
                Text("Verified edits: ${if (diag.safeForVerifiedEdits) "AN TOÀN" else "BỊ KHÓA"}")
                TextButton(onClick = { onExport(ExportPayload(diag.toPlainText().toByteArray(), "save_diagnostics.txt", ExportKind.TEXT)) }) { Text("Xuất diagnostics") }
            } }
        }
        session.comparisonDiff?.let { diff ->
            Text("Diff: ${diff.vars.size} Var · ${diff.objects.size} Object")
            diff.vars.take(30).forEach { Text("${it.kind} ${it.name}: ${it.before} → ${it.after}") }
            TextButton(onClick = { onExport(ExportPayload(diff.toPlainText().toByteArray(), "full_save_diff.txt", ExportKind.TEXT)) }) { Text("Xuất full diff") }
        }
    }
}
