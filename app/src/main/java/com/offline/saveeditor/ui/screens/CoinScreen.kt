package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.edit.EditRules
import com.offline.saveeditor.edit.ModulePreflightEngine
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel
import com.offline.saveeditor.ui.components.EditPreviewCard

@Composable
fun CoinScreen(
    session: EditorSession,
    viewModel: EditorViewModel,
    onOpen: () -> Unit,
) {
    val document = session.document
    val rule = EditRules.COIN
    val preflight = remember(document) {
        ModulePreflightEngine.inspect(document, listOf(rule)).single()
    }
    var input by remember(document?.sha256, preflight.currentValue) {
        mutableStateOf(preflight.currentValue ?: "")
    }
    val parsed = input.toLongOrNull()
    val valid = parsed != null && parsed in 0..2_000_000_000L
    val canPrepare = document != null && document.canEncode && preflight.variableExists && valid && !session.busy

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Coin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            AssistChip(onClick = {}, label = { Text("EXPERIMENTAL") })
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Module thử nghiệm", fontWeight = FontWeight.Bold)
                Text("Module chỉ sửa biến money, sau đó kiểm tra diff, encode và mở lại file để xác minh. Hãy backup toàn bộ save trước khi thử trên Township.")
            }
        }

        Button(onClick = onOpen, enabled = !session.busy, modifier = Modifier.fillMaxWidth()) {
            Text(if (document == null) "Chọn mGameInfo.xml" else "Chọn save khác")
        }

        if (document == null) {
            Text("Chưa mở save. Hãy chọn file mGameInfo nhị phân.")
            return@Column
        }

        Card {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Save hiện tại", style = MaterialTheme.typography.titleMedium)
                Text("Nguồn: ${document.source}")
                Text("Coin hiện tại: ${preflight.currentValue ?: "không tìm thấy"}", fontWeight = FontWeight.Bold)
                Text("Trạng thái: ${when {
                    !document.canEncode -> "Chỉ đọc — cần mGameInfo nhị phân"
                    !preflight.variableExists -> "Không tìm thấy biến money"
                    else -> "Sẵn sàng tạo bản thử nghiệm"
                }}")
            }
        }

        OutlinedTextField(
            value = input,
            onValueChange = { value -> input = value.filter(Char::isDigit).take(10) },
            label = { Text("Coin mới (0–2.000.000.000)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = input.isNotBlank() && !valid,
            supportingText = {
                if (input.isNotBlank() && !valid) Text("Giá trị phải nằm trong khoảng 0–2.000.000.000")
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { viewModel.prepareExperimentalCoin(requireNotNull(parsed)) },
            enabled = canPrepare,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Tạo bản xem trước Coin")
        }

        session.pendingEditPreview?.takeIf { it.ruleId == rule.id }?.let { preview ->
            EditPreviewCard(
                preview = preview,
                enabled = !session.busy,
                onConfirm = viewModel::confirmPendingEditExport,
                onDiscard = viewModel::discardPendingEdit,
            )
        }

        Text(session.status, style = MaterialTheme.typography.bodySmall)
    }
}
