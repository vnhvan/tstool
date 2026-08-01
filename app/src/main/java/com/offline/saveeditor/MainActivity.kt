package com.offline.saveeditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.offline.saveeditor.ui.ChucksOfflineTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.offline.saveeditor.crash.CrashReporter
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.restore.RestoreStagingStore
import com.offline.saveeditor.settings.SettingsStore
import com.offline.saveeditor.state.EditorViewModel
import com.offline.saveeditor.storage.BackupStore
import com.offline.saveeditor.storage.HistoryStore
import com.offline.saveeditor.coin.CoinWorkerClient
import com.offline.saveeditor.ui.OfflineEditorApp
import com.offline.saveeditor.ui.launcher.rememberDocumentLaunchers
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsStore = SettingsStore(this)
        val initialSettings = settingsStore.load()
        val backupStore = BackupStore(this, initialSettings.maxBackups)
        val historyStore = HistoryStore(this)
        val restoreStore = RestoreStagingStore(this)
        val crashReporter = CrashReporter(this)
        val coinWorker = CoinWorkerClient(this)

        setContent {
            ChucksOfflineTheme {
                val editorViewModel: EditorViewModel = viewModel()
                val session by editorViewModel.session.collectAsStateWithLifecycle()
                LaunchedEffect(editorViewModel) {
                    editorViewModel.attachStores(backupStore, historyStore, restoreStore)
                    editorViewModel.attachCoinWorker(coinWorker)
                }
                var settings by remember { mutableStateOf(initialSettings) }
                val launchers = rememberDocumentLaunchers(
                    resolver = contentResolver,
                    onOpenBytes = { editorViewModel.openDocument(it, "file đã chọn") },
                    onCompareBytes = editorViewModel::compareDocument,
                    onExportStarted = { editorViewModel.markExportWriting(it.safeName) },
                    onExported = { payload: ExportPayload ->
                        payload.audit?.let { audit ->
                            historyStore.add(
                                sourceSha256 = audit.sourceSha256,
                                outputName = payload.safeName,
                                outputSha256 = com.offline.saveeditor.util.sha256(payload.bytes),
                                report = audit.report,
                            )
                        }
                        editorViewModel.markExportCompleted(payload.safeName)
                    },
                    onExportCancelled = { editorViewModel.markExportCancelled(it.safeName) },
                    onError = { editorViewModel.markExportFailed(null, it) },
                )
                OfflineEditorApp(
                    session = session,
                    viewModel = editorViewModel,
                    backupStore = backupStore,
                    historyStore = historyStore,
                    restoreStore = restoreStore,
                    settingsStore = settingsStore,
                    crashReporter = crashReporter,
                    settings = settings,
                    onSettingsChanged = { settings = it },
                    onOpen = launchers.open,
                    onCompare = launchers.compare,
                    onOpenBytes = editorViewModel::openDocument,
                    onExport = launchers.export,
                )
            }
        }
    }
}
