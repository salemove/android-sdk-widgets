package com.glia.exampleapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Glia brand colors
private val GliaPrimary = Color(0xFF6B4EFF)
private val GliaSecondary = Color(0xFF4DD0E1)
private val GliaBackground = Color(0xFFF5F5F5)
private val GliaSurface = Color(0xFFFFFFFF)
private val GliaError = Color(0xFFFF1744)

private val LightColorScheme = lightColorScheme(
    primary = GliaPrimary,
    secondary = GliaSecondary,
    background = GliaBackground,
    surface = GliaSurface,
    error = GliaError,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = GliaPrimary,
    secondary = GliaSecondary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    error = GliaError,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

@Composable
fun GliaExampleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
