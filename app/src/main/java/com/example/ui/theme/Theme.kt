package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Definition of CSS-like design variables for consistent Notion aesthetic
data class NotionColors(
    val bg: Color,
    val text: Color,
    val textMuted: Color,
    val border: Color,
    val cardBg: Color,
    val itemBg: Color,
    val accent: Color,
    val accentBg: Color,
    val success: Color,
    val error: Color
)

// Notion signature palette (Light Mode)
val NotionLightColors = NotionColors(
    bg = Color(0xFFFCFCFB),         // --notion-bg-color
    text = Color(0xFF191919),       // --notion-text-color
    textMuted = Color(0xFF787774),  // --notion-text-muted-color
    border = Color(0xFFE9E9E7),     // --notion-border-color
    cardBg = Color(0xFFFFFFFF),     // --notion-card-bg
    itemBg = Color(0xFFF1F1EF),     // --notion-item-bg
    accent = Color(0xFF2383E2),     // --notion-accent-color
    accentBg = Color(0xFFEBF5FE),   // --notion-accent-bg
    success = Color(0xFF0F7B4A),    // --notion-success-color
    error = Color(0xFFD13438)       // --notion-error-color
)

// Notion signature palette (Dark Mode)
val NotionDarkColors = NotionColors(
    bg = Color(0xFF191919),         // --notion-bg-color (dark)
    text = Color(0xFFE3E3E2),       // --notion-text-color (dark)
    textMuted = Color(0xFF9B9B9A),  // --notion-text-muted-color (dark)
    border = Color(0xFF2F2F2F),     // --notion-border-color (dark)
    cardBg = Color(0xFF202020),     // --notion-card-bg (dark)
    itemBg = Color(0xFF252525),     // --notion-item-bg (dark)
    accent = Color(0xFF2383E2),     // --notion-accent-color (dark)
    accentBg = Color(0xFF1D2D3D),   // --notion-accent-bg (dark)
    success = Color(0xFF34D399),    // --notion-success-color (dark)
    error = Color(0xFFF87171)       // --notion-error-color (dark)
)

val LocalNotionColors = staticCompositionLocalOf { NotionLightColors }

object NotionTheme {
    val colors: NotionColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNotionColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2383E2),
    onPrimary = Color(0xFF191919),
    primaryContainer = Color(0xFF202020),
    onPrimaryContainer = Color(0xFFE3E3E2),
    secondary = Color(0xFF1D2D3D),
    onSecondary = Color(0xFF2383E2),
    background = Color(0xFF191919),
    onBackground = Color(0xFFE3E3E2),
    surface = Color(0xFF202020),
    onSurface = Color(0xFFE3E3E2),
    surfaceVariant = Color(0xFF252525),
    onSurfaceVariant = Color(0xFF9B9B9A),
    outline = Color(0xFF2F2F2F),
    outlineVariant = Color(0xFF2F2F2F),
    error = Color(0xFFF87171)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2383E2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFF191919),
    secondary = Color(0xFFEBF5FE),
    onSecondary = Color(0xFF2383E2),
    background = Color(0xFFFCFCFB),
    onBackground = Color(0xFF191919),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191919),
    surfaceVariant = Color(0xFFF1F1EF),
    onSurfaceVariant = Color(0xFF787774),
    outline = Color(0xFFE9E9E7),
    outlineVariant = Color(0xFFE9E9E7),
    error = Color(0xFFD13438)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val notionColors = if (darkTheme) NotionDarkColors else NotionLightColors

    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    CompositionLocalProvider(LocalNotionColors provides notionColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
