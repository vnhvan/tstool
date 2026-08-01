package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel
import com.offline.saveeditor.storage.BackupStore
import com.offline.saveeditor.storage.HistoryStore
import com.offline.saveeditor.ui.browser.NamedBrowser
import com.offline.saveeditor.ui.components.BackupRow
import com.offline.saveeditor.ui.components.HistoryRow
import com.offline.saveeditor.ui.components.PagedControls

@Composable
fun BackupScreen(
    session: EditorSession,
    viewModel: EditorViewModel,
    backupStore: BackupStore,
    historyStore: HistoryStore,
    onOpenBytes: (ByteArray, String) -> Unit,
    onExport: (ExportPayload) -> Unit,
) {
    val storage = session.storage
    val backupState = session.workspace.backup
    val historyState = session.workspace.history
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Backup", style = MaterialTheme.typography.titleLarge)
        Button(enabled = session.document != null && !session.busy, onClick = viewModel::createBackup) { Text("Backup save đang mở") }
        OutlinedTextField(value = backupState.query, onValueChange = viewModel::updateBackupQuery, label = { Text("Tìm backup") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        val page = remember(storage.backups, backupState) { NamedBrowser.browse(storage.backups, backupState.query, backupState.page, 5) { it.fileName + " " + it.sha256 } }
        Text("${page.totalItems} backup · trang ${page.page + 1}/${page.totalPages}")
        page.items.forEach { backup ->
            BackupRow(
                backup,
                onOpen = { viewModel.openBackup(backup.fileName) },
                onInspect = { viewModel.inspectBackup(backup.fileName) },
                onExport = { viewModel.exportBackup(backup.fileName) },
                onDelete = { viewModel.deleteBackup(backup.fileName) },
            )
        }
        PagedControls(page.page, page.totalPages, page.canPrevious, page.canNext, { viewModel.setBackupPage(page.page - 1) }, { viewModel.setBackupPage(page.page + 1) })
        storage.backupInspection?.let { item ->
            Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) {
                Text(item.backup.fileName)
                Text("SHA tên file: ${if (item.fileNameDigestMatches) "khớp" else "không khớp"}")
                Text("Container: ${item.container.kind} · ${if (item.isValid) "hợp lệ" else "không an toàn"}")
            } }
        }
        HorizontalDivider()
        Text("Lịch sử xuất file", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = historyState.query, onValueChange = viewModel::updateHistoryQuery, label = { Text("Tìm lịch sử") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        val historyPage = remember(storage.history, historyState) { NamedBrowser.browse(storage.history, historyState.query, historyState.page, 5) { it.outputName + " " + it.summary + " " + it.outputSha256 } }
        historyPage.items.forEach { HistoryRow(it) }
        PagedControls(historyPage.page, historyPage.totalPages, historyPage.canPrevious, historyPage.canNext, { viewModel.setHistoryPage(historyPage.page - 1) }, { viewModel.setHistoryPage(historyPage.page + 1) })
        if (storage.history.isNotEmpty()) TextButton(enabled = !session.busy, onClick = viewModel::clearHistory) { Text("Xóa lịch sử") }
    }
}
