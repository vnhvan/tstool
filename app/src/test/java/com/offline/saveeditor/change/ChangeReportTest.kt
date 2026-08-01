package com.offline.saveeditor.change

import com.offline.saveeditor.model.SaveFields
import org.junit.Assert.*
import org.junit.Test

class ChangeReportTest {
    @Test fun reportsOnlyChangedFields() {
        val before = SaveFields(634, 666, 0, null)
        val after = SaveFields(634, 666, 100, null)
        val report = ChangeReport.between(before, after)
        assertEquals(1, report.changes.size)
        assertEquals("Sound Volume", report.changes.single().field)
        assertTrue(report.toPlainText().contains("0 -> 100"))
    }
}
