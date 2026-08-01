package com.offline.saveeditor.edit

/** Immutable registry for edit rules. Keeps module metadata and executable rules decoupled. */
class EditRuleProvider private constructor(private val rules: Map<String, EditRule>) {
    fun find(id: String): EditRule? = rules[id]
    fun require(id: String): EditRule = find(id) ?: error("Không có edit rule cho module: $id")
    fun all(): List<EditRule> = rules.values.sortedBy { it.title.lowercase() }
    fun verified(): List<EditRule> = all().filter { it.confidence == EditConfidence.VERIFIED }

    class Builder {
        private val rules = linkedMapOf<String, EditRule>()
        fun register(rule: EditRule): Builder = apply {
            require(rule.id.matches(Regex("[a-z0-9_]+"))) { "Rule id không hợp lệ: ${rule.id}" }
            require(rule.id !in rules) { "Rule id bị trùng: ${rule.id}" }
            require(rule.variableName.isNotBlank()) { "Rule ${rule.id} thiếu variableName" }
            require(rule.minValue == null || rule.maxValue == null || rule.minValue <= rule.maxValue) {
                "Rule ${rule.id} có khoảng giá trị không hợp lệ"
            }
            rules[rule.id] = rule
        }
        fun build(): EditRuleProvider = EditRuleProvider(rules.toMap())
    }
}

object DefaultEditRuleProvider {
    val instance: EditRuleProvider = EditRuleProvider.Builder()
        .register(EditRules.SOUND_VOLUME)
        .register(EditRules.COIN)
        .register(EditRules.TCASH)
        .register(EditRules.COW_FACTORY_SLOTS)
        .build()
}
