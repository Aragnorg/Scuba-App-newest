package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.screens.excursions.DiverExcursionsScreen
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
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val studentName = currentUser?.fullName ?: progress?.studentName ?: if (currentLanguage == AppLanguage.DE) "Tauchschüler" else "Diver Student"
    val profilePicUri = currentUser?.profilePictureUri ?: progress?.profilePictureUri
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
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
                            borderColor = if (hasExpirationAlerts) Color(0xFFE53E3E) else AquaCyan,
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
                                1 -> strings.tabExcursions
                                2 -> strings.tabDivePlanning
                                else -> strings.tabLogbook
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AquaCyan.copy(alpha = 0.12f),
                            border = BorderStroke(1.2.dp, AquaCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("student_profile_pill_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = strings.studentLabel,
                                    tint = AquaCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = studentName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AquaCyan,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Quick Profile & Documents Button (Standardized 40.dp with 20.dp icon)
                    IconButton(
                        onClick = { showProfileModal = true },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("quick_profile_menu_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = strings.profileTitle,
                            tint = AquaCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Language Switcher (EN / DE)
                    LanguageSwitchButton(
                        currentLanguage = currentLanguage,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    // Sign Out (Standardized 40.dp with 20.dp icon)
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("student_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = strings.signOut,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        },
        bottomBar = {
            val navItemColors = NavigationBarItemDefaults.colors(
                indicatorColor = AquaCyan.copy(alpha = 0.22f),
                selectedIconColor = AquaCyan,
                selectedTextColor = AquaCyan,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .testTag("student_navigation_bar"),
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp
                    ) {
                        val tabs = listOf(
                            Triple(0, strings.tabLearn, Icons.Rounded.School),
                            Triple(1, strings.tabExcursions, Icons.Rounded.DirectionsBoat),
                            Triple(2, strings.tabDivePlanning, Icons.Rounded.Explore),
                            Triple(3, strings.tabLogbook, Icons.Rounded.MenuBook)
                        )
                        tabs.forEach { (index, title, icon) ->
                            val isSelected = selectedTab == index
                            val testTag = when (index) {
                                0 -> "nav_student_learn"
                                1 -> "nav_student_excursions"
                                2 -> "nav_student_planning"
                                else -> "nav_student_logbook"
                            }
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                colors = navItemColors,
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag(testTag)
                            )
                        }
                    }
                }
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
                    1 -> DiverExcursionsScreen(viewModel = viewModel)
                    2 -> DivePlanningScreen(viewModel = viewModel)
                    3 -> LogbookScreen(viewModel = viewModel)
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

