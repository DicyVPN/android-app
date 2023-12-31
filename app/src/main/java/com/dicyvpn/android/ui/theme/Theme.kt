package com.dicyvpn.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val colorScheme = darkColorScheme(
    primary = Blue300,
    onPrimary = Color.White,
    secondary = Gray200,
    secondaryContainer = Gray500,
    onSecondaryContainer = Color.White,
    tertiary = Gray900,
    background = Gray500,
    surface = Gray700,
    onSurface = Color.White, // Also text inside text fields
    surfaceVariant = Gray800, // Also text field background
    onSurfaceVariant = Gray200, // Also text fields labels
    error = Red300
)

@Composable
fun DicyVPNTheme(
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }

        else -> colorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Gray600.toArgb()
            window.navigationBarColor = Gray800.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
        shapes = Shapes
    )
}