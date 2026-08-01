package com.offline.saveeditor.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.offline.saveeditor.analysis.SaveAnalyzer
import com.offline.saveeditor.analysis.SaveDiffEngine
import com.offline.saveeditor.concurrency.LatestOperationGate
import com.offline.saveeditor.diagnostics.DiagnosticsEngine
import com.offline.saveeditor.change.ChangeReport
import com.offline.saveeditor.change.FieldChange
import com.offline.saveeditor.edit.DefaultEditRuleProvider
import com.offline.saveeditor.edit.EditRules
import com.offline.saveeditor.edit.SafeEditEngine
import com.offline.saveeditor.edit.EditRiskAssessor
import com.offline.saveeditor.edit.PendingEditPreview
import com.offline.saveeditor.export.ExportAudit
import com.offline.saveeditor.export.ExportKind
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.model.SaveRepository
import com.offline.saveeditor.navigation.AppScreen
import com.offline.saveeditor.storage.BackupRepository
import com.offline.saveeditor.storage.HistoryRepository
import com.offline.saveeditor.restore.RestorePipeline
import com.offline.saveeditor.storage.RestoreRepository
import com.offline.saveeditor.root.RootReadOnlyProbe
import com.offline.saveeditor.coin.CoinWorkerClient
import com.offline.saveeditor.startdate.StartDateWorkerClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/** Owns document, diagnostics, comparison and navigation/search state. */
class EditorViewModel : ViewModel() {
    private val _session = MutableStateFlow(EditorSession())
    val session: StateFlow<EditorSession> = _session.asStateFlow()
    private val gate = LatestOperationGate()
    private var activeJob: Job? = null
    private var backupStore: BackupRepository? = null
    private var historyStore: HistoryRepository? = null
    private var restoreStore: RestoreRepository? = null
    private var coinWorker: CoinWorkerClient? = null
    private var startDateWorker: StartDateWorkerClient? = null

    fun attachCoinWorker(worker: CoinWorkerClient) { coinWorker = worker }

    fun attachStartDateWorker(worker: StartDateWorkerClient) { startDateWorker = worker }

    fun attachStores(backup: BackupRepository, history: HistoryRepository, restore: RestoreRepository) {
        backupStore = backup
        historyStore = history
        restoreStore = restore
        refreshStorage()
    }

    fun refreshStorage(message: String? = null) {
        val backup = backupStore ?: return
        val history = historyStore ?: return
        val restore = restoreStore ?: return
        viewModelScope.launch {
            val snapshot = withContext(Dispatchers.IO) {
                _session.value.storage.copy(
                    backups = backup.list(),
                    history = history.list(),
                    stagedRestore = restore.currentOrNull(),
                )
            }
            dispatch(EditorAction.StorageLoaded(snapshot, message))
        }
    }

    fun dispatch(action: EditorAction) { _session.value = EditorReducer.reduce(_session.value, action) }
    fun navigate(screen: AppScreen) = dispatch(EditorAction.Navigate(screen))
    fun updateStatus(message: String) = dispatch(EditorAction.Status(message))
    fun updateVarQuery(value: String) = dispatch(EditorAction.SearchVars(value.take(80)))
    fun updateObjectQuery(value: String) = dispatch(EditorAction.SearchObjects(value.take(120)))
    fun previousVarPage() = dispatch(EditorAction.VarPage((_session.value.varPage - 1).coerceAtLeast(0)))
    fun nextVarPage() = dispatch(EditorAction.VarPage(_session.value.varPage + 1))
    fun previousObjectPage() = dispatch(EditorAction.ObjectPage((_session.value.objectPage - 1).coerceAtLeast(0)))
    fun nextObjectPage() = dispatch(EditorAction.ObjectPage(_session.value.objectPage + 1))
    fun resetDocumentUi() = dispatch(EditorAction.ResetDocumentUi)
    fun updateBackupQuery(value: String) = dispatch(EditorAction.SearchBackups(value))
    fun updateHistoryQuery(value: String) = dispatch(EditorAction.SearchHistory(value))
    fun updateCrashQuery(value: String) = dispatch(EditorAction.SearchCrash(value))
    fun setBackupPage(value: Int) = dispatch(EditorAction.BackupPage(value))
    fun setHistoryPage(value: Int) = dispatch(EditorAction.HistoryPage(value))
    fun setCrashPage(value: Int) = dispatch(EditorAction.CrashPage(value))
    fun setShowSettings(value: Boolean) = dispatch(EditorAction.ShowSettings(value))
    fun consumePendingExport() = dispatch(EditorAction.ExportConsumed)

