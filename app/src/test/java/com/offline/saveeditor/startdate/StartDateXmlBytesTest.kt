package com.offline.saveeditor.startdate

import org.junit.Assert.assertEquals
import org.junit.Test

class StartDateXmlBytesTest {
    @Test fun readAndReplace() {
        val xml = "<root><Var name=\"gameStartDate\" v=\"1785301092\"/></root>".toByteArray()
        assertEquals(1785301092L, StartDateXmlBytes.read(xml))
        val changed = StartDateXmlBytes.replace(xml, 1782838800L)
        assertEquals(1782838800L, StartDateXmlBytes.read(changed))
        assertEquals(1782838800L, StartDateTime.toEpochSeconds(2026, 7, 1))
    }
}
