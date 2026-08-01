package com.offline.saveeditor.history

import com.offline.saveeditor.change.FieldChange
import java.util.ArrayDeque

data class EditHistoryEntry(
    val createdAtEpochMillis: Long,
    val sourceSha256: String,
    val outputName: String,
    val outputSha256: String,
    val changes: List<FieldChange>,
)

class EditHistory(private val capacity: Int = 50) {
    init { require(capacity > 0) }
    private val entries = ArrayDeque<EditHistoryEntry>()

    fun add(entry: EditHistoryEntry) {
        entries.addFirst(entry)
        while (entries.size > capacity) entries.removeLast()
    }

    fun all(): List<EditHistoryEntry> = entries.toList()
    fun clear() = entries.clear()
}
