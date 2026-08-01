package com.offline.saveeditor.settings

import android.content.Context

data class AppSettings(
    val maxBackups: Int = 25,
    val showCandidateModules: Boolean = true,
    val enableRootReadOnlyProbe: Boolean = false,
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("offline_save_editor_settings", Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        maxBackups = prefs.getInt("max_backups", 25).coerceIn(5, 100),
        showCandidateModules = prefs.getBoolean("show_candidate_modules", true),
        enableRootReadOnlyProbe = prefs.getBoolean("root_read_only_probe", false),
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putInt("max_backups", settings.maxBackups.coerceIn(5, 100))
            .putBoolean("show_candidate_modules", settings.showCandidateModules)
            .putBoolean("root_read_only_probe", settings.enableRootReadOnlyProbe)
            .apply()
    }
}
