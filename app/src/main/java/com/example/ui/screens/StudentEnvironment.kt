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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LanguageSwitchButton
import com.example.ui.components.UserAvatarView
import com.example.ui.components.WorkspaceSwitcherPill
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.screens.logbook.LogbookScreen
import com.example.ui.screens.planning.DivePlanningScreen
import com.example.ui.screens.profile.ProfileMenuModal
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

/**
 * Dedicated Student Environment:
 * - Top-Left: Diver Profile & Credentials Avatar Menu (with profile photo upload and personal documents vault)
 * - Main Tabs: Learn Roadmap, Diver Logbook, and Personal Progression
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentEnvironment(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val progress by viewModel.userProgress.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val studentName = currentUser?.fullName ?: progress?.studentName ?: "Diver Student"
    val profilePicUri = currentUser?.profilePictureUri ?: progress?.profilePictureUri
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val hasExpirationAlerts = remember(documents) {
        com.example.util.DocumentExpirationHelper.hasUrgentExpirationAlerts(documents)
    }

    // 0: Learn & Progress, 1: Logbook
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showProfileModal by remember { mutableStateOf(false) }

    // Intercept back button if in secondary tab to return to Learn tab
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
                            .testTag("top_left_profile_btn")
                    ) {
                        UserAvatarView(
                            photoUri = profilePicUri,
                            displayName = studentName,
                            size = 38.dp,
                            borderWidth = 2.dp,
                            borderColor = if (hasExpirationAlerts) Color(0xFFE53E3E) else SeaGreen,
                            hasAlertBadge = hasExpirationAlerts,
                            alertBadgeColor = Color(0xFFE53E3E),
                            onClick = { showProfileModal = true }
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("app_bar_title_row")
                    ) {
                        Text(
                            text = when (selectedTab) {
                                0 -> strings.tabLearn
                                1 -> strings.tabDivePlanning
                                else -> strings.tabLogbook
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SeaGreen.copy(alpha = 0.15f),
                            modifier = Modifier.testTag("student_profile_pill_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = strings.studentLabel,
                                    tint = SeaGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = studentName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeaGreen,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Workspace / Organization Switcher
                    WorkspaceSwitcherPill(
                        viewModel = viewModel,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Quick Profile & Documents Button
                    IconButton(
                        onClick = { showProfileModal = true },
                        modifier = Modifier.testTag("quick_profile_menu_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = strings.profileTitle,
                            tint = SeaGreen
                        )
                    }

                    // Language Switcher (EN / DE)
                    LanguageSwitchButton(
                        currentLanguage = currentLanguage,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Sign Out
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("student_logout_button")
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
                modifier = Modifier.testTag("student_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.School else Icons.Outlined.School,
                            contentDescription = strings.tabLearn
                        )
                    },
                    label = { Text(strings.tabLearn) },
                    modifier = Modifier.testTag("nav_student_learn")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = strings.tabDivePlanning
                        )
                    },
                    label = { Text(strings.tabDivePlanning) },
                    modifier = Modifier.testTag("nav_student_planning")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = strings.tabLogbook
                        )
                    },
                    label = { Text(strings.tabLogbook) },
                    modifier = Modifier.testTag("nav_student_logbook")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "student_screen_transition") { tab ->
                when (tab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToDocuments = { showProfileModal = true }
                    )
                    1 -> DivePlanningScreen(viewModel = viewModel)
                    2 -> LogbookScreen(viewModel = viewModel)
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

