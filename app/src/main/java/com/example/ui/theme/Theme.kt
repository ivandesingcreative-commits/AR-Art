package com.example.ui.theme

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

private val StudioDarkColorScheme = darkColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = TerracottaDark,
    secondary = ClayAmber,
    onSecondary = StudioDarkBg,
    tertiary = ArNeonCyan,
    background = StudioDarkBg,
    onBackground = TextPrimaryDark,
    surface = StudioDarkSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = StudioDarkCard,
    onSurfaceVariant = TextSecondaryDark
)

private val StudioLightColorScheme = lightColorScheme(
    primary = TerracottaPrimary,
    onPrimary = Color.White,
    primaryContainer = ClayAmber,
    secondary = ClaySage,
    onSecondary = Color.Black,
    tertiary = ArNeonCyan,
    background = LightBg,
    onBackground = Color(0xFF2D3139),
    surface = LightSurface,
    onSurface = Color(0xFF2D3139),
    surfaceVariant = LightCard,
    onSurfaceVariant = Color(0xFF626875)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default dark studio theme for lightbox / studio contrast
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StudioDarkColorScheme else StudioLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

