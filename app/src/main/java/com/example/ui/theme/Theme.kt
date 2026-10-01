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
    primary = AquaCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004F5D),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = OceanSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = OceanSurfaceVariantDark,
    onSecondaryContainer = Color(0xFFCAF0F8),
    tertiary = OceanTertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF5C2000),
    onTertiaryContainer = Color(0xFFFFDBCF),
    background = OceanBackgroundDark,
    onBackground = Color(0xFFE1EDF2),
    surface = OceanSurfaceDark,
    onSurface = Color(0xFFE1EDF2),
    surfaceVariant = OceanSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB5D1DE),
    outline = OceanOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimaryContainer,
    onPrimaryContainer = OceanOnPrimaryContainer,
    secondary = OceanSecondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = OceanSecondaryContainer,
    onSecondaryContainer = OceanOnSecondaryContainer,
    tertiary = OceanTertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = OceanTertiaryContainer,
    onTertiaryContainer = OceanOnTertiaryContainer,
    background = OceanBackgroundLight,
    onBackground = Color(0xFF102A36),
    surface = OceanSurfaceLight,
    onSurface = Color(0xFF102A36),
    surfaceVariant = OceanSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF3E5A69),
    outline = OceanOutlineLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep oceanic brand consistency
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
        typography = Typography,
        content = content
    )
}
