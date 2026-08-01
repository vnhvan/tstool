package com.offline.saveeditor.state

import com.offline.saveeditor.navigation.AppScreen
import kotlin.test.Test
import kotlin.test.assertEquals

class EditorReducerTest {
    @Test fun searchResetsOnlyItsOwnPage() {
        val initial = EditorSession(varPage = 5, objectPage = 7)
        val next = EditorReducer.reduce(initial, EditorAction.SearchVars("money"))
        assertEquals("money", next.varQuery)
        assertEquals(0, next.varPage)
        assertEquals(7, next.objectPage)
    }

    @Test fun pageNeverBecomesNegative() {
        assertEquals(0, EditorReducer.reduce(EditorSession(), EditorAction.VarPage(-3)).varPage)
    }

    @Test fun navigationIsExplicit() {
        assertEquals(AppScreen.TOOLS, EditorReducer.reduce(EditorSession(), EditorAction.Navigate(AppScreen.TOOLS)).screen)
    }
}
