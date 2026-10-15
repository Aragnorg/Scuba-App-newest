package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiveLogEntity
import com.example.data.model.DiverDocumentEntity
import com.example.ui.components.QrCodeRenderer
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.screens.documents.DocumentApprovalDialog
import com.example.ui.screens.documents.DocumentDetailDialog
import com.example.ui.screens.logbook.authorizedDiveCenterStamps
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

/**
 * Dedicated Instructor & Dive Center Screen:
 *
 * CRITICAL ACCESS CONTROL:
 * - Only Dive Centers & Instructors have permission to generate & provide the QR code.
 * - Instructors provide the Official Stamp QR code for students to scan.
 * - Instructors can also review and directly stamp student dive logs.
 */
@Composable
fun InstructorDivesScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val dives by viewModel.allDiveLogs.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val instructorName = currentUser?.fullName ?: "Adrian Scheid"
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Student Dives to Stamp, 1: Provide Center QR Code, 2: Student Documents
    var diveForQrPresentation by remember { mutableStateOf<DiveLogEntity?>(null) }
    var selectedCenterStampIndex by remember { mutableIntStateOf(0) }
    var documentForApproval by remember { mutableStateOf<DiverDocumentEntity?>(null) }
    var documentForDetails by remember { mutableStateOf<DiverDocumentEntity?>(null) }

    val pendingDives = remember(dives) { dives.filter { it.approvalStatus == "PENDING" } }
    val approvedDives = remember(dives) { dives.filter { it.approvalStatus != "PENDING" } }
    val pendingDocs = remember(documents) { documents.filter { it.approvalStatus == "PENDING" } }
    val approvedDocs = remember(documents) { documents.filter { it.approvalStatus == "APPROVED" } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("instructor_dives_screen")
    ) {
        // Authority Header Banner
        Surface(
            color = Color(0xFF0F2636),
            contentColor = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SeaGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = SeaGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = strings.officialQrProvider,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AquaCyan
                        )
                        Text(
                            text = "${strings.authorizedInstructor}: $instructorName (PADI OWSI #482910)",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SeaGreen.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SeaGreen)
                ) {
                    Text(
                        text = strings.instructorAuthority,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SeaGreen,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Tab Selector
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = OceanPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${strings.reviewDivesTab} (${pendingDives.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_instructor_review")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Documents (${pendingDocs.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_instructor_docs")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.provideCenterQrTab,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                modifier = Modifier.testTag("tab_instructor_qr_display")
            )
        }

        if (selectedTab == 0) {
            // LIST OF PENDING & APPROVED STUDENT LOGS
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "${strings.pendingStudentDives} (${pendingDives.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                    Text(
                        text = strings.reviewTelemetryInstruction,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (pendingDives.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = strings.allStudentDivesUpToDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.noPendingDivesDesc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    items(pendingDives, key = { it.id }) { dive ->
                        InstructorPendingDiveCard(
                            dive = dive,
                            onStampClick = {
                                viewModel.approveDiveWithInstructorQr(
                                    diveId = dive.id,
                                    qrStampCode = "AQUALEARN-CERT-STAMP-ADRIAN-OWSI482910-DIVE-${dive.id}",
                                    centerName = "AquaLearn PADI 5-Star Academy",
                                    instructorName = instructorName,
                                    license = "PADI OWSI #482910"
                                )
                            },
                            onPresentQrClick = {
                                diveForQrPresentation = dive
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "${strings.previouslyApprovedDives} (${approvedDives.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(approvedDives, key = { it.id }) { dive ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${strings.diveNumberPrefix}${dive.diveNumber}: ${dive.siteName} (${dive.diverName.ifBlank { "Leo Chen" }})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${dive.date} • ${dive.maxDepthMeters}m • ${dive.durationMinutes} min • ${strings.statusLabel}: ${dive.approvalStatus}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // TAB 1: STUDENT DOCUMENTS & CERTIFICATIONS TO REVIEW & APPROVE
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Candidate Documents Awaiting Sign-off (${pendingDocs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                    Text(
                        text = "Review and sign off on student certifications (C-Cards), medical clearance statements, and liability waivers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (pendingDocs.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "All student documents are verified!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "No pending certification cards or medical statements awaiting approval.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(pendingDocs, key = { it.id }) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = doc.category.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanPrimary
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = SandGold.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SandGold)
                                    ) {
                                        Text(
                                            text = if (currentLanguage == AppLanguage.DE) "WARTE AUF PRÜFUNG" else "AWAITING APPROVAL",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SandGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = doc.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${if (currentLanguage == AppLanguage.DE) "Kandidat" else "Candidate"}: ${doc.diverName} (${doc.diverEmail}) • #${doc.documentNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (doc.category == "MEDICAL_FORM" || doc.documentUri != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SeaGreen.copy(alpha = 0.12f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SeaGreen.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Description, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "📄 Physisches Dokument angehängt (Prüfbereit)" else "📄 Physical Paper Statement Attached (Ready for Cross-Check)",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SeaGreen
                                            )
                                        }
                                    }
                                }

                                if (doc.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${if (currentLanguage == AppLanguage.DE) "Bemerkungen" else "Remarks"}: \"${doc.notes}\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { documentForDetails = doc },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(if (currentLanguage == AppLanguage.DE) "Details ansehen" else "View Details", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { documentForApproval = doc },
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (currentLanguage == AppLanguage.DE) "Prüfen & Signieren" else "Sign & Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (approvedDocs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${if (currentLanguage == AppLanguage.DE) "Kürzlich verifizierte Dokumente" else "Recently Verified Documents"} (${approvedDocs.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SeaGreen
                        )
                    }

                    items(approvedDocs, key = { it.id }) { doc ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${doc.title} (${doc.diverName})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${if (currentLanguage == AppLanguage.DE) "Freigegeben von" else "Approved by"}: ${doc.approverName} • ${if (currentLanguage == AppLanguage.DE) "Stempel" else "Stamp"}: ${doc.approvalStampCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: PROVIDE INSTRUCTOR / DIVE CENTER OFFICIAL QR CODE
            val currentStamp = authorizedDiveCenterStamps.getOrElse(selectedCenterStampIndex) { authorizedDiveCenterStamps.first() }
            val qrContent = "${currentStamp.stampCode}-CENTER"

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0A2333),
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Authority Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SeaGreen.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SeaGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.diveCenterQrAuthority,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SeaGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = strings.officialCenterStampQrTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AquaCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.presentQrToStudentsInstruction,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = Color.LightGray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // High-contrast QR Matrix Generated for the Instructor
                            QrCodeRenderer(
                                content = qrContent,
                                size = 200.dp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF13364C),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = currentStamp.centerName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AquaCyan
                                    )
                                    Text(
                                        text = "${strings.instructorLabel}: ${currentStamp.instructorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${strings.licenseLabel}: ${currentStamp.licenseNumber} • ${currentStamp.location}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = strings.selectCenterBranch,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    authorizedDiveCenterStamps.forEachIndexed { index, stamp ->
                        val isSelected = selectedCenterStampIndex == index
                        Card(
                            onClick = { selectedCenterStampIndex = index },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OceanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, OceanPrimary) else null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isSelected) OceanPrimary else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = stamp.centerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(text = "${stamp.instructorName} • ${stamp.licenseNumber}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog to Present QR Code for a Specific Student Dive
    diveForQrPresentation?.let { dive ->
        Dialog(
            onDismissRequest = { diveForQrPresentation = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp)),
                color = Color(0xFF0A2333),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.diveNumberPrefix}${dive.diveNumber} ${strings.qrCodeLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AquaCyan
                        )
                        IconButton(onClick = { diveForQrPresentation = null }) {
                            Icon(Icons.Default.Close, contentDescription = strings.close, tint = Color.White)
                        }
                    }

                    Text(
                        text = strings.holdQrForStudentInstruction,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    QrCodeRenderer(
                        content = "AQUALEARN-CERT-STAMP-ADRIAN-OWSI482910-DIVE-${dive.id}",
                        size = 210.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF13364C),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "${strings.diveSiteLabel}: ${dive.siteName}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "${strings.diverLabel}: ${dive.diverName.ifBlank { "Leo Chen" }} • ${dive.maxDepthMeters}m • ${dive.durationMinutes} min", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                viewModel.approveDiveWithInstructorQr(
                                    diveId = dive.id,
                                    qrStampCode = "AQUALEARN-CERT-STAMP-ADRIAN-OWSI482910-DIVE-${dive.id}",
                                    centerName = "AquaLearn PADI 5-Star Academy",
                                    instructorName = instructorName,
                                    license = "PADI OWSI #482910"
                                )
                                diveForQrPresentation = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF00382E))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.directlyStampAndVerifyNow,
                                color = Color(0xFF00382E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Document Approval Dialog for Instructor
    documentForApproval?.let { doc ->
        DocumentApprovalDialog(
            document = doc,
            onApprove = { documentId, approverName, license, org, approvalDate, stampCode, signatureJson, notes ->
                viewModel.approveDocument(
                    documentId = documentId,
                    approverName = approverName,
                    license = license,
                    org = org,
                    approvalDate = approvalDate,
                    stampCode = stampCode,
                    signatureJson = signatureJson,
                    notes = notes
                )
                documentForApproval = null
            },
            onReject = { documentId, notes ->
                viewModel.rejectDocument(documentId, notes)
                documentForApproval = null
            },
            onDismiss = { documentForApproval = null }
        )
    }

    // Document Detail Dialog for Instructor
    documentForDetails?.let { doc ->
        DocumentDetailDialog(
            document = doc,
            onApproveClick = {
                documentForDetails = null
                documentForApproval = doc
            },
            onEditClick = { documentForDetails = null },
            onDeleteClick = {
                viewModel.deleteDocument(doc.id)
                documentForDetails = null
            },
            onDismiss = { documentForDetails = null }
        )
    }
}

@Composable
private fun InstructorPendingDiveCard(
    dive: DiveLogEntity,
    onStampClick: () -> Unit,
    onPresentQrClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = OceanPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${strings.diveNumberPrefix}${dive.diveNumber}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFF8E1)
                ) {
                    Text(
                        text = strings.pendingVerification,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${dive.siteName} (${dive.location})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${strings.diverLabel}: ${dive.diverName.ifBlank { "Leo Chen" }} • ${dive.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryTag(icon = Icons.Default.Water, label = "${dive.maxDepthMeters}m Max")
                TelemetryTag(icon = Icons.Default.Timer, label = "${dive.durationMinutes} min")
                TelemetryTag(icon = Icons.Default.Air, label = "${(dive.pressureInBar - dive.pressureOutBar).coerceAtLeast(0)} bar")
            }

            if (dive.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${strings.diverNotesLabel}: \"${dive.notes}\"",
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Two Instructor Actions: Present QR to Student OR 1-Click Authorize
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onPresentQrClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("present_qr_dive_${dive.diveNumber}")
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.showQr, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onStampClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SeaGreen,
                        contentColor = Color(0xFF00382E)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(42.dp)
                        .testTag("instructor_stamp_dive_${dive.diveNumber}")
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.authorizeStamp,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryTag(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = OceanPrimary)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
        }
    }
}
