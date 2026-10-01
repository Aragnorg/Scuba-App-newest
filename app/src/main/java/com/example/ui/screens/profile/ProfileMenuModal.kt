package com.example.ui.screens.profile

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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PhotoLibrary
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiverDocumentEntity
import com.example.ui.components.DocumentExpirationAlertBanner
import com.example.ui.components.ExamReadinessGauge
import com.example.ui.components.LanguageSwitchButton
import com.example.ui.components.UserAvatarView
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.screens.documents.AddEditDocumentDialog
import com.example.ui.screens.documents.DocumentApprovalDialog
import com.example.ui.screens.documents.DocumentDetailDialog
import com.example.ui.screens.documents.DocumentVaultCard
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.OceanTertiary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import com.example.util.DocumentExpirationHelper
import java.io.ByteArrayOutputStream

/**
 * Full-featured Profile Option Menu & Personal Documents Vault Hub.
 * Opened from the top-left avatar button.
 */
@Composable
fun ProfileMenuModal(
    viewModel: AquaViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val categoryFilter by viewModel.selectedDocCategoryFilter.collectAsState()
    val searchQuery by viewModel.documentSearchQuery.collectAsState()
    val readiness by viewModel.examReadiness.collectAsState()
    val currentLanguage = LocalCurrentLanguage.current
    val strings = LocalAppStrings.current

    val diverName = currentUser?.fullName ?: userProgress?.studentName ?: "Diver Candidate"
    val diverEmail = currentUser?.email ?: "diver@example.com"
    val profilePicUri = currentUser?.profilePictureUri ?: userProgress?.profilePictureUri

    var showAddDocumentDialog by remember { mutableStateOf(false) }
    var documentToEdit by remember { mutableStateOf<DiverDocumentEntity?>(null) }
    var selectedDocForDetails by remember { mutableStateOf<DiverDocumentEntity?>(null) }
    var selectedDocForApproval by remember { mutableStateOf<DiverDocumentEntity?>(null) }
    var documentToDelete by remember { mutableStateOf<DiverDocumentEntity?>(null) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }

    // Camera Capture Launcher for Profile Picture
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            viewModel.updateProfilePicture("data:image/jpeg;base64,$base64String")
        }
    }

    // Photo Library Picker Launcher (Zero runtime permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateProfilePicture(uri.toString())
        }
    }

    val approvedCount = documents.count { it.approvalStatus == "APPROVED" }
    val pendingCount = documents.count { it.approvalStatus == "PENDING" }

    val alertDocuments = remember(documents, currentLanguage) {
        DocumentExpirationHelper.getAlertDocuments(documents, currentLanguage == AppLanguage.DE)
    }
    val alertDocsSet = remember(alertDocuments) {
        alertDocuments.map { it.first.id }.toSet()
    }

    val filteredDocs = documents.filter { doc ->
        val matchesCategory = when (categoryFilter) {
            "ALL" -> true
            "ALERTS" -> alertDocsSet.contains(doc.id)
            else -> doc.category == categoryFilter
        }
        val matchesSearch = searchQuery.isBlank() ||
                doc.title.contains(searchQuery, ignoreCase = true) ||
                doc.issuingOrganization.contains(searchQuery, ignoreCase = true) ||
                doc.documentNumber.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("profile_menu_modal"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = OceanPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.School, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.profileTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.personalDocumentsDesc,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LanguageSwitchButton(
                            currentLanguage = currentLanguage,
                            onLanguageChange = { viewModel.setLanguage(it) },
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_profile_modal_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = strings.close)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Diver Profile Card with Avatar Upload
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_diver_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Profile Avatar View
                                    UserAvatarView(
                                        photoUri = profilePicUri,
                                        displayName = diverName,
                                        size = 72.dp,
                                        borderWidth = 2.5.dp,
                                        borderColor = AquaCyan
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = diverName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            IconButton(
                                                onClick = {
                                                    editNameText = diverName
                                                    showEditNameDialog = true
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DriveFileRenameOutline,
                                                    contentDescription = "Edit Name",
                                                    tint = OceanPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = diverEmail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SeaGreen.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (currentUser?.role == "INSTRUCTOR") "Certified Instructor" else strings.diverStatusActive,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SeaGreen
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Avatar Actions Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { cameraLauncher.launch(null) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("avatar_take_photo_btn"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.takePhotoCamera, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("avatar_gallery_btn"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.uploadProfilePhoto, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (profilePicUri != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    TextButton(
                                        onClick = { viewModel.updateProfilePicture(null) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("remove_avatar_btn")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.removeProfilePicture, color = Color(0xFFE53E3E), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Diver Stats Counter Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatMiniCard(
                                title = strings.totalDocumentsCount,
                                count = documents.size.toString(),
                                icon = Icons.Default.Description,
                                color = OceanPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = strings.approvedDocumentsCount,
                                count = approvedCount.toString(),
                                icon = Icons.Default.Verified,
                                color = SeaGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = "Exam Readiness",
                                count = "$readiness%",
                                icon = Icons.Default.EmojiEvents,
                                color = SandGold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Section: Settings & Preferences
                    item {
                        val isDarkMode by viewModel.isDarkMode.collectAsState()
                        val isImperial by viewModel.isImperial.collectAsState()
                        var wifiOnly by remember { mutableStateOf(true) }
                        var dailyReminder by remember { mutableStateOf(true) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_settings_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.DE) "App-Einstellungen & Präferenzen" else "App Settings & Preferences",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Toggle Dark Mode
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.DarkMode,
                                            contentDescription = null,
                                            tint = OceanPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Dunkelmodus" else "Dark Mode",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Dunkles Farbschema aktivieren" else "Switch to dark oceanic colors",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isDarkMode,
                                        onCheckedChange = { viewModel.setDarkModeEnabled(it) },
                                        modifier = Modifier.testTag("toggle_dark_mode")
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Toggle Unit System (Metric / Imperial)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Straighten,
                                            contentDescription = null,
                                            tint = OceanPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Einheitensystem" else "Unit System",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (isImperial) {
                                                    if (currentLanguage == AppLanguage.DE) "Imperial (ft, PSI, °F, lbs)" else "Imperial (ft, PSI, °F, lbs)"
                                                } else {
                                                    if (currentLanguage == AppLanguage.DE) "Metrisch (m, bar, °C, kg)" else "Metric (m, bar, °C, kg)"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (currentLanguage == AppLanguage.DE) "Imperial" else "Imperial",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Switch(
                                            checked = isImperial,
                                            onCheckedChange = { viewModel.setImperialEnabled(it) },
                                            modifier = Modifier.testTag("toggle_unit_system")
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Language Switcher inside Settings
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = OceanPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Sprache" else "Language",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Deutsch (DE) aktiv" else "English (EN) active",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    LanguageSwitchButton(
                                        currentLanguage = currentLanguage,
                                        onLanguageChange = { viewModel.setLanguage(it) },
                                        modifier = Modifier.testTag("settings_language_btn")
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // WiFi Only Downloads Preference
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Wifi,
                                            contentDescription = null,
                                            tint = OceanPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Nur über WLAN laden" else "Wi-Fi Only Downloads",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Datenvolumen für Videos & Slides sparen" else "Save mobile data for offline material downloads",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = wifiOnly,
                                        onCheckedChange = { wifiOnly = it },
                                        modifier = Modifier.testTag("toggle_wifi_only")
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                // Daily Study Reminder Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = OceanPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Tägliche Lerneinstufung" else "Daily Study Reminder",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Mahnung zur Aufrechterhaltung der Streak" else "Notify to maintain daily learning streak",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = dailyReminder,
                                        onCheckedChange = { dailyReminder = it },
                                        modifier = Modifier.testTag("toggle_daily_reminder")
                                    )
                                }
                            }
                        }
                    }

                    // Section: Personal Documents & Certifications Vault
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.personalDocumentsSection,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${documents.size} saved documents",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    documentToEdit = null
                                    showAddDocumentDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("profile_add_doc_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addDocumentButton, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    // Expiration Alert Banner
                    if (alertDocuments.isNotEmpty()) {
                        item {
                            DocumentExpirationAlertBanner(
                                alertDocuments = alertDocuments,
                                onDocumentClick = { doc -> selectedDocForDetails = doc }
                            )
                        }
                    }

                    // Search & Category Filters
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setDocumentSearchQuery(it) },
                            placeholder = { Text("Search certifications, medical statement, waivers...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setDocumentSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_doc_search_bar"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                ProfileFilterChip(
                                    title = strings.filterAllDocs,
                                    isSelected = categoryFilter == "ALL",
                                    onClick = { viewModel.setDocumentCategoryFilter("ALL") }
                                )
                            }
                            if (alertDocuments.isNotEmpty()) {
                                item {
                                    ProfileFilterChip(
                                        title = "⚠️ ${strings.filterAlertsOnly} (${alertDocuments.size})",
                                        isSelected = categoryFilter == "ALERTS",
                                        onClick = { viewModel.setDocumentCategoryFilter("ALERTS") }
                                    )
                                }
                            }
                            item {
                                ProfileFilterChip(
                                    title = "📜 ${strings.filterCerts}",
                                    isSelected = categoryFilter == "CERTIFICATION",
                                    onClick = { viewModel.setDocumentCategoryFilter("CERTIFICATION") }
                                )
                            }
                            item {
                                ProfileFilterChip(
                                    title = "🩺 ${strings.filterMedical}",
                                    isSelected = categoryFilter == "MEDICAL_FORM",
                                    onClick = { viewModel.setDocumentCategoryFilter("MEDICAL_FORM") }
                                )
                            }
                            item {
                                ProfileFilterChip(
                                    title = "🛡️ ${strings.filterInsurance}",
                                    isSelected = categoryFilter == "INSURANCE",
                                    onClick = { viewModel.setDocumentCategoryFilter("INSURANCE") }
                                )
                            }
                            item {
                                ProfileFilterChip(
                                    title = "📋 ${strings.filterWaivers}",
                                    isSelected = categoryFilter == "LIABILITY_WAIVER",
                                    onClick = { viewModel.setDocumentCategoryFilter("LIABILITY_WAIVER") }
                                )
                            }
                        }
                    }

                    // Documents List
                    if (filteredDocs.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "🗂️", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = strings.noDocumentsFound,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = strings.noDocumentsFoundDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredDocs, key = { it.id }) { document ->
                            DocumentVaultCard(
                                document = document,
                                onOpenDetails = { selectedDocForDetails = document },
                                onOpenApproval = { selectedDocForApproval = document },
                                onEdit = {
                                    documentToEdit = document
                                    showAddDocumentDialog = true
                                },
                                onDelete = { documentToDelete = document }
                            )
                        }
                    }

                    // Sign Out & Danger Zone
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                viewModel.logout()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_logout_btn"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFE53E3E).copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.signOut, color = Color(0xFFE53E3E), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Add / Edit Document Dialog
    if (showAddDocumentDialog) {
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
                showAddDocumentDialog = false
                documentToEdit = null
            },
            onDismiss = {
                showAddDocumentDialog = false
                documentToEdit = null
            }
        )
    }

    // Document Details Dialog
    selectedDocForDetails?.let { doc ->
        DocumentDetailDialog(
            document = doc,
            onApproveClick = {
                selectedDocForDetails = null
                selectedDocForApproval = doc
            },
            onEditClick = {
                selectedDocForDetails = null
                documentToEdit = doc
                showAddDocumentDialog = true
            },
            onDeleteClick = {
                selectedDocForDetails = null
                documentToDelete = doc
            },
            onDismiss = { selectedDocForDetails = null }
        )
    }

    // Document Approval Dialog
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
                selectedDocForApproval = null
            },
            onReject = { documentId, notes ->
                viewModel.rejectDocument(documentId, notes)
                selectedDocForApproval = null
            },
            onDismiss = { selectedDocForApproval = null }
        )
    }

    // Delete Confirmation Dialog
    documentToDelete?.let { doc ->
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text(strings.delete) },
            text = { Text("${strings.deleteDocumentConfirm}\n\n\"${doc.title}\" (#${doc.documentNumber})") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDocument(doc.id)
                        documentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text(strings.updateStudentName) },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    label = { Text(strings.studentNameLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameText.isNotBlank()) {
                            viewModel.updateStudentName(editNameText.trim())
                        }
                        showEditNameDialog = false
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, fontWeight = FontWeight.Black, fontSize = 16.sp, color = color)
            Text(text = title, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun ProfileFilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) OceanPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}
