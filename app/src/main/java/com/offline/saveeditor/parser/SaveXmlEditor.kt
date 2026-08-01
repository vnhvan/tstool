package com.offline.saveeditor.parser

import com.offline.saveeditor.model.SaveFields

object SaveXmlEditor {
    private fun escaped(name: String) = Regex.escape(name)

    // Accepts different attribute orders and whitespace while remaining scoped to one <Var ...> tag.
    private fun varTagRegex(name: String) = Regex(
        """<Var\b(?=[^>]*\bname\s*=\s*[\"']${escaped(name)}[\"'])[^>]*>""",
        RegexOption.IGNORE_CASE,
    )

    private val valueAttribute = Regex("""\bv\s*=\s*([\"'])(.*?)\1""")

    fun readFields(xml: ByteArray): SaveFields {
        val text = xml.toString(Charsets.UTF_8)
        return SaveFields(
            coin = findLong(text, "money"),
            tcash = findLong(text, "moneyCash"),
            soundVolume = findLong(text, "soundVolume")?.toInt(),
            mineDepth = findLong(text, "diggingDepth") ?: findLong(text, "mineDepth"),
        )
    }

    fun listVars(xml: ByteArray): List<VarEntry> {
        val text = xml.toString(Charsets.UTF_8)
        val tagRegex = Regex("""<Var\b[^>]*>""", RegexOption.IGNORE_CASE)
        val nameAttribute = Regex("""\bname\s*=\s*([\"'])(.*?)\1""", RegexOption.IGNORE_CASE)
        return tagRegex.findAll(text).mapNotNull { match ->
            val name = nameAttribute.find(match.value)?.groupValues?.getOrNull(2) ?: return@mapNotNull null
            val value = valueAttribute.find(match.value)?.groupValues?.getOrNull(2) ?: return@mapNotNull null
            VarEntry(name = name, value = value, rawTag = match.value)
        }.toList()
    }

    fun searchVars(xml: ByteArray, query: String, limit: Int = 200): List<VarEntry> {
        require(limit in 1..2000) { "Giới hạn kết quả không hợp lệ" }
        val normalized = query.trim()
        return listVars(xml).asSequence()
            .filter { normalized.isEmpty() || it.name.contains(normalized, ignoreCase = true) || it.value.contains(normalized, ignoreCase = true) }
            .take(limit)
            .toList()
    }

    fun setVar(xml: ByteArray, name: String, newValue: String): ByteArray {
        require(newValue.none { it == '<' || it == '>' || it == '\"' || it == '\'' }) {
            "Giá trị chứa ký tự không an toàn cho thuộc tính XML"
        }
        val text = xml.toString(Charsets.UTF_8)
        val tagRegex = varTagRegex(name)
        val tag = tagRegex.find(text) ?: error("Không tìm thấy biến $name")
        val value = valueAttribute.find(tag.value) ?: error("Biến $name không có thuộc tính v")
        val replacementTag = tag.value.replaceRange(value.range, "v=${value.groupValues[1]}$newValue${value.groupValues[1]}")
        return text.replaceRange(tag.range, replacementTag).toByteArray(Charsets.UTF_8)
    }

    fun setSoundVolume(xml: ByteArray, value: Int): ByteArray {
        require(value in 0..100) { "Sound Volume phải trong khoảng 0..100" }
        return setVar(xml, "soundVolume", value.toString())
    }

    private fun findLong(text: String, name: String): Long? {
        val tag = varTagRegex(name).find(text)?.value ?: return null
        return valueAttribute.find(tag)?.groupValues?.getOrNull(2)?.toLongOrNull()
    }
}
