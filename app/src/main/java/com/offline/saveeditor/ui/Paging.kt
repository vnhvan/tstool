package com.offline.saveeditor.ui

data class Page<T>(val items: List<T>, val page: Int, val pageSize: Int, val totalItems: Int) {
    val totalPages: Int get() = if (totalItems == 0) 1 else (totalItems + pageSize - 1) / pageSize
    val canPrevious: Boolean get() = page > 0
    val canNext: Boolean get() = page + 1 < totalPages
}

object Paging {
    fun <T> page(items: List<T>, page: Int, pageSize: Int): Page<T> {
        require(pageSize in 1..500) { "Kích thước trang không hợp lệ" }
        val totalPages = if (items.isEmpty()) 1 else (items.size + pageSize - 1) / pageSize
        val safePage = page.coerceIn(0, totalPages - 1)
        val from = (safePage * pageSize).coerceAtMost(items.size)
        val to = (from + pageSize).coerceAtMost(items.size)
        return Page(items.subList(from, to), safePage, pageSize, items.size)
    }
}
