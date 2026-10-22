package com.example.ui.screens.documents

import android.graphics.Bitmap
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Checkbox
import androidx.compose.ui.platform.LocalContext
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
import com.example.data.model.DiverDocumentEntity
import com.example.ui.components.RealisticMedicalStatementPaper
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.SeaGreen
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDocumentDialog(
    initialDocument: DiverDocumentEntity? = null,
    onSave: (
        title: String,
        category: String,
        documentNumber: String,
        issuingOrganization: String,
        issueDate: String,
        expiryDate: String,
        notes: String,
        cardColorHex: String,
        requiredApproverType: String,
        documentUri: String?,
        existingId: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val todayDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var title by remember { mutableStateOf(initialDocument?.title ?: "") }
    var category by remember { mutableStateOf(initialDocument?.category ?: "CERTIFICATION") }
    var documentNumber by remember { mutableStateOf(initialDocument?.documentNumber ?: "") }
    var issuingOrganization by remember { mutableStateOf(initialDocument?.issuingOrganization ?: "") }
    var issueDate by remember { mutableStateOf(initialDocument?.issueDate ?: todayDate) }
    var expiryDate by remember { mutableStateOf(initialDocument?.expiryDate ?: "Lifetime / No Expiration") }
    var notes by remember { mutableStateOf(initialDocument?.notes ?: "") }
    var requiredApproverType by remember { mutableStateOf(initialDocument?.requiredApproverType ?: "INSTRUCTOR") }
    var documentUri by remember { mutableStateOf(initialDocument?.documentUri) }
    var cardColorHex by remember {
        mutableStateOf(
            initialDocument?.cardColorHex ?: when (category) {
                "CERTIFICATION" -> "#006699"
                "MEDICAL_FORM" -> "#00875A"
                "INSURANCE" -> "#C93B2B"
                "LIABILITY_WAIVER" -> "#4A5568"
                "EQUIPMENT_RECORD" -> "#D97706"
                else -> "#005B60"
            }
        )
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var isLifetime by remember {
        mutableStateOf(expiryDate == "Lifetime / No Expiration" || expiryDate.lowercase().contains("lifetime"))
    }

    val context = LocalContext.current
    val calendar = remember { java.util.Calendar.getInstance() }

    val issueDatePicker = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                issueDate = formattedDate
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    val expiryDatePicker = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format(java.util.Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                expiryDate = formattedDate
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }

    // Camera Capture Launcher (No external storage required, returns Bitmap directly)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            documentUri = "data:image/jpeg;base64,$base64String"
        }
    }

    // Modern Photo Picker (Zero runtime storage permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            documentUri = uri.toString()
        }
    }

    // Generic Document / PDF File Picker
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            documentUri = uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(22.dp))
                .testTag("add_edit_doc_dialog"),
            shape = RoundedCornerShape(22.dp),
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
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Description,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (initialDocument == null) strings.addDocumentButton else strings.editDocumentTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_doc_btn")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = when (category) {
                            "CERTIFICATION" -> strings.categoryCertification
                            "MEDICAL_FORM" -> strings.categoryMedicalForm
                            "INSURANCE" -> strings.categoryInsurance
                            "LIABILITY_WAIVER" -> strings.categoryLiabilityWaiver
                            "EQUIPMENT_RECORD" -> strings.categoryEquipmentRecord
                            else -> strings.categoryCustom
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.documentCategoryLabel.ifBlank { "Dokumenten-Kategorie" }) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("doc_category_picker"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.categoryCertification) },
                            onClick = {
                                category = "CERTIFICATION"
                                requiredApproverType = "INSTRUCTOR"
                                cardColorHex = "#006699"
                                if (title.isBlank()) title = "PADI Advanced Open Water Diver"
                                if (issuingOrganization.isBlank()) issuingOrganization = "PADI Worldwide"
                                categoryDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.categoryMedicalForm) },
                            onClick = {
                                category = "MEDICAL_FORM"
                                requiredApproverType = "DOCTOR"
                                cardColorHex = "#00875A"
                                if (title.isBlank()) title = "RSTC Tauchtauglichkeit (TTU)"
                                if (issuingOrganization.isBlank()) issuingOrganization = "Dr. med. Weber (Tauchmediziner)"
                                categoryDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.categoryInsurance) },
                            onClick = {
                                category = "INSURANCE"
                                requiredApproverType = "AGENCY"
                                cardColorHex = "#C93B2B"
                                if (title.isBlank()) title = "DAN Tauchunfall-Versicherung"
                                if (issuingOrganization.isBlank()) issuingOrganization = "DAN Europe"
                                categoryDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.categoryLiabilityWaiver) },
                            onClick = {
                                category = "LIABILITY_WAIVER"
                                requiredApproverType = "DIVE_CENTER"
                                cardColorHex = "#4A5568"
                                if (title.isBlank()) title = "Haftungsausschluss & Taucherklärung"
                                if (issuingOrganization.isBlank()) issuingOrganization = "AquaLearn Akademie"
                                categoryDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.categoryEquipmentRecord) },
                            onClick = {
                                category = "EQUIPMENT_RECORD"
                                requiredApproverType = "DIVE_CENTER"
                                cardColorHex = "#D97706"
                                if (title.isBlank()) title = "Atemregler-Revision & TÜV Flaschenprüfung"
                                if (issuingOrganization.isBlank()) issuingOrganization = "Zertifizierter Tauch-Servicebetrieb"
                                categoryDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.categoryCustom) },
                            onClick = {
                                category = "CUSTOM"
                                requiredApproverType = "INSTRUCTOR"
                                cardColorHex = "#005B60"
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.documentTitleLabel) },
                    placeholder = { Text("z.B. Open Water Diver (18m) oder TTU-Attest") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Document / Card # Input
                OutlinedTextField(
                    value = documentNumber,
                    onValueChange = { documentNumber = it },
                    label = { Text(strings.documentNumberLabel) },
                    placeholder = { Text("e.g. OWD-948201 or MED-2026-99") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_number_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Issuing Body / Physician / Organization
                OutlinedTextField(
                    value = issuingOrganization,
                    onValueChange = { issuingOrganization = it },
                    label = { Text(strings.issuingOrgLabel) },
                    placeholder = { Text("e.g. PADI, SSI, Dr. Weber, DAN Europe") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_issuer_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Issue Date and Expiry Date Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Clickable Issue Date Box
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clickable { issueDatePicker.show() }
                    ) {
                        OutlinedTextField(
                            value = issueDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.issueDateLabel) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = "Datum auswählen",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Transparent)
                                .clickable { issueDatePicker.show() }
                        )
                    }

                    // Clickable Expiry Date Box
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .clickable { if (!isLifetime) expiryDatePicker.show() }
                    ) {
                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = {},
                            readOnly = true,
                            enabled = !isLifetime,
                            label = { Text(strings.expiryDateLabel) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = "Ablaufdatum auswählen",
                                    tint = if (isLifetime) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        if (!isLifetime) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color.Transparent)
                                    .clickable { expiryDatePicker.show() }
                            )
                        }
                    }
                }

                // Lifetime / No Expiration Checkbox Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isLifetime,
                        onCheckedChange = { checked ->
                            isLifetime = checked
                            if (checked) {
                                expiryDate = "Lifetime / No Expiration"
                            } else {
                                expiryDate = todayDate
                            }
                        },
                        modifier = Modifier.testTag("doc_lifetime_checkbox")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = strings.statusLifetime + " (Permanent)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // =========================================================================
                // PAPER STATEMENT / DOCUMENT SCAN UPLOAD & PHOTO CAPTURE SECTION
                // =========================================================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paper_scan_upload_section"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (category == "MEDICAL_FORM") SeaGreen.copy(alpha = 0.08f) else OceanPrimary.copy(alpha = 0.06f)
                    ),
                    border = BorderStroke(1.5.dp, if (category == "MEDICAL_FORM") SeaGreen.copy(alpha = 0.4f) else OceanPrimary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (category == "MEDICAL_FORM") SeaGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.DocumentScanner,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.uploadPaperScanTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (category == "MEDICAL_FORM") SeaGreen else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = strings.uploadPaperScanDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Modern, compact 3-way Attachment Selector Row (Non-stretched, well proportioned)
                        Text(
                            text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE) "Datei / Nachweis anhängen:" else "Attach Proof / File:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Kamera
                            OutlinedButton(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("doc_camera_capture_btn"),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.DE) "Kamera" else "Camera",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            // 2. Galerie
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("doc_gallery_pick_btn"),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.DE) "Galerie" else "Gallery",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }

                            // 3. PDF / Datei / Drive
                            OutlinedButton(
                                onClick = {
                                    filePickerLauncher.launch(
                                        arrayOf("application/pdf", "image/*", "*/*")
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("doc_file_pick_btn"),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(18.dp), tint = SeaGreen)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.DE) "PDF / Datei" else "PDF / File",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = {
                                documentUri = "sample_rstc_medical_statement"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("doc_use_sample_scan_btn")
                        ) {
                            Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, modifier = Modifier.size(16.dp), tint = SeaGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.useSampleMedicalScan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SeaGreen)
                        }

                        // Preview of Attached Scan if available
                        if (documentUri != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = strings.paperScanAttached,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SeaGreen
                                            )
                                        }

                                        IconButton(
                                            onClick = { documentUri = null },
                                            modifier = Modifier.size(24.dp).testTag("remove_scan_btn")
                                        ) {
                                            Icon(Icons.Rounded.Delete, contentDescription = strings.removeScan, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Render Thumbnail
                                    RealisticMedicalStatementPaper(
                                        document = DiverDocumentEntity(
                                            id = "preview",
                                            diverId = "preview",
                                            diverName = initialDocument?.diverName ?: "Alex Rivera",
                                            diverEmail = "",
                                            title = title.ifBlank { "Tauchtauglichkeit" },
                                            category = category,
                                            documentNumber = documentNumber.ifBlank { "MED-2026-99" },
                                            issuingOrganization = issuingOrganization.ifBlank { "Dr. Weber" },
                                            issueDate = issueDate,
                                            expiryDate = expiryDate,
                                            documentUri = documentUri
                                        ),
                                        isThumbnail = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Required Approver Role Selector
                Text(
                    text = strings.requiredApproverLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoleSelectButton(
                        title = "Arzt",
                        isSelected = requiredApproverType == "DOCTOR",
                        onClick = { requiredApproverType = "DOCTOR" },
                        modifier = Modifier.weight(1f)
                    )
                    RoleSelectButton(
                        title = "Tauchlehrer",
                        isSelected = requiredApproverType == "INSTRUCTOR",
                        onClick = { requiredApproverType = "INSTRUCTOR" },
                        modifier = Modifier.weight(1f)
                    )
                    RoleSelectButton(
                        title = "Tauchbasis",
                        isSelected = requiredApproverType == "DIVE_CENTER",
                        onClick = { requiredApproverType = "DIVE_CENTER" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen & Taucher-Bemerkungen") },
                    placeholder = { Text("z.B. Jährliche TTU ohne Einschränkungen bestanden, Nitrox freigegeben...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_notes_input"),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("cancel_add_doc_btn"),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text(strings.cancel)
                    }

                    Button(
                        onClick = {
                            onSave(
                                title,
                                category,
                                documentNumber.ifBlank { "DOC-${(1000..9999).random()}" },
                                issuingOrganization.ifBlank { "AquaLearn Academy" },
                                issueDate,
                                expiryDate,
                                notes,
                                cardColorHex,
                                requiredApproverType,
                                documentUri,
                                initialDocument?.id
                            )
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("save_doc_btn"),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.save, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleSelectButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("role_select_${title.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

