package com.sanjana.mornote.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MorNoteLightColorScheme = lightColorScheme(
    primary = PrimaryAmber,
    onPrimary = OnPrimary,
    secondary = SecondaryAmber,
    onSecondary = OnPrimary,
    background = BackgroundBeige,
    surface = SurfaceWhite,
    onSurface = TextMain,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    onError = Color.White
)

private val MorNoteDarkColorScheme = darkColorScheme(
    primary = SecondaryAmber,
    onPrimary = Color.Black,
    secondary = PrimaryAmber,
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun MorNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MorNoteDarkColorScheme else MorNoteLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
