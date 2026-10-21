package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val ScubaShapes = Shapes(
    small = RoundedCornerShape(10.dp),     // Canonical ScubaCompactButton
    medium = RoundedCornerShape(14.dp),    // Canonical ScubaSecondaryButton & MetricTile
    large = RoundedCornerShape(16.dp),     // Canonical ScubaPrimaryButton
    extraLarge = RoundedCornerShape(20.dp) // Canonical ScubaCard
)

private val DarkColorScheme = darkColorScheme(
    primary = AquaBioluminescentCyan, // High-Tech Electric Cyan (#00F2FE)
    onPrimary = Color(0xFF002733),
    primaryContainer = AquaElectricAzure, // High-Tech Electric Azure (#0284C7)
    onPrimaryContainer = Color(0xFFE0F2FE),
    secondary = EmeraldSafeGlow,
    onSecondary = Color(0xFF002A19),
    secondaryContainer = Color(0xFF0B4636),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = TacticalAmber,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = AquaSurfaceBase,       // Base (#070D18)
    onBackground = Color(0xFFE2E8F0),
    surface = AquaSurfaceLow,           // SurfaceLow (#0F172A)
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = AquaSurfaceDefault, // SurfaceDefault (#142036)
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = AquaOutline,              // Outline (#334155)
    outlineVariant = AquaOutlineVariant, // OutlineVariant (#1E2D44)
    surfaceContainerLowest = AquaSurfaceBase,       // #070D18
    surfaceContainerLow = AquaSurfaceLow,           // #0F172A
    surfaceContainer = AquaSurfaceDefault,         // #142036
    surfaceContainerHigh = AquaSurfaceHigh,         // #1C2B46
    surfaceContainerHighest = AquaSurfaceHighest,   // #25395A
    error = SafetyStopCoral,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF5A1212),
    onErrorContainer = Color(0xFFFFDAD6)
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
    surfaceContainerHighest = OceanSurfaceContainerHighestLight,
    error = CoralAlert,
    onError = Color(0xFFFFFFFF)
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
        shapes = ScubaShapes,
        content = content
    )
}
