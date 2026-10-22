package com.example.ui.components

import com.example.ui.theme.*

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AbyssalCardGradient
import com.example.ui.theme.AbyssalSlateBase
import com.example.ui.theme.AbyssalSlateHighlight
import com.example.ui.theme.AbyssalSlateSurface
import com.example.ui.theme.AquaButtonTextStyle
import com.example.ui.theme.AquaCompactButtonTextStyle
import com.example.ui.theme.AquaFilterChipTextStyle
import com.example.ui.theme.AquaSecondaryButtonTextStyle
import com.example.ui.theme.BioCyanCore
import com.example.ui.theme.BioCyanGlow
import com.example.ui.theme.CylinderPressureBrush
import com.example.ui.theme.EmeraldSafeDark
import com.example.ui.theme.EmeraldSafeGlow
import com.example.ui.theme.EmeraldZoneBrush
import com.example.ui.theme.GlassBorderBrush
import com.example.ui.theme.HudCaption
import com.example.ui.theme.HudTagStyle
import com.example.ui.theme.HudTelemetryMedium
import com.example.ui.theme.HudTelemetrySmall
import com.example.ui.theme.NitroxWarningYellow
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SafetyStopCoral
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.theme.TacticalAmber
import java.util.Locale

// =========================================================================
// Canonical Scuba Button & Card Library (Strict Design System Standard)
// =========================================================================

/**
 * ScubaPrimaryButton:
 * Exact height = 52.dp, shape = RoundedCornerShape(16.dp),
 * background = Brush.horizontalGradient(listOf(OceanPrimary, AquaCyan)),
 * text = 15.sp bold white.
 */
@Composable
fun ScubaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val gradientBrush = if (enabled) {
        Brush.horizontalGradient(listOf(OceanPrimary, AquaCyan))
    } else {
        Brush.horizontalGradient(listOf(OceanPrimary.copy(alpha = 0.35f), AquaCyan.copy(alpha = 0.35f)))
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        shadowElevation = if (enabled) 4.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * ScubaSecondaryButton:
 * Exact height = 48.dp, shape = RoundedCornerShape(14.dp),
 * containerColor = surfaceContainerLow,
 * border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.4f)),
 * text = 14.sp bold.
 */
@Composable
fun ScubaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val borderColor = if (enabled) AquaCyan.copy(alpha = 0.4f) else AquaCyan.copy(alpha = 0.15f)
    val textColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) AquaCyan else AquaCyan.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * ScubaCompactButton:
 * Exact height = 36.dp, shape = RoundedCornerShape(10.dp),
 * text = 12.sp bold.
 */
@Composable
fun ScubaCompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isOutlined: Boolean = false
) {
    val shape = RoundedCornerShape(10.dp)
    val border = if (isOutlined) BorderStroke(1.dp, AquaCyan.copy(alpha = 0.5f)) else null
    val containerColor = if (isOutlined) MaterialTheme.colorScheme.surfaceContainerLow else OceanPrimary
    val textColor = if (isOutlined) AquaCyan else Color.White

    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 38.dp),
        shape = shape,
        color = containerColor,
        border = border
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * ScubaCard:
 * Exact shape = RoundedCornerShape(20.dp), colors = surfaceContainerLow,
 * border = 1.dp outlineVariant.copy(0.4f), contentPadding = 16.dp.
 */
@Composable
fun ScubaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, borderColor), shape),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, borderColor), shape),
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = content
            )
        }
    }
}

/**
 * Namespace object for ScubaButton.
 */
object ScubaButton {
    @Composable
    fun Primary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true
    ) = ScubaPrimaryButton(text, onClick, modifier, icon, enabled)

    @Composable
    fun Secondary(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        enabled: Boolean = true
    ) = ScubaSecondaryButton(text, onClick, modifier, icon, enabled)

    @Composable
    fun Compact(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        icon: ImageVector? = null,
        isOutlined: Boolean = false
    ) = ScubaCompactButton(text, onClick, modifier, icon, isOutlined)
}

// Aqua Design System Aliases for full backward compatibility
@Composable
fun AquaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) = ScubaPrimaryButton(text = text, onClick = onClick, modifier = modifier, enabled = enabled)

@Composable
fun AquaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) = ScubaSecondaryButton(text = text, onClick = onClick, modifier = modifier, enabled = enabled)

@Composable
fun AquaCompactButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    icon: (@Composable () -> Unit)? = null
) = ScubaCompactButton(text = text, onClick = onClick, modifier = modifier)

@Composable
fun AquaCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) = ScubaCard(modifier = modifier, content = content)

/**
 * Standard Metric Tile for AquaLearn Pro.
 * Shape: RoundedCornerShape(14.dp), Background: surfaceContainer,
 * Border: 1dp outlineVariant.copy(alpha=0.4f), Padding: 12.dp.
 */
@Composable
fun AquaMetricTile(
    label: String,
    value: String,
    unit: String = "",
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    subtext: String? = null,
    icon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(Locale.US),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (icon != null) {
                    icon()
                }
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    ),
                    color = accentColor
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            if (!subtext.isNullOrBlank()) {
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Standard Filter Chip for AquaLearn Pro.
 * Height: 36dp, Shape: RoundedCornerShape(12.dp), Animated color transitions,
 * Clean typography.
 */
@Composable
fun AquaFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    val animatedBg by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.20f) else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "chipBg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "chipBorder"
    )
    val animatedText by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "chipText"
    )

    Surface(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = animatedBg,
        border = BorderStroke(1.dp, animatedBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                style = AquaFilterChipTextStyle,
                color = animatedText
            )
        }
    }
}

// =========================================================================
// Tactical HUD & Abyssal Glass Helper Composables
// =========================================================================

/**
 * Frosted abyssal card standardized to canonical AquaCard geometry (20dp radius, 16dp padding).
 */
