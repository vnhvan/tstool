package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.module.ModuleRegistry
import com.offline.saveeditor.edit.EditRules
import com.offline.saveeditor.edit.ModulePreflightEngine
import com.offline.saveeditor.restore.RestoreStagingStore
import com.offline.saveeditor.settings.AppSettings
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel

@Composable
fun RestoreScreen(
    session: EditorSession,
    viewModel: EditorViewModel,
    restoreStore: RestoreStagingStore,
    settings: AppSettings,
    onExport: (ExportPayload) -> Unit,
) {
    val storage = session.storage
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Restore và modules", style = MaterialTheme.typography.titleLarge)
        Button(enabled = session.document != null && !session.busy, onClick = viewModel::createRestoreStage) { Text("Tạo restore staging") }
        storage.stagedRestore?.let { item ->
            Text("${item.fileName} · ${item.size} byte · ${item.sha256.take(12)}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = !session.busy, onClick = viewModel::exportRestoreStage) { Text("Xuất restore-ready") }
                TextButton(enabled = !session.busy, onClick = viewModel::clearRestoreStage) { Text("Xóa staging") }
            }
        }
        HorizontalDivider()
        Button(enabled = settings.enableRootReadOnlyProbe && !session.busy, onClick = { viewModel.runRootProbe(settings.enableRootReadOnlyProbe) }) { Text("Root probe chỉ đọc") }
        storage.rootProbe?.let { Text(it.message + if (it.files.isNotEmpty()) "\n" + it.files.take(20).joinToString() else "") }
        HorizontalDivider()
        Text("Module động", style = MaterialTheme.typography.titleMedium)
        val preflight = ModulePreflightEngine.inspect(session.document, EditRules.all).associateBy { it.rule.id }
        ModuleRegistry.catalog.visible(settings.showCandidateModules).forEach { module ->
            val check = preflight[module.id]
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(module.title, style = MaterialTheme.typography.titleSmall)
                Text("${module.status} — ${module.description}")
                check?.let {
                    Text("Biến: ${it.rule.variableName} · hiện tại: ${it.currentValue ?: "không thấy"}")
                    Text(if (it.writable) "Có thể ghi" else "Đang khóa: ${it.reason}", style = MaterialTheme.typography.bodySmall)
                }
            } }
        }
    }
}
