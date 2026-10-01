package com.example.ui.screens.logbook

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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Water
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiveLogEntity
import com.example.ui.components.SignaturePreview
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen

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
                .clip(RoundedCornerShape(20.dp))
                .testTag("dive_detail_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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
                                color = OceanPrimary
                            ) {
                                Text(
                                    text = "${strings.diveNumberPrefix}${dive.diveNumber}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
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
                        Text(
                            text = "📍 ${dive.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dive_details_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Stamp Banner
                when (dive.approvalStatus) {
                    "APPROVED_BY_INSTRUCTOR" -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2E7D32)),
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
                                        .background(Color(0xFF2E7D32)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = strings.officialCenterStampBadge,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "${strings.verifiedByLabel}: ${dive.approvedBy.ifBlank { "Adrian Scheid" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    if (dive.instructorLicense.isNotBlank()) {
                                        Text(
                                            text = "${strings.licenseLabel}: ${dive.instructorLicense}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color(0xFF388E3C)
                                        )
                                    }
                                    if (dive.qrStampCode.isNotBlank()) {
                                        Text(
                                            text = "${strings.qrHashLabel}: ${dive.qrStampCode.take(24)}...",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "APPROVED_BY_BUDDY_SIGNATURE" -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFE0F7FA),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00838F)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("approved_buddy_sig_banner")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Draw,
                                        contentDescription = null,
                                        tint = Color(0xFF00838F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.verifiedByBuddySignatureTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF006064)
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${strings.signedByLabel} ${dive.approvedBy}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF00838F),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                if (dive.buddySignaturePathJson.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SignaturePreview(
                                        signatureJson = dive.buddySignaturePathJson,
                                        height = 55.dp
                                    )
                                }

                                if (dive.buddyApprovalNote.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "\"${dive.buddyApprovalNote}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFF006064)
                                    )
                                }
                            }
                        }
                    }

                    "APPROVED_BY_BUDDY_PUSH" -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFE8F5E9),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, SeaGreen),
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
                                        imageVector = Icons.Default.NotificationsActive,
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
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFA000)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pending_approval_banner")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFFF57C00),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.diveLogPendingApprovalTitle,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.chooseApprovalMethodPrompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5D4037)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 1. Scan Instructor / Center QR Code
                                    Button(
                                        onClick = onApproveWithQrClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("action_approve_qr")
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.scanCenterQrButton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // 2. Buddy Approval (Signature or Push message)
                                    Button(
                                        onClick = onApproveWithBuddyClick,
                                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("action_approve_buddy")
                                    ) {
                                        Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFF00382E), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.buddyApproveButton, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00382E))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick test button for push simulation
                                Text(
                                    text = strings.demoTriggerPushPrompt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OceanPrimary,
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
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailCard(
                        icon = Icons.Default.Water,
                        label = strings.maxDepth,
                        value = maxDepthStr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCard(
                        icon = Icons.Default.Water,
                        label = strings.avgDepth,
                        value = avgDepthStr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCard(
                        icon = Icons.Default.Timer,
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
                        icon = Icons.Default.Thermostat,
                        label = strings.waterTemp,
                        value = waterTempStr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCard(
                        icon = Icons.Default.Thermostat,
                        label = strings.airTemp,
                        value = airTempStr,
                        modifier = Modifier.weight(1f)
                    )
                    DetailCard(
                        icon = Icons.Default.Visibility,
                        label = strings.visibility,
                        value = visibilityStr,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GAS CONSUMPTION & CYLINDER
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Air, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = strings.breathingGasTankPressureTitle, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${strings.gasMixLabel}: ${dive.gasType}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "${strings.cylinderLabel}: ${dive.tankType}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${strings.pressureInLabel}: $pressureInStr", style = MaterialTheme.typography.bodySmall)
                            Text(text = "${strings.pressureOutLabel}: $pressureOutStr", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "${strings.usedPressureLabel}: $pressureUsedStr",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
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
                        shape = RoundedCornerShape(10.dp),
                        color = if (dive.safetyStopCompleted) SeaGreen.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = null,
                                tint = if (dive.safetyStopCompleted) SeaGreen else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dive.safetyStopCompleted) strings.safetyStopDone else strings.safetyStopNotLogged,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (dive.safetyStopCompleted) SeaGreen else Color.DarkGray
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.weights}: $weightStr (${dive.suitType})",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                if (dive.sightings.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = strings.marineLifeSightings, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🐠 ${dive.sightings}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OceanPrimary
                    )
                }

                if (dive.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = strings.diverComments, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${dive.notes}\"",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Delete Dive Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            onDeleteClick(dive.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB00020)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("delete_dive_log_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.deleteDiveLog, fontSize = 12.sp)
                    }
                }
            }
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
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = Color.Gray)
            Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}
