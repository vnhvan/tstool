package com.offline.saveeditor.analysis

enum class DiffKind { ADDED, REMOVED, CHANGED }

data class VarDiff(val name: String, val before: String?, val after: String?, val kind: DiffKind)

data class ObjectDiff(
    val identity: String,
    val before: ObjectEntry?,
    val after: ObjectEntry?,
    val kind: DiffKind,
)

data class SaveDiff(
    val vars: List<VarDiff>,
    val objects: List<ObjectDiff>,
) {
    val totalChanges: Int get() = vars.size + objects.size
    val hasChanges: Boolean get() = totalChanges > 0

    fun toPlainText(maxObjectData: Int = 240): String = buildString {
        appendLine("Township Offline Save Editor - Full Diff")
        appendLine("Var changes: ${vars.size}")
        vars.forEach { appendLine("VAR ${it.kind} ${it.name}: ${it.before ?: "<missing>"} -> ${it.after ?: "<missing>"}") }
        appendLine("Object changes: ${objects.size}")
        objects.forEach { item ->
            appendLine("OBJECT ${item.kind} ${item.identity}")
            item.before?.let { appendLine("  before=${describe(it, maxObjectData)}") }
            item.after?.let { appendLine("  after=${describe(it, maxObjectData)}") }
        }
    }

    private fun describe(entry: ObjectEntry, max: Int): String {
        val data = entry.data?.let { if (it.length <= max) it else it.take(max) + "…" }
        return "name=${entry.name}, bid=${entry.bid}, data=$data"
    }
}

object SaveDiffEngine {
    fun between(before: SaveSnapshot, after: SaveSnapshot): SaveDiff {
        val names = (before.vars.keys + after.vars.keys).toSortedSet(String.CASE_INSENSITIVE_ORDER)
        val varDiffs = names.mapNotNull { name ->
            val old = before.vars[name]
            val new = after.vars[name]
            when {
                old == new -> null
                old == null -> VarDiff(name, null, new, DiffKind.ADDED)
                new == null -> VarDiff(name, old, null, DiffKind.REMOVED)
                else -> VarDiff(name, old, new, DiffKind.CHANGED)
            }
        }
        val oldObjects = indexObjects(before.objects)
        val newObjects = indexObjects(after.objects)
        val identities = (oldObjects.keys + newObjects.keys).toSortedSet()
        val objectDiffs = identities.mapNotNull { id ->
            val old = oldObjects[id]
            val new = newObjects[id]
            when {
                old == new -> null
                old == null -> ObjectDiff(id, null, new, DiffKind.ADDED)
                new == null -> ObjectDiff(id, old, null, DiffKind.REMOVED)
                else -> ObjectDiff(id, old, new, DiffKind.CHANGED)
            }
        }
        return SaveDiff(varDiffs, objectDiffs)
    }

    private fun indexObjects(items: List<ObjectEntry>): Map<String, ObjectEntry> {
        val counts = mutableMapOf<String, Int>()
        return buildMap {
            items.forEachIndexed { index, item ->
                val base = when {
                    !item.bid.isNullOrBlank() -> "bid:${item.bid}"
                    !item.name.isNullOrBlank() -> "name:${item.name}"
                    else -> "index:$index"
                }
                val sequence = counts.getOrDefault(base, 0)
                counts[base] = sequence + 1
                put(if (sequence == 0) base else "$base#$sequence", item)
            }
        }
    }
}
