package com.offline.saveeditor.navigation

enum class AppScreen(val title: String, val symbol: String, val showInBottomBar: Boolean = true) {
    HOME("Home", "⌂"),
    MODULES("Modules", "▦"),
    TOOLS("Tools", "⚙"),
    SETTINGS("Settings", "☰"),
    COIN("Coin", "●", false),
}

object ScreenState {
    fun normalize(screen: AppScreen?, fallback: AppScreen = AppScreen.HOME): AppScreen = screen ?: fallback
}
