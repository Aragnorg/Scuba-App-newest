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
    primary = BioluminescentSky,
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF03445C),
    onPrimaryContainer = Color(0xFFBAE6FD),
    secondary = BioluminescentEmerald,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = BioluminescentCoral,
    onTertiary = Color(0xFF4C0018),
    tertiaryContainer = Color(0xFF700027),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = OceanBackgroundDark,
    onBackground = Color(0xFFF1F5F9),
    surface = OceanSurfaceDark,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = OceanSurfaceVariantDark,
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = OceanOutlineDark,
    outlineVariant = OceanOutlineVariantDark,
    surfaceContainerLowest = OceanSurfaceContainerLowestDark,
    surfaceContainerLow = OceanSurfaceContainerLowDark,
    surfaceContainer = OceanSurfaceContainerDark,
    surfaceContainerHigh = OceanSurfaceContainerHighDark,
    surfaceContainerHighest = OceanSurfaceContainerHighestDark
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
    onBackground = Color(0xFF0F172A),
    surface = OceanSurfaceLight,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = OceanSurfaceVariantLight,
    onSurfaceVariant = Color(0xFF475569),
    outline = OceanOutlineLight,
    outlineVariant = OceanOutlineVariantLight,
    surfaceContainerLowest = OceanSurfaceContainerLowestLight,
    surfaceContainerLow = OceanSurfaceContainerLowLight,
    surfaceContainer = OceanSurfaceContainerLight,
    surfaceContainerHigh = OceanSurfaceContainerHighLight,
    surfaceContainerHighest = OceanSurfaceContainerHighestLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve oceanic brand identity
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
