package com.offline.saveeditor.ui.launcher

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import com.offline.saveeditor.export.ExportKind
import com.offline.saveeditor.export.ExportPayload
import com.offline.saveeditor.io.LimitedInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DocumentLaunchers internal constructor(
    val open: () -> Unit,
    val compare: () -> Unit,
    val export: (ExportPayload) -> Unit,
)

@Composable
fun rememberDocumentLaunchers(
    resolver: ContentResolver,
    onOpenBytes: (ByteArray) -> Unit,
    onCompareBytes: (ByteArray) -> Unit,
    onExportStarted: (ExportPayload) -> Unit = {},
    onExported: (ExportPayload) -> Unit,
    onExportCancelled: (ExportPayload) -> Unit = {},
    onError: (String) -> Unit,
): DocumentLaunchers {
    var pending by remember { mutableStateOf<ExportPayload?>(null) }
    val scope = rememberCoroutineScope()
    fun read(uri: Uri, target: (ByteArray) -> Unit) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { resolver.openInputStream(uri)?.use(LimitedInput::read) ?: error("Không mở được input stream") }
            }.onSuccess(target).onFailure { onError(it.message ?: "Lỗi đọc file") }
        }
    }
    fun write(uri: Uri, payload: ExportPayload) {
        onExportStarted(payload)
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { resolver.openOutputStream(uri, "w")?.use { it.write(payload.bytes) } ?: error("Không mở được output stream") }
            }.onSuccess { onExported(payload) }.onFailure { onError(it.message ?: "Lỗi xuất file") }
        }
    }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let { uri -> read(uri, onOpenBytes) } }
    val compareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let { uri -> read(uri, onCompareBytes) } }
    fun finishCreate(uri: Uri?) {
        val payload = pending
        pending = null
        if (payload != null) {
            if (uri != null) write(uri, payload) else onExportCancelled(payload)
        }
    }
    val binary = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(ExportKind.BINARY.mimeType), ::finishCreate)
    val xml = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(ExportKind.XML.mimeType), ::finishCreate)
    val text = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(ExportKind.TEXT.mimeType), ::finishCreate)

    return remember(openLauncher, compareLauncher, binary, xml, text) {
        DocumentLaunchers(
            open = { openLauncher.launch(arrayOf("*/*")) },
            compare = { compareLauncher.launch(arrayOf("*/*")) },
            export = { payload ->
                pending = payload
                when (payload.kind) {
                    ExportKind.BINARY -> binary.launch(payload.safeName)
                    ExportKind.XML -> xml.launch(payload.safeName)
                    ExportKind.TEXT -> text.launch(payload.safeName)
                }
            },
        )
    }
}
