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

val ModernPurple = Color(0xFF7F52FF)
val AbsoluteBlack = Color(0xFF000000)
val White = Color(0xFFFFFFFF) // Add White color here for consistency

// Create one color scheme that will be used for both light and dark system themes.
private val BaseColorScheme = darkColorScheme(
    primary = ModernPurple, // Default primary for the app
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
    // This parameter will override the primary color if provided.
    // If null, it uses the default `BaseColorScheme.primary` (ModernPurple).
    dynamicPrimaryColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicPrimaryColor != null) {
        // Create a new color scheme for the player components
        BaseColorScheme.copy(
            primary = dynamicPrimaryColor
        )
    } else {
        // Use the base color scheme for the rest of the app
        BaseColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}