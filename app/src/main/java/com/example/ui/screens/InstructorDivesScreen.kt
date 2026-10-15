package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
@OptIn(ExperimentalMaterial3Api::class)
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

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Student Dives to Stamp, 1: Student Documents, 2: Provide Center QR Code
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
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SeaGreen.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = SeaGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.officialQrProvider,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                        Text(
                            text = "${strings.authorizedInstructor}: $instructorName (PADI OWSI #482910)",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SeaGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = strings.instructorAuthority,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SeaGreen,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Tab Selector
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = OceanPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
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
                        Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == AppLanguage.DE) "Dokumente (${pendingDocs.size})" else "Documents (${pendingDocs.size})",
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
                        Icon(Icons.Rounded.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (pendingDives.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(54.dp),
                                    shape = CircleShape,
                                    color = SeaGreen.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Check, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(30.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = strings.allStudentDivesUpToDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strings.noPendingDivesDesc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${strings.previouslyApprovedDives} (${approvedDives.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(approvedDives, key = { it.id }) { dive ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = SeaGreen.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Verified, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${strings.diveNumberPrefix}${dive.diveNumber}: ${dive.siteName} (${dive.diverName.ifBlank { "Leo Chen" }})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = if (currentLanguage == AppLanguage.DE) "Zu prüfende Schüler-Dokumente (${pendingDocs.size})" else "Candidate Documents Awaiting Sign-off (${pendingDocs.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                    Text(
                        text = if (currentLanguage == AppLanguage.DE) "Prüfe und unterzeichne Schüler-Brevets (C-Cards), ärztliche TTU-Atteste und Haftungsausschlüsse." else "Review and sign off on student certifications (C-Cards), medical clearance statements, and liability waivers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (pendingDocs.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(54.dp),
                                    shape = CircleShape,
                                    color = SeaGreen.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Check, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(30.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.DE) "Alle Schüler-Dokumente sind verifiziert!" else "All student documents are verified!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentLanguage == AppLanguage.DE) "Keine ausstehenden Brevetkarten oder Atteste zur Genehmigung." else "No pending certification cards or medical statements awaiting approval.",
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
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                        shape = RoundedCornerShape(8.dp),
                                        color = SandGold.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, SandGold.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = if (currentLanguage == AppLanguage.DE) "WARTE AUF PRÜFUNG" else "AWAITING APPROVAL",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SandGold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = doc.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${if (currentLanguage == AppLanguage.DE) "Kandidat" else "Candidate"}: ${doc.diverName} (${doc.diverEmail}) • #${doc.documentNumber}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (doc.category == "MEDICAL_FORM" || doc.documentUri != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SeaGreen.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.35f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Rounded.Description, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "📄 Physisches Dokument angehängt (Prüfbereit)" else "📄 Physical Paper Statement Attached (Ready for Cross-Check)",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SeaGreen
                                            )
                                        }
                                    }
                                }

                                if (doc.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${if (currentLanguage == AppLanguage.DE) "Bemerkungen" else "Remarks"}: \"${doc.notes}\"",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { documentForDetails = doc },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Text(if (currentLanguage == AppLanguage.DE) "Details ansehen" else "View Details", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { documentForApproval = doc },
                                        modifier = Modifier.weight(1.3f),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
                                    ) {
                                        Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
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
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = SeaGreen.copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Verified, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${doc.title} (${doc.diverName})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
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
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Authority Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SeaGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = strings.diveCenterQrAuthority,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SeaGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = strings.officialCenterStampQrTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.presentQrToStudentsInstruction,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // High-contrast QR Matrix Generated for the Instructor
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Box(modifier = Modifier.padding(12.dp)) {
                                    QrCodeRenderer(
                                        content = qrContent,
                                        size = 200.dp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = currentStamp.centerName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${strings.instructorLabel}: ${currentStamp.instructorName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${strings.licenseLabel}: ${currentStamp.licenseNumber} • ${currentStamp.location}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

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
                                containerColor = if (isSelected) OceanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, OceanPrimary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(36.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) OceanPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Rounded.Check else Icons.Rounded.School,
                                            contentDescription = null,
                                            tint = if (isSelected) OceanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = stamp.centerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                    .clip(RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
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
                            color = OceanPrimary
                        )
                        IconButton(onClick = { diveForQrPresentation = null }) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.close)
                        }
                    }

                    Text(
                        text = strings.holdQrForStudentInstruction,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            QrCodeRenderer(
                                content = "AQUALEARN-CERT-STAMP-ADRIAN-OWSI482910-DIVE-${dive.id}",
                                size = 210.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${strings.diveSiteLabel}: ${dive.siteName}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${strings.diverLabel}: ${dive.diverName.ifBlank { "Leo Chen" }} • ${dive.maxDepthMeters}m • ${dive.durationMinutes} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Rounded.Verified, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.directlyStampAndVerifyNow,
                                color = Color.White,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OceanPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${strings.diveNumberPrefix}${dive.diveNumber}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SandGold.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, SandGold.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = strings.pendingVerification,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SandGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${dive.siteName} (${dive.location})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${strings.diverLabel}: ${dive.diverName.ifBlank { "Leo Chen" }} • ${dive.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryTag(icon = Icons.Rounded.Water, label = "${dive.maxDepthMeters}m Max")
                TelemetryTag(icon = Icons.Rounded.Timer, label = "${dive.durationMinutes} min")
                TelemetryTag(icon = Icons.Rounded.Air, label = "${(dive.pressureInBar - dive.pressureOutBar).coerceAtLeast(0)} bar")
            }

            if (dive.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${strings.diverNotesLabel}: \"${dive.notes}\"",
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two Instructor Actions: Present QR to Student OR 1-Click Authorize
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPresentQrClick,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("present_qr_dive_${dive.diveNumber}")
                ) {
                    Icon(Icons.Rounded.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.showQr, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onStampClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SeaGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(44.dp)
                        .testTag("instructor_stamp_dive_${dive.diveNumber}")
                ) {
                    Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
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
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = OceanPrimary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}
