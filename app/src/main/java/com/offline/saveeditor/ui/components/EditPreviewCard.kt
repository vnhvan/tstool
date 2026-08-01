package com.offline.saveeditor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.edit.EditRiskLevel
import com.offline.saveeditor.edit.PendingEditPreview

@Composable
fun EditPreviewCard(
    preview: PendingEditPreview,
    enabled: Boolean,
    onConfirm: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Xem trước thay đổi", style = MaterialTheme.typography.titleMedium)
            Text("${preview.title}: ${preview.oldValue} → ${preview.newValue}")
            Text("Biến: ${preview.variableName}")
            Text("Diff: ${preview.diff.vars.size} Var · ${preview.diff.objects.size} Object")
            preview.diff.vars.forEach { change -> Text("• ${change.name}: ${change.before} → ${change.after}") }
            val riskLabel = when (preview.risk.level) {
                EditRiskLevel.NORMAL -> "Bình thường"
                EditRiskLevel.CAUTION -> "Cần chú ý"
                EditRiskLevel.HIGH -> "Rủi ro cao"
            }
            Text("Đánh giá: $riskLabel — ${preview.risk.message}")
            Text("Độ lệch tuyệt đối: ${preview.risk.absoluteDelta}", style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = enabled, onClick = onConfirm) { Text("Xác nhận xuất") }
                OutlinedButton(enabled = enabled, onClick = onDiscard) { Text("Hủy/đặt lại") }
            }
        }
    }
}
