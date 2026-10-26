package com.example.aivisionscanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HudColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = HudBackground,
    secondary = HudGreen,
    background = HudBackground,
    onBackground = HudText,
    surface = HudPanel,
    onSurface = HudText,
    error = HudRed
)

@Composable
fun AIVisionScannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HudColorScheme,
        content = content
    )
}
