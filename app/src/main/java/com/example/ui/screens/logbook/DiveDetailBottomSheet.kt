package com.example.ui.screens.logbook

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiveLogEntity
import com.example.ui.components.SignaturePreview
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen

/**
 * Formats a dive log into a clean text summary and launches the Android share sheet.
 */
fun shareDiveLog(context: Context, dive: DiveLogEntity, isImperial: Boolean = false) {
    val scaleToFt = 3.28084f
    val depthStr = if (isImperial) "${kotlin.math.round(dive.maxDepthMeters * scaleToFt * 10f) / 10f} ft" else "${dive.maxDepthMeters} m"
    val avgDepthStr = if (isImperial) "${kotlin.math.round(dive.avgDepthMeters * scaleToFt * 10f) / 10f} ft" else "${dive.avgDepthMeters} m"
    val waterTempStr = if (isImperial) "${kotlin.math.round((dive.waterTempCelsius * 1.8f + 32f) * 10f) / 10f}°F" else "${dive.waterTempCelsius}°C"
    val approvalText = when (dive.approvalStatus) {
        "APPROVED_BY_INSTRUCTOR" -> "Verified by Instructor (${dive.instructorName.ifBlank { "Instructor" }})"
        "APPROVED_BY_BUDDY_SIGNATURE" -> "Verified by Buddy Signature (${dive.approvedBy.ifBlank { dive.buddyName }})"
        "APPROVED_BY_BUDDY_PUSH" -> "Verified by Buddy Push (${dive.approvedBy.ifBlank { dive.buddyName }})"
        else -> "Pending Verification"
    }

    val summary = """
        🤿 AquaLearn Pro - Dive Log #${dive.diveNumber}
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        📍 Site: ${dive.siteName}
        🗺️ Location: ${dive.location}
        📅 Date: ${dive.date} (${dive.timeIn} - ${dive.timeOut})
        🌊 Water: ${dive.bodyOfWater} (${dive.entryType})
        
        📊 Telemetry:
        • Max Depth: $depthStr (Avg: $avgDepthStr)
        • Bottom Time: ${dive.durationMinutes} min
        • Water Temp: $waterTempStr
        • Gas: ${dive.gasType} (${dive.tankType})
        • Tank Pressure: ${dive.pressureInBar} bar → ${dive.pressureOutBar} bar (Used: ${(dive.pressureInBar - dive.pressureOutBar).coerceAtLeast(0)} bar)
        • Safety Stop: ${if (dive.safetyStopCompleted) "Completed (${dive.safetyStopMinutes}m @ ${dive.safetyStopDepthMeters}m)" else "Not logged"}
        
        👥 Team & Verification:
        • Buddy: ${dive.buddyName.ifBlank { "None recorded" }}
        • Status: $approvalText
        ${if (dive.sightings.isNotBlank()) "🐠 Sightings: ${dive.sightings}\n" else ""}${if (dive.notes.isNotBlank()) "📝 Notes: \"${dive.notes}\"\n" else ""}
        Logged with AquaLearn Pro Dive Computer Cockpit
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, summary)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share Dive #${dive.diveNumber} Log")
    context.startActivity(shareIntent)
}

@Composable
fun DiveDetailDialog(
    dive: DiveLogEntity,
    isImperial: Boolean = false,
    onApproveWithQrClick: () -> Unit,
    onApproveWithBuddyClick: () -> Unit,
    onSimulatePushClick: () -> Unit,
    onDeleteClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val tankLiters = remember(dive.tankType) {
        val t = dive.tankType.lowercase()
        when {
            t.contains("15") -> 15.0f
            t.contains("10") -> 10.0f
            t.contains("80") || t.contains("11") -> 11.1f
            t.contains("7") -> 7.0f
            else -> 12.0f
        }
    }

    val sacRateData = remember(dive, tankLiters) {
        if (dive.pressureInBar > dive.pressureOutBar && dive.durationMinutes > 0 && dive.avgDepthMeters > 0) {
            val deltaBar = (dive.pressureInBar - dive.pressureOutBar).toFloat()
            val ambientAta = 1.0f + (dive.avgDepthMeters / 10.0f)
            val sacBarPerMin = deltaBar / (ambientAta * dive.durationMinutes.toFloat())
            val sacLitersPerMin = sacBarPerMin * tankLiters
            val sacCuFtPerMin = sacLitersPerMin / 28.3168f
            Triple(sacBarPerMin, sacLitersPerMin, sacCuFtPerMin)
        } else {
            null
        }
    }

    val scaleToFt = 3.28084f
    val scaleToF = 1.8f
    val scaleToPsi = 14.5038f
    val scaleToLbs = 2.20462f

    val maxDepthStr = if (isImperial) "${kotlin.math.round(dive.maxDepthMeters * scaleToFt * 10f) / 10f} ft" else "${dive.maxDepthMeters} m"
    val avgDepthStr = if (isImperial) "${kotlin.math.round(dive.avgDepthMeters * scaleToFt * 10f) / 10f} ft" else "${dive.avgDepthMeters} m"
    val visibilityStr = if (isImperial) "${kotlin.math.round(dive.visibilityMeters * scaleToFt * 10f) / 10f} ft" else "${dive.visibilityMeters} m"

    val waterTempStr = if (isImperial) "${kotlin.math.round((dive.waterTempCelsius * scaleToF + 32f) * 10f) / 10f}°F" else "${dive.waterTempCelsius}°C"
    val airTempStr = if (isImperial) "${kotlin.math.round((dive.airTempCelsius * scaleToF + 32f) * 10f) / 10f}°F" else "${dive.airTempCelsius}°C"

    val pressureInStr = if (isImperial) "${kotlin.math.round(dive.pressureInBar * scaleToPsi).toInt()} PSI" else "${dive.pressureInBar} bar"
    val pressureOutStr = if (isImperial) "${kotlin.math.round(dive.pressureOutBar * scaleToPsi).toInt()} PSI" else "${dive.pressureOutBar} bar"
    val pressureUsedStr = if (isImperial) "${kotlin.math.round((dive.pressureInBar - dive.pressureOutBar).coerceAtLeast(0) * scaleToPsi).toInt()} PSI" else "${(dive.pressureInBar - dive.pressureOutBar).coerceAtLeast(0)} bar"

    val weightStr = if (isImperial) "${kotlin.math.round(dive.weightKg * scaleToLbs * 10f) / 10f} lbs" else "${dive.weightKg} kg"
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("dive_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "${strings.diveNumberPrefix}${dive.diveNumber}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dive.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dive.siteName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dive.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dive_details_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Stamp Banner
                when (dive.approvalStatus) {
                    "APPROVED_BY_INSTRUCTOR" -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SeaGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("approved_instructor_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SeaGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = null,
                                        tint = SeaGreen,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.officialCenterStampBadge,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SeaGreen
                                    )
                                    Text(
                                        text = "${strings.verifiedByLabel}: ${dive.approvedBy.ifBlank { "Adrian Scheid" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (dive.instructorLicense.isNotBlank()) {
                                        Text(
                                            text = "${strings.licenseLabel}: ${dive.instructorLicense}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (dive.qrStampCode.isNotBlank()) {
                                        Text(
                                            text = "${strings.qrHashLabel}: ${dive.qrStampCode.take(24)}...",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "APPROVED_BY_BUDDY_SIGNATURE" -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = AquaCyan.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("approved_buddy_sig_banner")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(AquaCyan.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Draw,
                                            contentDescription = null,
                                            tint = AquaCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = strings.verifiedByBuddySignatureTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = AquaCyan
                                        )
                                        Text(
                                            text = "${strings.signedByLabel} ${dive.approvedBy}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                if (dive.buddySignaturePathJson.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        SignaturePreview(
                                            signatureJson = dive.buddySignaturePathJson,
                                            height = 55.dp
                                        )
                                    }
                                }

                                if (dive.buddyApprovalNote.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "\"${dive.buddyApprovalNote}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    "APPROVED_BY_BUDDY_PUSH" -> {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SeaGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("approved_buddy_push_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SeaGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        tint = SeaGreen,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.verifiedByBuddyPushTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SeaGreen
                                    )
                                    Text(
                                        text = "${strings.approvedByLabel}: ${dive.approvedBy.ifBlank { dive.buddyName }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (dive.buddyApprovalNote.isNotBlank()) {
                                        Text(
                                            text = "\"${dive.buddyApprovalNote}\"",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // PENDING APPROVAL - SHOW ACTION OPTIONS
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pending_approval_banner")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.diveLogPendingApprovalTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.chooseApprovalMethodPrompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Scan Instructor / Center QR Code
                                    Button(
                                        onClick = onApproveWithQrClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("action_approve_qr")
                                    ) {
                                        Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.scanCenterQrButton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // 2. Buddy Approval (Signature or Push message)
                                    Button(
                                        onClick = onApproveWithBuddyClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("action_approve_buddy")
                                    ) {
                                        Icon(Icons.Rounded.People, contentDescription = null, tint = Color(0xFF00382E), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.buddyApproveButton, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00382E))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick test button for push simulation
                                Text(
                                    text = strings.demoTriggerPushPrompt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { onSimulatePushClick() }
                                        .padding(vertical = 4.dp)
                                        .testTag("demo_trigger_push_button")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TELEMETRY GRID
                Text(
                    text = strings.diveTelemetryProfileTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailCard(
                        icon = Icons.Rounded.Water,
                        label = strings.maxDepth,
                        value = maxDepthStr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCard(
                        icon = Icons.Rounded.Water,
                        label = strings.avgDepth,
                        value = avgDepthStr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCard(
                        icon = Icons.Rounded.Timer,
                        label = strings.bottomTime,
                        value = "${dive.durationMinutes} min",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailCard(
                        icon = Icons.Rounded.Thermostat,
                        label = strings.waterTemp,
                        value = waterTempStr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCard(
                        icon = Icons.Rounded.Thermostat,
                        label = strings.airTemp,
                        value = airTempStr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCard(
                        icon = Icons.Rounded.Visibility,
                        label = strings.visibility,
                        value = visibilityStr,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GAS CONSUMPTION & CYLINDER
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Air, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = strings.breathingGasTankPressureTitle, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${strings.gasMixLabel}: ${dive.gasType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${strings.cylinderLabel}: ${dive.tankType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${strings.pressureInLabel}: $pressureInStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${strings.pressureOutLabel}: $pressureOutStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${strings.usedPressureLabel}: $pressureUsedStr",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // SAC RATE CALCULATION ENGINE
                if (sacRateData != null) {
                    val (sacBarPerMin, sacLitersPerMin, sacCuFtPerMin) = sacRateData
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dive_sac_rate_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AquaCyan.copy(alpha = 0.15f),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Rounded.Air,
                                                contentDescription = null,
                                                tint = AquaCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isGerman) "SAC-Atemgasrate (Surface Air Consumption)" else "SAC Breathing Rate (Surface Air Consumption)",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isGerman) "Auf Meereshöhe normiert (1 ATA Basis)" else "Normalized to sea level (1 ATA basis)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        sacLitersPerMin < 14.0f -> SeaGreen.copy(alpha = 0.15f)
                                        sacLitersPerMin <= 20.0f -> AquaCyan.copy(alpha = 0.15f)
                                        else -> SandGold.copy(alpha = 0.15f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            sacLitersPerMin < 14.0f -> SeaGreen.copy(alpha = 0.5f)
                                            sacLitersPerMin <= 20.0f -> AquaCyan.copy(alpha = 0.5f)
                                            else -> SandGold.copy(alpha = 0.5f)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = when {
                                            sacLitersPerMin < 14.0f -> if (isGerman) "SEHR EFFIZIENT" else "EXCELLENT"
                                            sacLitersPerMin <= 20.0f -> if (isGerman) "NORMAL" else "NORMAL"
                                            else -> if (isGerman) "ERHÖHT" else "HIGH USAGE"
                                        },
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when {
                                            sacLitersPerMin < 14.0f -> SeaGreen
                                            sacLitersPerMin <= 20.0f -> AquaCyan
                                            else -> SandGold
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = if (isImperial) {
                                            "${kotlin.math.round(sacCuFtPerMin * 100f) / 100f} cu ft/min"
                                        } else {
                                            "${kotlin.math.round(sacLitersPerMin * 10f) / 10f} L/min"
                                        },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = AquaCyan
                                    )
                                    Text(
                                        text = "${kotlin.math.round(sacBarPerMin * 100f) / 100f} bar/min (${tankLiters.toInt()}L Tank)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "Δ ${(dive.pressureInBar - dive.pressureOutBar)} bar • ${dive.durationMinutes}m @ ${dive.avgDepthMeters}m",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SAFETY STOP & WEIGHT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (dive.safetyStopCompleted) SeaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, if (dive.safetyStopCompleted) SeaGreen.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = if (dive.safetyStopCompleted) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dive.safetyStopCompleted) strings.safetyStopDone else strings.safetyStopNotLogged,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (dive.safetyStopCompleted) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.weights}: $weightStr (${dive.suitType})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (dive.sightings.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = strings.marineLifeSightings, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🐠 ${dive.sightings}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (dive.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = strings.diverComments, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${dive.notes}\"",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Row: Share Dive & Delete Dive
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            shareDiveLog(context, dive, isImperial)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AquaCyan),
                        border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("share_dive_detail_button")
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isGerman) "Tauchgang teilen" else "Share Dive", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            showDeleteConfirmDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("delete_dive_log_button")
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.deleteDiveLog, fontSize = 12.sp)
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                title = {
                    Text(
                        text = if (isGerman) "Tauchgang löschen?" else "Delete Dive Log #${dive.diveNumber}?",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        text = if (isGerman)
                            "Möchtest du Tauchgang #${dive.diveNumber} an '${dive.siteName}' wirklich dauerhaft aus deinem Logbuch löschen? Diese Aktion kann nicht rückgängig gemacht werden."
                        else
                            "Are you sure you want to permanently delete dive #${dive.diveNumber} at '${dive.siteName}' from your logbook? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDeleteClick(dive.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isGerman) "Endgültig löschen" else "Delete Permanently", color = MaterialTheme.colorScheme.onError)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text(strings.cancel)
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
