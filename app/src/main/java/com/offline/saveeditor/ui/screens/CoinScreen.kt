package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel

@Composable
fun CoinScreen(session: EditorSession, viewModel: EditorViewModel) {
    val coin = session.coin
    var input by remember(coin.currentValue) { mutableStateOf(coin.currentValue?.toString().orEmpty()) }
    val parsed = input.toLongOrNull()
    val valid = parsed != null && parsed in 0..2_000_000_000L && parsed != coin.currentValue

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Coin", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            AssistChip(onClick = {}, label = { Text("EXPERIMENTAL") })
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Worker tách riêng", fontWeight = FontWeight.Bold)
                Text("Không parse toàn bộ save trong giao diện. Worker riêng chỉ quét byte để đọc/sửa biến money, encode–verify rồi mới thay file game.")
            }
        }
        Button(onClick = viewModel::loadCoinFromTownship, enabled = !session.busy, modifier = Modifier.fillMaxWidth()) {
            Text(if (coin.currentValue == null) "Đọc Coin trực tiếp" else "Đọc lại Coin")
        }
        Text("/data/data/com.playrix.township.vn/saves/mGameInfo.xml", style = MaterialTheme.typography.bodySmall)

        coin.currentValue?.let {
            Card {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Coin hiện tại: $it", fontWeight = FontWeight.Bold)
                    Text("SHA-256: ${coin.sourceSha256?.take(16)}…", style = MaterialTheme.typography.bodySmall)
                    coin.rootBackupPath?.let { path -> Text("Backup: $path", style = MaterialTheme.typography.bodySmall) }
                }
            }
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.filter(Char::isDigit).take(10) },
                label = { Text("Coin mới (0–2.000.000.000)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = input.isNotBlank() && !valid,
                modifier = Modifier.fillMaxWidth(),
            )
            if (coin.pendingValue == null) {
                Button(onClick = { viewModel.previewCoin(requireNotNull(parsed)) }, enabled = valid && !session.busy, modifier = Modifier.fillMaxWidth()) {
                    Text("Xem trước thay đổi")
                }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("money: ${coin.currentValue} → ${coin.pendingValue}", fontWeight = FontWeight.Bold)
                        Text("Worker sẽ force stop Township, backup, sửa, encode–verify và ghi trực tiếp.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = viewModel::discardCoinPreview, enabled = !session.busy, modifier = Modifier.weight(1f)) { Text("Hủy") }
                            Button(onClick = viewModel::confirmPendingCoinDirect, enabled = !session.busy, modifier = Modifier.weight(1f)) { Text("Ghi Coin") }
                        }
                    }
                }
            }
        }
        Text(session.status, style = MaterialTheme.typography.bodySmall)
    }
}