@Composable
fun AbyssalCard(
    modifier: Modifier = Modifier,
    borderBrush: Brush = GlassBorderBrush,
    backgroundBrush: Brush = AbyssalCardGradient,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, borderBrush), shape),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundBrush)
        ) {
            // Top-edge subtle luminous sheen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                BioCyanGlow.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = content
            )
        }
    }
}

/**
 * Tactile dive computer HUD pill badge with neon glow indicator.
 */
@Composable
fun TacticalPill(
    label: String,
    value: String? = null,
    glowColor: Color = BioCyanGlow,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = AbyssalSlateSurface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, glowColor.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                icon()
            } else {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(glowColor)
                )
            }
            Text(
                text = label.uppercase(Locale.US),
                style = HudTagStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
            )
            if (value != null) {
                Text(
                    text = value,
                    style = HudTelemetrySmall,
                    color = glowColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Compact HUD telemetry readout box standardized to canonical AquaMetricTile geometry.
 */
@Composable
fun HudTelemetryItem(
    label: String,
    value: String,
    unit: String = "",
    accentColor: Color = BioCyanGlow,
    modifier: Modifier = Modifier
) {
    AquaMetricTile(
        label = label,
        value = value,
        unit = unit,
        accentColor = accentColor,
        modifier = modifier
    )
}

// =========================================================================
// Main Diving Visual Components
// =========================================================================

/**
 * Boyle's Law Interactive Cylinder Pressure Simulation.
 * Redesigned into a sleek tactical pressure cylinder visual with smooth
 * animated water fill, compressed gas volume, and depth markers.
 */
@Composable
fun BoylesLawCalculator(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    var depthSlider by remember { mutableFloatStateOf(0f) }
    val depth = depthSlider.toInt()

    val animatedDepth by animateFloatAsState(
        targetValue = depthSlider,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "animatedDepth"
    )

    // Scientific calculations
    val pressureAta = (animatedDepth / 10.0f) + 1.0f
    val volumeFraction = 1.0f / pressureAta
    val densityFactor = pressureAta

    ScubaCard(
        modifier = modifier.testTag("boyles_law_calculator")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Depth & Pressure Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.boylesLawTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BioCyanGlow
                    )
                    Text(
                        text = "P₁ × V₁ = P₂ × V₂",
                        style = HudTagStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                TacticalPill(
                    label = "${depth}M",
                    value = "${String.format(Locale.US, "%.1f", pressureAta)} ATA",
                    glowColor = if (pressureAta >= 3.5f) SafetyStopCoral else BioCyanGlow
                )
            }

            Text(
                text = strings.boylesLawPrompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Tactile Depth Slider & Quick Presets
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Quick Depth Presets via ScubaCompactButton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0f to "0m", 10f to "10m", 20f to "20m", 30f to "30m", 40f to "40m").forEach { (d, label) ->
                        val isSelected = depthSlider == d
                        ScubaCompactButton(
                            text = label,
                            onClick = { depthSlider = d },
                            isOutlined = !isSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Slider(
                    value = depthSlider,
                    onValueChange = { depthSlider = it },
                    valueRange = 0f..40f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = BioCyanGlow,
                        activeTrackColor = BioCyanCore,
                        inactiveTrackColor = AbyssalSlateHighlight
                    ),
                    modifier = Modifier.testTag("depth_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0m (${strings.surfaceLabel})", style = HudCaption, color = BioCyanGlow)
                    Text("10m", style = HudCaption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("20m", style = HudCaption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("30m", style = HudCaption, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("40m (${strings.deepLabel})", style = HudCaption, color = SafetyStopCoral)
                }
            }

            // Interactive Pressure Chamber Visual & Telemetry
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AbyssalSlateBase.copy(alpha = 0.9f))
                    .border(BorderStroke(1.dp, GlassBorderBrush), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Cylinder Chamber Column
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AbyssalSlateSurface)
                        .border(BorderStroke(1.dp, BioCyanGlow.copy(alpha = 0.35f)), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Graduated depth markers & water fill canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val waterFraction = (animatedDepth / 40.0f).coerceIn(0f, 1f)
                        val waterHeight = h * waterFraction

                        // Depth tick lines on right edge
                        for (i in 0..4) {
                            val tickY = (h * i) / 4.0f
                            val isSurface = i == 0
                            drawLine(
                                color = if (isSurface) BioCyanGlow.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.25f),
                                start = Offset(w - 12f, tickY),
                                end = Offset(w - 2f, tickY),
                                strokeWidth = if (isSurface) 2f else 1.5f
                            )
                        }

                        // Hydrostatic water fill from bottom
                        if (waterHeight > 0f) {
                            drawRoundRect(
                                brush = CylinderPressureBrush,
                                topLeft = Offset(0f, h - waterHeight),
                                size = Size(w, waterHeight),
                                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                            )

                            // Water surface meniscus glow
                            drawLine(
                                color = BioCyanGlow,
                                start = Offset(0f, h - waterHeight),
                                end = Offset(w, h - waterHeight),
                                strokeWidth = 2.5f
                            )
                        }
                    }

                    // Flexible compressed gas volume cell
                    val maxGasSize = 70.dp
                    val currentGasSize = (maxGasSize.value * volumeFraction.coerceIn(0.25f, 1.0f)).dp

                    Box(
                        modifier = Modifier
                            .size(currentGasSize)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        BioCyanGlow,
                                        BioCyanCore,
                                        OceanPrimary.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .border(BorderStroke(2.dp, Color.White.copy(alpha = 0.9f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(volumeFraction * 100).toInt()}%",
                            style = HudTelemetrySmall,
                            color = AbyssalSlateBase,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Chamber label
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "${strings.airVolumeLabel}",
                            style = HudCaption,
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Telemetry Data Grid (Standardized with AquaMetricTile)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AquaMetricTile(
                        label = strings.ambientPressureLabel,
                        value = String.format(Locale.US, "%.1f", pressureAta),
                        unit = "ATA",
                        accentColor = BioCyanGlow,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AquaMetricTile(
                        label = strings.airDensityLabel,
                        value = String.format(Locale.US, "%.1f", densityFactor),
                        unit = "x SURFACE",
                        accentColor = NitroxWarningYellow,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AquaMetricTile(
                        label = strings.breatheRateLabel,
                        value = String.format(Locale.US, "%.1f", densityFactor),
                        unit = "x FASTER",
                        accentColor = if (pressureAta >= 3.0f) SafetyStopCoral else SeaGreen,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Tactical Critical Safety Warning Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, SafetyStopCoral.copy(alpha = 0.6f)), RoundedCornerShape(14.dp)),
                color = SafetyStopCoral.copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Warning",
                        tint = SafetyStopCoral,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = strings.neverHoldBreathWarning,
                        style = MaterialTheme.typography.bodySmall,
                        color = SafetyStopCoral,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Reset Action via Canonical ScubaSecondaryButton
            ScubaSecondaryButton(
                text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                    "Auf Oberfläche zurücksetzen (0m)"
                else
                    "Reset to Surface (0m)",
                onClick = { depthSlider = 0f },
                icon = Icons.Rounded.Speed,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("boyles_law_reset_button")
            )
        }
    }
}

/**
 * Tactical Dive Computer Ascent Profile Visualizer.
 * Redesigned with precise sonar depth grid, 5m safety stop emerald zone band,
 * smooth ascent curves, waypoint markers, and an ascent rate indicator.
 */
@Composable
fun AscentProfileVisual(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current

    AbyssalCard(
        modifier = modifier.testTag("ascent_profile_visual")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.safeAscentTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BioCyanGlow
                    )
                    Text(
                        text = "MAX ASCENT: 9m/MIN (30ft/MIN)",
                        style = HudTagStyle,
                        color = EmeraldSafeGlow
                    )
                }
                TacticalPill(
                    label = "SAFETY STOP",
                    value = "3 MIN @ 5M",
                    glowColor = EmeraldSafeGlow
                )
            }

            Text(
                text = strings.safeAscentDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Tactical Dive Computer Sonar Graph
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AbyssalSlateBase)
                    .border(BorderStroke(1.dp, GlassBorderBrush), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val ySurface = 18f
                    val y5m = h * 0.35f
                    val y10m = h * 0.58f
                    val y18m = h * 0.85f

                    // Subtle background depth grid lines
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                    // 10m grid
                    drawLine(
                        color = Color(0xFF1E3555).copy(alpha = 0.5f),
                        start = Offset(0f, y10m),
                        end = Offset(w, y10m),
                        strokeWidth = 1f,
                        pathEffect = dashEffect
                    )

                    // 5m SAFETY STOP EMERALD ZONE BAND (4m to 6m band)
                    val stopZoneTop = y5m - 12f
                    val stopZoneHeight = 24f
                    drawRect(
                        brush = EmeraldZoneBrush,
                        topLeft = Offset(0f, stopZoneTop),
                        size = Size(w, stopZoneHeight)
                    )

                    // 5m safety stop center dashed guide
                    drawLine(
                        color = EmeraldSafeGlow.copy(alpha = 0.75f),
                        start = Offset(0f, y5m),
                        end = Offset(w, y5m),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )

                    // Surface boundary line
                    drawLine(
                        color = BioCyanGlow.copy(alpha = 0.9f),
                        start = Offset(0f, ySurface),
                        end = Offset(w, ySurface),
                        strokeWidth = 2.5f
                    )

                    // Dive Profile Path (Bottom -> Controlled Ascent -> 3min Stop -> Surface)
                    val xBottomStart = w * 0.05f
                    val xAscentStart = w * 0.32f
                    val xStopStart = w * 0.52f
                    val xStopEnd = w * 0.78f
                    val xSurfaced = w * 0.95f

                    // Underwater fill under profile
                    val fillPath = Path().apply {
                        moveTo(xBottomStart, y18m)
                        lineTo(xAscentStart, y18m)
                        lineTo(xStopStart, y5m)
                        lineTo(xStopEnd, y5m)
                        lineTo(xSurfaced, ySurface)
                        lineTo(xSurfaced, h)
                        lineTo(xBottomStart, h)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            listOf(
                                BioCyanGlow.copy(alpha = 0.18f),
                                Color.Transparent
                            )
                        )
                    )

                    // Neon dive path
                    val curvePath = Path().apply {
                        moveTo(xBottomStart, y18m)
                        lineTo(xAscentStart, y18m)
                        lineTo(xStopStart, y5m)
                        lineTo(xStopEnd, y5m)
                        lineTo(xSurfaced, ySurface)
                    }

                    // Bloom outer stroke
                    drawPath(
                        path = curvePath,
                        color = BioCyanGlow.copy(alpha = 0.35f),
                        style = Stroke(width = 8f, cap = StrokeCap.Round)
                    )
                    // Crisp core neon stroke
                    drawPath(
                        path = curvePath,
                        color = BioCyanGlow,
                        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                    )

                    // Waypoint node: Bottom Time (18m)
                    drawCircle(color = BioCyanGlow.copy(alpha = 0.3f), radius = 8f, center = Offset(xAscentStart, y18m))
                    drawCircle(color = BioCyanGlow, radius = 4f, center = Offset(xAscentStart, y18m))

                    // Waypoint node: Safety Stop Start (5m)
                    drawCircle(color = EmeraldSafeGlow.copy(alpha = 0.35f), radius = 9f, center = Offset(xStopStart, y5m))
                    drawCircle(color = EmeraldSafeGlow, radius = 4.5f, center = Offset(xStopStart, y5m))

                    // Waypoint node: Safety Stop Complete (5m)
                    drawCircle(color = EmeraldSafeGlow.copy(alpha = 0.35f), radius = 9f, center = Offset(xStopEnd, y5m))
                    drawCircle(color = EmeraldSafeGlow, radius = 4.5f, center = Offset(xStopEnd, y5m))

                    // Surfaced node
                    drawCircle(color = Color.White, radius = 5f, center = Offset(xSurfaced, ySurface))
                }

                // Overlay labels on graph
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0m SURFACE", style = HudTagStyle, color = BioCyanGlow)
                    Text("5M SAFETY STOP ZONE", style = HudTagStyle, color = EmeraldSafeGlow)
                    Text("18M BOTTOM", style = HudTagStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Tactical Ascent Rate Indicator
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, AbyssalSlateHighlight), RoundedCornerShape(10.dp)),
                color = AbyssalSlateBase.copy(alpha = 0.7f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = "Ascent Rate",
                            tint = EmeraldSafeGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "ASCENT RATE: 9m/min (SAFE)",
                            style = HudCaption,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSafeGlow
                        )
                    }
                    Text(
                        text = "MAX: 18m/min NO-FLY",
                        style = HudCaption,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Legend markers row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.ascentBottomMarker,
                    style = HudCaption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = strings.ascentSafetyStopMarker,
                    style = HudCaption,
                    color = EmeraldSafeGlow,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.ascentSurfaceMarker,
                    style = HudCaption,
                    color = BioCyanGlow
                )
            }
        }
    }
}

