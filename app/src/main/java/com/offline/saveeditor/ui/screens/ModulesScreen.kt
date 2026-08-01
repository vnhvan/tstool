package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.ui.home.*

@Composable
fun ModulesScreen(onOpenFeature: (HomeFeature) -> Unit) {
    var query by remember { mutableStateOf("") }
    var selectedComingSoon by remember { mutableStateOf<HomeFeature?>(null) }
    val categories = remember(query) {
        HomeCatalog.categories.map { category ->
            category.copy(features = category.features.filter { feature ->
                feature.title.contains(query, true) || feature.subtitle.contains(query, true)
            })
        }.filter { it.features.isNotEmpty() }
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("All Modules", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tìm module") }, singleLine = true)
        categories.forEach { category ->
            Text(category.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            category.features.forEach { feature ->
                val enabled = feature.state != FeatureState.COMING_SOON
                Card(
                    Modifier.fillMaxWidth().clickable {
                        if (enabled) onOpenFeature(feature) else selectedComingSoon = feature
                    },
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(feature.symbol, style = MaterialTheme.typography.titleLarge)
                        Column(Modifier.weight(1f)) {
                            Text(feature.title, fontWeight = FontWeight.SemiBold)
                            Text(feature.subtitle, style = MaterialTheme.typography.bodySmall)
                        }
                        SuggestionChip(
                            onClick = { if (enabled) onOpenFeature(feature) else selectedComingSoon = feature },
                            label = { Text(when (feature.state) {
                                FeatureState.AVAILABLE -> "Open"
                                FeatureState.EXPERIMENTAL -> "Experimental"
                                FeatureState.COMING_SOON -> "Soon"
                            }) },
                        )
                    }
                }
            }
        }
    }
    selectedComingSoon?.let { ComingSoonDialog(it) { selectedComingSoon = null } }
}
