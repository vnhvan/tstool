package com.offline.saveeditor.concurrency

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LatestOperationGateTest {
    @Test fun newestOperationWins() {
        val gate = LatestOperationGate()
        val first = gate.begin()
        val second = gate.begin()
        assertFalse(gate.isCurrent(first))
        assertTrue(gate.isCurrent(second))
        gate.invalidate()
        assertFalse(gate.isCurrent(second))
    }
}
