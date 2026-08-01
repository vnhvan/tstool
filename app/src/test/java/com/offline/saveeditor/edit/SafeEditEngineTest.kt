package com.offline.saveeditor.edit

import com.offline.saveeditor.model.SaveRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SafeEditEngineTest {
    private fun fixture(name: String): ByteArray = requireNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")).readBytes()

    @Test fun verifiedSoundChangesExactlyOneVarAndRoundTrips() {
        val doc = SaveRepository.open(fixture("mGameInfo_original.xml"))
        val prepared = SafeEditEngine.prepare(doc, EditRules.SOUND_VOLUME, 100)
        assertEquals(1, prepared.diff.vars.size)
        assertEquals("soundVolume", prepared.diff.vars.single().name)
        val encoded = SafeEditEngine.encodeAndVerify(doc, prepared)
        assertEquals(300_008, encoded.output.size)
    }


    @Test fun experimentalCoinChangesOnlyMoneyAndRoundTrips() {
        val doc = SaveRepository.open(fixture("mGameInfo_original.xml"))
        val prepared = SafeEditEngine.prepare(doc, EditRules.COIN, 734, allowCandidate = true)
        assertEquals(1, prepared.diff.vars.size)
        assertEquals("money", prepared.diff.vars.single().name)
        assertEquals(0, prepared.diff.objects.size)
        val encoded = SafeEditEngine.encodeAndVerify(doc, prepared)
        assertEquals(300_008, encoded.output.size)
    }

    @Test fun candidateModulesAreLockedByDefault() {
        val doc = SaveRepository.open(fixture("mGameInfo_original.xml"))
        assertThrows(IllegalArgumentException::class.java) {
            SafeEditEngine.prepare(doc, EditRules.COIN, 999)
        }
    }
}
