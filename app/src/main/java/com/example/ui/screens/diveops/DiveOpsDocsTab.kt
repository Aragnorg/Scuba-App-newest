@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens.diveops

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import com.example.util.GoogleDriveHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentSignatureEntity
import com.example.data.model.OrganizationDocumentEntity
import com.example.data.model.OrganizationEntity
import com.example.data.model.OrganizationMemberEntity
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tab 5: Dokumenten-Tresor & Nachweis-Management (Modul 4)
 * Multi-tenant compliance center for club statutes, safety waivers, meeting protocols,
 * and mandatory check-in digital signatures.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiveOpsDocsTab(
    viewModel: AquaViewModel,
    organization: OrganizationEntity,
    isGerman: Boolean,
    primaryAccentColor: Color,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.currentOrgDocuments.collectAsState()
    val signatures by viewModel.currentOrgSignatures.collectAsState()
    val members by viewModel.currentOrgMembers.collectAsState()
    val strings = LocalAppStrings.current

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedVisibilityFilter by remember { mutableStateOf("ALL") }

    var showUploadDialog by remember { mutableStateOf(false) }
    var docToPreview by remember { mutableStateOf<OrganizationDocumentEntity?>(null) }
    var docToSign by remember { mutableStateOf<OrganizationDocumentEntity?>(null) }
    var docSignaturesToView by remember { mutableStateOf<OrganizationDocumentEntity?>(null) }
    var docToDelete by remember { mutableStateOf<OrganizationDocumentEntity?>(null) }

    // KPI Metrics
    val totalDocsCount = documents.size
    val mandatoryDocsCount = documents.count { it.requiresAcknowledgment }
    val totalSignaturesCount = signatures.size
    val adminRestrictedCount = documents.count { it.visibility == "ADMINS_ONLY" || it.visibility == "DIVE_LEADERS_ONLY" }

    // Filtered Documents
    val filteredDocs = documents.filter { doc ->
        val categoryMatch = selectedCategoryFilter == "ALL" || doc.category.equals(selectedCategoryFilter, ignoreCase = true)
        val visibilityMatch = selectedVisibilityFilter == "ALL" || doc.visibility.equals(selectedVisibilityFilter, ignoreCase = true)
        categoryMatch && visibilityMatch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Compliance KPI Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, primaryAccentColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.docVaultTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.docVaultSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { showUploadDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.uploadNewDocAction, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 KPI Stat Counters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Total
                        DocKpiCounter(
                            title = strings.totalDocsKpi,
                            value = totalDocsCount.toString(),
                            accentColor = OceanPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        // Mandatory
                        DocKpiCounter(
                            title = strings.mandatoryDocsKpi,
                            value = mandatoryDocsCount.toString(),
                            accentColor = if (mandatoryDocsCount > 0) Color(0xFFE65100) else Color.Gray,
                            modifier = Modifier.weight(1f)
                        )
                        // Signatures
                        DocKpiCounter(
                            title = strings.signaturesKpi,
                            value = totalSignaturesCount.toString(),
                            accentColor = SeaGreen,
                            modifier = Modifier.weight(1f)
                        )
                        // Admin Restricted
                        DocKpiCounter(
                            title = strings.adminRestrictedKpi,
                            value = adminRestrictedCount.toString(),
                            accentColor = Color(0xFF7E57C2),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    val context = LocalContext.current
                    OutlinedButton(
                        onClick = {
                            GoogleDriveHelper.openInGoogleDriveOrBrowser(context, "https://drive.google.com")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryAccentColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGerman) "☁️ Geteilten Vereins-Google-Drive öffnen" else "☁️ Open Club Google Drive",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. Filter Chips: Categories & Visibility
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Category Filter
                Text(
                    text = strings.docCategoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categories = listOf(
                        "ALL" to strings.catAllDocs,
                        "WAIVER" to strings.catWaiver,
                        "RULES" to strings.catRules,
                        "CONSTITUTION" to strings.catConstitution,
                        "PROTOCOL" to strings.catProtocol,
                        "EMERGENCY_PLAN" to strings.catEmergencyPlan,
                        "PRICE_LIST" to strings.catPriceList,
                        "MAP" to strings.catMap
                    )
                    categories.forEach { (catKey, catLabel) ->
                        FilterChip(
                            selected = selectedCategoryFilter == catKey,
                            onClick = { selectedCategoryFilter = catKey },
                            label = { Text(catLabel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                                selectedLabelColor = primaryAccentColor
                            )
                        )
                    }
                }

                // Visibility Filter
                Text(
                    text = strings.docVisibilityLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val visibilities = listOf(
                        "ALL" to strings.catAllDocs,
                        "PUBLIC" to strings.visibilityPublic,
                        "ALL_MEMBERS" to strings.visibilityAllMembers,
                        "DIVE_LEADERS_ONLY" to strings.visibilityDiveLeaders,
                        "ADMINS_ONLY" to strings.visibilityAdmins
                    )
                    visibilities.forEach { (visKey, visLabel) ->
                        FilterChip(
                            selected = selectedVisibilityFilter == visKey,
                            onClick = { selectedVisibilityFilter = visKey },
                            label = { Text(visLabel, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                                selectedLabelColor = primaryAccentColor
                            )
                        )
                    }
                }
            }
        }

        // 3. Document Cards List
        if (filteredDocs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isGerman) "Keine Dokumente für die ausgewählten Filter gefunden." else "No documents found for selected filters.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(filteredDocs, key = { it.id }) { doc ->
                DocumentCard(
                    doc = doc,
                    signaturesCount = signatures.count { it.documentId == doc.id },
                    primaryAccentColor = primaryAccentColor,
                    strings = strings,
                    isGerman = isGerman,
                    onPreview = { docToPreview = doc },
                    onSign = { docToSign = doc },
                    onViewSignatures = { docSignaturesToView = doc },
                    onDelete = { docToDelete = doc }
                )
            }
        }
    }

    // --- DIALOGS ---

    // 1. Upload/Create Document Dialog
    if (showUploadDialog) {
        UploadOrganizationDocumentDialog(
            isGerman = isGerman,
            strings = strings,
            primaryAccentColor = primaryAccentColor,
            onDismiss = { showUploadDialog = false },
            onConfirm = { title, cat, vis, reqAck, summary, driveUri ->
                viewModel.uploadOrganizationDocument(
                    title = title,
                    category = cat,
                    visibility = vis,
                    requiresAcknowledgment = reqAck,
                    contentSummary = summary,
                    fileUri = driveUri,
                    fileName = if (driveUri.isNotBlank()) "Google_Drive_Doc" else "Dokument",
                    fileSizeBytes = 0L
                )
                showUploadDialog = false
            }
        )
    }

    // 2. View/Preview Document Dialog
    docToPreview?.let { doc ->
        ViewDocumentPreviewDialog(
            doc = doc,
            isGerman = isGerman,
            strings = strings,
            onDismiss = { docToPreview = null },
            onProceedToSign = {
                val d = docToPreview
                docToPreview = null
                docToSign = d
            }
        )
    }

    // 3. Sign Document Dialog
    docToSign?.let { doc ->
        SignDocumentDialog(
            doc = doc,
            members = members,
            isGerman = isGerman,
            strings = strings,
            primaryAccentColor = primaryAccentColor,
            onDismiss = { docToSign = null },
            onConfirm = { memberId, signerName, signerEmail, sigType, touchPoints ->
                viewModel.signDocument(
                    documentId = doc.id,
                    memberId = memberId,
                    signerName = signerName,
                    signerEmail = signerEmail,
                    signatureType = sigType,
                    touchPathPointsJson = touchPoints
                )
                docToSign = null
            }
        )
    }

    // 4. View Signatures Dialog
    docSignaturesToView?.let { doc ->
        val docSigs = signatures.filter { it.documentId == doc.id }
        ViewSignaturesDialog(
            doc = doc,
            signatures = docSigs,
            isGerman = isGerman,
            strings = strings,
            onDismiss = { docSignaturesToView = null }
        )
    }

    // 5. Delete Document Confirmation Dialog
    docToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text(if (isGerman) "Dokument löschen?" else "Delete Document?") },
            text = {
                Text(
                    if (isGerman)
                        "Möchtest du '${doc.title}' und alle zugehörigen Unterschriften wirklich unwiderruflich löschen?"
                    else
                        "Do you really want to permanently delete '${doc.title}' and all its associated signatures?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteOrganizationDocument(doc.id)
                        docToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

// --- SUB-COMPONENTS ---

@Composable
private fun DocKpiCounter(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DocumentCard(
    doc: OrganizationDocumentEntity,
    signaturesCount: Int,
    primaryAccentColor: Color,
    strings: com.example.ui.i18n.Strings,
    isGerman: Boolean,
    onPreview: () -> Unit,
    onSign: () -> Unit,
    onViewSignatures: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryEmoji = when (doc.category.uppercase()) {
        "WAIVER" -> "⚠️"
        "RULES" -> "📜"
        "CONSTITUTION" -> "🏛️"
        "PROTOCOL" -> "📋"
        "EMERGENCY_PLAN" -> "🚨"
        "PRICE_LIST" -> "💶"
        "MAP" -> "🗺️"
        else -> "📄"
    }

    val visibilityColor = when (doc.visibility.uppercase()) {
        "PUBLIC" -> SeaGreen
        "ALL_MEMBERS" -> OceanPrimary
        "DIVE_LEADERS_ONLY" -> Color(0xFFF57C00)
        "ADMINS_ONLY" -> Color(0xFFD32F2F)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (doc.requiresAcknowledgment) Color(0xFFFFA000).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Emoji, Title, Mandatory Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = primaryAccentColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = categoryEmoji, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${doc.fileName} • ${(doc.fileSizeBytes / 1024)} KB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (GoogleDriveHelper.isGoogleDriveUrl(doc.fileUri)) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SeaGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "☁️ Google Drive",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SeaGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badges Row: Mandatory Tag & Visibility Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (doc.requiresAcknowledgment) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = strings.mandatoryBadge,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE65100),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Visibility Chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = visibilityColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, visibilityColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = doc.visibility,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = visibilityColor,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Signatures Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (signaturesCount > 0) SeaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onViewSignatures() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            if (signaturesCount > 0) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (signaturesCount > 0) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$signaturesCount ${strings.signaturesKpi}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (signaturesCount > 0) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Excerpt / Summary Quote
            if (doc.contentSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = doc.contentSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Open / Preview
                OutlinedButton(
                    onClick = onPreview,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(strings.viewDocAction, fontSize = 12.sp)
                }

                // Sign / Acknowledge
                Button(
                    onClick = onSign,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (doc.requiresAcknowledgment) Color(0xFFE65100) else primaryAccentColor
                    ),
                    modifier = Modifier.weight(1.2f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.signDocAction, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- DIALOG IMPLEMENTATIONS ---

@Composable
private fun UploadOrganizationDocumentDialog(
    isGerman: Boolean,
    strings: com.example.ui.i18n.Strings,
    primaryAccentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, visibility: String, requiresAck: Boolean, summary: String, fileUri: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("WAIVER") }
    var visibility by remember { mutableStateOf("ALL_MEMBERS") }
    var requiresAcknowledgment by remember { mutableStateOf(false) }
    var contentSummary by remember { mutableStateOf("") }
    var googleDriveLink by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = strings.uploadDocDialogTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.docTitleLabel) },
                    placeholder = { Text(if (isGerman) "z. B. Haftungsausschluss Saison 2026" else "e.g. Liability Waiver 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Google Drive Link Option
                OutlinedTextField(
                    value = googleDriveLink,
                    onValueChange = { googleDriveLink = it },
                    label = { Text("☁️ Google Drive Link (optional)") },
                    placeholder = { Text("https://drive.google.com/...") },
                    leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null, tint = primaryAccentColor) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        if (GoogleDriveHelper.isGoogleDriveUrl(googleDriveLink)) {
                            Text("✓ Google Drive Link erkannt", color = SeaGreen, fontWeight = FontWeight.Bold)
                        } else {
                            Text(if (isGerman) "Freigabelink zu PDF/Datei im Vereins-Drive" else "Link to file in club Google Drive")
                        }
                    }
                )

                // Category Selection
                Text(
                    text = strings.docCategoryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val cats = listOf("WAIVER", "RULES", "CONSTITUTION", "PROTOCOL")
                    cats.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Visibility Selection
                Text(
                    text = strings.docVisibilityLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val visList = listOf("PUBLIC", "ALL_MEMBERS", "DIVE_LEADERS_ONLY", "ADMINS_ONLY")
                    visList.forEach { vis ->
                        FilterChip(
                            selected = visibility == vis,
                            onClick = { visibility = vis },
                            label = { Text(vis.take(8), fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Mandatory Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requiresAcknowledgment = !requiresAcknowledgment }
                ) {
                    Checkbox(
                        checked = requiresAcknowledgment,
                        onCheckedChange = { requiresAcknowledgment = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.docMandatoryCheckInLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (requiresAcknowledgment) FontWeight.Bold else FontWeight.Normal,
                        color = if (requiresAcknowledgment) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                    )
                }

                OutlinedTextField(
                    value = contentSummary,
                    onValueChange = { contentSummary = it },
                    label = { Text(strings.docSummaryLabel) },
                    placeholder = { Text(if (isGerman) "Kernaussagen, Klauseln oder Bestimmungen..." else "Key clauses or terms...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, category, visibility, requiresAcknowledgment, contentSummary, googleDriveLink) },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
private fun ViewDocumentPreviewDialog(
    doc: OrganizationDocumentEntity,
    isGerman: Boolean,
    strings: com.example.ui.i18n.Strings,
    onDismiss: () -> Unit,
    onProceedToSign: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📄", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = doc.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "${doc.category} • ${doc.visibility}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isGerman) "Dokumenten-Metadaten:" else "Document Metadata:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dateiname: ${doc.fileName}\nGröße: ${(doc.fileSizeBytes / 1024)} KB\nHochgeladen von: ${doc.uploadedByName}\nErstellt: ${dateFormat.format(Date(doc.createdAt))}\nPflichtdokument: ${if (doc.requiresAcknowledgment) "JA ⚠️" else "Nein"}",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = if (isGerman) "Dokumenten-Inhalt / Auszug:" else "Document Content / Excerpt:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = doc.contentSummary.ifBlank {
                            if (isGerman) "Kein Volltextauszug hinterlegt. Das Dokument ist im lokalen Dokumententresor sicher archiviert."
                            else "No full text excerpt provided. Document securely archived in local vault."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (doc.fileUri.isNotBlank()) {
                    val context = LocalContext.current
                    val isDrive = GoogleDriveHelper.isGoogleDriveUrl(doc.fileUri)
                    Button(
                        onClick = {
                            GoogleDriveHelper.openInGoogleDriveOrBrowser(context, doc.fileUri)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDrive) SeaGreen else OceanPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(if (isDrive) Icons.Default.Cloud else Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDrive) "☁️ Auf Google Drive öffnen" else "📄 Datei öffnen",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onProceedToSign) {
                Text(strings.signDocAction)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}

@Composable
private fun SignDocumentDialog(
    doc: OrganizationDocumentEntity,
    members: List<OrganizationMemberEntity>,
    isGerman: Boolean,
    strings: com.example.ui.i18n.Strings,
    primaryAccentColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (memberId: String, signerName: String, signerEmail: String, sigType: String, touchPoints: String?) -> Unit
) {
    var selectedMemberId by remember { mutableStateOf(members.firstOrNull()?.id ?: "") }
    val selectedMember = members.find { it.id == selectedMemberId } ?: members.firstOrNull()

    var acceptedCheckbox by remember { mutableStateOf(false) }
    var useTouchSignature by remember { mutableStateOf(false) }
    val touchPoints = remember { mutableStateListOf<Offset>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✍️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = strings.signDocDialogTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryAccentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Member Selector
                Text(
                    text = if (isGerman) "Unterzeichnende Person:" else "Signing Diver / Member:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    members.forEach { m ->
                        FilterChip(
                            selected = selectedMemberId == m.id,
                            onClick = { selectedMemberId = m.id },
                            label = { Text(m.fullName, fontSize = 11.sp) }
                        )
                    }
                }

                // Document Terms Excerpt
                Text(
                    text = strings.signDocDialogInstruction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = doc.contentSummary.ifBlank {
                            if (isGerman)
                                "Hiermit bestätige ich die Einhaltung sämtlicher Sicherheitsrichtlinien, Tauchplatzregeln und Haftungsfreistellungen der Organisation."
                            else
                                "I hereby confirm compliance with all safety guidelines, site rules, and liability releases of the organization."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                // Signature Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGerman) "Touch-Unterschrift zeichnen:" else "Draw Touch Signature:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Checkbox(
                        checked = useTouchSignature,
                        onCheckedChange = { useTouchSignature = it }
                    )
                }

                // Interactive Touch Signature Canvas
                AnimatedVisibility(visible = useTouchSignature) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.5.dp, primaryAccentColor), RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        touchPoints.add(change.position)
                                    }
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                if (touchPoints.size > 1) {
                                    val path = Path()
                                    path.moveTo(touchPoints.first().x, touchPoints.first().y)
                                    for (i in 1 until touchPoints.size) {
                                        path.lineTo(touchPoints[i].x, touchPoints[i].y)
                                    }
                                    drawPath(
                                        path = path,
                                        color = Color(0xFF0D47A1),
                                        style = Stroke(width = 4f)
                                    )
                                }
                            }
                            if (touchPoints.isEmpty()) {
                                Text(
                                    text = if (isGerman) "Hier mit Finger unterschreiben..." else "Sign with finger here...",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                        if (touchPoints.isNotEmpty()) {
                            TextButton(
                                onClick = { touchPoints.clear() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(if (isGerman) "Unterschrift leeren" else "Clear signature", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Acknowledgment Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { acceptedCheckbox = !acceptedCheckbox }
                ) {
                    Checkbox(
                        checked = acceptedCheckbox,
                        onCheckedChange = { acceptedCheckbox = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.iAcceptCheckbox,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (acceptedCheckbox) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val m = selectedMember
                    if (m != null) {
                        val sigType = if (useTouchSignature && touchPoints.size > 2) "DIGITAL_SIGNATURE" else "CHECKBOX_CONFIRMED"
                        val touchJson = if (useTouchSignature && touchPoints.size > 2) {
                            "[${touchPoints.joinToString(",") { "{x:${it.x},y:${it.y}}" }}]"
                        } else null

                        onConfirm(m.id, m.fullName, m.email, sigType, touchJson)
                    }
                },
                enabled = acceptedCheckbox && selectedMember != null,
                colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
            ) {
                Text(strings.confirmSignatureButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
private fun ViewSignaturesDialog(
    doc: OrganizationDocumentEntity,
    signatures: List<DocumentSignatureEntity>,
    isGerman: Boolean,
    strings: com.example.ui.i18n.Strings,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = strings.signaturesListTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (signatures.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.noSignaturesYet,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(signatures, key = { it.id }) { sig ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sig.signerName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (sig.signatureType == "DIGITAL_SIGNATURE") SeaGreen.copy(alpha = 0.2f) else OceanPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = sig.signatureType,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (sig.signatureType == "DIGITAL_SIGNATURE") SeaGreen else OceanPrimary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = sig.signerEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${strings.signedAtLabel}: ${dateFormat.format(Date(sig.signedTimestamp))} • ${sig.deviceVerificationNote}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(strings.close)
            }
        }
    )
}
