package org.blindsystems.bop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Dark theme (Premium Neon) ─────────────────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary         = Color(0xFF00E5FF),   // Cyan Neon
    onPrimary       = Color(0xFF00373E),
    primaryContainer= Color(0xFF004E59),
    onPrimaryContainer = Color(0xFFB2F5FF),
    secondary       = Color(0xFF76FF03),   // Lime Neon
    onSecondary     = Color(0xFF1B3600),
    tertiary        = Color(0xFFFF4081),   // Pink Neon
    onTertiary      = Color(0xFF3F0019),
    background      = Color(0xFF0A0E14),   // Deep dark blue-black
    surface         = Color(0xFF141B24),
    onBackground    = Color(0xFFE1E2E5),
    onSurface       = Color(0xFFE1E2E5),
    surfaceVariant  = Color(0xFF232D39),
    onSurfaceVariant= Color(0xFFC1C7CE),
    outline         = Color(0xFF8B9199),
    error           = Color(0xFFFF5252),
)

// ── Light theme ───────────────────────────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary         = Color(0xFF0277BD),
    onPrimary       = Color(0xFFFFFFFF),
    secondary       = Color(0xFF388E3C),
    tertiary        = Color(0xFFF57C00),
    background      = Color(0xFFF5F5F5),
    surface         = Color(0xFFFFFFFF),
    onBackground    = Color(0xFF1A1A1A),
    onSurface       = Color(0xFF1A1A1A),
    surfaceVariant  = Color(0xFFE8E8E8),
)

// ── High-contrast theme ───────────────────────────────────────────────────────
private val HighContrastColors = darkColorScheme(
    primary         = Color(0xFFFFFF00),
    onPrimary       = Color(0xFF000000),
    primaryContainer= Color(0xFF333300),
    secondary       = Color(0xFF00FF00),
    tertiary        = Color(0xFFFF8000),
    background      = Color(0xFF000000),
    surface         = Color(0xFF000000),
    onBackground    = Color(0xFFFFFFFF),
    onSurface       = Color(0xFFFFFFFF),
    surfaceVariant  = Color(0xFF1A1A1A),
    error           = Color(0xFFFF0000),
)

fun colorSchemeFor(theme: String): ColorScheme = when (theme) {
    "LIGHT"        -> LightColors
    "HIGH_CONTRAST"-> HighContrastColors
    else           -> DarkColors   // DARK
}

@Composable
fun BopTheme(theme: String = "DARK", content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorSchemeFor(theme),
        content = content
    )
}