/**
 * Exam Readiness Gauge.
 * Overhauled into a glowing neon circular arc with gradient sweep,
 * dual-stroke neon bloom, and bold tactical percentage display.
 */
@Composable
fun ExamReadinessGauge(
    readinessPercentage: Int,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val animatedPercent by animateFloatAsState(
        targetValue = readinessPercentage / 100f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "readiness"
    )

    val gaugeColor = when {
        readinessPercentage >= 80 -> EmeraldSafeGlow
        readinessPercentage >= 50 -> TacticalAmber
        else -> BioCyanGlow
    }

    ScubaCard(
        modifier = modifier.testTag("exam_readiness_gauge")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Neon Circular Arc Gauge
            Box(
                modifier = Modifier.size(92.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 9.dp.toPx()
                    val bloomStrokeW = 15.dp.toPx()

                    // Background track
                    drawArc(
                        color = Color(0xFF132238),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )

                    // Luminous Bloom Pass (glow halo)
                    drawArc(
                        color = gaugeColor.copy(alpha = 0.22f),
                        startAngle = 135f,
                        sweepAngle = 270f * animatedPercent,
                        useCenter = false,
                        style = Stroke(bloomStrokeW, cap = StrokeCap.Round)
                    )

                    // Crisp Neon Progress Arc
                    drawArc(
                        color = gaugeColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedPercent,
                        useCenter = false,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                }

                // Center Telemetry Readout
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$readinessPercentage%",
                        style = HudTelemetryMedium,
                        fontWeight = FontWeight.Black,
                        color = gaugeColor
                    )
                    Text(
                        text = strings.readyGaugeWord.uppercase(Locale.US),
                        style = HudTagStyle,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Status & Certification Progress
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = strings.examReadinessLabel.uppercase(Locale.US),
                    style = HudTagStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TacticalPill(
                        label = if (readinessPercentage >= 80) strings.examReadyBadge else strings.examInProgressBadge,
                        glowColor = gaugeColor,
                        icon = {
                            Icon(
                                imageVector = if (readinessPercentage >= 80) Icons.Rounded.CheckCircle else Icons.Rounded.ArrowUpward,
                                contentDescription = null,
                                tint = gaugeColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    )
                }

                Text(
                    text = if (readinessPercentage >= 80)
                        strings.examReadyDesc
                    else
                        strings.examInProgressDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                // Tactical progress bar
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { animatedPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = gaugeColor,
                    trackColor = AbyssalSlateHighlight
                )
            }
        }
    }
}

/**
 * BWRAF Pre-Dive Checklist Card.
 * Enhanced with tactile checklist pills, completed state glows,
 * and a celebration state when all 5 checks are satisfied.
 */
@Composable
fun BwrafChecklistCard(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val items = listOf(
        Triple("B", strings.bwrafBTitle, strings.bwrafBDesc),
        Triple("W", strings.bwrafWTitle, strings.bwrafWDesc),
        Triple("R", strings.bwrafRTitle, strings.bwrafRDesc),
        Triple("A", strings.bwrafATitle, strings.bwrafADesc),
        Triple("F", strings.bwrafFTitle, strings.bwrafFDesc)
    )

    val checkedStates = remember { mutableStateListOf(false, false, false, false, false) }
    val completedCount = checkedStates.count { it }
    val allCompleted = completedCount == 5

    ScubaCard(
        modifier = modifier.testTag("bwraf_checklist_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.bwrafTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BioCyanGlow
                    )
                    Text(
                        text = "BEGIN WITH REVIEW AND FRIEND",
                        style = HudTagStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                TacticalPill(
                    label = if (allCompleted) "READY TO DIVE" else "$completedCount/5 ${strings.bwrafDone}",
                    glowColor = if (allCompleted) EmeraldSafeGlow else BioCyanGlow
                )
            }

            Text(
                text = strings.bwrafPrompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Progress bar
            LinearProgressIndicator(
                progress = { completedCount / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (allCompleted) EmeraldSafeGlow else BioCyanGlow,
                trackColor = AbyssalSlateHighlight
            )

            // Tactile Checklist Pills
            items.forEachIndexed { index, (letter, title, desc) ->
                val isChecked = checkedStates[index]

                val bgCol by animateColorAsState(
                    targetValue = if (isChecked) EmeraldSafeGlow.copy(alpha = 0.14f) else AbyssalSlateSurface,
                    label = "bg"
                )

                val borderCol by animateColorAsState(
                    targetValue = if (isChecked) EmeraldSafeGlow.copy(alpha = 0.65f) else Color(0xFF1B2E4B).copy(alpha = 0.6f),
                    label = "border"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(bgCol)
                        .border(BorderStroke(1.dp, borderCol), RoundedCornerShape(14.dp))
                        .clickable { checkedStates[index] = !checkedStates[index] }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tactile letter circle
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isChecked) {
                                    Brush.radialGradient(listOf(EmeraldSafeGlow, EmeraldSafeDark))
                                } else {
                                    Brush.radialGradient(listOf(OceanPrimary, AbyssalSlateHighlight))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isChecked) EmeraldSafeGlow else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Check Icon with Completed Glow
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Checked",
                            tint = EmeraldSafeGlow,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)), CircleShape)
                        )
                    }
                }
            }

            // Canonical BWRAF Checklist Actions (ScubaSecondaryButton & ScubaPrimaryButton)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScubaSecondaryButton(
                    text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                        "Zurücksetzen"
                    else
                        "Reset",
                    onClick = {
                        for (i in checkedStates.indices) {
                            checkedStates[i] = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bwraf_reset_button")
                )
                ScubaPrimaryButton(
                    text = if (allCompleted) {
                        if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                            "Bereit zum Tauchen ✓"
                        else
                            "Ready to Dive ✓"
                    } else {
                        if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                            "Alle bestätigen"
                        else
                            "Confirm All"
                    },
                    onClick = {
                        for (i in checkedStates.indices) {
                            checkedStates[i] = true
                        }
                    },
                    icon = Icons.Rounded.CheckCircle,
                    modifier = Modifier
                        .weight(1.35f)
                        .testTag("bwraf_confirm_all_button")
                )
            }
        }
    }
}

