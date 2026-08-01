package com.offline.saveeditor.history

import com.offline.saveeditor.change.FieldChange
import kotlin.test.Test
import kotlin.test.assertEquals

class EditHistoryTest {
    @Test fun keepsNewestEntriesWithinCapacity() {
        val history = EditHistory(capacity = 2)
        repeat(3) { index ->
            history.add(EditHistoryEntry(index.toLong() * 1000, "src$index", "out$index", "hash$index", listOf(FieldChange("x", "$index", "${index+1}"))))
        }
        assertEquals(listOf("out2", "out1"), history.all().map { it.outputName })
    }
}
