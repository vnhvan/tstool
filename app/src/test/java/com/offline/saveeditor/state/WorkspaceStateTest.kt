package com.offline.saveeditor.state

import kotlin.test.Test
import kotlin.test.assertEquals

class WorkspaceStateTest {
    @Test fun searchResetsOnlyItsOwnPage() {
        val state = EditorSession(workspace = WorkspaceState(backup = BrowserState("old", 4), history = BrowserState("h", 2)))
        val updated = EditorReducer.reduce(state, EditorAction.SearchBackups("new"))
        assertEquals("new", updated.workspace.backup.query)
        assertEquals(0, updated.workspace.backup.page)
        assertEquals(2, updated.workspace.history.page)
    }

    @Test fun negativePageIsClamped() {
        val updated = EditorReducer.reduce(EditorSession(), EditorAction.CrashPage(-9))
        assertEquals(0, updated.workspace.crash.page)
    }
}
