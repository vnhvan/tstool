package com.offline.saveeditor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.storage.BackupInfo
import com.offline.saveeditor.storage.PersistedHistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRow(backup: BackupInfo, onOpen: () -> Unit, onInspect: () -> Unit, onExport: () -> Unit, onDelete: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(backup.fileName)
        Text("${backup.size} byte · SHA ${backup.sha256.take(12)}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpen) { Text("Mở backup") }
            TextButton(onClick = onInspect) { Text("Kiểm tra") }
            TextButton(onClick = onExport) { Text("Xuất lại") }
            TextButton(onClick = onDelete) { Text("Xóa") }
        }
    }
}

@Composable
fun HistoryRow(entry: PersistedHistoryEntry) {
    val formatted = remember(entry.createdAtEpochMillis) {
        SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(entry.createdAtEpochMillis))
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("$formatted · ${entry.outputName}")
        Text(entry.summary.ifBlank { "Không có mô tả thay đổi" })
        Text("Output SHA: ${entry.outputSha256.take(12)}")
    }
}
