package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.crash.CrashReporter
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.restore.RestoreStagingStore
import com.offline.saveeditor.settings.AppSettings
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel
import com.offline.saveeditor.storage.BackupStore
import com.offline.saveeditor.storage.HistoryStore

enum class ToolPage(val title: String) { EDITOR("Save"), ANALYZER("Analyzer"), BACKUP("Backup"), RESTORE("Restore") }

@Composable fun ToolsHubScreen(session: EditorSession, viewModel: EditorViewModel, backupStore: BackupStore, historyStore: HistoryStore, restoreStore: RestoreStagingStore, settings: AppSettings, onOpen: () -> Unit, onCompare: () -> Unit, onOpenBytes: (ByteArray, String) -> Unit, onExport: (ExportPayload) -> Unit) {
    var page by remember { mutableStateOf(ToolPage.EDITOR) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Developer Tools", style = MaterialTheme.typography.headlineSmall)
        Text("Các công cụ kỹ thuật được giữ riêng, không làm rối giao diện module chính.")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            ToolPage.entries.forEachIndexed { index, item -> SegmentedButton(selected = page == item, onClick = { page = item }, shape = SegmentedButtonDefaults.itemShape(index, ToolPage.entries.size)) { Text(item.title) } }
        }
        when (page) {
            ToolPage.EDITOR -> EditorScreen(session, viewModel, onOpen, onExport)
            ToolPage.ANALYZER -> AnalyzerScreen(session, viewModel, onCompare, onExport)
            ToolPage.BACKUP -> BackupScreen(session, viewModel, backupStore, historyStore, onOpenBytes, onExport)
            ToolPage.RESTORE -> RestoreScreen(session, viewModel, restoreStore, settings, onExport)
        }
    }
}
