package com.example.ui.screens.documents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiverDocumentEntity
import com.example.ui.components.DocumentExpirationDetailCard
import com.example.ui.components.PaperVsDigitalCorrelationCard
import com.example.ui.components.PaperStatementInspectDialog
import com.example.ui.components.QrCodeRenderer
import com.example.ui.components.SignaturePreview
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.util.DocumentExpirationHelper

@Composable
fun DocumentDetailDialog(
    document: DiverDocumentEntity,
    onApproveClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    val isApproved = document.approvalStatus == "APPROVED"
    var showPaperInspectionDialog by remember { mutableStateOf(false) }

    val baseColor = when (document.category) {
        "CERTIFICATION" -> OceanPrimary
        "MEDICAL_FORM" -> SeaGreen
        "INSURANCE" -> Color(0xFFC93B2B)
        "LIABILITY_WAIVER" -> Color(0xFF4A5568)
        "EQUIPMENT_RECORD" -> Color(0xFFD97706)
        else -> Color(0xFF006699)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("document_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (document.category) {
                            "CERTIFICATION" -> strings.categoryCertification
                            "MEDICAL_FORM" -> strings.categoryMedicalForm
                            "INSURANCE" -> strings.categoryInsurance
                            "LIABILITY_WAIVER" -> strings.categoryLiabilityWaiver
                            "EQUIPMENT_RECORD" -> strings.categoryEquipmentRecord
                            else -> strings.categoryCustom
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = baseColor
                    )

                    Row {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.testTag("edit_doc_btn")
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = strings.edit, tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.testTag("delete_doc_btn")
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_doc_detail_btn")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.close)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Digital C-Card / Credential Hero Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_hero_credential_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DepthNavy
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        baseColor.copy(alpha = 0.9f),
                                        DepthNavy
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = document.issuingOrganization.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.8f),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = document.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = if (isApproved) SeaGreen.copy(alpha = 0.25f) else SandGold.copy(alpha = 0.25f),
                                    border = BorderStroke(1.5.dp, if (isApproved) SeaGreen else SandGold)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isApproved) Icons.Rounded.Verified else Icons.Rounded.HourglassTop,
                                            contentDescription = null,
                                            tint = if (isApproved) Color(0xFF68D391) else SandGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isApproved) "VERIFIED" else "PENDING",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isApproved) Color(0xFF68D391) else SandGold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "DIVER NAME",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = document.diverName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "DOCUMENT / ID",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    )
                                    Text(
                                        text = document.documentNumber,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AquaCyan
                                    )
                                }
                            }
                        }
                    }
                }

                val currentLanguage = LocalCurrentLanguage.current
                val expirationInfo = remember(document, currentLanguage) {
                    DocumentExpirationHelper.computeExpirationInfo(document, currentLanguage == AppLanguage.DE)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dates & Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailMetricItem(
                        title = strings.issueDateLabel,
                        value = document.issueDate,
                        icon = Icons.Rounded.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )

                    DetailMetricItem(
                        title = strings.expiryDateLabel,
                        value = document.expiryDate,
                        icon = Icons.Rounded.Shield,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Expiration & Validity Status Card
                Spacer(modifier = Modifier.height(14.dp))
                DocumentExpirationDetailCard(
                    expirationInfo = expirationInfo,
                    document = document,
                    onRenewClick = onEditClick
                )

                // =========================================================================
                // DIVE CENTER PAPER MEDICAL STATEMENT & CORRELATION INSPECTION
                // =========================================================================
                if (document.category == "MEDICAL_FORM" || document.documentUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    PaperVsDigitalCorrelationCard(
                        document = document,
                        onInspectFullScan = { showPaperInspectionDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (document.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isGerman) "Tauchernotizen" else "Diver Notes",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = document.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Official Verification & Sign-off Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isApproved) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, if (isApproved) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isApproved) Icons.Rounded.Verified else Icons.Rounded.HourglassTop,
                                    contentDescription = null,
                                    tint = if (isApproved) MaterialTheme.colorScheme.primary else SandGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isApproved) strings.statusApproved else strings.statusPending,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isApproved) MaterialTheme.colorScheme.primary else SandGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isApproved) {
                            Text(
                                text = "${if (isGerman) "Prüfer / Aussteller" else "Approver"}: ${document.approverName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${if (isGerman) "Qualifikation / Lizenz" else "Credential"}: ${document.approverTitleOrLicense}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (document.approverOrganization.isNotBlank()) {
                                Text(
                                    text = "${if (isGerman) "Organisation" else "Organization"}: ${document.approverOrganization}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${if (isGerman) "Verifiziert am" else "Verified on"}: ${document.approvalDate}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (document.approverNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "${if (isGerman) "Prüfbericht" else "Assessment"}: ${document.approverNotes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            if (document.approverSignatureJson.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Digital Signature on File:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                ) {
                                    SignaturePreview(
                                        signatureJson = document.approverSignatureJson,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(6.dp),
                                        height = 50.dp,
                                        strokeColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Verification Status Banner
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Official Digital Seal",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = document.approvalStampCode,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isGerman) "Offiziell verifiziert und gestempelt mit digitaler Unterschrift und Siegel." else "Officially verified and stamped with digital signature & seal.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            val approverRoleGerman = when (document.requiredApproverType) {
                                "DOCTOR" -> "einen Arzt"
                                "INSTRUCTOR" -> "einen Tauchlehrer"
                                "DIVE_CENTER" -> "eine Tauchbasis"
                                else -> "einen Prüfer"
                            }
                            Text(
                                text = if (isGerman) {
                                    "Dieses Dokument wartet aktuell auf Prüfung und Unterschrift durch $approverRoleGerman."
                                } else {
                                    "This document is currently awaiting review and signature from a ${document.requiredApproverType.lowercase().replace("_", " ")}."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = onApproveClick,
                                    modifier = Modifier.testTag("open_sign_approve_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.approveDocumentTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_btn"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp)
                    ) {
                        Text(strings.close, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showPaperInspectionDialog) {
        PaperStatementInspectDialog(
            document = document,
            onDismiss = { showPaperInspectionDialog = false }
        )
    }
}

@Composable
private fun DetailMetricItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.onPrimaryContainer, 
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
