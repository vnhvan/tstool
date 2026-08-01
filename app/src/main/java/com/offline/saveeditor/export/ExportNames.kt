package com.offline.saveeditor.export

object ExportNames {
    private val unsafe = Regex("[^A-Za-z0-9._-]+")
    fun safeFileName(input: String, fallback: String = "export.bin"): String {
        val normalized = input.trim().replace(unsafe, "_").trim('_', '.')
        return normalized.take(120).ifBlank { fallback }
    }
}
