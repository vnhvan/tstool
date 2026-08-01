package com.offline.saveeditor.change

import com.offline.saveeditor.model.SaveFields

data class FieldChange(
    val field: String,
    val before: String?,
    val after: String?,
)

data class ChangeReport(
    val changes: List<FieldChange>,
) {
    val hasChanges: Boolean get() = changes.isNotEmpty()

    fun toPlainText(): String = buildString {
        appendLine("Township Offline Save Editor - Change Report")
        if (changes.isEmpty()) {
            appendLine("Không có thay đổi đã nhận diện.")
        } else {
            changes.forEach { appendLine("${it.field}: ${it.before ?: "<missing>"} -> ${it.after ?: "<missing>"}") }
        }
    }

    companion object {
        fun between(before: SaveFields, after: SaveFields): ChangeReport {
            val items = buildList {
                addIfChanged("Coin", before.coin, after.coin)
                addIfChanged("TCash", before.tcash, after.tcash)
                addIfChanged("Sound Volume", before.soundVolume, after.soundVolume)
                addIfChanged("Mine Depth", before.mineDepth, after.mineDepth)
            }
            return ChangeReport(items)
        }

        private fun <T> MutableList<FieldChange>.addIfChanged(name: String, before: T?, after: T?) {
            if (before != after) add(FieldChange(name, before?.toString(), after?.toString()))
        }
    }
}
