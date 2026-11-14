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

data class UbuntuAppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val card: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentOrange: Color,
    val aubergine: Color
)

val UbuntuHighContrastDarkColors = UbuntuAppColors(
    isDark = true,
    background = UbuntuHighContrastDarkBackground,
    surface = UbuntuHighContrastDarkSurface,
    card = UbuntuHighContrastDarkCard,
    border = UbuntuHighContrastDarkBorder,
    textPrimary = UbuntuHighContrastTextPrimary,
    textSecondary = UbuntuHighContrastTextSecondary,
    accentOrange = UbuntuHighContrastOrange,
    aubergine = UbuntuAubergine
)

val UbuntuLightColors = UbuntuAppColors(
    isDark = false,
    background = UbuntuLightBackground,
    surface = UbuntuLightSurface,
    card = UbuntuLightCard,
    border = UbuntuLightBorder,
    textPrimary = UbuntuLightTextPrimary,
    textSecondary = UbuntuLightTextSecondary,
    accentOrange = UbuntuOrange,
    aubergine = UbuntuAubergine
)

val LocalUbuntuColors = staticCompositionLocalOf { UbuntuHighContrastDarkColors }

private val DarkColorScheme = darkColorScheme(
    primary = UbuntuHighContrastOrange,
    onPrimary = Color.White,
    primaryContainer = UbuntuDeepAubergine,
    onPrimaryContainer = UbuntuLightOrange,
    secondary = UbuntuAubergine,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF491244),
    onSecondaryContainer = Color(0xFFE89EE1),
    tertiary = UbuntuLightOrange,
    onTertiary = UbuntuDeepAubergine,
    background = UbuntuHighContrastDarkBackground,
    onBackground = UbuntuHighContrastTextPrimary,
    surface = UbuntuHighContrastDarkSurface,
    onSurface = UbuntuHighContrastTextPrimary,
    surfaceVariant = UbuntuHighContrastDarkCard,
    onSurfaceVariant = UbuntuHighContrastTextSecondary,
    outline = UbuntuHighContrastDarkBorder,
    error = UbuntuErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = UbuntuOrange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE5),
    onPrimaryContainer = UbuntuDarkOrange,
    secondary = UbuntuAubergine,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E5F1),
    onSecondaryContainer = UbuntuAubergine,
    tertiary = UbuntuDarkOrange,
    onTertiary = Color.White,
    background = UbuntuLightBackground,
    onBackground = UbuntuLightTextPrimary,
    surface = UbuntuLightSurface,
    onSurface = UbuntuLightTextPrimary,
    surfaceVariant = Color(0xFFECECEC),
    onSurfaceVariant = UbuntuLightTextSecondary,
    outline = UbuntuLightBorder,
    error = UbuntuErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Ubuntu High-Contrast Dark theme
    dynamicColor: Boolean = false, // Keep distinct Ubuntu identity
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val ubuntuColors = if (darkTheme) UbuntuHighContrastDarkColors else UbuntuLightColors

    CompositionLocalProvider(LocalUbuntuColors provides ubuntuColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// ---------------------------------------------------------------------------
// Theme-aware design tokens
//
// Prefer these over the fixed palette constants (UbuntuDarkSurface, Color.White,
// UbuntuWarmGrey, ...). The fixed constants always render dark chrome; these
// accessors follow whichever theme is active, so light mode stays readable and
// every surface keeps the same contrast relationships.
// ---------------------------------------------------------------------------

/** Window / page background. */
val ThemeBackground: Color
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.background

/** Panel and sheet surface sitting on the background. */
val ThemeSurface: Color
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.surface

/** Raised card / tile surface. */
val ThemeCard: Color
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.card

/** 1dp hairline used for card outlines and dividers. */
val ThemeBorder: Color
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.border

/** Primary body and heading text on [ThemeSurface] / [ThemeCard]. */
val ThemeTextPrimary: Color
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.textPrimary

/** Supporting text, captions and inactive iconography. */
val ThemeTextSecondary: Color
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.textSecondary

/** Ubuntu orange accent (active states, primary actions). */
val ThemeAccent: Color
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.accentOrange

/** Aubergine brand tint for emphasis surfaces. */
val ThemeAubergine: Color
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.aubergine

/** True when the dark palette is active. */
val ThemeIsDark: Boolean
    @Composable @ReadOnlyComposable get() = LocalUbuntuColors.current.isDark

/** Text/icon color that stays legible on top of [ThemeAccent] fills. */
val ThemeOnAccent: Color
    @Composable @ReadOnlyComposable get() = Color.White
