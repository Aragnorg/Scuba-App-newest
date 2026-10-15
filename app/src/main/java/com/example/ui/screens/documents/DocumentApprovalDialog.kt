package com.example.ui.screens.documents

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalHospital
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.DiverDocumentEntity
import com.example.ui.components.InteractiveSignaturePad
import com.example.ui.components.PaperStatementInspectDialog
import com.example.ui.components.RealisticMedicalStatementPaper
import com.example.ui.components.SignaturePoint
import com.example.ui.components.serializeSignaturePoints
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun DocumentApprovalDialog(
    document: DiverDocumentEntity,
    onApprove: (
        documentId: String,
        approverName: String,
        license: String,
        org: String,
        approvalDate: String,
        stampCode: String,
        signatureJson: String,
        notes: String
    ) -> Unit,
    onReject: (documentId: String, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

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

    var isScanningQr by remember { mutableStateOf(false) }
    var scannedStamp by remember {
        mutableStateOf<AuthorizedDocumentStamp?>(authorizedDocumentStamps.first())
    }

    // Laser Animation for QR Scanner
    val infiniteTransition = rememberInfiniteTransition(label = "doc_scanner_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    var showPaperInspection by remember { mutableStateOf(false) }

    // Default approver role based on document category
    var approverRole by remember {
        mutableStateOf(
            when (document.category) {
                "MEDICAL_FORM" -> "DOCTOR"
                "CERTIFICATION" -> "INSTRUCTOR"
                "LIABILITY_WAIVER", "EQUIPMENT_RECORD" -> "DIVE_CENTER"
                else -> document.requiredApproverType
            }
        )
    }

    var approverName by remember {
        mutableStateOf(
            when (approverRole) {
                "DOCTOR" -> "Dr. med. Thomas Weber"
                "INSTRUCTOR" -> "Adrian Scheid"
                "DIVE_CENTER" -> "AquaLearn Dive Base Sharm"
                else -> "Certification Agency Verifier"
            }
        )
    }

    var approverLicense by remember(isGerman, approverRole) {
        mutableStateOf(
            when (approverRole) {
                "DOCTOR" -> "EDTC / GTMÜ Diving Medicine Lic. #DE-77492"
                "INSTRUCTOR" -> "PADI Master Scuba Diver Trainer #391024"
                "DIVE_CENTER" -> if (isGerman) "Tauchbasis #DC-9842" else "Dive Center #DC-9842"
                else -> if (isGerman) "Verbands-Prüfer-ID #AG-551" else "Agency Auditor ID #AG-551"
            }
        )
    }

    var approverOrg by remember(isGerman, approverRole) {
        mutableStateOf(
            when (approverRole) {
                "DOCTOR" -> if (isGerman) "Klinik für Hyperbar- & Tauchmedizin" else "Hyperbaric & Sports Medicine Clinic"
                "INSTRUCTOR" -> "AquaLearn Diving Academy"
                "DIVE_CENTER" -> if (isGerman) "AquaLearn Tauchbasis & Center" else "AquaLearn Dive Resort & Base"
                else -> if (isGerman) "Internationaler Tauchsportverband" else "International Scuba Accreditation Board"
            }
        )
    }

    var assessmentNotes by remember(isGerman, approverRole) {
        mutableStateOf(
            when (approverRole) {
                "DOCTOR" -> if (isGerman) {
                    "Kandidat tauchsportärztlich untersucht: Trommelfelle intakt, Lungenfunktion normal, kardiovaskuläre Parameter unauffällig. Tauchtauglichkeit ohne Einschränkung bis 30m bescheinigt."
                } else {
                    "Candidate physically examined: Ear drums healthy, lung function normal, cardiovascular parameters fit for unrestricted scuba diving to 30m."
                }
                "INSTRUCTOR" -> if (isGerman) {
                    "Alle theoretischen Prüfungen, Standards und praktischen Übungen gemäß den geltenden Ausbildungsrichtlinien erfolgreich demonstriert und abgenommen."
                } else {
                    "All prerequisites, theoretical knowledge, and practical skill demonstrations verified in compliance with international training standards."
                }
                else -> if (isGerman) {
                    "Dokument auf Echtheit und Gültigkeit geprüft. Entspricht den gesetzlichen und verbandsspezifischen Vorgaben."
                } else {
                    "Document verified authentic and currently in full legal compliance."
                }
            }
        )
    }

    val signaturePoints = remember { mutableStateListOf<SignaturePoint>() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
                .testTag("document_approval_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = SeaGreen.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = SeaGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.approveDocumentTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = document.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_doc_approval_btn")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = strings.close,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Document Info Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Diver: ${document.diverName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "#${document.documentNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Issuer: ${document.issuingOrganization}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (document.category == "MEDICAL_FORM" || document.documentUri != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Physical Paper Statement / Scan:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SeaGreen
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    RealisticMedicalStatementPaper(
                        document = document,
                        isThumbnail = true,
                        onInspectClick = { showPaperInspection = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High-visibility QR Verification Toggle Button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isScanningQr) OceanPrimary.copy(alpha = 0.08f) else SeaGreen.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.2.dp, if (isScanningQr) OceanPrimary else SeaGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isScanningQr) "QR-Code Scanner aktiv" else "Sofortige QR-Code Prüfung",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isScanningQr) OceanPrimary else SeaGreen
                            )
                            Text(
                                text = if (isScanningQr) "Kamera sucht nach verifizierten Siegeln / Stempeln" else "Offiziellen Stempel-QR-Code scannen zum sofortigen Signieren",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { isScanningQr = !isScanningQr },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isScanningQr) MaterialTheme.colorScheme.surfaceVariant else OceanPrimary,
                                contentColor = if (isScanningQr) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("doc_toggle_qr_scan_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "QR scannen",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isScanningQr) "Abbrechen" else "Kamera öffnen",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isScanningQr) {
                    // ==========================================
                    // MODE A: ACTIVE CAMERA QR SCANNER VIEW
                    // ==========================================
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
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
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Offizielles QR-Siegel im Rahmen ausrichten",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kamera aktiv. Suche nach Zertifikats-QR-Code...",
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
                                        .testTag("doc_enable_camera_btn")
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF00382E))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kamerazugriff erlauben", fontSize = 11.sp, color = Color(0xFF00382E), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SeaGreen.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = "KAMERA BEREIT",
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

                    Text(
                        text = "Scanned Stamp Detection:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    authorizedDocumentStamps.forEach { stamp ->
                        val isSelected = scannedStamp == stamp
                        Card(
                            onClick = { scannedStamp = stamp },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("doc_stamp_option_${stamp.approverName.replace(" ", "_")}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) SeaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, SeaGreen) else null,
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
                                        imageVector = if (isSelected) Icons.Default.Verified else Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF00382E) else Color.DarkGray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stamp.approverName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${stamp.approverLicense} • ${stamp.approverOrg}",
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
                                            text = "DETECTED ✓",
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

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Verified Summary Box
                    scannedStamp?.let { stamp ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE8F5E9),
                            border = BorderStroke(1.dp, Color(0xFF81C784)),
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
                                        text = "Official Stamp QR Authenticated",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Stamp code: ${stamp.stampCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Color(0xFF388E3C)
                                )
                                Text(
                                    text = "Assessment: ${stamp.assessmentNotes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // QR Approval Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_doc_approval_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(strings.cancel)
                        }

                        Button(
                            onClick = {
                                val currentStamp = scannedStamp
                                if (currentStamp != null) {
                                    // Generate a simulated signature path for approval stamp
                                    val simulatedSignatureJson = "[{\"x\":20.5,\"y\":30.5},{\"x\":120.0,\"y\":45.0,\"isBreak\":true}]"
                                    onApprove(
                                        document.id,
                                        currentStamp.approverName,
                                        currentStamp.approverLicense,
                                        currentStamp.approverOrg,
                                        todayDate,
                                        currentStamp.stampCode,
                                        simulatedSignatureJson,
                                        currentStamp.assessmentNotes
                                    )
                                }
                            },
                            enabled = scannedStamp != null,
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("confirm_doc_approval_btn"),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Per QR-Code verifizieren", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // ==========================================
                    // MODE B: MANUAL SIGNATURE APPROVAL FORM
                    // ==========================================
                    // Select Approver Role
                    Text(
                        text = strings.requiredApproverLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ApproverRoleChip(
                        title = "Arzt",
                        icon = Icons.Default.LocalHospital,
                        isSelected = approverRole == "DOCTOR",
                        color = Color(0xFF00875A),
                        onClick = {
                            approverRole = "DOCTOR"
                            approverName = "Dr. med. Thomas Weber"
                            approverLicense = "EDTC / GTMÜ Diving Medicine Lic. #DE-77492"
                            approverOrg = "Tauch- und Sportmedizinisches Zentrum"
                            assessmentNotes = "Kandidat ärztlich untersucht: Trommelfelle intakt, Lungenfunktion altersgerecht, uneingeschränkt tauchtauglich."
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ApproverRoleChip(
                        title = "Tauchlehrer",
                        icon = Icons.Default.School,
                        isSelected = approverRole == "INSTRUCTOR",
                        color = OceanPrimary,
                        onClick = {
                            approverRole = "INSTRUCTOR"
                            approverName = "Adrian Scheid"
                            approverLicense = "PADI Master Scuba Diver Trainer #391024"
                            approverOrg = "AquaLearn Tauchakademie"
                            assessmentNotes = "Alle Ausbildungsstandards, Pool- und Freiwassertauchgänge erfolgreich absolviert und bestätigt."
                        },
                        modifier = Modifier.weight(1f)
                    )

                    ApproverRoleChip(
                        title = "Tauchbasis",
                        icon = Icons.Default.Business,
                        isSelected = approverRole == "DIVE_CENTER",
                        color = Color(0xFFD97706),
                        onClick = {
                            approverRole = "DIVE_CENTER"
                            approverName = "AquaLearn Dive Base"
                            approverLicense = "Tauchbasis-Lizenz #DC-9842"
                            approverOrg = "AquaLearn Tauchbasis & Resort"
                            assessmentNotes = "Dokument auf Echtheit geprüft und für aktive Tauchaktivitäten im System hinterlegt."
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Approver Name Input
                OutlinedTextField(
                    value = approverName,
                    onValueChange = { approverName = it },
                    label = { Text(strings.approverNameLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_approver_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // License / Credential Input
                OutlinedTextField(
                    value = approverLicense,
                    onValueChange = { approverLicense = it },
                    label = { Text(strings.approverCredentialLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_approver_license_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Organization Input
                OutlinedTextField(
                    value = approverOrg,
                    onValueChange = { approverOrg = it },
                    label = { Text(strings.approverOrgLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_approver_org_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Assessment / Findings
                OutlinedTextField(
                    value = assessmentNotes,
                    onValueChange = { assessmentNotes = it },
                    label = { Text(strings.approverAssessmentNotesLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_approver_notes_input"),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                    // Digital Finger Signature Pad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Draw,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.signHerePrompt,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (signaturePoints.isNotEmpty()) {
                            TextButton(
                                onClick = { signaturePoints.clear() },
                                modifier = Modifier.testTag("clear_signature_btn")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    ) {
                        InteractiveSignaturePad(
                            points = signaturePoints,
                            onAddPoint = { signaturePoints.add(it) },
                            onEndStroke = {
                                if (signaturePoints.isNotEmpty()) {
                                    val last = signaturePoints.last()
                                    signaturePoints[signaturePoints.size - 1] = last.copy(isBreak = true)
                                }
                            },
                            modifier = Modifier.testTag("doc_signature_pad"),
                            height = 140.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_doc_approval_btn"),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(strings.cancel)
                        }

                        Button(
                            onClick = {
                                val stampCode = "STAMP-${approverRole}-${UUID.randomUUID().toString().take(8).uppercase()}"
                                val signatureJson = serializeSignaturePoints(signaturePoints)
                                onApprove(
                                    document.id,
                                    approverName,
                                    approverLicense,
                                    approverOrg,
                                    todayDate,
                                    stampCode,
                                    signatureJson,
                                    assessmentNotes
                                )
                            },
                            enabled = approverName.isNotBlank() && approverLicense.isNotBlank(),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("confirm_doc_approval_btn"),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SeaGreen
                            )
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.confirmApprovalButton, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showPaperInspection) {
        PaperStatementInspectDialog(
            document = document,
            onDismiss = { showPaperInspection = false }
        )
    }
}

@Composable
private fun ApproverRoleChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("approver_role_${title.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

data class AuthorizedDocumentStamp(
    val approverName: String,
    val approverLicense: String,
    val approverOrg: String,
    val stampCode: String,
    val assessmentNotes: String
)

val authorizedDocumentStamps = listOf(
    AuthorizedDocumentStamp(
        approverName = "Dr. med. Thomas Weber",
        approverLicense = "EDTC / GTMÜ Diving Medicine Lic. #DE-77492",
        approverOrg = "Tauch- und Sportmedizinisches Zentrum",
        stampCode = "STAMP-MD-WEBER-9842",
        assessmentNotes = "Kandidat uneingeschränkt tauchtauglich. Herz, Lunge und HNO ohne pathologischen Befund."
    ),
    AuthorizedDocumentStamp(
        approverName = "Adrian Scheid",
        approverLicense = "PADI Master Scuba Diver Trainer #391024",
        approverOrg = "AquaLearn Tauchakademie",
        stampCode = "STAMP-INST-SCHEID-1024",
        assessmentNotes = "Alle Ausbildungsstandards, Theorie- und Praxisanforderungen vollständig erfüllt."
    ),
    AuthorizedDocumentStamp(
        approverName = "AquaLearn Dive Base",
        approverLicense = "Tauchbasis-Lizenz #DC-9842",
        approverOrg = "AquaLearn Tauchbasis & Resort",
        stampCode = "STAMP-DC-AQUALEARN-8422",
        assessmentNotes = "Dokument auf Echtheit geprüft und im Basissystem hinterlegt."
    )
)
