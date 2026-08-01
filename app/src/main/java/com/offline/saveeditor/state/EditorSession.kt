package com.offline.saveeditor.state

import com.offline.saveeditor.analysis.SaveDiff
import com.offline.saveeditor.diagnostics.DiagnosticsReport
import com.offline.saveeditor.model.SaveDocument
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.edit.PendingEditPreview
import com.offline.saveeditor.navigation.AppScreen

data class EditorSession(
    val screen: AppScreen = AppScreen.HOME,
    val status: String = "Chưa chọn file.",
    val varQuery: String = "",
    val objectQuery: String = "",
    val varPage: Int = 0,
    val objectPage: Int = 0,
    val busy: Boolean = false,
    val operationLabel: String? = null,
    val document: SaveDocument? = null,
    val diagnostics: DiagnosticsReport? = null,
    val comparisonDiff: SaveDiff? = null,
    val pendingExport: ExportPayload? = null,
    val pendingEditPreview: PendingEditPreview? = null,
    val coin: CoinDirectState = CoinDirectState(),
    val startDate: StartDateDirectState = StartDateDirectState(),
    val workspace: WorkspaceState = WorkspaceState(),
    val storage: StorageState = StorageState(),
)

data class StartDateDirectState(
    val currentEpochSeconds: Long? = null,
    val pendingEpochSeconds: Long? = null,
    val sourceSha256: String? = null,
    val rootBackupPath: String? = null,
)

data class CoinDirectState(
    val currentValue: Long? = null,
    val pendingValue: Long? = null,
    val sourceSha256: String? = null,
    val rootBackupPath: String? = null,
)

sealed interface EditorAction {
    data class Navigate(val screen: AppScreen) : EditorAction
    data class Status(val message: String) : EditorAction
    data class SearchVars(val query: String) : EditorAction
    data class SearchObjects(val query: String) : EditorAction
    data class VarPage(val page: Int) : EditorAction
    data class ObjectPage(val page: Int) : EditorAction
    data class Busy(val value: Boolean, val label: String? = null) : EditorAction
    data class DocumentLoaded(val document: SaveDocument, val diagnostics: DiagnosticsReport, val message: String) : EditorAction
    data class ComparisonReady(val diff: SaveDiff, val message: String) : EditorAction
    data class OperationFailed(val message: String, val clearDocument: Boolean = false) : EditorAction
    data class ExportReady(val payload: ExportPayload, val message: String) : EditorAction
    data class EditPreviewReady(val preview: PendingEditPreview, val message: String) : EditorAction
    data object EditPreviewDiscarded : EditorAction
    data object ExportConsumed : EditorAction
    data object ClearComparison : EditorAction
    data class SearchBackups(val query: String) : EditorAction
    data class SearchHistory(val query: String) : EditorAction
    data class SearchCrash(val query: String) : EditorAction
    data class BackupPage(val page: Int) : EditorAction
    data class HistoryPage(val page: Int) : EditorAction
    data class CrashPage(val page: Int) : EditorAction
    data class ShowSettings(val value: Boolean) : EditorAction
    data class StorageLoaded(val storage: StorageState, val message: String? = null) : EditorAction
    data class ExportPhaseChanged(val phase: ExportPhase, val fileName: String? = null, val message: String? = null) : EditorAction
    data class CoinLoaded(val value: Long, val sha256: String, val message: String) : EditorAction
    data class CoinPreview(val value: Long) : EditorAction
    data class CoinWritten(val value: Long, val sha256: String, val backupPath: String, val message: String) : EditorAction
    data object CoinPreviewDiscarded : EditorAction
    data class StartDateLoaded(val value: Long, val sha256: String, val message: String) : EditorAction
    data class StartDatePreview(val value: Long) : EditorAction
    data class StartDateWritten(val value: Long, val sha256: String, val backupPath: String, val message: String) : EditorAction
    data object StartDatePreviewDiscarded : EditorAction
    data object ResetDocumentUi : EditorAction
}

