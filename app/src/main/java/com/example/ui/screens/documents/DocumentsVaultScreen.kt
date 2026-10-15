package com.example.ui.screens.documents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiverDocumentEntity
import com.example.ui.components.DocumentExpirationAlertBanner
import com.example.ui.components.DocumentExpirationBadge
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import com.example.util.DocumentExpirationHelper
import com.example.util.ExpirationAlertLevel

@Composable
fun DocumentsVaultScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.allDocuments.collectAsState()
    val categoryFilter by viewModel.selectedDocCategoryFilter.collectAsState()
    val statusFilter by viewModel.selectedDocStatusFilter.collectAsState()
    val searchQuery by viewModel.documentSearchQuery.collectAsState()
    val selectedDocForDetails by viewModel.selectedDocumentForDetails.collectAsState()
    val selectedDocForApproval by viewModel.selectedDocumentForApproval.collectAsState()
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current

    var showAddDialog by remember { mutableStateOf(false) }
    var documentToEdit by remember { mutableStateOf<DiverDocumentEntity?>(null) }
    var documentToDelete by remember { mutableStateOf<DiverDocumentEntity?>(null) }

    val alertDocuments = remember(documents, currentLanguage) {
        DocumentExpirationHelper.getAlertDocuments(documents, currentLanguage == AppLanguage.DE)
    }
    val alertDocsSet = remember(alertDocuments) {
        alertDocuments.map { it.first.id }.toSet()
    }

    // Filter documents
    val filteredDocs = documents.filter { doc ->
        val matchesCategory = when (categoryFilter) {
            "ALL" -> true
            "ALERTS" -> alertDocsSet.contains(doc.id)
            else -> doc.category == categoryFilter
        }
        val matchesStatus = (statusFilter == "ALL" || doc.approvalStatus == statusFilter)
        val matchesSearch = searchQuery.isBlank() ||
                doc.title.contains(searchQuery, ignoreCase = true) ||
                doc.issuingOrganization.contains(searchQuery, ignoreCase = true) ||
                doc.documentNumber.contains(searchQuery, ignoreCase = true) ||
                doc.approverName.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesStatus && matchesSearch
    }

    val approvedCount = documents.count { it.approvalStatus == "APPROVED" }
    val pendingCount = documents.count { it.approvalStatus == "PENDING" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    documentToEdit = null
                    showAddDialog = true
                },
                containerColor = OceanPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_document_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.addDocumentButton)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.addDocumentButton, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Vault Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_header_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = OceanPrimary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = OceanPrimary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.documentsVaultTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.documentsVaultSubtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Metric Counter Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCounterCard(
                                title = strings.totalDocumentsCount,
                                count = documents.size.toString(),
                                color = OceanPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            StatCounterCard(
                                title = strings.approvedDocumentsCount,
                                count = approvedCount.toString(),
                                color = SeaGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatCounterCard(
                                title = strings.pendingDocumentsCount,
                                count = pendingCount.toString(),
                                color = SandGold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Expiration Alert Banner
            if (alertDocuments.isNotEmpty()) {
                item {
                    DocumentExpirationAlertBanner(
                        alertDocuments = alertDocuments,
                        onDocumentClick = { doc -> viewModel.selectDocumentForDetails(doc) }
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setDocumentSearchQuery(it) },
                    placeholder = { Text("Zertifikate, TTU, Brevets durchsuchen...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Suchen") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setDocumentSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_search_bar"),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            title = strings.filterAllDocs,
                            isSelected = categoryFilter == "ALL",
                            onClick = { viewModel.setDocumentCategoryFilter("ALL") }
                        )
                    }
                    if (alertDocuments.isNotEmpty()) {
                        item {
                            FilterChip(
                                title = "⚠️ ${strings.filterAlertsOnly} (${alertDocuments.size})",
                                isSelected = categoryFilter == "ALERTS",
                                onClick = { viewModel.setDocumentCategoryFilter("ALERTS") }
                            )
                        }
                    }
                    item {
                        FilterChip(
                            title = "📜 ${strings.filterCerts}",
                            isSelected = categoryFilter == "CERTIFICATION",
                            onClick = { viewModel.setDocumentCategoryFilter("CERTIFICATION") }
                        )
                    }
                    item {
                        FilterChip(
                            title = "🩺 ${strings.filterMedical}",
                            isSelected = categoryFilter == "MEDICAL_FORM",
                            onClick = { viewModel.setDocumentCategoryFilter("MEDICAL_FORM") }
                        )
                    }
                    item {
                        FilterChip(
                            title = "🛡️ ${strings.filterInsurance}",
                            isSelected = categoryFilter == "INSURANCE",
                            onClick = { viewModel.setDocumentCategoryFilter("INSURANCE") }
                        )
                    }
                    item {
                        FilterChip(
                            title = "📝 ${strings.filterWaivers}",
                            isSelected = categoryFilter == "LIABILITY_WAIVER",
                            onClick = { viewModel.setDocumentCategoryFilter("LIABILITY_WAIVER") }
                        )
                    }
                    item {
                        FilterChip(
                            title = "🤿 ${strings.filterEquipment}",
                            isSelected = categoryFilter == "EQUIPMENT_RECORD",
                            onClick = { viewModel.setDocumentCategoryFilter("EQUIPMENT_RECORD") }
                        )
                    }
                }
            }

            // Documents List
            if (filteredDocs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .testTag("empty_docs_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📂", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.noDocumentsFound,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.noDocumentsFoundDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredDocs, key = { it.id }) { document ->
                    DocumentVaultCard(
                        document = document,
                        onOpenDetails = { viewModel.selectDocumentForDetails(document) },
                        onOpenApproval = { viewModel.selectDocumentForApproval(document) },
                        onEdit = {
                            documentToEdit = document
                            showAddDialog = true
                        },
                        onDelete = { documentToDelete = document }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog) {
        AddEditDocumentDialog(
            initialDocument = documentToEdit,
            onSave = { title, category, documentNumber, issuingOrganization, issueDate, expiryDate, notes, cardColorHex, requiredApproverType, documentUri, existingId ->
                viewModel.saveDocument(
                    title = title,
                    category = category,
                    documentNumber = documentNumber,
                    issuingOrganization = issuingOrganization,
                    issueDate = issueDate,
                    expiryDate = expiryDate,
                    notes = notes,
                    cardColorHex = cardColorHex,
                    requiredApproverType = requiredApproverType,
                    documentUri = documentUri,
                    existingId = existingId
                )
                showAddDialog = false
                documentToEdit = null
            },
            onDismiss = {
                showAddDialog = false
                documentToEdit = null
            }
        )
    }

    // Detail Dialog
    selectedDocForDetails?.let { doc ->
        DocumentDetailDialog(
            document = doc,
            onApproveClick = {
                viewModel.selectDocumentForDetails(null)
                viewModel.selectDocumentForApproval(doc)
            },
            onEditClick = {
                viewModel.selectDocumentForDetails(null)
                documentToEdit = doc
                showAddDialog = true
            },
            onDeleteClick = {
                viewModel.selectDocumentForDetails(null)
                documentToDelete = doc
            },
            onDismiss = { viewModel.selectDocumentForDetails(null) }
        )
    }

    // Approval Dialog
    selectedDocForApproval?.let { doc ->
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
            },
            onReject = { documentId, notes ->
                viewModel.rejectDocument(documentId, notes)
            },
            onDismiss = { viewModel.selectDocumentForApproval(null) }
        )
    }

    // Delete Confirmation
    documentToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text("Dokument löschen") },
            text = { Text("Möchtest du das Dokument '${doc.title}' wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDocument(doc.id)
                        documentToDelete = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { documentToDelete = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun DocumentVaultCard(
    document: DiverDocumentEntity,
    onOpenDetails: () -> Unit,
    onOpenApproval: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val currentLanguage = LocalCurrentLanguage.current
    val expirationInfo = remember(document, currentLanguage) {
        DocumentExpirationHelper.computeExpirationInfo(document, currentLanguage == AppLanguage.DE)
    }
    val isApproved = document.approvalStatus == "APPROVED"
    val isExpired = expirationInfo.level == ExpirationAlertLevel.EXPIRED
    val isExpiringSoon = expirationInfo.level == ExpirationAlertLevel.EXPIRING_SOON

    val baseColor = when {
        isExpired -> Color(0xFFE53E3E)
        document.category == "CERTIFICATION" -> OceanPrimary
        document.category == "MEDICAL_FORM" -> SeaGreen
        document.category == "INSURANCE" -> Color(0xFFC93B2B)
        document.category == "LIABILITY_WAIVER" -> Color(0xFF4A5568)
        document.category == "EQUIPMENT_RECORD" -> Color(0xFFD97706)
        else -> Color(0xFF006699)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
            .testTag("doc_card_${document.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            if (isExpired) 1.5.dp else 1.dp,
            if (isExpired) Color(0xFFE53E3E) else if (isExpiringSoon) SandGold else baseColor.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Card Top Banner Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                baseColor,
                                DepthNavy
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (document.category) {
                                "CERTIFICATION" -> Icons.Default.School
                                "MEDICAL_FORM" -> Icons.Default.LocalHospital
                                "INSURANCE" -> Icons.Default.Shield
                                "LIABILITY_WAIVER" -> Icons.Default.Description
                                else -> Icons.Default.Badge
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = document.issuingOrganization.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isApproved) SeaGreen.copy(alpha = 0.25f) else SandGold.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, if (isApproved) Color(0xFF68D391) else SandGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isApproved) Icons.Default.Verified else Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = if (isApproved) Color(0xFF68D391) else SandGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isApproved) "VERIFIED" else "PENDING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isApproved) Color(0xFF68D391) else SandGold
                                )
                            }
                        }
                    }
                }
            }

            // Card Body Content
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DocumentExpirationBadge(expirationInfo = expirationInfo)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ID: ${document.documentNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Expiry: ${document.expiryDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isExpired) Color(0xFFE53E3E) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isExpired) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (isExpired || isExpiringSoon) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isExpired) Color(0xFFE53E3E).copy(alpha = 0.1f) else SandGold.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (isExpired) Color(0xFFE53E3E).copy(alpha = 0.3f) else SandGold.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isExpired) "⛔" else "⚠️",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = expirationInfo.detailStatusText,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isExpired) Color(0xFFE53E3E) else Color(0xFFB7791F),
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Approval Status Chip & Details
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isApproved) SeaGreen.copy(alpha = 0.1f) else SandGold.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isApproved) Icons.Default.CheckCircle else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (isApproved) SeaGreen else SandGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isApproved) "Freigegeben von ${document.approverName}" else "Freigabe erforderlich: ${document.requiredApproverType.lowercase().replace("_", " ")}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isApproved) SeaGreen else SandGold,
                                maxLines = 1
                            )
                        }

                        if (!isApproved) {
                            Text(
                                text = "Jetzt prüfen",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenDetails,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("doc_details_btn_${document.id}"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Details", fontSize = 12.sp)
                    }

                    if (!isApproved) {
                        Button(
                            onClick = onOpenApproval,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("doc_approve_btn_${document.id}"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Freigeben", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCounterCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("filter_${title.filter { it.isLetterOrDigit() }.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) OceanPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
