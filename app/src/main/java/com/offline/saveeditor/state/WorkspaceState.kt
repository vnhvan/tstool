package com.offline.saveeditor.state

data class BrowserState(
    val query: String = "",
    val page: Int = 0,
) {
    fun search(value: String, maxLength: Int = 80): BrowserState = copy(query = value.take(maxLength), page = 0)
    fun moveTo(value: Int): BrowserState = copy(page = value.coerceAtLeast(0))
}

data class WorkspaceState(
    val backup: BrowserState = BrowserState(),
    val history: BrowserState = BrowserState(),
    val crash: BrowserState = BrowserState(),
    val showSettings: Boolean = false,
)
