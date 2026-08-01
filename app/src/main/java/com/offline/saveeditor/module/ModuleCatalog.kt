package com.offline.saveeditor.module

/** Central catalog so new modules can be registered without changing the UI. */
class ModuleCatalog private constructor(private val entries: Map<String, EditorModule>) {
    val modules: List<EditorModule> get() = entries.values.sortedBy { it.title.lowercase() }

    fun find(id: String): EditorModule? = entries[id]

    fun visible(showCandidate: Boolean): List<EditorModule> = modules.filter {
        showCandidate || it.status !in setOf(ModuleStatus.READ_ONLY, ModuleStatus.EXPERIMENTAL)
    }

    fun writable(): List<EditorModule> = modules.filter { it.status == ModuleStatus.VERIFIED }

    class Builder {
        private val entries = linkedMapOf<String, EditorModule>()
        fun register(module: EditorModule): Builder = apply {
            require(module.id.matches(Regex("[a-z0-9_]+"))) { "Module id không hợp lệ: ${module.id}" }
            require(module.id !in entries) { "Module id bị trùng: ${module.id}" }
            entries[module.id] = module
        }
        fun build(): ModuleCatalog = ModuleCatalog(entries.toMap())
    }
}