// =========================================================================
// Advanced Diving Technical Telemetry & Gas Calculators
// =========================================================================

/**
 * Surface Air Consumption (SAC) & Respiratory Minute Volume (RMV) Calculator.
 * Real mathematical formula:
 *   PressureUsed = StartBar - EndBar
 *   GasUsedLiters = PressureUsed * TankVolume
 *   Atmospheres = (AvgDepth / 10.0) + 1.0
 *   SAC = GasUsedLiters / (Atmospheres * BottomTime) in L/min (or cuft/min if imperial).
 * Ratings:
 *   Excellent: < 13 L/min (EmeraldSafeGlow)
 *   Normal: 13-18 L/min (BioCyanGlow)
 *   Elevated: > 18 L/min (TacticalAmber / SafetyStopCoral)
 * Includes estimated air durations across depths: 10m, 20m, 30m, 40m.
 */
@Composable
fun SacRateCalculatorCard(
    modifier: Modifier = Modifier,
    isImperial: Boolean = false
) {
    val currentLang = LocalCurrentLanguage.current
    val isGerman = currentLang == AppLanguage.DE

    val tankOptions = remember(isGerman, isImperial) {
        listOf(
            Triple("10 L", 10.0, if (isGerman) "Stahl kompakt" else "Compact Steel"),
            Triple("12 L", 12.0, if (isGerman) "Standard Stahl" else "Standard Steel"),
            Triple("15 L", 15.0, if (isGerman) "Groß / Tiefe" else "Large Steel"),
            Triple(if (isImperial) "AL80 (80 cuft)" else "AL80 (11.1 L)", 11.1, if (isGerman) "80 cuft Alu" else "80 cuft Aluminum")
        )
    }

    var selectedTankIndex by remember { mutableIntStateOf(1) } // Default 12L
    val tankVolume = tankOptions[selectedTankIndex].second

    // Start / End Pressure
    var startPressureMetric by remember { mutableFloatStateOf(200f) } // bar
    var endPressureMetric by remember { mutableFloatStateOf(50f) } // bar

    // Depth & Time
    var avgDepthMetric by remember { mutableFloatStateOf(18f) } // 18m
    var bottomTimeMin by remember { mutableIntStateOf(45) } // 45 min

    // Normalize values
    val startBar = startPressureMetric.toDouble()
    val endBar = endPressureMetric.toDouble()
    val pressureUsedBar = (startBar - endBar).coerceAtLeast(0.0)
    val gasUsedLiters = pressureUsedBar * tankVolume
    val atmospheres = ((avgDepthMetric.toDouble() / 10.0) + 1.0).coerceAtLeast(1.0)
    val safeBottomTime = bottomTimeMin.coerceAtLeast(1)

    // Real mathematical SAC calculation
    val sacLitersPerMin = if (atmospheres > 0.0 && safeBottomTime > 0 && pressureUsedBar > 0.0) {
        gasUsedLiters / (atmospheres * safeBottomTime.toDouble())
    } else 0.0

    val sacCuftPerMin = sacLitersPerMin / 28.3168466

    // Color-coded rating
    val (ratingColor, ratingTitle, ratingDesc) = when {
        sacLitersPerMin <= 0.0 -> Triple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            if (isGerman) "DATEN EINGEBEN" else "ENTER VALUES",
            if (isGerman) "Startdruck muss höher als Enddruck sein." else "Start pressure must exceed end pressure."
        )
        sacLitersPerMin < 13.0 -> Triple(
            EmeraldSafeGlow,
            if (isGerman) "EXZELLENT (< 13 L/MIN)" else "EXCELLENT (< 13 L/MIN)",
            if (isGerman) "Hervorragende Atemeffizienz! Sehr entspannte Atmung, optimaler Trimm." else "Superior air efficiency! Calm breathing, ideal buoyancy & trim."
        )
        sacLitersPerMin <= 18.0 -> Triple(
            BioCyanGlow,
            if (isGerman) "NORMAL (13 - 18 L/MIN)" else "NORMAL (13 - 18 L/MIN)",
            if (isGerman) "Typischer Freizeit-Atemminutenverbrauch. Gute Basis für Gasplanung." else "Standard recreational diver consumption rate. Solid planning baseline."
        )
        sacLitersPerMin <= 23.0 -> Triple(
            TacticalAmber,
            if (isGerman) "ERHÖHT (18 - 23 L/MIN)" else "ELEVATED (18 - 23 L/MIN)",
            if (isGerman) "Erhöhter Gasverbrauch. Prüfe Strömung, Kälte, Anstrengung oder Überbleiung." else "Elevated consumption. Check for exertion, cold, current, or excess weight."
        )
        else -> Triple(
            SafetyStopCoral,
            if (isGerman) "SEHR HOCH (> 23 L/MIN)" else "HIGH (> 23 L/MIN)",
            if (isGerman) "Schneller Gasverbrauch! Starke Anstrengung, Stress oder Geräteleckage." else "High depletion! Heavy exertion, high anxiety, cold, or equipment leak."
        )
    }

    // Usable gas for air duration projection (Start bar - 50 bar reserve)
    val usablePressureBar = (startBar - 50.0).coerceAtLeast(0.0)
    val usableLiters = usablePressureBar * tankVolume

    val depthDurations = listOf(10.0, 20.0, 30.0, 40.0).map { depthM ->
        val depthAtm = (depthM / 10.0) + 1.0
        val duration = if (sacLitersPerMin > 0.0) {
            (usableLiters / (sacLitersPerMin * depthAtm)).toInt().coerceAtLeast(0)
        } else 0
        Pair(depthM, duration)
    }

    ScubaCard(
        modifier = modifier.testTag("sac_rate_calculator_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Air,
                            contentDescription = null,
                            tint = BioCyanGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isGerman) "SAC- & RMV-RECHNER" else "SAC & RMV CALCULATOR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BioCyanGlow
                        )
                    }
                    Text(
                        text = if (isGerman) "Atemminutenvolumen & Gasreichweite" else "Surface Air Consumption & Gas Duration",
                        style = HudTagStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                TacticalPill(
                    label = if (isImperial) "IMPERIAL" else "METRISCH",
                    value = if (isImperial) "CUFT/MIN" else "L/MIN",
                    glowColor = BioCyanGlow
                )
            }

            // Tank Size Selector Buttons (Standardized with ScubaCompactButton)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isGerman) "FLASCHENGRÖSSE (VOLUMEN)" else "CYLINDER SIZE (VOLUME)",
                    style = HudTagStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tankOptions.forEachIndexed { index, (label, _, _) ->
                        val isSelected = selectedTankIndex == index
                        ScubaCompactButton(
                            text = label,
                            onClick = { selectedTankIndex = index },
                            isOutlined = !isSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Sliders: Pressures, Depth & Bottom Time
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // 1. Start Pressure
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isGerman) "Startdruck (Flaschendruck voll)" else "Start Pressure (Full Tank)",
                            style = HudCaption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isImperial) "${(startPressureMetric * 14.5038).toInt()} PSI" else "${startPressureMetric.toInt()} bar",
                            style = HudTelemetrySmall,
                            color = BioCyanGlow
                        )
                    }
                    Slider(
                        value = startPressureMetric,
                        onValueChange = { startPressureMetric = it },
                        valueRange = 150f..300f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = BioCyanGlow,
                            activeTrackColor = OceanPrimary,
                            inactiveTrackColor = AbyssalSlateHighlight
                        )
                    )
                }

                // 2. End Pressure
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isGerman) "Enddruck (Reserve / Aufstieg)" else "End Pressure (Reserve / Surfaced)",
                            style = HudCaption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isImperial) "${(endPressureMetric * 14.5038).toInt()} PSI" else "${endPressureMetric.toInt()} bar",
                            style = HudTelemetrySmall,
                            color = if (endPressureMetric < 50f) SafetyStopCoral else TacticalAmber
                        )
                    }
                    Slider(
                        value = endPressureMetric,
                        onValueChange = { endPressureMetric = it },
                        valueRange = 30f..100f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = if (endPressureMetric < 50f) SafetyStopCoral else TacticalAmber,
                            activeTrackColor = TacticalAmber,
                            inactiveTrackColor = AbyssalSlateHighlight
                        )
                    )
                }

                // 3. Average Depth
                Column {
                    val displayDepth = if (isImperial) (avgDepthMetric * 3.28084).toInt() else avgDepthMetric.toInt()
                    val depthUnit = if (isImperial) "ft" else "m"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isGerman) "Durchschnittliche Tauchtiefe" else "Average Dive Depth",
                            style = HudCaption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$displayDepth $depthUnit (${String.format(Locale.US, "%.1f", atmospheres)} ATA)",
                            style = HudTelemetrySmall,
                            color = AquaCyan
                        )
                    }
                    Slider(
                        value = avgDepthMetric,
                        onValueChange = { avgDepthMetric = it },
                        valueRange = 3f..40f,
                        steps = 36,
                        colors = SliderDefaults.colors(
                            thumbColor = AquaCyan,
                            activeTrackColor = AquaCyan,
                            inactiveTrackColor = AbyssalSlateHighlight
                        )
                    )
                }

                // 4. Bottom Time
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isGerman) "Grundzeit (Tauchgangsdauer)" else "Bottom Time (Duration)",
                            style = HudCaption,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$bottomTimeMin min",
                            style = HudTelemetrySmall,
                            color = EmeraldSafeGlow
                        )
                    }
                    Slider(
                        value = bottomTimeMin.toFloat(),
                        onValueChange = { bottomTimeMin = it.toInt() },
                        valueRange = 10f..90f,
                        steps = 15,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldSafeGlow,
                            activeTrackColor = EmeraldSafeGlow,
                            inactiveTrackColor = AbyssalSlateHighlight
                        )
                    )
                }
            }

            // Real-Time Calculated SAC & RMV Rating Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AbyssalSlateBase)
                    .border(BorderStroke(1.5.dp, ratingColor.copy(alpha = 0.7f)), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isGerman) "BERECHNETER SAC-WERT (RMV)" else "CALCULATED SAC RATE (RMV)",
                                style = HudTagStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (isImperial) String.format(Locale.US, "%.2f", sacCuftPerMin) else String.format(Locale.US, "%.1f", sacLitersPerMin),
                                    style = HudTelemetryMedium,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ratingColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isImperial) "cuft/min @ 1 ATA" else "L/min @ 1 ATA",
                                    style = HudCaption,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        TacticalPill(
                            label = ratingTitle,
                            glowColor = ratingColor
                        )
                    }

                    Text(
                        text = ratingDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )

                    // Secondary telemetry row (Standardized with AquaMetricTile)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AquaMetricTile(
                            label = if (isGerman) "Gas verbraucht" else "Gas Used",
                            value = "${gasUsedLiters.toInt()}",
                            unit = "L (${pressureUsedBar.toInt()} bar)",
                            accentColor = ratingColor,
                            modifier = Modifier.weight(1f)
                        )
                        AquaMetricTile(
                            label = if (isGerman) "Umgebungsdruck" else "Ambient Pressure",
                            value = String.format(Locale.US, "%.2f", atmospheres),
                            unit = "ATA",
                            accentColor = AquaCyan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Projected Air Duration Across Depths
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGerman) "PROJIZIERTE GASREICHWEITE (BIS 50 BAR RESERVE)" else "ESTIMATED AIR DURATION AT DEPTH",
                        style = HudTagStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isImperial) "${(usableLiters / 28.3168).toInt()} cuft usable" else "${usableLiters.toInt()} L nutzbar",
                        style = HudCaption,
                        fontSize = 9.sp,
                        color = BioCyanGlow
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    depthDurations.forEach { (depthM, duration) ->
                        val depthLabel = if (isImperial) "${(depthM * 3.28084).toInt()} ft" else "${depthM.toInt()}m"
                        val ata = (depthM / 10.0) + 1.0

                        AquaMetricTile(
                            label = depthLabel,
                            value = if (sacLitersPerMin > 0.0) "$duration" else "--",
                            unit = "min",
                            accentColor = if (duration >= 30) EmeraldSafeGlow else if (duration >= 15) SandGold else SafetyStopCoral,
                            subtext = "${String.format(Locale.US, "%.0f", ata)} ATA",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Canonical SAC Calculator Actions (ScubaSecondaryButton & ScubaPrimaryButton)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScubaSecondaryButton(
                    text = if (isGerman) "Standardwerte" else "Defaults",
                    onClick = {
                        startPressureMetric = 200f
                        endPressureMetric = 50f
                        avgDepthMetric = 18f
                        bottomTimeMin = 45
                        selectedTankIndex = 1
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sac_reset_defaults_button")
                )
                ScubaPrimaryButton(
                    text = if (isGerman) "SAC übernehmen" else "Apply SAC Rate",
                    onClick = { },
                    icon = Icons.Rounded.Air,
                    modifier = Modifier
                        .weight(1.35f)
                        .testTag("sac_save_rate_button")
                )
            }
        }
    }
}

