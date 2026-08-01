package com.offline.saveeditor.concurrency

/**
 * Monotonic generation gate for asynchronous UI work.
 * Only the newest started operation is allowed to publish a result.
 */
class LatestOperationGate {
    private var generation: Long = 0

    @Synchronized
    fun begin(): Long = ++generation

    @Synchronized
    fun isCurrent(token: Long): Boolean = token == generation

    @Synchronized
    fun invalidate(): Long = ++generation

    @Synchronized
    fun current(): Long = generation
}
