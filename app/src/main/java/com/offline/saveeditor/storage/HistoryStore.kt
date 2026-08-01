package com.offline.saveeditor.storage

import android.content.Context
import com.offline.saveeditor.change.ChangeReport
import org.json.JSONArray
import org.json.JSONObject

data class PersistedHistoryEntry(
    val createdAtEpochMillis: Long,
    val sourceSha256: String,
    val outputName: String,
    val outputSha256: String,
    val summary: String,
)

class HistoryStore(context: Context, private val capacity: Int = 50) : HistoryRepository {
    private val prefs = context.getSharedPreferences("edit_history", Context.MODE_PRIVATE)

    override fun add(sourceSha256: String, outputName: String, outputSha256: String, report: ChangeReport) {
        val current = list().toMutableList()
        current.add(0, PersistedHistoryEntry(
            createdAtEpochMillis = System.currentTimeMillis(),
            sourceSha256 = sourceSha256,
            outputName = outputName,
            outputSha256 = outputSha256,
            summary = report.changes.joinToString("; ") { "${it.field}: ${it.before} -> ${it.after}" },
        ))
        save(current.take(capacity))
    }

    override fun list(): List<PersistedHistoryEntry> {
        val raw = prefs.getString(KEY, "[]") ?: "[]"
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(PersistedHistoryEntry(
                        createdAtEpochMillis = item.getLong("createdAt"),
                        sourceSha256 = item.getString("sourceSha256"),
                        outputName = item.getString("outputName"),
                        outputSha256 = item.getString("outputSha256"),
                        summary = item.getString("summary"),
                    ))
                }
            }
        }.getOrElse { emptyList() }
    }

    override fun clear() { prefs.edit().remove(KEY).apply() }

    private fun save(items: List<PersistedHistoryEntry>) {
        val array = JSONArray()
        items.forEach { entry ->
            array.put(JSONObject().apply {
                put("createdAt", entry.createdAtEpochMillis)
                put("sourceSha256", entry.sourceSha256)
                put("outputName", entry.outputName)
                put("outputSha256", entry.outputSha256)
                put("summary", entry.summary)
            })
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    private companion object { const val KEY = "entries" }
}