    fun prepareVerifiedEdit(ruleId: String, value: Long) = prepareEdit(ruleId, value, allowCandidate = false)

    fun loadCoinFromTownship() {
        val worker = coinWorker ?: run { dispatch(EditorAction.Status("CoinWorker chưa được khởi tạo.")); return }
        val token = startOperation("Đang đọc Coin trong worker riêng…")
        activeJob = viewModelScope.launch {
            try {
                val result = withTimeout(45_000) { worker.inspect() }
                if (gate.isCurrent(token)) dispatch(EditorAction.CoinLoaded(result.coin, result.sha256, "Đã đọc Coin trực tiếp bằng worker tách riêng."))
            } catch (_: CancellationException) {
            } catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Coin worker không đọc được save: ${error.message}"))
            }
        }
    }

    fun previewCoin(value: Long) {
        require(value in 0..2_000_000_000L)
        val current = _session.value.coin.currentValue ?: run { dispatch(EditorAction.Status("Hãy đọc Coin trước.")); return }
        if (value == current) { dispatch(EditorAction.Status("Coin mới trùng Coin hiện tại.")); return }
        dispatch(EditorAction.CoinPreview(value))
    }

    fun discardCoinPreview() = dispatch(EditorAction.CoinPreviewDiscarded)

    fun confirmPendingCoinDirect() {
        val value = _session.value.coin.pendingValue ?: run { dispatch(EditorAction.Status("Không có Coin chờ ghi.")); return }
        val worker = coinWorker ?: run { dispatch(EditorAction.Status("CoinWorker chưa được khởi tạo.")); return }
        val token = startOperation("Đang backup và ghi Coin trong worker riêng…")
        activeJob = viewModelScope.launch {
            try {
                val result = withTimeout(60_000) { worker.write(value) }
                if (gate.isCurrent(token)) dispatch(EditorAction.CoinWritten(
                    result.coin,
                    result.sha256,
                    result.backupPath.orEmpty(),
                    "Đã ghi Coin và xác minh thành công. Root backup: ${result.backupPath}",
                ))
            } catch (_: CancellationException) {
            } catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Coin worker ghi thất bại: ${error.message}"))
            }
        }
    }


    fun loadStartDateFromTownship() {
        val worker = startDateWorker ?: run { dispatch(EditorAction.Status("StartDateWorker chưa được khởi tạo.")); return }
        val token = startOperation("Đang đọc gameStartDate trong worker riêng…")
        activeJob = viewModelScope.launch {
            try {
                val result = withTimeout(45_000) { worker.inspect() }
                if (gate.isCurrent(token)) dispatch(EditorAction.StartDateLoaded(
                    result.epochSeconds, result.sha256, "Đã đọc ngày tạo trực tiếp từ gameStartDate."
                ))
            } catch (_: CancellationException) {
            } catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Không đọc được gameStartDate: ${error.message}"))
            }
        }
    }

    fun previewStartDate(epochSeconds: Long) {
        require(epochSeconds in 0..4_102_444_800L)
        val current = _session.value.startDate.currentEpochSeconds ?: run {
            dispatch(EditorAction.Status("Hãy đọc ngày tạo trước.")); return
        }
        if (epochSeconds == current) { dispatch(EditorAction.Status("Ngày tạo mới trùng giá trị hiện tại.")); return }
        dispatch(EditorAction.StartDatePreview(epochSeconds))
    }

    fun discardStartDatePreview() = dispatch(EditorAction.StartDatePreviewDiscarded)

    fun confirmPendingStartDateDirect() {
        val value = _session.value.startDate.pendingEpochSeconds ?: run {
            dispatch(EditorAction.Status("Không có ngày tạo chờ ghi.")); return
        }
        val worker = startDateWorker ?: run { dispatch(EditorAction.Status("StartDateWorker chưa được khởi tạo.")); return }
        val token = startOperation("Đang backup và ghi gameStartDate…")
        activeJob = viewModelScope.launch {
            try {
                val result = withTimeout(60_000) { worker.write(value) }
                if (gate.isCurrent(token)) dispatch(EditorAction.StartDateWritten(
                    result.epochSeconds,
                    result.sha256,
                    result.backupPath.orEmpty(),
                    "Đã ghi ngày tạo và xác minh thành công. Root backup: ${result.backupPath}",
                ))
            } catch (_: CancellationException) {
            } catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Ghi gameStartDate thất bại: ${error.message}"))
            }
        }
    }

    private fun prepareEdit(ruleId: String, value: Long, allowCandidate: Boolean) {
        val document = _session.value.document ?: run {
            dispatch(EditorAction.Status("Hãy mở save trước khi chỉnh sửa.")); return
        }
        val rule = runCatching { DefaultEditRuleProvider.instance.require(ruleId) }.getOrElse {
            dispatch(EditorAction.Status(it.message ?: "Không tìm thấy edit rule.")); return
        }
        if (allowCandidate && rule.id != EditRules.COIN.id) {
            dispatch(EditorAction.Status("Chỉ Coin được phép chạy ở chế độ Experimental.")); return
        }
        val token = startOperation("Đang tạo và xác minh ${rule.title}…")
        activeJob = viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    val prepared = SafeEditEngine.prepare(document, rule, value, allowCandidate = allowCandidate)
                    val encoded = SafeEditEngine.encodeAndVerify(document, prepared)
                    val report = ChangeReport(listOf(FieldChange(rule.title, prepared.originalValue, prepared.newValue)))
                    val payload = ExportPayload(
                        bytes = encoded.output,
                        requestedName = "mGameInfo_${rule.id}_${prepared.newValue}.xml",
                        kind = ExportKind.BINARY,
                        audit = ExportAudit(document.sha256, report),
                    )
                    PendingEditPreview(
                        ruleId = rule.id,
                        title = rule.title,
                        variableName = rule.variableName,
                        oldValue = prepared.originalValue.toLong(),
                        newValue = prepared.newValue.toLong(),
                        diff = prepared.diff,
                        risk = EditRiskAssessor.assess(prepared.originalValue.toLong(), prepared.newValue.toLong(), rule),
                        payload = payload,
                    )
                }
                if (gate.isCurrent(token)) dispatch(EditorAction.EditPreviewReady(result, "Đã tạo bản xem trước; hãy kiểm tra diff trước khi xuất."))
            } catch (_: CancellationException) {
            } catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Không thể tạo ${rule.title}: ${error.message}"))
            }
        }
    }


    fun confirmPendingEditExport() {
        val preview = _session.value.pendingEditPreview ?: run {
            dispatch(EditorAction.Status("Không có bản chỉnh sửa chờ xuất.")); return
        }
        dispatch(EditorAction.ExportReady(preview.payload, "Đã xác nhận ${preview.title}; hãy chọn nơi xuất file."))
    }

    fun discardPendingEdit() = dispatch(EditorAction.EditPreviewDiscarded)

    fun createBackup() = runStorageOperation("Đang tạo backup…") {
        val document = _session.value.document ?: error("Hãy mở save trước khi backup")
        val result = requireNotNull(backupStore) { "BackupStore chưa được khởi tạo" }.createVerified(document.container)
        storageSnapshot().copy(backupInspection = null) to if (result.created) "Đã tạo backup." else "Backup đã tồn tại."
    }

    fun inspectBackup(fileName: String) = runStorageOperation("Đang kiểm tra backup…") {
        val inspection = requireNotNull(backupStore).inspect(fileName)
        storageSnapshot().copy(backupInspection = inspection) to "Đã kiểm tra ${inspection.backup.fileName}."
    }

    fun deleteBackup(fileName: String) = runStorageOperation("Đang xóa backup…") {
        require(requireNotNull(backupStore).delete(fileName)) { "Không thể xóa backup" }
        storageSnapshot().copy(backupInspection = null) to "Đã xóa backup."
    }

    fun openBackup(fileName: String) {
        val token = startOperation("Đang đọc và phân tích backup…")
        activeJob = viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val bytes = requireNotNull(backupStore).read(fileName)
                    val document = SaveRepository.open(bytes)
                    document to DiagnosticsEngine.inspect(document)
                }
                if (gate.isCurrent(token)) dispatch(EditorAction.DocumentLoaded(result.first, result.second, "Đã mở backup $fileName."))
            } catch (_: CancellationException) {} catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Không mở được backup: ${error.message}"))
            }
        }
    }

    fun exportBackup(fileName: String) = runExportOperation("Đang chuẩn bị backup để xuất…") {
        ExportPayload(requireNotNull(backupStore).read(fileName), fileName, ExportKind.BINARY)
    }

    fun clearHistory() = runStorageOperation("Đang xóa lịch sử…") {
        requireNotNull(historyStore).clear()
        storageSnapshot() to "Đã xóa lịch sử xuất file."
    }

    fun createRestoreStage() = runStorageOperation("Đang tạo restore staging…") {
        val document = _session.value.document ?: error("Hãy mở save trước khi tạo staging")
        val plan = RestorePipeline.prepare(document.container, "save đang mở")
        requireNotNull(restoreStore).stage(plan)
        storageSnapshot() to "Đã tạo và xác minh restore staging."
    }

    fun exportRestoreStage() = runExportOperation("Đang xác minh restore staging…") {
        val staged = _session.value.storage.stagedRestore ?: error("Chưa có restore staging")
        ExportPayload(requireNotNull(restoreStore).readVerified(staged.sha256), "mGameInfo_restore_ready.xml", ExportKind.BINARY)
    }

    fun clearRestoreStage() = runStorageOperation("Đang xóa restore staging…") {
        require(requireNotNull(restoreStore).clear()) { "Không thể xóa toàn bộ staging" }
        storageSnapshot().copy(stagedRestore = null) to "Đã xóa restore staging."
    }

    fun runRootProbe(enabled: Boolean) {
        if (!enabled) { dispatch(EditorAction.Status("Root probe đang bị tắt trong Settings.")); return }
        val token = startOperation("Đang root probe chỉ đọc…")
        activeJob = viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { RootReadOnlyProbe.probe() }
                if (gate.isCurrent(token)) dispatch(EditorAction.StorageLoaded(_session.value.storage.copy(rootProbe = result), result.message))
            } catch (_: CancellationException) {} catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Root probe thất bại: ${error.message}"))
            }
        }
    }

    fun markExportWriting(name: String) = dispatch(EditorAction.ExportPhaseChanged(ExportPhase.WRITING, name, "Đang ghi $name…"))
    fun markExportCompleted(name: String) { dispatch(EditorAction.ExportPhaseChanged(ExportPhase.COMPLETED, name, "Đã xuất $name.")); refreshStorage() }
    fun markExportCancelled(name: String?) = dispatch(EditorAction.ExportPhaseChanged(ExportPhase.CANCELLED, name, "Đã hủy xuất file."))
    fun markExportFailed(name: String?, message: String) = dispatch(EditorAction.ExportPhaseChanged(ExportPhase.FAILED, name, message))

    private fun storageSnapshot(): StorageState = _session.value.storage.copy(
        backups = requireNotNull(backupStore).list(),
        history = requireNotNull(historyStore).list(),
        stagedRestore = requireNotNull(restoreStore).currentOrNull(),
    )

    private fun runStorageOperation(label: String, block: () -> Pair<StorageState, String>) {
        val token = startOperation(label)
        activeJob = viewModelScope.launch {
            try {
                val (snapshot, message) = withContext(Dispatchers.IO) { block() }
                if (gate.isCurrent(token)) dispatch(EditorAction.StorageLoaded(snapshot, message))
            } catch (_: CancellationException) {} catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed(error.message ?: "Tác vụ lưu trữ thất bại"))
            }
        }
    }

    private fun runExportOperation(label: String, block: () -> ExportPayload) {
        val token = startOperation(label)
        activeJob = viewModelScope.launch {
            try {
                val payload = withContext(Dispatchers.IO) { block() }
                if (gate.isCurrent(token)) dispatch(EditorAction.ExportReady(payload, "Đã chuẩn bị ${payload.safeName}; hãy chọn nơi lưu."))
            } catch (_: CancellationException) {} catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed(error.message ?: "Không thể chuẩn bị file xuất"))
            }
        }
    }

    fun refreshDiagnostics() {
        val document = _session.value.document ?: run {
            dispatch(EditorAction.Status("Chưa có save để chẩn đoán.")); return
        }
        val token = startOperation("Đang chạy diagnostics…")
        activeJob = viewModelScope.launch {
            try {
                val report = withContext(Dispatchers.Default) { DiagnosticsEngine.inspect(document) }
                if (gate.isCurrent(token)) dispatch(EditorAction.DocumentLoaded(document, report, "Đã cập nhật diagnostics."))
            } catch (_: CancellationException) {
            } catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Diagnostics thất bại: ${error.message}"))
            }
        }
    }

    fun openDocument(bytes: ByteArray, source: String) {
        val token = startOperation("Đang giải mã và phân tích save…")
        activeJob = viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) {
                    val document = SaveRepository.open(bytes)
                    document to DiagnosticsEngine.inspect(document)
                }
                if (gate.isCurrent(token)) dispatch(EditorAction.DocumentLoaded(result.first, result.second, "Đã đọc và xác minh $source."))
            } catch (_: CancellationException) {
                // A newer operation or explicit cancel owns the UI now.
            } catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Lỗi đọc $source: ${error.message}", clearDocument = true))
            }
        }
    }

    fun compareDocument(otherBytes: ByteArray) {
        val current = _session.value.document ?: run {
            dispatch(EditorAction.Status("Hãy mở save chính trước khi so sánh.")); return
        }
        val token = startOperation("Đang giải mã và so sánh save…")
        activeJob = viewModelScope.launch {
            try {
                val diff = withContext(Dispatchers.Default) {
                    val other = SaveRepository.open(otherBytes)
                    SaveDiffEngine.between(SaveAnalyzer.snapshot(current.xml), SaveAnalyzer.snapshot(other.xml))
                }
                if (gate.isCurrent(token)) dispatch(EditorAction.ComparisonReady(diff, "Đã so sánh: ${diff.vars.size} biến và ${diff.objects.size} object thay đổi."))
            } catch (_: CancellationException) {
            } catch (error: Throwable) {
                if (gate.isCurrent(token)) dispatch(EditorAction.OperationFailed("Không thể so sánh save: ${error.message}"))
            }
        }
    }

    fun cancelActiveOperation() {
        gate.invalidate()
        activeJob?.cancel()
        activeJob = null
        dispatch(EditorAction.Busy(false))
        dispatch(EditorAction.Status("Đã hủy tác vụ."))
    }

    private fun startOperation(label: String): Long {
        activeJob?.cancel()
        val token = gate.begin()
        dispatch(EditorAction.Busy(true, label))
        return token
    }

    override fun onCleared() {
        gate.invalidate()
        activeJob?.cancel()
        super.onCleared()
    }
}
