package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Oceanic Material 3 Color Tokens - Modern Soft Contrast Palette
// =========================================================================

// Primary Oceanic Accents (Softer, modern marine azure - not harsh)
val OceanPrimary = Color(0xFF0284C7)         // Sky Azure (600)
val OceanOnPrimary = Color(0xFFFFFFFF)
val OceanPrimaryContainer = Color(0xFFE0F2FE) // Soft Azure Tint (100)
val OceanOnPrimaryContainer = Color(0xFF0369A1) // Deep Azure (700)

// Secondary Marine Teal Accents
val OceanSecondary = Color(0xFF0F766E)       // Refined Teal (700)
val OceanOnSecondary = Color(0xFFFFFFFF)
val OceanSecondaryContainer = Color(0xFFCCFBF1) // Soft Teal Tint
val OceanOnSecondaryContainer = Color(0xFF115E59)

// Tertiary Warm Accents (Soft Safety Amber/Coral)
val OceanTertiary = Color(0xFFEA580C)        // Scuba Coral (600)
val OceanOnTertiary = Color(0xFFFFFFFF)
val OceanTertiaryContainer = Color(0xFFFFEDD5)
val OceanOnTertiaryContainer = Color(0xFF9A3412)

// Light Theme Surface Hierarchy (Clean, modern slate minimalism - no harsh pure blacks)
val OceanBackgroundLight = Color(0xFFF8FAFC) // Slate 50
val OceanSurfaceLight = Color(0xFFFFFFFF)    // Pure White Card
val OceanSurfaceVariantLight = Color(0xFFF1F5F9) // Slate 100
val OceanOutlineLight = Color(0xFFCBD5E1)    // Slate 300
val OceanOutlineVariantLight = Color(0xFFE2E8F0) // Slate 200 hairline

val OceanSurfaceContainerLowestLight = Color(0xFFFFFFFF)
val OceanSurfaceContainerLowLight = Color(0xFFF8FAFC)
val OceanSurfaceContainerLight = Color(0xFFF1F5F9)
val OceanSurfaceContainerHighLight = Color(0xFFE2E8F0)
val OceanSurfaceContainerHighestLight = Color(0xFFCBD5E1)

// =========================================================================
// Modern Dark Theme Surface Hierarchy (Midnight Slate Navy - Soothing, No Jarring Pitch Black)
// =========================================================================
val AquaSurfaceBase = Color(0xFF0B1120)       // Midnight Slate Base (#0B1120)
val AquaSurfaceLow = Color(0xFF111C2E)        // Elevated Slate (#111C2E)
val AquaSurfaceDefault = Color(0xFF18263E)    // Card Surface (#18263E)
val AquaSurfaceHigh = Color(0xFF22324E)       // Elevated Container (#22324E)
val AquaSurfaceHighest = Color(0xFF2C3E5E)    // Interactive / Hover Surface (#2C3E5E)
val AquaOutline = Color(0xFF2C3E5E)           // Outline (#2C3E5E)
val AquaOutlineVariant = Color(0xFF1D2C44)    // Subtle Container Outline (#1D2C44)

// Refined Non-Harsh Primaries (Sky 400 & Sky 500 - crisp and modern without neon blinding)
val AquaElectricAzure = Color(0xFF0284C7)     // Ocean Azure (#0284C7)
val AquaBioluminescentCyan = Color(0xFF38BDF8) // Soft Electric Sky (#38BDF8 - soothing glow)
val AquaCyan = Color(0xFF0EA5E9)              // Canonical Refined Cyan (#0EA5E9)

// Dark Theme Surface Hierarchy Aliases
val OceanBackgroundDark = AquaSurfaceBase
val OceanSurfaceDark = AquaSurfaceLow
val OceanSurfaceVariantDark = AquaSurfaceDefault
val OceanOutlineDark = AquaOutline
val OceanOutlineVariantDark = AquaOutlineVariant

val OceanSurfaceContainerLowestDark = AquaSurfaceBase
val OceanSurfaceContainerLowDark = AquaSurfaceLow
val OceanSurfaceContainerDark = AquaSurfaceDefault
val OceanSurfaceContainerHighDark = AquaSurfaceHigh
val OceanSurfaceContainerHighestDark = AquaSurfaceHighest

// Status Accents (Balanced, modern saturation)
val CoralAlert = Color(0xFFEF4444)            // Soft Crimson (#EF4444)
val DepthNavy = AquaSurfaceLow                // Midnight Navy (#111C2E)
val SandGold = Color(0xFFF59E0B)              // Warm Gold Amber (#F59E0B)
val SeaGreen = Color(0xFF10B981)              // Fresh Emerald (#10B981)
val DeepAbyss = Color(0xFF0B1120)

// =========================================================================
// HUD & Glass Tokens (Harmonized, Softer Alpha to Eliminate Harsh Contrast)
// =========================================================================
val AbyssalSlateBase = AquaSurfaceBase
val AbyssalSlateSurface = AquaSurfaceLow
val AbyssalSlateElevated = AquaSurfaceDefault
val AbyssalSlateHighlight = AquaSurfaceHigh
val AbyssalSlateMuted = AquaOutline

// Softer Luminescence
val BioCyanGlow = AquaBioluminescentCyan
val BioCyanCore = Color(0xFF38BDF8)
val BioCyanMuted = AquaElectricAzure
val BioCyanSubtle = Color(0x1F38BDF8)         // 12% subtle sky tint

// Emerald Safe Glow
val EmeraldSafeGlow = Color(0xFF10B981)
val EmeraldSafeDark = Color(0xFF059669)
val EmeraldSafeSubtle = Color(0x1F10B981)

// Tactical Status Accents
val TacticalAmber = Color(0xFFF59E0B)
val TacticalAmberDark = Color(0xFFD97706)
val NitroxWarningYellow = Color(0xFFEAB308)
val SafetyStopCoral = Color(0xFFF87171)
val SafetyStopCoralDark = Color(0xFFDC2626)

// HUD Structural & Glass Colors (Low-contrast, elegant)
val HudNeonBlue = Color(0xFF38BDF8)
val HudGridCyan = Color(0x1A38BDF8)
val GlassBorderCyan = Color(0x2638BDF8)       // Softer 15% glass border
val GlassBorderMuted = Color(0x12FFFFFF)      // Soft 7% frost border

// =========================================================================
// Reusable Modern Gradients
// =========================================================================
val AbyssalCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF18263E),
        Color(0xFF111C2E)
    )
)

val AbyssalRadialGradient = Brush.radialGradient(
    colors = listOf(
        Color(0xFF1E2B44),
        Color(0xFF0E1626)
    )
)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF38BDF8).copy(alpha = 0.25f),
        Color(0xFF0284C7).copy(alpha = 0.12f),
        Color(0xFF111C2E).copy(alpha = 0.35f)
    )
)

val EmeraldZoneBrush = Brush.verticalGradient(
    colors = listOf(
        EmeraldSafeGlow.copy(alpha = 0.20f),
        EmeraldSafeDark.copy(alpha = 0.05f)
    )
)

val BioSweepGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0xFF0284C7),
        Color(0xFF38BDF8),
        Color(0xFF10B981),
        Color(0xFF38BDF8)
    )
)

val CylinderPressureBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF38BDF8).copy(alpha = 0.85f),
        Color(0xFF0284C7).copy(alpha = 0.90f),
        Color(0xFF0369A1).copy(alpha = 0.95f)
    )
)
