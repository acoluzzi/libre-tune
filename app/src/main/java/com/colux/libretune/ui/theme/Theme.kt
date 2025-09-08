package com.colux.libretune.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Green = Color(0xff1DB954)
val AbsoluteBlack = Color(0xFF000000)
val White = Color(0xFFFFFFFF) // Add White color here for consistency

// Create one color scheme that will be used for both light and dark system themes.
private val BaseColorScheme = darkColorScheme(
    primary = Green, // Default primary for the app
    background = AbsoluteBlack,
    surface = AbsoluteBlack,
    surfaceVariant = Color(0xFF1E1E1E), // A slightly lighter black for cards/bars
    onPrimary = White,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = White,
    // Add other colors as needed to match your design
)

@Composable
fun LibreTuneTheme(
    content: @Composable () -> Unit
) {

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = BaseColorScheme,
        typography = Typography,
        content = content
    )
}