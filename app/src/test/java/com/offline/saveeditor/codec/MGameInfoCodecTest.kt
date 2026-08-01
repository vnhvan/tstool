package com.offline.saveeditor.codec

import com.offline.saveeditor.model.SaveRepository
import com.offline.saveeditor.parser.SaveXmlEditor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MGameInfoCodecTest {
    private fun fixture(name: String): ByteArray =
        requireNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")) {
            "Missing fixture $name"
        }.use { it.readBytes() }

    @Test
    fun townshipAcceptedOriginalDecodes() {
        val doc = SaveRepository.open(fixture("mGameInfo_original.xml"))
        assertTrue(doc.xml.toString(Charsets.UTF_8).startsWith("<root>"))
        assertEquals(0, doc.fields.soundVolume)
        assertTrue(doc.containerSize == 300_008)
    }

    @Test
    fun townshipAcceptedSoundFixtureContains100() {
        val doc = SaveRepository.open(fixture("mGameInfo_sound100.xml"))
        assertEquals(100, doc.fields.soundVolume)
    }

    @Test
    fun editingSoundRoundTripsByteExactlyAtXmlLevel() {
        val original = SaveRepository.open(fixture("mGameInfo_original.xml"))
        val edited = SaveXmlEditor.setSoundVolume(original.xml, 100)
        val encoded = SaveRepository.encodeAndVerify(original, edited)
        assertEquals(300_008, encoded.size)
        assertEquals(100, SaveRepository.open(encoded).fields.soundVolume)
    }

    @Test
    fun knownRoundTripFixtureDecodesToSameXml() {
        val original = SaveRepository.open(fixture("mGameInfo_original.xml"))
        val roundTrip = SaveRepository.open(fixture("mGameInfo_roundtrip.xml"))
        assertTrue(original.xml.contentEquals(roundTrip.xml))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsTruncatedContainer() {
        MGameInfoCodec.decode(byteArrayOf(0x79, 0, 0))
    }
}
