package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.OceanTertiary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen

@Composable
fun BoylesLawCalculator(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    var depthSlider by remember { mutableFloatStateOf(0f) }
    val depth = depthSlider.toInt()
    val pressureAta = (depth / 10.0) + 1.0
    val volumeFraction = 1.0 / pressureAta
    val densityFactor = pressureAta

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("boyles_law_calculator"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.boylesLawTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${depth}m / ${(depth * 3.28).toInt()}ft",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = strings.boylesLawPrompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Slider(
                value = depthSlider,
                onValueChange = { depthSlider = it },
                valueRange = 0f..40f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = AquaCyan,
                    activeTrackColor = OceanPrimary
                ),
                modifier = Modifier.testTag("depth_slider")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0m (${strings.surfaceLabel})", style = MaterialTheme.typography.labelSmall)
                Text("10m", style = MaterialTheme.typography.labelSmall)
                Text("20m", style = MaterialTheme.typography.labelSmall)
                Text("30m", style = MaterialTheme.typography.labelSmall)
                Text("40m (${strings.deepLabel})", style = MaterialTheme.typography.labelSmall)
            }

            // Visual Simulation of Gas Volume & Density
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Balloon representation
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val balloonSize = (72 * volumeFraction).coerceIn(24.0, 72.0).dp
                    Box(
                        modifier = Modifier
                            .size(72.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(balloonSize)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(AquaCyan, OceanPrimary)
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${strings.airVolumeLabel}: ${(volumeFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Stats breakdown
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${strings.ambientPressureLabel}: ${String.format("%.1f", pressureAta)} ATA",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${strings.airDensityLabel}: ${String.format("%.1f", densityFactor)}x surface",
                        style = MaterialTheme.typography.bodySmall,
                        color = OceanSecondary
                    )
                    Text(
                        text = "${strings.breatheRateLabel}: ${String.format("%.1f", densityFactor)}x faster",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (pressureAta >= 3.0) CoralAlert else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                color = CoralAlert.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Warning",
                        tint = CoralAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.neverHoldBreathWarning,
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralAlert,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bwraf_checklist_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.bwrafTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${checkedStates.count { it }}/5 ${strings.bwrafDone}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (checkedStates.all { it }) SeaGreen else MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = strings.bwrafPrompt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            items.forEachIndexed { index, (letter, title, desc) ->
                val isChecked = checkedStates[index]
                val bgCol by animateColorAsState(
                    targetValue = if (isChecked) SeaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    label = "bg"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgCol)
                        .clickable { checkedStates[index] = !checkedStates[index] }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = if (isChecked) SeaGreen else MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = letter,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Check",
                        tint = if (isChecked) SeaGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AscentProfileVisual(modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ascent_profile_visual"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = strings.safeAscentTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = strings.safeAscentDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF071E2E))
            ) {
                val w = size.width
                val h = size.height

                // Water surface line
                drawLine(
                    color = AquaCyan.copy(alpha = 0.8f),
                    start = Offset(0f, 15f),
                    end = Offset(w, 15f),
                    strokeWidth = 3f
                )

                // 5m safety stop line
                val y5m = h * 0.35f
                drawLine(
                    color = SandGold.copy(alpha = 0.7f),
                    start = Offset(0f, y5m),
                    end = Offset(w, y5m),
                    strokeWidth = 2f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // Dive Profile Path (Bottom -> Slow Ascent -> 3min Stop -> Surface)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.05f, h * 0.85f) // Bottom (18m)
                    lineTo(w * 0.35f, h * 0.85f) // Bottom bottom time
                    lineTo(w * 0.55f, y5m)       // Ascent <= 9m/min
                    lineTo(w * 0.80f, y5m)       // 3 min safety stop hover
                    lineTo(w * 0.95f, 15f)       // Final ascent to surface
                }

                drawPath(
                    path = path,
                    color = AquaCyan,
                    style = Stroke(width = 5f, cap = StrokeCap.Round)
                )

                // Safety stop circle marker
                drawCircle(
                    color = SandGold,
                    radius = 8f,
                    center = Offset(w * 0.675f, y5m)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(strings.ascentBottomMarker, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(strings.ascentSafetyStopMarker, style = MaterialTheme.typography.labelSmall, color = SandGold, fontWeight = FontWeight.Bold)
                Text(strings.ascentSurfaceMarker, style = MaterialTheme.typography.labelSmall, color = AquaCyan)
            }
        }
    }
}

@Composable
fun ExamReadinessGauge(
    readinessPercentage: Int,
    modifier: Modifier = Modifier
) {
    val strings = com.example.ui.i18n.LocalAppStrings.current
    val animatedPercent by animateFloatAsState(
        targetValue = readinessPercentage / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "readiness"
    )

    val gaugeColor = when {
        readinessPercentage >= 80 -> SeaGreen
        readinessPercentage >= 50 -> SandGold
        else -> OceanSecondary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_readiness_gauge"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 10.dp.toPx()
                    // Track
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                    // Progress
                    drawArc(
                        color = gaugeColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedPercent,
                        useCenter = false,
                        style = Stroke(strokeW, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$readinessPercentage%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.readyGaugeWord,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.examReadinessLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (readinessPercentage >= 80) strings.examReadyBadge else strings.examInProgressBadge,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (readinessPercentage >= 80) SeaGreen else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (readinessPercentage >= 80)
                        strings.examReadyDesc
                    else
                        strings.examInProgressDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
