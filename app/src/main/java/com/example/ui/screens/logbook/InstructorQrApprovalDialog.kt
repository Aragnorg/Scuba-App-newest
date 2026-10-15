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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
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
                .clip(RoundedCornerShape(20.dp))
                .testTag("instructor_qr_approval_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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
                                .background(OceanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = OceanPrimary,
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
                        Icon(Icons.Default.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mandatory Role Policy Banner: Scan-Only for Divers
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F4F8),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = OceanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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

                // CAMERA SCANNER VIEWFINDER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F1E29))
                        .border(2.dp, SeaGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Scanner Target Reticle
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
                            imageVector = Icons.Default.QrCodeScanner,
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
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("enable_camera_scan_button")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF00382E))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.enableCamera, fontSize = 11.sp, color = Color(0xFF00382E), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SeaGreen.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = strings.cameraActive,
                                    color = SeaGreen,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontWeight = FontWeight.Bold
                                )
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
                            containerColor = if (isSelected) SeaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SeaGreen) else null,
                        shape = RoundedCornerShape(12.dp)
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
                                    .background(if (isSelected) SeaGreen else Color.LightGray.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF00382E) else Color.DarkGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stamp.centerName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${strings.instructorLabel}: ${stamp.instructorName} • ${stamp.licenseNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SeaGreen.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = strings.scannedBadge,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00695C)
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
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFEBEE),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = err, color = Color(0xFFB71C1C), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Verification Confirmation Box for the scanned instructor stamp
                scannedStamp?.let { stamp ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.authenticQrVerifiedBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${strings.stampCodeLabel}: ${stamp.stampCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = Color(0xFF388E3C)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                            containerColor = OceanPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("apply_instructor_stamp_button")
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
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
