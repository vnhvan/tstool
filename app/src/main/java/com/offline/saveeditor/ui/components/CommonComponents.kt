package com.offline.saveeditor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PagedControls(page: Int, totalPages: Int, canPrevious: Boolean, canNext: Boolean, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(enabled = canPrevious, onClick = onPrevious) { Text("Trang trước") }
        Text("${page + 1}/${totalPages.coerceAtLeast(1)}")
        TextButton(enabled = canNext, onClick = onNext) { Text("Trang sau") }
    }
}

@Composable
fun BusyPanel(label: String, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Text(label)
        Button(onClick = onCancel) { Text("Hủy tác vụ") }
    }
}
