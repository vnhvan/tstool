package com.offline.saveeditor.edit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EditRiskAssessorTest {
    private val rule = EditRule("sound", "Sound", "soundVolume", EditConfidence.VERIFIED, 0, 100)

    @Test fun normalChange() {
        val risk = EditRiskAssessor.assess(50, 55, rule)
        assertEquals(EditRiskLevel.NORMAL, risk.level)
        assertEquals(5, risk.absoluteDelta)
    }

    @Test fun largeRangeChangeIsHigh() {
        val risk = EditRiskAssessor.assess(5, 95, rule)
        assertEquals(EditRiskLevel.HIGH, risk.level)
        assertTrue(risk.absoluteDelta == 90L)
    }

    @Test fun zeroToNonZeroIsHigh() {
        assertEquals(EditRiskLevel.HIGH, EditRiskAssessor.assess(0, 1, rule).level)
    }
}
