package com.offline.saveeditor.storage

import com.offline.saveeditor.change.ChangeReport
import com.offline.saveeditor.restore.RestorePlan
import com.offline.saveeditor.restore.StagedRestore
import com.offline.saveeditor.util.sha256
import com.offline.saveeditor.validation.ContainerInspection
import com.offline.saveeditor.validation.ContainerKind
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RepositoryFakesTest {
    private class FakeBackupRepository : BackupRepository {
        private val files = linkedMapOf<String, ByteArray>()
        override fun createVerified(container: ByteArray): BackupCreateResult {
            val name = "backup_${sha256(container).take(8)}.bin"
            val created = files.putIfAbsent(name, container.copyOf()) == null
            return BackupCreateResult(info(name), created)
        }
        override fun list() = files.keys.map(::info)
        override fun inspect(fileName: String) = BackupInspection(
            backup = info(fileName),
            fileNameDigestMatches = true,
            container = ContainerInspection(ContainerKind.BINARY_79, true, 0, 0, false, "fake"),
        )
        override fun read(fileName: String) = requireNotNull(files[fileName]).copyOf()
        override fun delete(fileName: String) = files.remove(fileName) != null
        private fun info(name: String): BackupInfo {
            val bytes = requireNotNull(files[name])
            return BackupInfo(name, bytes.size.toLong(), 1L, sha256(bytes))
        }
    }

    private class FakeRestoreRepository : RestoreRepository {
        private var bytes: ByteArray? = null
        private var staged: StagedRestore? = null
        override fun stage(plan: RestorePlan): StagedRestore {
            bytes = plan.bytes.copyOf()
            return StagedRestore("staged.bin", plan.bytes.size.toLong(), plan.sha256, plan.sourceDescription).also { staged = it }
        }
        override fun currentOrNull() = staged
        override fun readVerified(expectedSha256: String): ByteArray {
            val value = requireNotNull(bytes)
            require(sha256(value) == expectedSha256)
            return value.copyOf()
        }
        override fun clear(): Boolean {
            val existed = staged != null
            staged = null; bytes = null
            return existed
        }
    }

    @Test fun backupFakeCoversCreateReadInspectDelete() {
        val repo = FakeBackupRepository()
        val input = byteArrayOf(1, 2, 3)
        val first = repo.createVerified(input)
        val second = repo.createVerified(input)
        assertTrue(first.created)
        assertFalse(second.created)
        assertEquals(1, repo.list().size)
        assertContentEquals(input, repo.read(first.backup.fileName))
        assertTrue(repo.inspect(first.backup.fileName).isValid)
        assertTrue(repo.delete(first.backup.fileName))
        assertTrue(repo.list().isEmpty())
    }

    @Test fun restoreFakeCoversStageVerifyAndClear() {
        val repo = FakeRestoreRepository()
        val bytes = byteArrayOf(7, 8, 9)
        val plan = RestorePlan(bytes, sha256(bytes), bytes.size, "test", "fake")
        val staged = repo.stage(plan)
        assertContentEquals(bytes, repo.readVerified(staged.sha256))
        assertTrue(repo.clear())
        assertNull(repo.currentOrNull())
    }
}
