package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Oceanic Material 3 Color Tokens
// =========================================================================

// Primary Oceanic Accents
val OceanPrimary = Color(0xFF0284C7) // Sky / Marine Azure (600)
val OceanOnPrimary = Color(0xFFFFFFFF)
val OceanPrimaryContainer = Color(0xFFE0F2FE) // Azure Tint (100)
val OceanOnPrimaryContainer = Color(0xFF0369A1) // Deep Azure (700)

// Secondary Marine Teal Accents
val OceanSecondary = Color(0xFF0F766E) // Marine Teal (700)
val OceanOnSecondary = Color(0xFFFFFFFF)
val OceanSecondaryContainer = Color(0xFFCCFBF1) // Teal Soft Tint
val OceanOnSecondaryContainer = Color(0xFF115E59)

// Tertiary Safety Coral / Sunburst Accents
val OceanTertiary = Color(0xFFEA580C) // Scuba Orange / Coral (600)
val OceanOnTertiary = Color(0xFFFFFFFF)
val OceanTertiaryContainer = Color(0xFFFFEDD5)
val OceanOnTertiaryContainer = Color(0xFF9A3412)

// Light Theme Surface Hierarchy (Clean Maritime Slate)
val OceanBackgroundLight = Color(0xFFF8FAFC) // Slate 50
val OceanSurfaceLight = Color(0xFFFFFFFF)
val OceanSurfaceVariantLight = Color(0xFFF1F5F9) // Slate 100
val OceanOutlineLight = Color(0xFF94A3B8) // Slate 400
val OceanOutlineVariantLight = Color(0xFFE2E8F0) // Slate 200

val OceanSurfaceContainerLowestLight = Color(0xFFFFFFFF)
val OceanSurfaceContainerLowLight = Color(0xFFF8FAFC)
val OceanSurfaceContainerLight = Color(0xFFF1F5F9)
val OceanSurfaceContainerHighLight = Color(0xFFE2E8F0)
val OceanSurfaceContainerHighestLight = Color(0xFFCBD5E1)

// =========================================================================
// Canonical Aqua Dark Theme Surface Hierarchy & Primaries
// =========================================================================
val AquaSurfaceBase = Color(0xFF070D18)       // Base (#070D18)
val AquaSurfaceLow = Color(0xFF0F172A)        // SurfaceLow (#0F172A)
val AquaSurfaceDefault = Color(0xFF142036)    // SurfaceDefault (#142036)
val AquaSurfaceHigh = Color(0xFF1C2B46)       // SurfaceHigh (#1C2B46)
val AquaSurfaceHighest = Color(0xFF25395A)    // Elevated hover / overlay
val AquaOutline = Color(0xFF334155)           // Outline (#334155)
val AquaOutlineVariant = Color(0xFF1E2D44)    // OutlineVariant subtle border

// High-Tech Electric Azure / Cyan Primaries
val AquaElectricAzure = Color(0xFF0284C7)     // High-Tech Electric Azure (#0284C7)
val AquaBioluminescentCyan = Color(0xFF00F2FE) // Bioluminescent Cyan Glow (#00F2FE)

// Dark Theme Surface Hierarchy (Deep Abyssal Ocean)
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

// Special Specialty & Status Highlights
val AquaCyan = Color(0xFF06B6D4) // Refined Turquoise Cyan
val CoralAlert = Color(0xFFEF4444) // Clean Modern Red
val DepthNavy = AquaSurfaceLow // Rich Midnight Navy
val SandGold = Color(0xFFF59E0B) // Warm Amber Gold
val SeaGreen = Color(0xFF10B981) // Fresh Emerald Green
val DeepAbyss = Color(0xFF020617)

// =========================================================================
// Abyssal Glass & Tactical Dive HUD Tokens
// =========================================================================

// Deep Abyssal Slate Hierarchy Aliases (Harmonized)
val AbyssalSlateBase = AquaSurfaceBase         // Deepest abyss floor (#070D18)
val AbyssalSlateSurface = AquaSurfaceLow       // Underwater HUD surface (#0F172A)
val AbyssalSlateElevated = AquaSurfaceDefault  // Card container (#142036)
val AbyssalSlateHighlight = AquaSurfaceHigh    // Border / hover elevation (#1C2B46)
val AbyssalSlateMuted = AquaOutline            // Outline border (#334155)

// Bioluminescent Cyan Glow
val BioCyanGlow = AquaBioluminescentCyan       // High-intensity HUD cyan neon (#00F2FE)
val BioCyanCore = Color(0xFF38BDF8)            // Electric cyan center
val BioCyanMuted = AquaElectricAzure           // High-tech electric azure (#0284C7)
val BioCyanSubtle = Color(0x1A00F2FE)          // 10% luminous tint

// Emerald Safe Glow
val EmeraldSafeGlow = Color(0xFF10B981)       // Tactical safe neon
val EmeraldSafeDark = Color(0xFF059669)       // Deep emerald gradient base
val EmeraldSafeSubtle = Color(0x2610B981)     // 15% safe zone highlight

// Tactical Status Accents
val TacticalAmber = Color(0xFFF59E0B)         // Warning / safety stop highlight
val TacticalAmberDark = Color(0xFFD97706)
val NitroxWarningYellow = Color(0xFFEAB308)   // Nitrox / caution marker
val SafetyStopCoral = Color(0xFFFF5252)       // Critical safety / deco alert
val SafetyStopCoralDark = Color(0xFFDC2626)

// HUD Structural & Glass Colors
val HudNeonBlue = Color(0xFF38BDF8)
val HudGridCyan = Color(0x2600F2FE)
val GlassBorderCyan = Color(0x4000F2FE)       // 25% frosted border
val GlassBorderMuted = Color(0x1FFFFFFF)      // 12% white frost border

// =========================================================================
// Reusable HUD Gradients & Glass Brushes
// =========================================================================

// Subtle vertical gradient for Abyssal HUD cards
val AbyssalCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0E1A2E),
        Color(0xFF060D19)
    )
)

// Subtle radial gradient for luminous card depth
val AbyssalRadialGradient = Brush.radialGradient(
    colors = listOf(
        Color(0xFF132742),
        Color(0xFF060C17)
    )
)

// Frosted glass border brush with subtle cyan luminescence
val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF00F2FE).copy(alpha = 0.38f),
        Color(0xFF38BDF8).copy(alpha = 0.16f),
        Color(0xFF050B14).copy(alpha = 0.55f)
    )
)

// Tactical safety stop zone gradient (4m - 6m band)
val EmeraldZoneBrush = Brush.verticalGradient(
    colors = listOf(
        EmeraldSafeGlow.copy(alpha = 0.28f),
        EmeraldSafeDark.copy(alpha = 0.08f)
    )
)

// Bioluminescent neon sweep gradient for gauges
val BioSweepGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0xFF0052D4),
        Color(0xFF00F2FE),
        Color(0xFF10B981),
        Color(0xFF00F2FE)
    )
)

// Sleek high-pressure cylinder fluid gradient
val CylinderPressureBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF00F2FE).copy(alpha = 0.90f),
        Color(0xFF0284C7).copy(alpha = 0.92f),
        Color(0xFF034694).copy(alpha = 0.98f)
    )
)