/**
 * Dalton's Law: Best Nitrox Mix Calculator Card.
 * Real mathematical formula:
 *   Atmospheres = (Depth / 10.0) + 1.0
 *   Best O2% = (TargetPO2 / Atmospheres) * 100, clamped between 21% and 40%.
 *   MOD (1.4 bar) = ((1.4 / (BestO2% / 100)) - 1.0) * 10.0
 *   Contingency Depth (1.6 bar) = ((1.6 / (BestO2% / 100)) - 1.0) * 10.0
 *   Equivalent Air Depth (EAD) = ((1 - FO2) * (Depth + 10) / 0.79) - 10
 */
@Composable
fun DaltonsLawBestMixCard(
    modifier: Modifier = Modifier,
    isImperial: Boolean = false
) {
    val currentLang = LocalCurrentLanguage.current
    val isGerman = currentLang == AppLanguage.DE

    var plannedDepthMetric by remember { mutableFloatStateOf(28f) } // 28m default
    var targetPO2 by remember { mutableDoubleStateOf(1.4) } // 1.4 default (recreational safe)

    val depthMeters = plannedDepthMetric.toDouble()
    val atmospheres = ((depthMeters / 10.0) + 1.0).coerceAtLeast(1.0)

    val rawO2Percent = if (atmospheres > 0.0) (targetPO2 / atmospheres) * 100.0 else 21.0
    val bestO2PercentClamped = rawO2Percent.coerceIn(21.0, 40.0)
    val bestO2Int = bestO2PercentClamped.toInt()

    // Derived MOD for this blend at PO2 = 1.4
    val mod14Meters = if (bestO2PercentClamped > 0.0) {
        ((1.4 / (bestO2PercentClamped / 100.0)) - 1.0) * 10.0
    } else 0.0

    // Contingency MOD for this blend at PO2 = 1.6
    val mod16Meters = if (bestO2PercentClamped > 0.0) {
        ((1.6 / (bestO2PercentClamped / 100.0)) - 1.0) * 10.0
    } else 0.0

    // Actual PO2 at planned depth with this mix
    val actualPO2 = atmospheres * (bestO2PercentClamped / 100.0)

    // EAD (Equivalent Air Depth)
    val fO2 = bestO2PercentClamped / 100.0
    val fN2 = 1.0 - fO2
    val eadMeters = (((fN2 * (depthMeters + 10.0)) / 0.79) - 10.0).coerceAtLeast(0.0)

    ScubaCard(
        modifier = modifier.testTag("daltons_law_best_mix_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Calculate,
                            contentDescription = null,
                            tint = AquaCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isGerman) "DALTON: BESTE NITROX-MISCHUNG" else "DALTON'S LAW: BEST NITROX MIX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AquaCyan
                        )
                    }
                    Text(
                        text = "PO₂ = FO₂ × P_amb  •  FO₂ = PO₂ / P_amb",
                        style = HudTagStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                TacticalPill(
                    label = "MAX PO₂",
                    value = "${String.format(Locale.US, "%.1f", targetPO2)} BAR",
                    glowColor = if (targetPO2 > 1.4) SandGold else SeaGreen
                )
            }

            // Target Depth Controls
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val displayDepth = if (isImperial) (plannedDepthMetric * 3.28084).toInt() else plannedDepthMetric.toInt()
                val depthUnit = if (isImperial) "ft" else "m"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isGerman) "GEPLANTE ZIELTIEFE" else "PLANNED TARGET DEPTH",
                        style = HudTagStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$displayDepth $depthUnit (${String.format(Locale.US, "%.1f", atmospheres)} ATA)",
                        style = HudTelemetrySmall,
                        color = BioCyanGlow
                    )
                }

                // Quick Depth Presets (Canonical ScubaCompactButton)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(18f, 24f, 30f, 36f, 40f).forEach { presetM ->
                        val isSelected = (plannedDepthMetric - presetM).let { it > -1f && it < 1f }
                        val label = if (isImperial) "${(presetM * 3.28084).toInt()}ft" else "${presetM.toInt()}m"
                        ScubaCompactButton(
                            text = label,
                            onClick = { plannedDepthMetric = presetM },
                            isOutlined = !isSelected,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Slider(
                    value = plannedDepthMetric,
                    onValueChange = { plannedDepthMetric = it },
                    valueRange = 10f..50f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = AquaCyan,
                        activeTrackColor = AquaCyan,
                        inactiveTrackColor = AbyssalSlateHighlight
                    )
                )
            }

            // Target PO2 Mode Presets (Canonical ScubaCompactButton)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isGerman) "ZIEL-SAUERSTOFFPARTIALDRUCK (PO₂)" else "TARGET OXYGEN PARTIAL PRESSURE (PO₂)",
                    style = HudTagStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScubaCompactButton(
                        text = if (isGerman) "1.4 bar (Sport)" else "1.4 bar (Working Safe)",
                        onClick = { targetPO2 = 1.4 },
                        isOutlined = targetPO2 != 1.4,
                        modifier = Modifier.weight(1f)
                    )
                    ScubaCompactButton(
                        text = if (isGerman) "1.6 bar (Deko-Limit)" else "1.6 bar (Deco Limit)",
                        onClick = { targetPO2 = 1.6 },
                        isOutlined = targetPO2 != 1.6,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Calculated Best Mix Main Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AbyssalSlateBase)
                    .border(BorderStroke(1.5.dp, GlassBorderBrush), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isGerman) "BERECHNETE BESTE MISCHUNG" else "CALCULATED BEST MIX",
                                style = HudTagStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = if (bestO2Int == 21) "AIR (21%)" else "EAN$bestO2Int",
                                    style = HudTelemetryMedium,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BioCyanGlow
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${String.format(Locale.US, "%.1f", bestO2PercentClamped)}% O₂)",
                                    style = HudTelemetrySmall,
                                    color = AquaCyan,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        TacticalPill(
                            label = "PO₂ @ TIEFE",
                            value = "${String.format(Locale.US, "%.2f", actualPO2)} BAR",
                            glowColor = if (actualPO2 <= 1.40) EmeraldSafeGlow else if (actualPO2 <= 1.60) SandGold else SafetyStopCoral
                        )
                    }

                    // Status notes for clamping
                    val statusText = when {
                        rawO2Percent > 40.0 -> if (isGerman)
                            "Auf 40% O₂ begrenzt (Freizeittauch-Maximum nach Standards)."
                        else
                            "Clamped to 40% O₂ (standard recreational Nitrox limit)."
                        rawO2Percent < 21.0 -> if (isGerman)
                            "Tiefe erfordert unter 21% O₂! Sporttauchen mit Pressluft/Nitrox nicht zulässig."
                        else
                            "Depth requires < 21% O₂! Exceeds recreational Nitrox limits."
                        else -> if (isGerman)
                            "Optimale Mischung für maximale Nullzeit ohne Überschreiten der PO₂-Grenze."
                        else
                            "Optimal recreational Nitrox blend for maximum bottom time."
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            }

            // Telemetry Grid: MOD, Contingency, EAD (Standardized with AquaMetricTile)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // MOD at 1.4
                    val mod14Label = if (isImperial) "${(mod14Meters * 3.28084).toInt()} ft" else "${String.format(Locale.US, "%.1f", mod14Meters)} m"
                    AquaMetricTile(
                        label = if (isGerman) "MOD (1.4 bar)" else "MOD (1.4 bar)",
                        value = mod14Label,
                        unit = "SAFE",
                        accentColor = SeaGreen,
                        modifier = Modifier.weight(1f)
                    )

                    // Contingency at 1.6
                    val mod16Label = if (isImperial) "${(mod16Meters * 3.28084).toInt()} ft" else "${String.format(Locale.US, "%.1f", mod16Meters)} m"
                    AquaMetricTile(
                        label = if (isGerman) "Deko-Limit (1.6)" else "Contingency (1.6)",
                        value = mod16Label,
                        unit = "MAX",
                        accentColor = SandGold,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Equivalent Air Depth (EAD)
                    val eadLabel = if (isImperial) "${(eadMeters * 3.28084).toInt()} ft" else "${String.format(Locale.US, "%.1f", eadMeters)} m"
                    AquaMetricTile(
                        label = if (isGerman) "Äquivalente Lufttiefe (EAD)" else "Equiv. Air Depth (EAD)",
                        value = eadLabel,
                        unit = if (isGerman) "N₂-Entlastung" else "N₂ Advantage",
                        accentColor = AquaCyan,
                        modifier = Modifier.weight(1f)
                    )

                    // Nitrogen Fraction
                    AquaMetricTile(
                        label = if (isGerman) "Stickstoffanteil (FN₂)" else "Nitrogen (FN₂)",
                        value = String.format(Locale.US, "%.1f", fN2 * 100.0),
                        unit = "% N₂",
                        accentColor = BioCyanGlow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Canonical Best Mix Actions (ScubaSecondaryButton & ScubaPrimaryButton)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScubaSecondaryButton(
                    text = if (isGerman) "Standard (28m)" else "Default (28m)",
                    onClick = {
                        plannedDepthMetric = 28f
                        targetPO2 = 1.4
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dalton_reset_defaults_button")
                )
                ScubaPrimaryButton(
                    text = if (isGerman) "EAN$bestO2Int übernehmen" else "Apply EAN$bestO2Int",
                    onClick = { },
                    icon = Icons.Rounded.CheckCircle,
                    modifier = Modifier
                        .weight(1.35f)
                        .testTag("dalton_apply_mix_button")
                )
            }
        }
    }
}

