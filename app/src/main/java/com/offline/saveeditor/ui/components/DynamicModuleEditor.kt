package com.offline.saveeditor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.edit.ModulePreflight

@Composable
fun DynamicModuleEditor(
    modules: List<ModulePreflight>,
    enabled: Boolean,
    onApply: (ModulePreflight, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Module chỉnh sửa đã xác minh", style = MaterialTheme.typography.titleMedium)
        if (modules.isEmpty()) { Text("Chưa có module nào đủ điều kiện hiển thị."); return@Column }
        modules.forEach { module ->
            val rule = module.rule
            var input by remember(rule.id, module.currentValue) { mutableStateOf(module.currentValue ?: rule.minValue?.toString() ?: "0") }
            val parsed = input.toLongOrNull()
            val valid = parsed != null && (rule.minValue == null || parsed >= rule.minValue) && (rule.maxValue == null || parsed <= rule.maxValue)
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(rule.title, style = MaterialTheme.typography.titleSmall)
                    Text("Biến: ${rule.variableName}")
                    Text("Hiện tại: ${module.currentValue ?: "không tìm thấy"}")
                    Text(module.reason, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it.filter { ch -> ch.isDigit() || ch == '-' }.take(20) },
                        label = { Text("Giá trị (${rule.minValue ?: "…"}–${rule.maxValue ?: "…"})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = input.isNotBlank() && !valid,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(enabled = enabled && module.writable && valid, onClick = { onApply(module, requireNotNull(parsed)) }) {
                        Text("Tạo save ${rule.title}")
                    }
                }
            }
        }
    }
}
