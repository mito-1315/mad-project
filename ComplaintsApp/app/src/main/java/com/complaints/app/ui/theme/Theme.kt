package com.complaints.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary          = Primary,
    onPrimary        = Color.White,
    primaryContainer = PrimaryLight,
    background       = Background,
    surface          = Surface,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    surfaceVariant   = SurfaceVariant,
    onSurfaceVariant = TextMuted,
    error            = Error,
    outline          = Border
)

private val DarkColorScheme = darkColorScheme(
    primary          = PrimaryLight,
    onPrimary        = Color.White,
    primaryContainer = PrimaryDark,
    background       = Color(0xFF0F172A),
    surface          = Color(0xFF1E293B),
    onBackground     = Color(0xFFF1F5F9),
    onSurface        = Color(0xFFF1F5F9),
    surfaceVariant   = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    error            = SeverityHigh,
    outline          = Color(0xFF334155)
)

@Composable
fun ComplaintsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
