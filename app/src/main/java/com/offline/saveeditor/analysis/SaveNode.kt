package com.offline.saveeditor.analysis

data class ObjectEntry(
    val name: String?,
    val bid: String?,
    val data: String?,
    val attributes: Map<String, String>,
    val rawTag: String,
)

data class SaveSnapshot(
    val vars: Map<String, String>,
    val objects: List<ObjectEntry>,
    val duplicateVars: Set<String>,
)

data class SnapshotSummary(
    val varCount: Int,
    val objectCount: Int,
    val duplicateVarCount: Int,
    val namedObjectCount: Int,
    val bidObjectCount: Int,
)
