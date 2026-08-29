package com.example.ayahflow.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AyahLightColors = lightColorScheme(
    primary          = Color(0xFF164E3D),
    onPrimary        = Color.White,
    secondary        = Color(0xFF2F8066),
    onSecondary      = Color.White,
    background       = Color(0xFFF7F5EE),
    onBackground     = Color(0xFF202936),
    surface          = Color(0xFFFFFDF8),
    onSurface        = Color(0xFF202936),
    surfaceVariant   = Color(0xFFF0EDE5),
    onSurfaceVariant = Color(0xFF687078)
)

private val AyahDarkColors = darkColorScheme(
    primary          = Color(0xFF4D9E80),
    onPrimary        = Color(0xFF002117),
    secondary        = Color(0xFF2F8066),
    onSecondary      = Color.White,
    background       = Color(0xFF0E2921),
    onBackground     = Color(0xFFFFFDF8),
    surface          = Color(0xFF12382D),
    onSurface        = Color(0xFFFFFDF8),
    surfaceVariant   = Color(0xFF163D30),
    onSurfaceVariant = Color(0xFFAFC4BB)
)

@Composable
fun AyahFlowTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) AyahDarkColors else AyahLightColors,
        typography  = Typography,
        content     = content
    )
}
