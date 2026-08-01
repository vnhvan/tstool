package com.offline.saveeditor.restore

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RestorePipelineTest {
    private fun fixture(): ByteArray = requireNotNull(javaClass.classLoader?.getResource("fixtures/mGameInfo_decoded.xml")).readBytes()

    @Test fun verifiesByteForByteStaging() {
        val bytes = fixture()
        val plan = RestorePipeline.prepare(bytes, "test")
        assertTrue(RestorePipeline.verifyWritten(plan, bytes.copyOf()).success)
        assertFalse(RestorePipeline.verifyWritten(plan, bytes.copyOf(bytes.size - 1)).success)
        assertFalse(RestorePipeline.verifyWritten(plan, null).success)
    }
}
