package com.example.ui.screens.logbook

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.components.LiveCameraPreviewView
import com.example.data.model.DiveLogEntity
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen

data class AuthorizedDiveCenterStamp(
    val centerName: String,
    val instructorName: String,
    val licenseNumber: String,
    val stampCode: String,
    val location: String
)

val authorizedDiveCenterStamps = listOf(
    AuthorizedDiveCenterStamp(
        centerName = "AquaLearn PADI 5-Star Academy",
        instructorName = "Adrian Scheid",
        licenseNumber = "PADI OWSI #482910",
        stampCode = "AQUALEARN-CERT-STAMP-ADRIAN-OWSI482910",
        location = "Official AquaLearn Diving Academy"
    ),
    AuthorizedDiveCenterStamp(
        centerName = "Persönlicher Profil-Stempel (Adrian Scheid)",
        instructorName = "Adrian Scheid (Instructor)",
        licenseNumber = "PADI / CMAS OWSI",
        stampCode = "AL-STAMP-ADRIAN-SCHEID-01",
        location = "Persönlicher QR-Code (adrian.scheid01@gmail.com)"
    ),
    AuthorizedDiveCenterStamp(
        centerName = "Red Sea Explorers Dive Center",
        instructorName = "Marco Rossi",
        licenseNumber = "SSI Master Instructor #77281",
        stampCode = "REDSEA-EXP-MARCO-SSI77281",
        location = "Sharm El Sheikh, Egypt"
    ),
    AuthorizedDiveCenterStamp(
        centerName = "Blue Hole Divers Dahab",
        instructorName = "Sarah Jenkins",
        licenseNumber = "PADI MSDT #91023",
        stampCode = "BLUEHOLE-DAHAB-SARAH-MSDT91023",
        location = "Dahab, South Sinai"
    )
)

/**
 * QR Code Approval Dialog for Students / Divers:
 *
 * CRITICAL ACCESS CONTROL:
 * - Only Dive Centers & Instructors can provide/generate the Center QR code.
 * - The Student / Diver is ONLY allowed to SCAN the QR code provided by the instructor.
 * - This dialog contains NO QR generation or QR display controls.
 * - It strictly operates as a QR Scanner & Validator for official instructor stamps.
 */
@Composable
fun InstructorQrApprovalDialog(
    dive: DiveLogEntity,
    onApprove: (diveId: String, stampCode: String, centerName: String, instructorName: String, license: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val strings = LocalAppStrings.current

    // Scanned state
    var scannedStamp by remember {
        mutableStateOf<AuthorizedDiveCenterStamp?>(authorizedDiveCenterStamps.first())
    }
    var manualQrInput by remember { mutableStateOf("") }
    var scanValidationErrorMessage by remember { mutableStateOf<String?>(null) }
    var isSimulatingCameraDetection by remember { mutableStateOf(false) }

    // Scanner laser animation
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("instructor_qr_approval_dialog"),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCodeScanner,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.scanInstructorQrTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.diveNumberPrefix}${dive.diveNumber}: ${dive.siteName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_qr_dialog_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mandatory Role Policy Banner: Scan-Only for Divers
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = strings.scanOnlyNotice,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CAMERA SCANNER VIEWFINDER WITH LIVE CAMERA WINDOW
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F1E29))
                        .border(2.dp, SeaGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // 1. REAL LIVE CAMERA PREVIEW FEED
                    if (hasCameraPermission) {
                        LiveCameraPreviewView(
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // 2. Scanner Target Reticle Overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cornerLen = 28f
                        val strokeW = 4f

                        // Top Left
                        drawLine(SeaGreen, Offset(24f, 24f), Offset(24f + cornerLen, 24f), strokeW)
                        drawLine(SeaGreen, Offset(24f, 24f), Offset(24f, 24f + cornerLen), strokeW)
                        // Top Right
                        drawLine(SeaGreen, Offset(w - 24f, 24f), Offset(w - 24f - cornerLen, 24f), strokeW)
                        drawLine(SeaGreen, Offset(w - 24f, 24f), Offset(w - 24f, 24f + cornerLen), strokeW)
                        // Bottom Left
                        drawLine(SeaGreen, Offset(24f, h - 24f), Offset(24f + cornerLen, h - 24f), strokeW)
                        drawLine(SeaGreen, Offset(24f, h - 24f), Offset(24f, h - 24f - cornerLen), strokeW)
                        // Bottom Right
                        drawLine(SeaGreen, Offset(w - 24f, h - 24f), Offset(w - 24f - cornerLen, h - 24f), strokeW)
                        drawLine(SeaGreen, Offset(w - 24f, h - 24f), Offset(w - 24f, h - 24f - cornerLen), strokeW)

                        // Animated Laser Line
                        val laserY = h * laserPosition
                        drawLine(
                            color = SeaGreen,
                            start = Offset(32f, laserY),
                            end = Offset(w - 32f, laserY),
                            strokeWidth = 3f
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = null,
                            tint = SeaGreen,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.alignQrInFrame,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.pointCameraInstruction,
                            color = Color.LightGray,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        if (!hasCameraPermission) {
                            Button(
                                onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .heightIn(min = 36.dp)
                                    .testTag("enable_camera_scan_button")
                            ) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF00382E))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.enableCamera, fontSize = 11.sp, color = Color(0xFF00382E), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xCC000000),
                                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(SeaGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "🟢 Live-Kamera aktiv (Sucher aktiv)",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scan detection selector (simulates scanning the instructor's physical or on-screen QR code)
                Text(
                    text = strings.selectScannedStamp,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strings.detectsAuthorizedNotice,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                authorizedDiveCenterStamps.forEach { stamp ->
                    val isSelected = scannedStamp == stamp
                    Card(
                        onClick = {
                            scannedStamp = stamp
                            scanValidationErrorMessage = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("stamp_option_${stamp.instructorName.replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stamp.centerName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${strings.instructorLabel}: ${stamp.instructorName} • ${stamp.licenseNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = strings.scannedBadge,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Error Message if invalid QR was entered
                AnimatedVisibility(visible = scanValidationErrorMessage != null) {
                    scanValidationErrorMessage?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.ErrorOutline, 
                                    contentDescription = null, 
                                    tint = MaterialTheme.colorScheme.error, 
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = err, 
                                    color = MaterialTheme.colorScheme.onErrorContainer, 
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Verification Confirmation Box for the scanned instructor stamp
                scannedStamp?.let { stamp ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Verified,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.authenticQrVerifiedBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${strings.stampCodeLabel}: ${stamp.stampCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm and Approve Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = {
                            val currentStamp = scannedStamp
                            if (currentStamp != null) {
                                onApprove(
                                    dive.id,
                                    currentStamp.stampCode,
                                    currentStamp.centerName,
                                    currentStamp.instructorName,
                                    currentStamp.licenseNumber
                                )
                            }
                        },
                        enabled = scannedStamp != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        modifier = Modifier.testTag("apply_instructor_stamp_button")
                    ) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.verifyWithScannedStampButton} #${dive.diveNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = strings.securityRuleNotice,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
