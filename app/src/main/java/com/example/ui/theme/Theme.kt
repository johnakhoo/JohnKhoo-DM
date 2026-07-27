package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = EvPrimaryCyan,
    onPrimary = Color.Black,
    primaryContainer = EvSurfaceVariant,
    onPrimaryContainer = EvPrimaryCyan,
    secondary = EvSecondaryLime,
    onSecondary = Color.Black,
    tertiary = EvPurpleHyper,
    background = EvBackground,
    onBackground = EvTextPrimary,
    surface = EvSurface,
    onSurface = EvTextPrimary,
    surfaceVariant = EvSurfaceVariant,
    onSurfaceVariant = EvTextSecondary,
    outline = EvSurfaceBorder,
    error = EvRedDanger
)

@Composable
fun PulseDriveEVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

