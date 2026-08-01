package com.offline.saveeditor.ui.browser

import com.offline.saveeditor.ui.Page
import com.offline.saveeditor.ui.Paging

/** Search + pagination shared by backups, history and crash logs. */
object NamedBrowser {
    fun <T> browse(
        items: List<T>,
        query: String,
        page: Int,
        pageSize: Int,
        searchableText: (T) -> String,
    ): Page<T> {
        require(pageSize > 0) { "pageSize must be positive" }
        val needle = query.trim()
        val filtered = if (needle.isEmpty()) items else items.filter {
            searchableText(it).contains(needle, ignoreCase = true)
        }
        return Paging.page(filtered, page, pageSize)
    }
}
