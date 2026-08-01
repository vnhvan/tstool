package com.offline.saveeditor.state

import com.offline.saveeditor.export.ExportKind
import com.offline.saveeditor.export.ExportPayload
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame

class PendingExportReducerTest {
    @Test
    fun exportReadyStopsBusyAndCanBeConsumed() {
        val payload = ExportPayload(byteArrayOf(1), "result.bin", ExportKind.BINARY)
        val ready = EditorReducer.reduce(
            EditorSession(busy = true, operationLabel = "work"),
            EditorAction.ExportReady(payload, "ready"),
        )
        assertSame(payload, ready.pendingExport)
        assertFalse(ready.busy)
        assertEquals("ready", ready.status)
        assertNull(EditorReducer.reduce(ready, EditorAction.ExportConsumed).pendingExport)
    }
}
