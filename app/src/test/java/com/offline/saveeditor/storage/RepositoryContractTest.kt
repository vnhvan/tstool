package com.offline.saveeditor.storage

import com.offline.saveeditor.change.ChangeReport
import com.offline.saveeditor.change.FieldChange
import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryContractTest {
    private class FakeHistoryRepository : HistoryRepository {
        private val entries = mutableListOf<PersistedHistoryEntry>()
        override fun add(sourceSha256: String, outputName: String, outputSha256: String, report: ChangeReport) {
            entries += PersistedHistoryEntry(1, sourceSha256, outputName, outputSha256, report.changes.joinToString())
        }
        override fun list() = entries.toList()
        override fun clear() = entries.clear()
    }

    @Test fun viewModelContractCanBeFakedWithoutContext() {
        val repo: HistoryRepository = FakeHistoryRepository()
        repo.add("source", "out", "output", ChangeReport(listOf(FieldChange("Sound", "1", "2"))))
        assertEquals(1, repo.list().size)
        repo.clear()
        assertEquals(0, repo.list().size)
    }
}
