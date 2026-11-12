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

private val DarkColorScheme = darkColorScheme(
    primary = SevaGreenDark,
    onPrimary = Color.Black,
    primaryContainer = SevaGreenSecondary,
    onPrimaryContainer = Color.White,
    secondary = SevaGreenSecondaryDark,
    tertiary = SevaGoldAccent,
    background = SevaBackgroundDark,
    surface = SevaSurfaceDark,
    surfaceVariant = SevaSurfaceVariantDark,
    onBackground = Color.White,
    onSurface = Color.White,
    error = SevaBloodRed
)

private val LightColorScheme = lightColorScheme(
    primary = SevaGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = SevaGreenContainer,
    onPrimaryContainer = SevaOnGreenContainer,
    secondary = SevaGreenSecondary,
    onSecondary = Color.White,
    tertiary = SevaGoldAccent,
    onTertiary = Color.White,
    background = SevaBackgroundLight,
    surface = SevaSurfaceLight,
    surfaceVariant = SevaSurfaceVariantLight,
    onBackground = SevaTextPrimary,
    onSurface = SevaTextPrimary,
    outline = SevaOutlineLight,
    error = SevaBloodRed
)

@Composable
fun SevaOrgTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        typography = Typography,
        content = content
    )
}
