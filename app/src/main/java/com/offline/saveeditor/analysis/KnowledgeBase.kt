package com.offline.saveeditor.analysis

data class KnowledgeEntry(val key: String, val label: String, val confidence: Confidence, val note: String)
enum class Confidence { VERIFIED, CANDIDATE, UNKNOWN }

object KnowledgeBase {
    private val vars = mapOf(
        "money" to KnowledgeEntry("money", "Coin", Confidence.CANDIDATE, "Đọc được; chưa xác minh quy trình ghi độc lập."),
        "moneyCash" to KnowledgeEntry("moneyCash", "TCash", Confidence.CANDIDATE, "Đọc được; chưa xác minh quy trình ghi độc lập."),
        "soundVolume" to KnowledgeEntry("soundVolume", "Sound Volume", Confidence.VERIFIED, "Đã kiểm chứng trên Township thật."),
        "diggingDepth" to KnowledgeEntry("diggingDepth", "Mine Depth", Confidence.CANDIDATE, "Tên trường phù hợp nhưng chưa có bộ mẫu độc lập."),
        "mineDepth" to KnowledgeEntry("mineDepth", "Mine Depth", Confidence.CANDIDATE, "Tên thay thế; chưa xác minh."),
        "cowfactory.slotsCount" to KnowledgeEntry("cowfactory.slotsCount", "Cow Factory Slots", Confidence.CANDIDATE, "Đã quan sát trong bộ dữ liệu ban đầu."),
    )

    val knownVars: List<KnowledgeEntry> get() = vars.values.sortedBy { it.key.lowercase() }

    fun describeVar(name: String): KnowledgeEntry = vars[name]
        ?: KnowledgeEntry(name, name, Confidence.UNKNOWN, "Chưa ánh xạ trong Knowledge Base.")
}
