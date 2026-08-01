package com.offline.saveeditor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offline.saveeditor.analysis.SaveAnalyzer
import com.offline.saveeditor.edit.DefaultEditRuleProvider
import com.offline.saveeditor.edit.ModulePreflightEngine
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.state.EditorSession
import com.offline.saveeditor.state.EditorViewModel
import com.offline.saveeditor.model.SaveMemoryProfile
import com.offline.saveeditor.ui.Paging
import com.offline.saveeditor.ui.components.DynamicModuleEditor
import com.offline.saveeditor.ui.components.EditPreviewCard

@Composable
fun EditorScreen(
    session: EditorSession,
    viewModel: EditorViewModel,
    onOpen: () -> Unit,
    onExport: (ExportPayload) -> Unit,
) {
    val document = session.document
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(enabled = !session.busy, onClick = onOpen) { Text("Chọn mGameInfo") }
        if (document == null) {
            Text("Chưa có save. Hãy chọn mGameInfo nhị phân hoặc XML đã giải mã.")
            return@Column
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Thông tin save", style = MaterialTheme.typography.titleMedium)
                Text("Nguồn: ${document.sourceKind}")
                Text("Container: ${document.containerSize} byte · XML: ${document.xml.size} byte")
                Text("SHA-256: ${document.sha256}")
        val memory = SaveMemoryProfile.from(document)
        Text("RAM document: %.2f MiB (container %,d + XML %,d byte)".format(memory.retainedMiB, memory.containerBytes, memory.xmlBytes))
        if (memory.duplicatesLargePayload) Text("Cảnh báo: đang giữ đồng thời hai buffer lớn; nên tránh mở nhiều save liên tiếp.")
                Text("Coin: ${document.fields.coin ?: "không thấy"} · TCash: ${document.fields.tcash ?: "không thấy"}")
                Text("Sound: ${document.fields.soundVolume ?: "không thấy"} · Mine: ${document.fields.mineDepth ?: "chưa xác định"}")
            }
        }
        val modulePreflight = remember(document.sha256) {
            ModulePreflightEngine.inspect(document, DefaultEditRuleProvider.instance.verified())
        }
        DynamicModuleEditor(
            modules = modulePreflight,
            enabled = !session.busy,
            onApply = { module, value -> viewModel.prepareVerifiedEdit(module.rule.id, value) },
            modifier = Modifier.fillMaxWidth(),
        )
        session.pendingEditPreview?.let { preview ->
            EditPreviewCard(
                preview = preview,
                enabled = !session.busy,
                onConfirm = viewModel::confirmPendingEditExport,
                onDiscard = viewModel::discardPendingEdit,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        val snapshot = remember(document.xml) { SaveAnalyzer.snapshot(document.xml) }
        OutlinedTextField(
            value = session.varQuery,
            onValueChange = viewModel::updateVarQuery,
            label = { Text("Tìm Var") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        val vars = remember(snapshot, session.varQuery) {
            snapshot.vars.entries.filter { session.varQuery.isBlank() || it.key.contains(session.varQuery, true) || it.value.contains(session.varQuery, true) }
        }
        val varPage = remember(vars, session.varPage) { Paging.page(vars, session.varPage, 20) }
        Text("${varPage.totalItems} Var · trang ${varPage.page + 1}/${varPage.totalPages}")
        varPage.items.forEach { Text("${it.key} = ${it.value}") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(enabled = varPage.canPrevious, onClick = viewModel::previousVarPage) { Text("Trang trước") }
            TextButton(enabled = varPage.canNext, onClick = viewModel::nextVarPage) { Text("Trang sau") }
        }

        OutlinedTextField(
            value = session.objectQuery,
            onValueChange = viewModel::updateObjectQuery,
            label = { Text("Tìm Object") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        val objects = remember(snapshot, session.objectQuery) {
            snapshot.objects.filter { node -> session.objectQuery.isBlank() || node.attributes.values.any { it.contains(session.objectQuery, true) } }
        }
        val objectPage = remember(objects, session.objectPage) { Paging.page(objects, session.objectPage, 12) }
        Text("${objectPage.totalItems} Object · trang ${objectPage.page + 1}/${objectPage.totalPages}")
        objectPage.items.forEach { Text("${it.name ?: "Object"} · BID=${it.bid ?: "-"} · ${it.data?.take(80) ?: ""}") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(enabled = objectPage.canPrevious, onClick = viewModel::previousObjectPage) { Text("Trang trước") }
            TextButton(enabled = objectPage.canNext, onClick = viewModel::nextObjectPage) { Text("Trang sau") }
        }
    }
}
