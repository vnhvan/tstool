package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.ui.home.*

@Composable
fun HomeScreen(
    onOpenModules: () -> Unit,
    onOpenFeature: (HomeFeature) -> Unit,
) {
    val experimental = HomeCatalog.allFeatures.count { it.state == FeatureState.EXPERIMENTAL }
    val available = HomeCatalog.allFeatures.count { it.state == FeatureState.AVAILABLE }
    val comingSoon = HomeCatalog.allFeatures.size - experimental - available
    val coin = HomeCatalog.allFeatures.first { it.id == "coin" }

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chuck's Tool Offline", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Township module hub", style = MaterialTheme.typography.titleMedium)
                Text("Coin đã có bản Experimental. Các module khác sẽ được mở dần sau khi kiểm chứng dữ liệu save.")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onOpenFeature(coin) }) { Text("Thử Coin") }
                    OutlinedButton(onClick = onOpenModules) { Text("Tất cả module") }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusChip(available.toString(), "Available", Modifier.weight(1f))
            StatusChip(experimental.toString(), "Experimental", Modifier.weight(1f))
            StatusChip(comingSoon.toString(), "Coming soon", Modifier.weight(1f))
        }
        Text("Danh mục", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        HomeCatalog.categories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { category -> CategoryCard(category, Modifier.weight(1f), onOpenModules) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable private fun StatusChip(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), tonalElevation = 2.dp) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable private fun CategoryCard(category: HomeCategory, modifier: Modifier, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(Modifier.size(42.dp).background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Text(category.symbol, style = MaterialTheme.typography.titleLarge)
            }
            Text(category.title, fontWeight = FontWeight.Bold)
            Text(category.subtitle, style = MaterialTheme.typography.bodySmall)
            Text("${category.features.size} module", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable fun ComingSoonDialog(feature: HomeFeature, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = onDismiss) { Text("Đã hiểu") } },
        icon = { Text(feature.symbol, style = MaterialTheme.typography.headlineMedium) },
        title = { Text(feature.title) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("COMING SOON") })
            Text(feature.subtitle)
            Text("Module này đang được chuẩn bị. Chức năng xử lý save sẽ được bổ sung sau khi có đủ bằng chứng kiểm thử.")
        } })
}
