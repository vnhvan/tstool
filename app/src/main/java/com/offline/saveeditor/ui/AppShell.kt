package com.offline.saveeditor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.crash.CrashReporter
import com.offline.saveeditor.export.ExportKind
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.navigation.AppScreen
import com.offline.saveeditor.restore.RestoreStagingStore
import com.offline.saveeditor.settings.AppSettings
import com.offline.saveeditor.settings.SettingsStore
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel
import com.offline.saveeditor.storage.BackupStore
import com.offline.saveeditor.storage.HistoryStore
import com.offline.saveeditor.ui.browser.NamedBrowser
import com.offline.saveeditor.ui.components.BusyPanel
import com.offline.saveeditor.ui.components.PagedControls
import com.offline.saveeditor.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineEditorApp(
    session: EditorSession,
    viewModel: EditorViewModel,
    backupStore: BackupStore,
    historyStore: HistoryStore,
    restoreStore: RestoreStagingStore,
    settingsStore: SettingsStore,
    crashReporter: CrashReporter,
    settings: AppSettings,
    onSettingsChanged: (AppSettings) -> Unit,
    onOpen: () -> Unit,
    onCompare: () -> Unit,
    onOpenBytes: (ByteArray, String) -> Unit,
    onExport: (ExportPayload) -> Unit,
) {
    LaunchedEffect(session.pendingExport) {
        session.pendingExport?.let { onExport(it); viewModel.consumePendingExport() }
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("Chuck's Tool", fontWeight = FontWeight.Bold)
                        Text("OFFLINE EDITION", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                AppScreen.entries.filter { it.showInBottomBar }.forEach { screen ->
                    NavigationBarItem(
                        selected = session.screen == screen,
                        onClick = { viewModel.navigate(screen) },
                        icon = { Text(screen.symbol, style = MaterialTheme.typography.titleMedium) },
                        label = { Text(screen.title) },
                    )
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            if (session.busy) BusyPanel(session.operationLabel ?: "Đang xử lý…", viewModel::cancelActiveOperation, Modifier.fillMaxWidth())
            when (session.screen) {
                AppScreen.HOME -> HomeScreen(
                    onOpenModules = { viewModel.navigate(AppScreen.MODULES) },
                    onOpenFeature = { feature -> when (feature.id) {
                        "coin" -> viewModel.navigate(AppScreen.COIN)
                        "game_start_date" -> viewModel.navigate(AppScreen.START_DATE)
                        else -> viewModel.navigate(AppScreen.MODULES)
                    } },
                )
                AppScreen.MODULES -> ModulesScreen { feature -> when (feature.id) {
                    "coin" -> viewModel.navigate(AppScreen.COIN)
                    "game_start_date" -> viewModel.navigate(AppScreen.START_DATE)
                    else -> Unit
                } }
                AppScreen.TOOLS -> ToolsHubScreen(session, viewModel, backupStore, historyStore, restoreStore, settings, onOpen, onCompare, onOpenBytes, onExport)
                AppScreen.SETTINGS -> SettingsScreen(session, viewModel, settings, settingsStore, onSettingsChanged)
                AppScreen.COIN -> CoinScreen(session, viewModel)
                AppScreen.START_DATE -> StartDateScreen(session, viewModel)
            }
            if (session.screen == AppScreen.TOOLS || session.screen == AppScreen.SETTINGS) {
                CrashLogPanel(session, viewModel, crashReporter, onExport)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun CrashLogPanel(session: EditorSession, viewModel: EditorViewModel, crashReporter: CrashReporter, onExport: (ExportPayload) -> Unit) {
    var logsVersion by remember { mutableIntStateOf(0) }
    val logs = remember(logsVersion) { crashReporter.list() }
    val state = session.workspace.crash
    HorizontalDivider()
    Text("Crash logs (${logs.size})", style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(value = state.query, onValueChange = viewModel::updateCrashQuery, label = { Text("Tìm crash log") }, singleLine = true, modifier = Modifier.fillMaxWidth())
    val page = remember(logs, state) { NamedBrowser.browse(logs, state.query, state.page, 5) { it.name } }
    page.items.forEach { file ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(file.name, Modifier.weight(1f))
            TextButton(onClick = { runCatching { crashReporter.read(file.name) }.onSuccess { onExport(ExportPayload(it, file.name, ExportKind.TEXT)) }.onFailure { viewModel.updateStatus("Không thể đọc crash log: ${it.message}") } }) { Text("Xuất") }
        }
    }
    PagedControls(page.page, page.totalPages, page.canPrevious, page.canNext, { viewModel.setCrashPage(page.page - 1) }, { viewModel.setCrashPage(page.page + 1) })
    if (logs.isNotEmpty()) TextButton(onClick = { crashReporter.clear(); logsVersion++ }) { Text("Xóa crash logs") }
}
