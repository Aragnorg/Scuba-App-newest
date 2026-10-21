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
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CorporateFare
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.model.UserAccountEntity
import com.example.ui.components.CreateOrganizationDialog
import com.example.ui.components.DocumentExpirationAlertBanner
import com.example.ui.components.EditOrganizationDialog
import com.example.ui.components.ExamReadinessGauge
import com.example.ui.components.LanguageSwitchButton
import com.example.ui.components.ScubaCompactButton
import com.example.ui.components.ScubaPrimaryButton
import com.example.ui.components.ScubaSecondaryButton
import com.example.ui.components.UserAvatarView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableIntStateOf
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.screens.documents.AddEditDocumentDialog
import com.example.ui.screens.documents.DocumentApprovalDialog
import com.example.ui.screens.documents.DocumentDetailDialog
import com.example.ui.screens.documents.DocumentVaultCard
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
// import com.example.ui.theme.OceanPrimary
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

    val isInstructor = currentUser?.role == "INSTRUCTOR"
    val diverName = if (isInstructor) {
        currentUser?.fullName?.takeIf { it.isNotBlank() } ?: "Adrian Scheid"
    } else {
        currentUser?.fullName?.takeIf { it.isNotBlank() }
            ?: userProgress?.studentName?.takeIf { it.isNotBlank() }
            ?: if (currentLanguage == AppLanguage.DE) "Leo Chen" else "Leo Chen"
    }
    val diverEmail = if (isInstructor) {
        currentUser?.email?.takeIf { it.isNotBlank() } ?: "adrian.scheid01@gmail.com"
    } else {
        currentUser?.email?.takeIf { it.isNotBlank() } ?: "leo.chen@diver.com"
    }
    val profilePicUri = currentUser?.profilePictureUri ?: userProgress?.profilePictureUri

    var showAddDocumentDialog by remember { mutableStateOf(false) }
    var documentToEdit by remember { mutableStateOf<DiverDocumentEntity?>(null) }
    var selectedDocForDetails by remember { mutableStateOf<DiverDocumentEntity?>(null) }
    var selectedDocForApproval by remember { mutableStateOf<DiverDocumentEntity?>(null) }
    var documentToDelete by remember { mutableStateOf<DiverDocumentEntity?>(null) }

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var showEditFullProfileDialog by remember { mutableStateOf(false) }
    var showCreateOrgDialog by remember { mutableStateOf(false) }
    var showEditOrgDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

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
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
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
                                    fontSize = 11.sp,
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
                                Icon(Icons.Rounded.Close, contentDescription = strings.close)
                            }
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
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
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
                                                    Icons.Rounded.DriveFileRenameOutline,
                                                    contentDescription = if (currentLanguage == AppLanguage.DE) "Name bearbeiten" else "Edit Name",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = diverEmail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Role Badge: Angemeldet als: [Role]
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isInstructor) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else SeaGreen.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, if (isInstructor) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f) else SeaGreen.copy(alpha = 0.45f)),
                                            modifier = Modifier.testTag("logged_in_role_badge")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isInstructor) Icons.Rounded.Verified else Icons.Rounded.School,
                                                    contentDescription = null,
                                                    tint = if (isInstructor) MaterialTheme.colorScheme.primary else SeaGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.DE) {
                                                        if (isInstructor) "Angemeldet als: Tauchlehrer / Master Instructor" else "Angemeldet als: TauchschÃ¼ler"
                                                    } else {
                                                        if (isInstructor) "Signed in as: Instructor / Master Instructor" else "Signed in as: Diving Student"
                                                    },
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isInstructor) MaterialTheme.colorScheme.primary else SeaGreen
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

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
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    ) {
                                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
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
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
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
                                        Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(strings.removeProfilePicture, color = Color(0xFFE53E3E), fontSize = 11.sp)
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                // Diver Brevet & Credentials Dossier
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = if (currentLanguage == AppLanguage.DE) "PersÃ¶nliche Taucher-Akte" else "Personal Diver Dossier",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    val certAgencyDisplay = currentUser?.certAgency?.takeIf { it.isNotBlank() } ?: "VDST / CMAS"
                                    val certLevelDisplay = if (isInstructor) {
                                        currentUser?.instructorLevel?.takeIf { it.isNotBlank() } ?: currentUser?.certLevel?.takeIf { it.isNotBlank() } ?: "Tauchlehrer ** (CMAS Instructor **)"
                                    } else {
                                        currentUser?.certLevel?.takeIf { it.isNotBlank() } ?: "Open Water Diver (18m)"
                                    }
                                    val licenseNumberDisplay = if (isInstructor) {
                                        currentUser?.instructorLicenseNumber?.takeIf { it.isNotBlank() } ?: currentUser?.licenseNumber?.takeIf { it.isNotBlank() } ?: "TL2-GER-9921"
                                    } else {
                                        currentUser?.licenseNumber?.takeIf { it.isNotBlank() } ?: "SSI-DIV-5512"
                                    }
                                    val ttuExpiryDisplay = currentUser?.ttuExpiryDate?.takeIf { it.isNotBlank() } ?: (if (isInstructor) "2027-05-20" else "2027-08-15")
                                    val ttuDoctorDisplay = currentUser?.ttuDoctorName?.takeIf { it.isNotBlank() } ?: (if (isInstructor) "Dr. med. Thomas Weber" else "Dr. med. Frank Meyer")
                                    val emergencyNameDisplay = currentUser?.emergencyContactName?.takeIf { it.isNotBlank() } ?: (if (isInstructor) "Monika Scheid" else "Sarah Chen")
                                    val emergencyPhoneDisplay = currentUser?.emergencyContactPhone?.takeIf { it.isNotBlank() } ?: (if (isInstructor) "+49 171 9876543" else "+49 176 1122334")

                                    // Row 1: Brevet & Dives
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.DE) "Brevet & Verband" else "Certification & Agency",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "$certAgencyDisplay â€¢ $certLevelDisplay",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(text = "Nr: $licenseNumberDisplay", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = SeaGreen.copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.2f)),
                                            modifier = Modifier.weight(0.7f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.DE) "Geloggte Dives" else "Logged Dives",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "ðŸ¤¿ ${currentUser?.totalLoggedDives ?: if (isInstructor) 340 else 14} Dives",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SeaGreen
                                                )
                                            }
                                        }
                                    }

                                    // Row 2: TTU Medical & Insurance
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF38A169).copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, Color(0xFF38A169).copy(alpha = 0.25f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.DE) "TTU Attest" else "Medical Statement",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "ðŸ©º $ttuExpiryDisplay",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32)
                                                )
                                                Text(text = ttuDoctorDisplay, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (currentLanguage == AppLanguage.DE) "Tauchversicherung" else "Diving Insurance",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "ðŸ›¡ï¸ ${currentUser?.insuranceProvider?.takeIf { it.isNotBlank() } ?: "DAN Europe"}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                val insNum = currentUser?.insuranceNumber?.takeIf { it.isNotBlank() } ?: "DAN-DE-449102"
                                                Text(text = "Nr: $insNum", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    // Row 3: Emergency contact & Rental preferences
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "ðŸš¨ Notfallkontakt: $emergencyNameDisplay ($emergencyPhoneDisplay)",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "ðŸŽ½ Leih-GrÃ¶ÃŸen: Anzug ${currentUser?.suitSize?.takeIf { it.isNotBlank() } ?: if (isInstructor) "L (52)" else "M"} â€¢ Schuhgr. ${currentUser?.shoeSize?.takeIf { it.isNotBlank() } ?: if (isInstructor) "43" else "42"} â€¢ BCD ${currentUser?.bcdSize?.takeIf { it.isNotBlank() } ?: "M"} â€¢ ${currentUser?.preferredTank?.takeIf { it.isNotBlank() } ?: "12L Stahl DIN"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // If Instructor / Leadership: Show Instructor Badge details
                                    if (currentUser?.role == "INSTRUCTOR") {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("ðŸ¢", fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Leitungs- & Ausbilder-Akte",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Text(
                                                    text = "${currentUser?.instructorLevel ?: "Tauchlehrer ** (CMAS Instructor **)"} (${currentUser?.instructorAgency ?: "VDST / CMAS"})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "Lizenz: ${currentUser?.instructorLicenseNumber ?: "TL2-GER-9921"} â€¢ GÃ¼ltig bis: ${currentUser?.instructorValidUntil ?: "2027-12-31"}",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                Text(
                                                    text = "Lehrbefugnisse: ${currentUser?.parseTeachingSpecialties()?.joinToString(", ") ?: "Nitrox, Tief, Trocken, Wrack"}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action Buttons Row: Compact & Balanced
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ScubaSecondaryButton(
                                            text = if (currentLanguage == AppLanguage.DE) "Profil bearbeiten" else "Edit Profile",
                                            icon = Icons.Rounded.Edit,
                                            onClick = { showEditFullProfileDialog = true },
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                            modifier = Modifier.weight(1f),
                                            testTag = "edit_full_profile_btn"
                                        )

                                        ScubaSecondaryButton(
                                            text = if (currentLanguage == AppLanguage.DE) "Datei anhÃ¤ngen" else "Attach File",
                                            icon = Icons.Rounded.AttachFile,
                                            onClick = {
                                                documentToEdit = null
                                                showAddDocumentDialog = true
                                            },
                                            modifier = Modifier.weight(1f),
                                            testTag = "profile_quick_add_doc_btn"
                                        )
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
                                icon = Icons.Rounded.Description,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = strings.approvedDocumentsCount,
                                count = approvedCount.toString(),
                                icon = Icons.Rounded.Verified,
                                color = SeaGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatMiniCard(
                                title = "Exam Readiness",
                                count = "$readiness%",
                                icon = Icons.Rounded.EmojiEvents,
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

                        // Profile / Role Switcher Card (Diver vs Leadership/Orga)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .testTag("profile_role_switcher_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentUser?.role == "INSTRUCTOR") MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else SeaGreen.copy(alpha = 0.08f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (currentUser?.role == "INSTRUCTOR") MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else SeaGreen.copy(alpha = 0.35f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (currentLanguage == AppLanguage.DE) "Aktives Profil:" else "Active Profile:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (currentUser?.role == "INSTRUCTOR") strings.instructorLabel else strings.studentLabel,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (currentUser?.role == "INSTRUCTOR") MaterialTheme.colorScheme.primary else SeaGreen
                                        )
                                    }
                                    ScubaCompactButton(
                                        text = if (currentUser?.role == "INSTRUCTOR") {
                                            if (currentLanguage == AppLanguage.DE) "Zu Taucher wechseln" else "Switch to Diver"
                                        } else {
                                            if (currentLanguage == AppLanguage.DE) "Zu Leitung wechseln" else "Switch to Leadership"
                                        },
                                        onClick = {
                                            val targetRole = if (currentUser?.role == "INSTRUCTOR") "STUDENT" else "INSTRUCTOR"
                                            viewModel.switchUserRole(targetRole)
                                            onDismiss()
                                        },
                                        containerColor = if (currentUser?.role == "INSTRUCTOR") SeaGreen else MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (currentUser?.role == "INSTRUCTOR") {
                                        if (currentLanguage == AppLanguage.DE)
                                            "Leitungs- & Vereinsmodus: Voller Zugriff auf Basis- & Vereinsverwaltung (DiveOps), Material, TÃœV, Ausfahrten & SchÃ¼ler."
                                        else
                                            "Leadership & Club mode: Full access to Dive Center & Club operations (DiveOps), assets, TÃœV, trips & students."
                                    } else {
                                        if (currentLanguage == AppLanguage.DE)
                                            "Standard-Taucherprofil: Fokus auf persÃ¶nliches Logbuch, eigene Kurse, Brevets und TTU-Akte."
                                        else
                                            "Standard diver profile: Focus on personal logbook, enrolled courses, certifications and medical file."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Organization & Club Quick Access Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .testTag("profile_org_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                val currentOrg by viewModel.currentOrganization.collectAsState()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.CorporateFare, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.DE) "Tauchorganisation (Verein / Basis)" else "Diving Organization (Club / Center)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = if (currentOrg != null) {
                                        if (currentLanguage == AppLanguage.DE)
                                            "Aktiver Workspace: ${currentOrg!!.name} (${if (currentOrg!!.isClub) "Verein" else "Tauchbasis"}) â€¢ ${currentOrg!!.activeMembersCount} Personen"
                                        else
                                            "Active workspace: ${currentOrg!!.name} (${if (currentOrg!!.isClub) "Club" else "Dive Center"})"
                                    } else {
                                        if (currentLanguage == AppLanguage.DE)
                                            "Aktuell im persÃ¶nlichen Tauchermodus. Du kannst jederzeit einen Verein oder eine Tauchbasis anlegen oder verwalten."
                                        else
                                            "Currently in personal diver mode. You can create or manage a club or dive center anytime."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ScubaSecondaryButton(
                                        text = if (currentLanguage == AppLanguage.DE) "+ Neue Org" else "+ New Org",
                                        onClick = { showCreateOrgDialog = true },
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (currentOrg != null) {
                                        ScubaSecondaryButton(
                                            text = if (currentLanguage == AppLanguage.DE) "Org bearbeiten" else "Edit Org",
                                            onClick = { showEditOrgDialog = true },
                                            containerColor = if (currentOrg!!.isClub) SeaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = if (currentOrg!!.isClub) SeaGreen else MaterialTheme.colorScheme.primary,
                                            border = BorderStroke(1.dp, if (currentOrg!!.isClub) SeaGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_settings_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.DE) "App-Einstellungen & PrÃ¤ferenzen" else "App Settings & Preferences",
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
                                            imageVector = Icons.Rounded.DarkMode,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
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
                                            imageVector = Icons.Rounded.Straighten,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
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
                                                    if (currentLanguage == AppLanguage.DE) "Imperial (ft, PSI, Â°F, lbs)" else "Imperial (ft, PSI, Â°F, lbs)"
                                                } else {
                                                    if (currentLanguage == AppLanguage.DE) "Metrisch (m, bar, Â°C, kg)" else "Metric (m, bar, Â°C, kg)"
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
                                            imageVector = Icons.Rounded.Language,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
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
                                            imageVector = Icons.Rounded.Wifi,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Nur Ã¼ber WLAN laden" else "Wi-Fi Only Downloads",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "Datenvolumen fÃ¼r Videos & Slides sparen" else "Save mobile data for offline material downloads",
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
                                            imageVector = Icons.Rounded.Notifications,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (currentLanguage == AppLanguage.DE) "TÃ¤gliche Lerneinstufung" else "Daily Study Reminder",
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
                                        text = if (currentLanguage == AppLanguage.DE) "${documents.size} Dokumente hinterlegt" else "${documents.size} saved documents",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ScubaCompactButton(
                                    text = strings.addDocumentButton,
                                    icon = Icons.Rounded.Add,
                                    onClick = {
                                        documentToEdit = null
                                        showAddDocumentDialog = true
                                    },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    testTag = "profile_add_doc_btn"
                                )
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
                            placeholder = {
                                Text(if (currentLanguage == AppLanguage.DE) "Zertifikate, TTU, Brevets durchsuchen..." else "Search certifications, medical statement, waivers...")
                            },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = if (currentLanguage == AppLanguage.DE) "Suchen" else "Search")
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setDocumentSearchQuery("") }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = if (currentLanguage == AppLanguage.DE) "LÃ¶schen" else "Clear")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_doc_search_bar"),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
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
                                        title = "âš ï¸ ${strings.filterAlertsOnly} (${alertDocuments.size})",
                                        isSelected = categoryFilter == "ALERTS",
                                        onClick = { viewModel.setDocumentCategoryFilter("ALERTS") }
                                    )
                                }
                            }
                            item {
                                ProfileFilterChip(
                                    title = "ðŸ“œ ${strings.filterCerts}",
                                    isSelected = categoryFilter == "CERTIFICATION",
                                    onClick = { viewModel.setDocumentCategoryFilter("CERTIFICATION") }
                                )
                            }
                            item {
                                ProfileFilterChip(
                                    title = "ðŸ©º ${strings.filterMedical}",
                                    isSelected = categoryFilter == "MEDICAL_FORM",
                                    onClick = { viewModel.setDocumentCategoryFilter("MEDICAL_FORM") }
                                )
                            }
                            item {
                                ProfileFilterChip(
                                    title = "ðŸ›¡ï¸ ${strings.filterInsurance}",
                                    isSelected = categoryFilter == "INSURANCE",
                                    onClick = { viewModel.setDocumentCategoryFilter("INSURANCE") }
                                )
                            }
                            item {
                                ProfileFilterChip(
                                    title = "ðŸ“‹ ${strings.filterWaivers}",
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
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "ðŸ—‚ï¸", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = strings.noDocumentsFound,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = strings.noDocumentsFoundDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
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
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            ScubaSecondaryButton(
                                text = if (currentLanguage == AppLanguage.DE) "Abmelden" else strings.signOut,
                                icon = Icons.AutoMirrored.Rounded.Logout,
                                onClick = { showLogoutConfirmDialog = true },
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.error,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                                modifier = Modifier.testTag("profile_logout_btn")
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
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
            shape = RoundedCornerShape(22.dp),
            title = { Text(strings.delete) },
            text = { Text("${strings.deleteDocumentConfirm}\n\n\"${doc.title}\" (#${doc.documentNumber})") },
            confirmButton = {
                ScubaSecondaryButton(
                    text = strings.delete,
                    onClick = {
                        viewModel.deleteDocument(doc.id)
                        documentToDelete = null
                    },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    border = null
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { documentToDelete = null },
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            shape = RoundedCornerShape(22.dp),
            title = { Text(strings.updateStudentName) },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    label = { Text(strings.studentNameLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )
            },
            confirmButton = {
                ScubaPrimaryButton(
                    text = strings.save,
                    onClick = {
                        if (editNameText.isNotBlank()) {
                            val newName = editNameText.trim()
                            val current = currentUser
                            if (current != null) {
                                viewModel.updateFullUserProfile(current.copy(fullName = newName))
                            } else {
                                viewModel.updateStudentName(newName)
                            }
                        }
                        showEditNameDialog = false
                    },
                    modifier = Modifier.width(120.dp)
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false },
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    val currentOrg by viewModel.currentOrganization.collectAsState()

    // Full Profile Edit Dialog
    if (showEditFullProfileDialog) {
        val userToEdit = currentUser ?: UserAccountEntity(
            id = if (isInstructor) "usr_adrian_instructor" else "usr_leo_student",
            email = diverEmail,
            fullName = diverName,
            role = if (isInstructor) "INSTRUCTOR" else "STUDENT",
            phoneNumber = if (isInstructor) "+49 170 1234567" else "+49 176 9988776",
            certLevel = if (isInstructor) "CMAS *** / PADI OWSI" else "Open Water Diver (18m)",
            certAgency = if (isInstructor) "VDST / CMAS" else "SSI",
            licenseNumber = if (isInstructor) "TL2-GER-9921" else "SSI-DIV-5512",
            totalLoggedDives = if (isInstructor) 340 else 14,
            ttuExpiryDate = if (isInstructor) "2027-05-20" else "2027-08-15",
            ttuDoctorName = if (isInstructor) "Dr. med. Thomas Weber" else "Dr. med. Frank Meyer",
            emergencyContactName = if (isInstructor) "Monika Scheid" else "Sarah Chen",
            emergencyContactPhone = if (isInstructor) "+49 171 9876543" else "+49 176 1122334",
            insuranceProvider = "DAN Europe",
            insuranceNumber = "DAN-DE-449102",
            suitSize = if (isInstructor) "L (52)" else "M",
            shoeSize = if (isInstructor) "43" else "42",
            bcdSize = "M",
            preferredTank = "12L Stahl DIN",
            instructorAgency = "VDST / CMAS",
            instructorLicenseNumber = "TL2-GER-9921",
            instructorLevel = "Tauchlehrer ** (CMAS Instructor **)",
            teachingSpecialtiesJson = "[\"Nitrox EANx\", \"Tieftauchen\", \"Trockentauchen\", \"Wracktauchen\", \"HLW / O2-Provider\"]",
            instructorValidUntil = "2027-12-31",
            homeOrganizationName = "Tauchclub Delphin Bodensee e.V."
        )
        EditFullProfileDialog(
            user = userToEdit,
            onSave = { updated ->
                viewModel.updateFullUserProfile(updated)
                showEditFullProfileDialog = false
            },
            onDismiss = { showEditFullProfileDialog = false }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        tint = Color(0xFFE53E3E),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (currentLanguage == AppLanguage.DE) "Abmelden bestÃ¤tigen" else "Confirm Sign Out",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = if (currentLanguage == AppLanguage.DE)
                        "MÃ¶chtest du dich wirklich abmelden? Deine aktive Sitzung wird beendet und du wirst zur Anmeldeseite weitergeleitet."
                    else
                        "Are you sure you want to sign out? Your session will end and you will be returned to the sign-in screen.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                ScubaSecondaryButton(
                    text = if (currentLanguage == AppLanguage.DE) "Abmelden" else strings.signOut,
                    onClick = {
                        showLogoutConfirmDialog = false
                        onDismiss()
                        viewModel.logout()
                    },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    border = null,
                    modifier = Modifier.testTag("confirm_logout_btn")
                )
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutConfirmDialog = false },
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("cancel_logout_btn")
                ) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showCreateOrgDialog) {
        CreateOrganizationDialog(
            viewModel = viewModel,
            onDismiss = { showCreateOrgDialog = false }
        )
    }

    if (showEditOrgDialog && currentOrg != null) {
        EditOrganizationDialog(
            organization = currentOrg!!,
            onSave = { updated -> viewModel.updateOrganizationDetails(updated) },
            onDismiss = { showEditOrgDialog = false }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, fontWeight = FontWeight.Black, fontSize = 16.sp, color = color)
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp
        )
    }
}

@Composable
fun EditFullProfileDialog(
    user: UserAccountEntity,
    onSave: (UserAccountEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var email by remember { mutableStateOf(user.email) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var certLevel by remember { mutableStateOf(user.certLevel) }
    var certAgency by remember { mutableStateOf(user.certAgency) }
    var licenseNumber by remember { mutableStateOf(user.licenseNumber) }
    var totalLoggedDives by remember { mutableStateOf(user.totalLoggedDives.toString()) }
    var ttuExpiryDate by remember { mutableStateOf(user.ttuExpiryDate) }
    var ttuDoctorName by remember { mutableStateOf(user.ttuDoctorName) }
    var emergencyContactName by remember { mutableStateOf(user.emergencyContactName) }
    var emergencyContactPhone by remember { mutableStateOf(user.emergencyContactPhone) }
    var insuranceProvider by remember { mutableStateOf(user.insuranceProvider) }
    var insuranceNumber by remember { mutableStateOf(user.insuranceNumber) }
    var suitSize by remember { mutableStateOf(user.suitSize) }
    var shoeSize by remember { mutableStateOf(user.shoeSize) }
    var bcdSize by remember { mutableStateOf(user.bcdSize) }
    var preferredTank by remember { mutableStateOf(user.preferredTank) }

    // Instructor fields
    var instructorAgency by remember { mutableStateOf(user.instructorAgency) }
    var instructorLicenseNumber by remember { mutableStateOf(user.instructorLicenseNumber) }
    var instructorLevel by remember { mutableStateOf(user.instructorLevel) }
    var teachingSpecialtiesJson by remember { mutableStateOf(user.teachingSpecialtiesJson) }
    var instructorValidUntil by remember { mutableStateOf(user.instructorValidUntil) }
    var homeOrganizationName by remember { mutableStateOf(user.homeOrganizationName) }

    var selectedTab by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(22.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Column {
                Text(
                    text = "VollstÃ¤ndiges Profil bearbeiten",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Brevets, TTU, Notfallkontakte & Tauchlehrer-Akte verwalten.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tab switcher between Diver and Instructor
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedTab == 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(1.dp, if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent),
                        modifier = Modifier.weight(1f).clickable { selectedTab = 0 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                            Text(
                                text = "ðŸ‘¤ Taucher-Profil",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedTab == 1) SeaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(1.dp, if (selectedTab == 1) SeaGreen else Color.Transparent),
                        modifier = Modifier.weight(1f).clickable { selectedTab = 1 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                            Text(
                                text = "ðŸ¤¿ Leitung & TL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) SeaGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (selectedTab == 0) {
                    // SECTION 0: Diver Profile
                    OutlinedButton(
                        onClick = {
                            if (user.role == "INSTRUCTOR") {
                                fullName = "Adrian Scheid"
                                email = "adrian.scheid01@gmail.com"
                                phoneNumber = "+49 170 1234567"
                                certLevel = "CMAS *** / PADI OWSI"
                                certAgency = "VDST / CMAS"
                                licenseNumber = "TL2-GER-9921"
                                totalLoggedDives = "340"
                                ttuExpiryDate = "2027-05-20"
                                ttuDoctorName = "Dr. med. Thomas Weber"
                                emergencyContactName = "Monika Scheid"
                                emergencyContactPhone = "+49 171 9876543"
                                insuranceProvider = "DAN Europe"
                                insuranceNumber = "DAN-DE-449102"
                                suitSize = "L (52)"
                                shoeSize = "43"
                                bcdSize = "M"
                                preferredTank = "12L Stahl DIN"
                            } else {
                                fullName = "Leo Chen"
                                email = "leo.chen@diver.com"
                                phoneNumber = "+49 176 9988776"
                                certLevel = "Open Water Diver (18m)"
                                certAgency = "SSI"
                                licenseNumber = "SSI-DIV-5512"
                                totalLoggedDives = "14"
                                ttuExpiryDate = "2027-08-15"
                                ttuDoctorName = "Dr. med. Frank Meyer"
                                emergencyContactName = "Sarah Chen"
                                emergencyContactPhone = "+49 176 1122334"
                                insuranceProvider = "AquaMed"
                                insuranceNumber = "AQM-78192-DE"
                                suitSize = "M"
                                shoeSize = "42"
                                bcdSize = "M"
                                preferredTank = "12L Alu DIN"
                            }
                        },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("âš¡ Mit Test-Taucherdaten befÃ¼llen", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("VollstÃ¤ndiger Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-Mail Adresse") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_email_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Telefonnummer") },
                        placeholder = { Text("+49 170 1234567") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = certLevel,
                        onValueChange = { certLevel = it },
                        label = { Text("Brevet-Stufe / Zertifizierung") },
                        placeholder = { Text("z.B. OWD, AOWD, Rescue Diver") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_cert_level_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = certAgency,
                        onValueChange = { certAgency = it },
                        label = { Text("Tauchverband") },
                        placeholder = { Text("z.B. VDST / CMAS, PADI, SSI") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_agency_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = licenseNumber,
                        onValueChange = { licenseNumber = it },
                        label = { Text("Brevet- / Lizenznummer") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_license_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = totalLoggedDives,
                        onValueChange = { totalLoggedDives = it },
                        label = { Text("Geloggte TauchgÃ¤nge") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_dives_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ttuExpiryDate,
                        onValueChange = { ttuExpiryDate = it },
                        label = { Text("TTU GÃ¼ltig bis (JJJJ-MM-TT)") },
                        placeholder = { Text("2027-06-30") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_ttu_expiry_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = ttuDoctorName,
                        onValueChange = { ttuDoctorName = it },
                        label = { Text("Ausstellender Arzt / Praxis") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_ttu_doctor_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = emergencyContactName,
                        onValueChange = { emergencyContactName = it },
                        label = { Text("Notfallkontakt Name") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_emergency_name_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = emergencyContactPhone,
                        onValueChange = { emergencyContactPhone = it },
                        label = { Text("Notfallkontakt Telefon") },
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_emergency_phone_field"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = insuranceProvider,
                        onValueChange = { insuranceProvider = it },
                        label = { Text("Tauchversicherung (z.B. DAN)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = insuranceNumber,
                        onValueChange = { insuranceNumber = it },
                        label = { Text("Versicherungsnummer / Police") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = suitSize,
                        onValueChange = { suitSize = it },
                        label = { Text("AnzuggrÃ¶ÃŸe (z.B. M, L, XL)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = shoeSize,
                        onValueChange = { shoeSize = it },
                        label = { Text("SchuhgrÃ¶ÃŸe (z.B. 43)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = bcdSize,
                        onValueChange = { bcdSize = it },
                        label = { Text("Tarierweste / BCD GrÃ¶ÃŸe") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = preferredTank,
                        onValueChange = { preferredTank = it },
                        label = { Text("Bevorzugte Flasche") },
                        placeholder = { Text("12L Stahl DIN, 15L oder Alu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                } else {
                    // SECTION 1: Instructor / Leadership Profile
                    OutlinedButton(
                        onClick = {
                            instructorAgency = "VDST / CMAS"
                            instructorLevel = "Tauchlehrer ** (CMAS Instructor **)"
                            instructorLicenseNumber = "TL2-GER-9921"
                            instructorValidUntil = "2027-12-31"
                            homeOrganizationName = "Tauchclub Delphin Bodensee e.V."
                            teachingSpecialtiesJson = "[\"Nitrox EANx\", \"Tieftauchen\", \"Trockentauchen\", \"Wracktauchen\", \"HLW / O2-Provider\"]"
                        },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("âš¡ Mit Test-Tauchlehrerdaten befÃ¼llen", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SeaGreen)
                    }

                    OutlinedTextField(
                        value = instructorLevel,
                        onValueChange = { instructorLevel = it },
                        label = { Text("Ausbilder- / Leitungs-Stufe") },
                        placeholder = { Text("z.B. CMAS TL**, PADI OWSI/MSDT") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = instructorAgency,
                        onValueChange = { instructorAgency = it },
                        label = { Text("Verband (Instructor)") },
                        placeholder = { Text("VDST / CMAS, PADI, SSI") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = instructorLicenseNumber,
                        onValueChange = { instructorLicenseNumber = it },
                        label = { Text("Tauchlehrer- / Lizenznummer") },
                        placeholder = { Text("z.B. TL2-GER-9921") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = instructorValidUntil,
                        onValueChange = { instructorValidUntil = it },
                        label = { Text("Lizenz gÃ¼ltig bis (JJJJ-MM-TT)") },
                        placeholder = { Text("2027-12-31") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = homeOrganizationName,
                        onValueChange = { homeOrganizationName = it },
                        label = { Text("Stamm-Organisation / Verein / Basis") },
                        placeholder = { Text("Tauchclub Delphin e.V.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = teachingSpecialtiesJson,
                        onValueChange = { teachingSpecialtiesJson = it },
                        label = { Text("Lehrbefugnisse / Spezialkurse") },
                        placeholder = { Text("[\"Nitrox\", \"Tieftauchen\", \"Trockenanzug\"]") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            ScubaPrimaryButton(
                text = "Speichern",
                onClick = {
                    val updated = user.copy(
                        fullName = fullName.trim(),
                        email = email.trim(),
                        phoneNumber = phoneNumber.trim(),
                        certLevel = certLevel.trim(),
                        certAgency = certAgency.trim(),
                        licenseNumber = licenseNumber.trim(),
                        totalLoggedDives = totalLoggedDives.toIntOrNull() ?: user.totalLoggedDives,
                        ttuExpiryDate = ttuExpiryDate.trim(),
                        ttuDoctorName = ttuDoctorName.trim(),
                        emergencyContactName = emergencyContactName.trim(),
                        emergencyContactPhone = emergencyContactPhone.trim(),
                        insuranceProvider = insuranceProvider.trim(),
                        insuranceNumber = insuranceNumber.trim(),
                        suitSize = suitSize.trim(),
                        shoeSize = shoeSize.trim(),
                        bcdSize = bcdSize.trim(),
                        preferredTank = preferredTank.trim(),
                        instructorAgency = instructorAgency.trim(),
                        instructorLicenseNumber = instructorLicenseNumber.trim(),
                        instructorLevel = instructorLevel.trim(),
                        teachingSpecialtiesJson = teachingSpecialtiesJson.trim(),
                        instructorValidUntil = instructorValidUntil.trim(),
                        homeOrganizationName = homeOrganizationName.trim()
                    )
                    onSave(updated)
                    onDismiss()
                },
                modifier = Modifier.width(130.dp),
                testTag = "save_edit_profile_btn"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(44.dp)
            ) {
                Text("Abbrechen")
            }
        }
    )
}
