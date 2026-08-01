package com.offline.saveeditor.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel
import com.offline.saveeditor.startdate.StartDateTime
import java.util.Calendar

@Composable
fun StartDateScreen(session: EditorSession, viewModel: EditorViewModel) {
    val state = session.startDate
    val context = LocalContext.current
    val currentCalendar = remember(state.currentEpochSeconds) {
        Calendar.getInstance(StartDateTime.timeZone).apply {
            timeInMillis = (state.currentEpochSeconds ?: System.currentTimeMillis() / 1000L) * 1000L
        }
    }
    var monthText by remember(state.currentEpochSeconds) { mutableStateOf((currentCalendar.get(Calendar.MONTH) + 1).toString()) }
    var yearText by remember(state.currentEpochSeconds) { mutableStateOf(currentCalendar.get(Calendar.YEAR).toString()) }
    var selectedEpoch by remember(state.currentEpochSeconds) { mutableStateOf<Long?>(null) }

    val month = monthText.toIntOrNull()
    val year = yearText.toIntOrNull()
    val monthYearValid = month != null && month in 1..12 && year != null && year in 1970..2100
    val monthYearEpoch = if (monthYearValid) runCatching { StartDateTime.toEpochSeconds(year!!, month!!, 1) }.getOrNull() else null
    val proposedEpoch = selectedEpoch ?: monthYearEpoch
    val nowEpoch = System.currentTimeMillis() / 1000L
    val proposedValid = proposedEpoch != null && proposedEpoch <= nowEpoch && proposedEpoch != state.currentEpochSeconds

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ngày tạo tài khoản", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            AssistChip(onClick = {}, label = { Text("EXPERIMENTAL") })
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Trường XML: gameStartDate", fontWeight = FontWeight.Bold)
                Text("Nhập tháng/năm sẽ tự đặt ngày 01 lúc 00:00 theo múi giờ Việt Nam (UTC+7). Hoặc chọn một ngày cụ thể trên lịch.")
            }
        }
        Button(onClick = viewModel::loadStartDateFromTownship, enabled = !session.busy, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.currentEpochSeconds == null) "Đọc ngày tạo trực tiếp" else "Đọc lại ngày tạo")
        }
        Text("/data/data/com.playrix.township.vn/saves/mGameInfo.xml", style = MaterialTheme.typography.bodySmall)

        state.currentEpochSeconds?.let { current ->
            Card {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Ngày hiện tại: ${StartDateTime.format(current)}", fontWeight = FontWeight.Bold)
                    Text("Unix: $current", style = MaterialTheme.typography.bodySmall)
                    Text("SHA-256: ${state.sourceSha256?.take(16)}…", style = MaterialTheme.typography.bodySmall)
                    state.rootBackupPath?.let { Text("Backup: $it", style = MaterialTheme.typography.bodySmall) }
                }
            }

            Text("Chọn theo tháng và năm", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = monthText,
                    onValueChange = { monthText = it.filter(Char::isDigit).take(2); selectedEpoch = null },
                    label = { Text("Tháng") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = monthText.isNotBlank() && month !in 1..12,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it.filter(Char::isDigit).take(4); selectedEpoch = null },
                    label = { Text("Năm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = yearText.isNotBlank() && year !in 1970..2100,
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedButton(
                onClick = {
                    val base = Calendar.getInstance(StartDateTime.timeZone).apply {
                        timeInMillis = (selectedEpoch ?: monthYearEpoch ?: current) * 1000L
                    }
                    DatePickerDialog(
                        context,
                        { _, pickedYear, pickedMonthZero, pickedDay ->
                            selectedEpoch = StartDateTime.toEpochSeconds(pickedYear, pickedMonthZero + 1, pickedDay)
                            monthText = (pickedMonthZero + 1).toString()
                            yearText = pickedYear.toString()
                        },
                        base.get(Calendar.YEAR),
                        base.get(Calendar.MONTH),
                        base.get(Calendar.DAY_OF_MONTH),
                    ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
                },
                enabled = !session.busy,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Mở lịch chọn ngày cụ thể") }

            proposedEpoch?.let {
                Text("Giá trị dự kiến: ${StartDateTime.format(it)} · Unix $it", style = MaterialTheme.typography.bodyMedium)
            }

            if (state.pendingEpochSeconds == null) {
                Button(
                    onClick = { viewModel.previewStartDate(requireNotNull(proposedEpoch)) },
                    enabled = proposedValid && !session.busy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Xem trước thay đổi") }
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("gameStartDate", fontWeight = FontWeight.Bold)
                        Text("${StartDateTime.format(current)} → ${StartDateTime.format(state.pendingEpochSeconds)}")
                        Text("$current → ${state.pendingEpochSeconds}", style = MaterialTheme.typography.bodySmall)
                        Text("Worker sẽ force stop Township, backup, sửa, encode–verify rồi ghi trực tiếp.")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = viewModel::discardStartDatePreview, enabled = !session.busy, modifier = Modifier.weight(1f)) { Text("Hủy") }
                            Button(onClick = viewModel::confirmPendingStartDateDirect, enabled = !session.busy, modifier = Modifier.weight(1f)) { Text("Ghi ngày tạo") }
                        }
                    }
                }
            }
        }
        Text(session.status, style = MaterialTheme.typography.bodySmall)
    }
}

