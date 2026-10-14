package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.components.LanguageSwitchButton
import com.example.ui.components.UserAvatarView
import com.example.ui.components.WorkspaceSwitcherPill
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.screens.documents.DocumentsVaultScreen
import com.example.ui.screens.planning.DivePlanningScreen
import com.example.ui.screens.profile.ProfileMenuModal
import com.example.ui.theme.OceanPrimary
import com.example.ui.viewmodel.AquaViewModel

/**
 * Dedicated Instructor Environment:
 * - Deploy course materials to students (Presentations, Videos, Quizzes)
 * - Monitor student progression roster with status filtering (Active students vs Students from the past)
 * - Review and stamp student dive logs and present Center QR codes
 * - Plan technical and recreational dives with gas calculator & emergency dossiers
 * - Access and manage candidate certification documents & medical clearances
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorEnvironment(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val instructorName = currentUser?.fullName ?: "Adrian Scheid"
    val profilePicUri = currentUser?.profilePictureUri
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val hasExpirationAlerts = remember(documents) {
        com.example.util.DocumentExpirationHelper.hasUrgentExpirationAlerts(documents)
    }

    // 0: Studio (Deploy), 1: Students Roster, 2: Approvals & QR Code, 3: Dive Planning, 4: Documents Vault
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showProfileModal by remember { mutableStateOf(false) }

    // Intercept back button if in secondary tab to return to tab 0
    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .testTag("instructor_top_left_profile_btn")
                    ) {
                        UserAvatarView(
                            photoUri = profilePicUri,
                            displayName = instructorName,
                            size = 38.dp,
                            borderWidth = 2.dp,
                            borderColor = if (hasExpirationAlerts) androidx.compose.ui.graphics.Color(0xFFE53E3E) else OceanPrimary,
                            hasAlertBadge = hasExpirationAlerts,
                            alertBadgeColor = androidx.compose.ui.graphics.Color(0xFFE53E3E),
                            onClick = { showProfileModal = true }
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (selectedTab) {
                                0 -> strings.tabDeploy
                                1 -> strings.tabStudents
                                2 -> strings.tabApprovals
                                3 -> strings.tabDivePlanning
                                else -> strings.tabDocuments
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "🏢 ${strings.instructorLabel} • $instructorName",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                        }
                    }
                },
                actions = {
                    // Workspace / Organization Switcher
                    WorkspaceSwitcherPill(
                        viewModel = viewModel,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Language Switcher (EN / DE)
                    LanguageSwitchButton(
                        currentLanguage = currentLanguage,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Sign Out
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("instructor_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = strings.signOut,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("instructor_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Upload else Icons.Outlined.Upload,
                            contentDescription = strings.tabDeploy
                        )
                    },
                    label = { Text(strings.tabDeploy, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("nav_instructor_deploy")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.People else Icons.Outlined.People,
                            contentDescription = strings.tabStudents
                        )
                    },
                    label = { Text(strings.tabStudents, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("nav_instructor_students")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Verified else Icons.Outlined.Verified,
                            contentDescription = strings.tabApprovals
                        )
                    },
                    label = { Text(strings.tabApprovals, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("nav_instructor_approvals")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = strings.tabDivePlanning
                        )
                    },
                    label = { Text(strings.tabDivePlanning, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("nav_instructor_planning")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 4) Icons.Filled.Description else Icons.Outlined.Description,
                            contentDescription = strings.tabDocuments
                        )
                    },
                    label = { Text(strings.tabDocuments, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("nav_instructor_documents")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "instructor_screen_transition") { tab ->
                when (tab) {
                    0 -> InstructorStudioScreen(viewModel = viewModel)
                    1 -> InstructorRosterScreen(viewModel = viewModel)
                    2 -> InstructorDivesScreen(viewModel = viewModel)
                    3 -> DivePlanningScreen(viewModel = viewModel)
                    4 -> DocumentsVaultScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showProfileModal) {
        ProfileMenuModal(
            viewModel = viewModel,
            onDismiss = { showProfileModal = false }
        )
    }
}
