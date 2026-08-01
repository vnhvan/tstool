package com.offline.saveeditor.analysis

object SaveAnalyzer {
    private val varTagRegex = Regex("""<Var\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val objectTagRegex = Regex("""<Object\b[^>]*>""", RegexOption.IGNORE_CASE)
    private val attributeRegex = Regex("""([A-Za-z_][A-Za-z0-9_.:-]*)\s*=\s*([\"'])(.*?)\2""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

    fun snapshot(xml: ByteArray): SaveSnapshot {
        val text = xml.toString(Charsets.UTF_8)
        val vars = linkedMapOf<String, String>()
        val duplicates = linkedSetOf<String>()
        varTagRegex.findAll(text).forEach { match ->
            val attrs = parseAttributes(match.value)
            val name = attrs["name"] ?: return@forEach
            val value = attrs["v"] ?: return@forEach
            if (vars.containsKey(name)) duplicates += name
            vars[name] = value
        }
        val objects = objectTagRegex.findAll(text).map { match ->
            val attrs = parseAttributes(match.value)
            ObjectEntry(
                name = attrs["name"],
                bid = attrs["bid"] ?: attrs["BID"] ?: attrs["id"],
                data = attrs["data"],
                attributes = attrs,
                rawTag = match.value,
            )
        }.toList()
        return SaveSnapshot(vars, objects, duplicates)
    }

    fun summarize(snapshot: SaveSnapshot): SnapshotSummary = SnapshotSummary(
        varCount = snapshot.vars.size,
        objectCount = snapshot.objects.size,
        duplicateVarCount = snapshot.duplicateVars.size,
        namedObjectCount = snapshot.objects.count { !it.name.isNullOrBlank() },
        bidObjectCount = snapshot.objects.count { !it.bid.isNullOrBlank() },
    )

    fun searchObjects(snapshot: SaveSnapshot, query: String, limit: Int = 200): List<ObjectEntry> {
        require(limit in 1..2000) { "Giới hạn kết quả không hợp lệ" }
        val q = query.trim()
        return snapshot.objects.asSequence().filter { item ->
            q.isEmpty() || item.name?.contains(q, true) == true || item.bid?.contains(q, true) == true ||
                item.data?.contains(q, true) == true || item.attributes.any { (k, v) -> k.contains(q, true) || v.contains(q, true) }
        }.take(limit).toList()
    }

    private fun parseAttributes(tag: String): Map<String, String> = buildMap {
        attributeRegex.findAll(tag).forEach { match ->
            put(match.groupValues[1], decodeXmlEntities(match.groupValues[3]))
        }
    }

    private fun decodeXmlEntities(value: String): String = value
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
}
