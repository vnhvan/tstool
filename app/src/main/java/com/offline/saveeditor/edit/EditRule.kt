package com.offline.saveeditor.edit

import com.offline.saveeditor.analysis.SaveDiff

enum class EditConfidence { VERIFIED, CANDIDATE, BLOCKED }

data class EditRule(
    val id: String,
    val title: String,
    val variableName: String,
    val confidence: EditConfidence,
    val minValue: Long? = null,
    val maxValue: Long? = null,
)

data class PreparedEdit(
    val rule: EditRule,
    val originalValue: String,
    val newValue: String,
    val editedXml: ByteArray,
    val diff: SaveDiff,
)

data class EncodedEdit(
    val prepared: PreparedEdit,
    val output: ByteArray,
    val outputSha256: String,
)

object EditRules {
    val SOUND_VOLUME = EditRule("sound", "Sound Volume", "soundVolume", EditConfidence.VERIFIED, 0, 100)
    val COIN = EditRule("coin", "Coin", "money", EditConfidence.CANDIDATE, 0, 2_000_000_000)
    val TCASH = EditRule("tcash", "TCash", "moneyCash", EditConfidence.CANDIDATE, 0, Long.MAX_VALUE)
    val COW_FACTORY_SLOTS = EditRule("cow_factory_slots", "Cow Factory Slots", "cowfactory.slotsCount", EditConfidence.CANDIDATE, 0, 100)
    val all = listOf(SOUND_VOLUME, COIN, TCASH, COW_FACTORY_SLOTS)
}

