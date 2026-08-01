package com.offline.saveeditor.state

import com.offline.saveeditor.restore.StagedRestore
import com.offline.saveeditor.root.RootProbeResult
import com.offline.saveeditor.storage.BackupInfo
import com.offline.saveeditor.storage.BackupInspection
import com.offline.saveeditor.storage.PersistedHistoryEntry

enum class ExportPhase { IDLE, READY, WRITING, COMPLETED, CANCELLED, FAILED }

data class StorageState(
    val backups: List<BackupInfo> = emptyList(),
    val history: List<PersistedHistoryEntry> = emptyList(),
    val backupInspection: BackupInspection? = null,
    val stagedRestore: StagedRestore? = null,
    val rootProbe: RootProbeResult? = null,
    val exportPhase: ExportPhase = ExportPhase.IDLE,
    val lastExportName: String? = null,
)
