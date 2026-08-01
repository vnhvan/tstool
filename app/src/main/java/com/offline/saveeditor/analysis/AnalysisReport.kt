package com.offline.saveeditor.analysis

data class AnalysisReport(
    val summary: SnapshotSummary,
    val notableVars: List<Pair<KnowledgeEntry, String>>,
    val duplicateVars: Set<String>,
) {
    fun toPlainText(): String = buildString {
        appendLine("Township Offline Save Editor - Analysis Report")
        appendLine("Vars: ${summary.varCount}")
        appendLine("Objects: ${summary.objectCount}")
        appendLine("Named objects: ${summary.namedObjectCount}")
        appendLine("Objects with BID/ID: ${summary.bidObjectCount}")
        appendLine("Duplicate vars: ${summary.duplicateVarCount}")
        notableVars.forEach { (entry, value) -> appendLine("${entry.label} [${entry.confidence}] = $value (${entry.key})") }
        if (duplicateVars.isNotEmpty()) appendLine("Duplicate names: ${duplicateVars.sorted().joinToString()}")
    }
}

object AnalysisReporter {
    fun create(snapshot: SaveSnapshot): AnalysisReport {
        val notable = snapshot.vars.mapNotNull { (name, value) ->
            val entry = KnowledgeBase.describeVar(name)
            if (entry.confidence == Confidence.UNKNOWN) null else entry to value
        }
        return AnalysisReport(SaveAnalyzer.summarize(snapshot), notable, snapshot.duplicateVars)
    }
}
