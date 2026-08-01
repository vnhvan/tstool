package com.offline.saveeditor.analysis

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SaveAnalyzerDiffTest {
    private fun fixture(name: String): ByteArray = requireNotNull(javaClass.classLoader?.getResource(name)).readBytes()

    @Test fun analyzesRealDecodedFixture() {
        val snapshot = SaveAnalyzer.snapshot(fixture("fixtures/mGameInfo_decoded.xml"))
        val summary = SaveAnalyzer.summarize(snapshot)
        assertEquals("0", snapshot.vars["soundVolume"])
        assertTrue(summary.varCount > 1000)
        assertTrue(summary.objectCount > 500)
        assertEquals(0, summary.duplicateVarCount)
    }

    @Test fun fullDiffFindsVerifiedSoundChange() {
        val before = SaveAnalyzer.snapshot(fixture("fixtures/mGameInfo_decoded.xml"))
        val after = SaveAnalyzer.snapshot(fixture("fixtures/mGameInfo_sound100_decoded.xml"))
        val diff = SaveDiffEngine.between(before, after)
        assertEquals(1, diff.vars.size)
        assertEquals("soundVolume", diff.vars.single().name)
        assertEquals("0", diff.vars.single().before)
        assertEquals("100", diff.vars.single().after)
        assertEquals(0, diff.objects.size)
    }
}
