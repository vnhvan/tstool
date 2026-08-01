package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.settings.AppSettings
import com.offline.saveeditor.settings.SettingsStore
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel

@Composable
fun SettingsScreen(
    session: EditorSession,
    viewModel: EditorViewModel,
    initial: AppSettings,
    store: SettingsStore,
    onChanged: (AppSettings) -> Unit,
) {
    var settings by remember(initial) { mutableStateOf(initial) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Cài đặt", style = MaterialTheme.typography.titleLarge)
        Text("Số backup tối đa: ${settings.maxBackups}")
        Slider(value = settings.maxBackups.toFloat(), onValueChange = { settings = settings.copy(maxBackups = it.toInt().coerceIn(5, 100)) }, valueRange = 5f..100f, steps = 18)
        Row { Switch(checked = settings.showCandidateModules, onCheckedChange = { settings = settings.copy(showCandidateModules = it) }); Spacer(Modifier.width(8.dp)); Text("Hiển thị module Candidate") }
        Row { Switch(checked = settings.enableRootReadOnlyProbe, onCheckedChange = { settings = settings.copy(enableRootReadOnlyProbe = it) }); Spacer(Modifier.width(8.dp)); Text("Cho phép root probe chỉ đọc") }
        Button(onClick = { store.save(settings); onChanged(settings); viewModel.updateStatus("Đã lưu cài đặt.") }) { Text("Lưu cài đặt") }
        Text("Ứng dụng không yêu cầu quyền Internet. Root probe chỉ đọc và bị tắt mặc định.")
        Text("State hiện tại: ${session.screen.title}")
    }
}
