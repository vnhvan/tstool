package com.offline.saveeditor.coin

import org.junit.Assert.assertEquals
import org.junit.Test

class CoinXmlBytesTest {
    @Test fun readsAndReplacesOnlyMoney() {
        val xml = "<root><Var name=\"moneyCash\" v=\"99\"></Var><Var v='604' name='money'></Var><Var name=\"soundVolume\" v=\"50\"></Var></root>".toByteArray()
        assertEquals(604L, CoinXmlBytes.read(xml))
        val edited = CoinXmlBytes.replace(xml, 704L)
        assertEquals(704L, CoinXmlBytes.read(edited))
        val text = edited.toString(Charsets.UTF_8)
        assert(text.contains("moneyCash\" v=\"99"))
        assert(text.contains("soundVolume\" v=\"50"))
    }
}
