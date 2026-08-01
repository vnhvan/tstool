package com.offline.saveeditor.storage

import com.offline.saveeditor.change.ChangeReport
import com.offline.saveeditor.restore.RestorePlan
import com.offline.saveeditor.restore.StagedRestore

/** Android-free contracts used by EditorViewModel and unit tests. */
interface BackupRepository {
    fun createVerified(container: ByteArray): BackupCreateResult
    fun list(): List<BackupInfo>
    fun inspect(fileName: String): BackupInspection
    fun read(fileName: String): ByteArray
    fun delete(fileName: String): Boolean
}

interface HistoryRepository {
    fun add(sourceSha256: String, outputName: String, outputSha256: String, report: ChangeReport)
    fun list(): List<PersistedHistoryEntry>
    fun clear()
}

interface RestoreRepository {
    fun stage(plan: RestorePlan): StagedRestore
    fun currentOrNull(): StagedRestore?
    fun readVerified(expectedSha256: String): ByteArray
    fun clear(): Boolean
}
