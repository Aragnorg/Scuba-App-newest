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

// Softer, modern Dark Color Scheme (Midnight Slate - pleasant on the eyes, no harsh neon or pitch black)
private val DarkColorScheme = darkColorScheme(
    primary = AquaBioluminescentCyan,        // Soft Electric Sky (#38BDF8)
    onPrimary = Color(0xFF082F49),          // Deep Sky Text (#082F49)
    primaryContainer = Color(0xFF0369A1),   // Sky 700 (#0369A1)
    onPrimaryContainer = Color(0xFFE0F2FE), // Soft Sky Tint (#E0F2FE)
    secondary = Color(0xFF2DD4BF),          // Soft Marine Teal (#2DD4BF)
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFFCCFBF1),
    tertiary = Color(0xFFFB923C),           // Soft Warm Coral (#FB923C)
    onTertiary = Color(0xFF431407),
    tertiaryContainer = Color(0xFF9A3412),
    onTertiaryContainer = Color(0xFFFFEDD5),
    background = AquaSurfaceBase,           // Soothing Midnight Slate (#0B1120)
    onBackground = Color(0xFFE2E8F0),       // Soft off-white Slate 200
    surface = AquaSurfaceLow,               // Elevated Slate (#111C2E)
    onSurface = Color(0xFFF1F5F9),          // Clean Slate 100
    surfaceVariant = AquaSurfaceDefault,    // Card Surface (#18263E)
    onSurfaceVariant = Color(0xFF94A3B8),   // Muted Slate 400
    outline = AquaOutline,                  // Subtle Hairline (#2C3E5E)
    outlineVariant = AquaOutlineVariant,    // Container Outline (#1D2C44)
    surfaceContainerLowest = AquaSurfaceBase,       // #0B1120
    surfaceContainerLow = AquaSurfaceLow,           // #111C2E
    surfaceContainer = AquaSurfaceDefault,         // #18263E
    surfaceContainerHigh = AquaSurfaceHigh,         // #22324E
    surfaceContainerHighest = AquaSurfaceHighest,   // #2C3E5E
    error = Color(0xFFF87171),             // Soft Coral Red (#F87171)
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2)
)

// Clean, modern Light Color Scheme (Slate Minimalism - no stark black on white)
private val LightColorScheme = lightColorScheme(
    primary = OceanPrimary,                 // Ocean Azure (#0284C7)
    onPrimary = OceanOnPrimary,             // #FFFFFF
    primaryContainer = OceanPrimaryContainer, // #E0F2FE
    onPrimaryContainer = OceanOnPrimaryContainer, // #0369A1
    secondary = OceanSecondary,             // Marine Teal (#0F766E)
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = OceanSecondaryContainer,
    onSecondaryContainer = OceanOnSecondaryContainer,
    tertiary = OceanTertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = OceanTertiaryContainer,
    onTertiaryContainer = OceanOnTertiaryContainer,
    background = OceanBackgroundLight,      // Soft Slate 50 (#F8FAFC)
    onBackground = Color(0xFF1E293B),       // Gentle Slate 800 (#1E293B - softer than pure black)
    surface = OceanSurfaceLight,            // Pure White Card (#FFFFFF)
    onSurface = Color(0xFF1E293B),          // Gentle Slate 800
    surfaceVariant = OceanSurfaceVariantLight, // Slate 100 (#F1F5F9)
    onSurfaceVariant = Color(0xFF64748B),   // Slate 500 (#64748B)
    outline = OceanOutlineLight,            // Hairline Slate 300 (#CBD5E1)
    outlineVariant = OceanOutlineVariantLight, // Hairline Slate 200 (#E2E8F0)
    surfaceContainerLowest = OceanSurfaceContainerLowestLight,
    surfaceContainerLow = OceanSurfaceContainerLowLight,
    surfaceContainer = OceanSurfaceContainerLight,
    surfaceContainerHigh = OceanSurfaceContainerHighLight,
    surfaceContainerHighest = OceanSurfaceContainerHighestLight,
    error = Color(0xFFDC2626),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve brand identity
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
