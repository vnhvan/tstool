package com.offline.saveeditor.model

/** Lightweight visibility into the two largest in-memory buffers retained by a document. */
data class SaveMemoryProfile(
    val containerBytes: Int,
    val xmlBytes: Int,
) {
    val retainedBytes: Long get() = containerBytes.toLong() + xmlBytes.toLong()
    val retainedMiB: Double get() = retainedBytes / (1024.0 * 1024.0)
    val duplicatesLargePayload: Boolean get() = containerBytes >= LARGE_BUFFER && xmlBytes >= LARGE_BUFFER

    companion object {
        private const val LARGE_BUFFER = 8 * 1024 * 1024
        fun from(document: SaveDocument) = SaveMemoryProfile(document.container.size, document.xml.size)
    }
}
