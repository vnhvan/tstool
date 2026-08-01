package com.offline.saveeditor.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF226B5D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8F0DF),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4B635C),
    secondaryContainer = Color(0xFFCDE8DF),
    tertiary = Color(0xFF47617A),
    background = Color(0xFFF7FAF7),
    surface = Color(0xFFF7FAF7),
    surfaceVariant = Color(0xFFDDE5E1),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9CD4C4),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF075043),
    secondary = Color(0xFFB1CCC3),
    secondaryContainer = Color(0xFF334B44),
    tertiary = Color(0xFFAFC9E6),
)

@Composable fun ChucksOfflineTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, typography = Typography(), content = content)
}
