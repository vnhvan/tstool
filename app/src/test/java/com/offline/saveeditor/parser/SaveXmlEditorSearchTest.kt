package com.offline.saveeditor.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveXmlEditorSearchTest {
    private fun fixture(): ByteArray = requireNotNull(
        javaClass.getResourceAsStream("/fixtures/mGameInfo_original.xml")
    ).readBytes().let { com.offline.saveeditor.codec.MGameInfoCodec.decode(it) }

    @Test fun listsKnownVariables() {
        val vars = SaveXmlEditor.listVars(fixture())
        assertTrue(vars.size > 100)
        assertEquals("634", vars.first { it.name == "money" }.value)
        assertEquals("666", vars.first { it.name == "moneyCash" }.value)
    }

    @Test fun searchIsCaseInsensitiveAndLimited() {
        val result = SaveXmlEditor.searchVars(fixture(), "MONEY", limit = 10)
        assertTrue(result.any { it.name == "money" })
        assertTrue(result.size <= 10)
    }
}