object EditorReducer {
    fun reduce(state: EditorSession, action: EditorAction): EditorSession = when (action) {
        is EditorAction.Navigate -> state.copy(screen = action.screen)
        is EditorAction.Status -> state.copy(status = action.message)
        is EditorAction.SearchVars -> state.copy(varQuery = action.query, varPage = 0)
        is EditorAction.SearchObjects -> state.copy(objectQuery = action.query, objectPage = 0)
        is EditorAction.VarPage -> state.copy(varPage = action.page.coerceAtLeast(0))
        is EditorAction.ObjectPage -> state.copy(objectPage = action.page.coerceAtLeast(0))
        is EditorAction.Busy -> state.copy(busy = action.value, operationLabel = if (action.value) action.label else null)
        is EditorAction.DocumentLoaded -> state.copy(
            document = action.document,
            diagnostics = action.diagnostics,
            comparisonDiff = null,
            pendingEditPreview = null,
            status = action.message,
            busy = false,
            operationLabel = null,
            varQuery = "",
            objectQuery = "",
            varPage = 0,
            objectPage = 0,
        )
        is EditorAction.ComparisonReady -> state.copy(
            comparisonDiff = action.diff,
            status = action.message,
            busy = false,
            operationLabel = null,
        )
        is EditorAction.ExportReady -> state.copy(pendingExport = action.payload, pendingEditPreview = null, storage = state.storage.copy(exportPhase = ExportPhase.READY, lastExportName = action.payload.safeName), status = action.message, busy = false, operationLabel = null)
        is EditorAction.EditPreviewReady -> state.copy(pendingEditPreview = action.preview, status = action.message, busy = false, operationLabel = null)
        EditorAction.EditPreviewDiscarded -> state.copy(pendingEditPreview = null, status = "Đã hủy bản chỉnh sửa chưa xuất.")
        EditorAction.ExportConsumed -> state.copy(pendingExport = null)
        is EditorAction.OperationFailed -> state.copy(
            document = if (action.clearDocument) null else state.document,
            diagnostics = if (action.clearDocument) null else state.diagnostics,
            comparisonDiff = if (action.clearDocument) null else state.comparisonDiff,
            status = action.message,
            busy = false,
            operationLabel = null,
        )
        EditorAction.ClearComparison -> state.copy(comparisonDiff = null)
        is EditorAction.SearchBackups -> state.copy(workspace = state.workspace.copy(backup = state.workspace.backup.search(action.query)))
        is EditorAction.SearchHistory -> state.copy(workspace = state.workspace.copy(history = state.workspace.history.search(action.query)))
        is EditorAction.SearchCrash -> state.copy(workspace = state.workspace.copy(crash = state.workspace.crash.search(action.query)))
        is EditorAction.BackupPage -> state.copy(workspace = state.workspace.copy(backup = state.workspace.backup.moveTo(action.page)))
        is EditorAction.HistoryPage -> state.copy(workspace = state.workspace.copy(history = state.workspace.history.moveTo(action.page)))
        is EditorAction.CrashPage -> state.copy(workspace = state.workspace.copy(crash = state.workspace.crash.moveTo(action.page)))
        is EditorAction.ShowSettings -> state.copy(workspace = state.workspace.copy(showSettings = action.value))
        is EditorAction.StorageLoaded -> state.copy(storage = action.storage, status = action.message ?: state.status, busy = false, operationLabel = null)
        is EditorAction.ExportPhaseChanged -> state.copy(
            storage = state.storage.copy(exportPhase = action.phase, lastExportName = action.fileName ?: state.storage.lastExportName),
            status = action.message ?: state.status,
        )
        is EditorAction.CoinLoaded -> state.copy(
            coin = state.coin.copy(currentValue = action.value, pendingValue = null, sourceSha256 = action.sha256),
            status = action.message,
            busy = false,
            operationLabel = null,
        )
        is EditorAction.CoinPreview -> state.copy(coin = state.coin.copy(pendingValue = action.value), status = "Đã tạo bản xem trước Coin.")
        is EditorAction.CoinWritten -> state.copy(
            coin = CoinDirectState(action.value, null, action.sha256, action.backupPath),
            status = action.message,
            busy = false,
            operationLabel = null,
        )
        EditorAction.CoinPreviewDiscarded -> state.copy(coin = state.coin.copy(pendingValue = null), status = "Đã hủy thay đổi Coin.")
        is EditorAction.StartDateLoaded -> state.copy(
            startDate = state.startDate.copy(currentEpochSeconds = action.value, pendingEpochSeconds = null, sourceSha256 = action.sha256),
            status = action.message, busy = false, operationLabel = null,
        )
        is EditorAction.StartDatePreview -> state.copy(
            startDate = state.startDate.copy(pendingEpochSeconds = action.value),
            status = "Đã tạo bản xem trước ngày tạo.",
        )
        is EditorAction.StartDateWritten -> state.copy(
            startDate = StartDateDirectState(action.value, null, action.sha256, action.backupPath),
            status = action.message, busy = false, operationLabel = null,
        )
        EditorAction.StartDatePreviewDiscarded -> state.copy(
            startDate = state.startDate.copy(pendingEpochSeconds = null),
            status = "Đã hủy thay đổi ngày tạo.",
        )
        EditorAction.ResetDocumentUi -> state.copy(varQuery = "", objectQuery = "", varPage = 0, objectPage = 0)
    }
}
