package com.acmeai.chargevibe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4FF),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D63),
    onPrimaryContainer = Color(0xFF97EFFF),
    secondary = Color(0xFFB4CAD6),
    onSecondary = Color(0xFF1F333C),
    secondaryContainer = Color(0xFF354A54),
    onSecondaryContainer = Color(0xFFD0E6F2),
    tertiary = Color(0xFFCBC1E9),
    onTertiary = Color(0xFF332C4B),
    tertiaryContainer = Color(0xFF4A4263),
    onTertiaryContainer = Color(0xFFE8DEFF),
    error = Color(0xFFFFB4AB),
    background = Color(0xFF0A0E12),
    onBackground = Color(0xFFE1E3E5),
    surface = Color(0xFF0A0E12),
    onSurface = Color(0xFFE1E3E5),
    surfaceVariant = Color(0xFF40484D),
    onSurfaceVariant = Color(0xFFC0C8CD),
    outline = Color(0xFF8A9297)
)

@Composable
fun ChargeVibeTheme(content: @Composable () -> Unit) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
