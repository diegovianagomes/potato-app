package com.diegoviana.potato.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = GhibliBlue,
    onPrimary = GhibliCream,
    primaryContainer = GhibliDeepBlue,
    secondary = GhibliGreen,
    onSecondary = GhibliCream,
    secondaryContainer = GhibliMoss,
    tertiary = GhibliPink,
    background = GhibliShadow,
    surface = GhibliCream.copy(alpha = 0.9f),
    onSurface = GhibliShadow,
    surfaceVariant = GhibliCream.copy(alpha = 0.7f),
    onSurfaceVariant = GhibliMoss
)

private val LightColorScheme = lightColorScheme(
    primary = GhibliBlue,
    onPrimary = GhibliCream,
    primaryContainer = GhibliDeepBlue.copy(alpha = 0.8f),
    secondary = GhibliGreen,
    onSecondary = GhibliShadow,
    secondaryContainer = GhibliMoss.copy(alpha = 0.5f),
    tertiary = GhibliTerracotta,
    background = GhibliBackground,
    surface = GhibliYellow.copy(alpha = 0.3f),
    onSurface = GhibliShadow,
    surfaceVariant = GhibliYellow.copy(alpha = 0.5f),
    onSurfaceVariant = GhibliShadow
)

@Composable
fun PotatoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}