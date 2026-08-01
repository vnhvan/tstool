package com.offline.saveeditor.edit

import com.offline.saveeditor.analysis.SaveDiff
import com.offline.saveeditor.export.ExportPayload

data class PendingEditPreview(
    val ruleId: String,
    val title: String,
    val variableName: String,
    val oldValue: Long,
    val newValue: Long,
    val diff: SaveDiff,
    val risk: EditRisk,
    val payload: ExportPayload,
)
