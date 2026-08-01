package com.offline.saveeditor.diagnostics

import com.offline.saveeditor.model.SaveRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticsEngineTest {
    private fun fixture(name: String): ByteArray = requireNotNull(javaClass.classLoader?.getResourceAsStream("fixtures/$name")).readBytes()

    @Test fun realFixtureProducesUsefulReport() {
        val report = DiagnosticsEngine.inspect(SaveRepository.open(fixture("mGameInfo_original.xml")))
        assertEquals(0, report.errorCount)
        assertTrue(report.safeForVerifiedEdits)
        assertTrue(report.toPlainText().contains("1299 Var"))
        assertTrue(report.toPlainText().contains("Sound Volume"))
    }
}
