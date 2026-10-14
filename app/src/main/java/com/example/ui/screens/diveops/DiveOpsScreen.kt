package com.example.ui.screens.diveops

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LanguageSwitchButton
import com.example.ui.components.WorkspaceSwitcherPill
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

/**
 * DiveOps & OrgFlow Mission Control:
 * Universal Multi-Tenant Management Platform for Diving Organizations:
 * 1. Dive Clubs (Tauchvereine e.V.)
 * 2. Commercial Dive Centers & Resorts (Tauchbasen)
 * 3. Dive Schools & Trip/Safari Operators
 *
 * Integrated 4-Module Tab Architecture:
 * - Tab 0: Mitglieder & Akten (Roster, TTU Traffic Lights, Emergency Plan)
 * - Tab 1: Ausfahrten & Trips (Event Planner, 1-Click Check-in with Auto-Safety-Gate)
 * - Tab 2: Smart Buddy-Board (Experience-balanced pairing algorithm & safety enforcement)
 * - Tab 3: Boot-/See-Manifest (Offline-ready roll call, splash/surfaced timestamps & air pressure)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiveOpsScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val currentOrg by viewModel.currentOrganization.collectAsState()
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE

    // If no organization is selected, return to personal mode
    if (currentOrg == null) {
        return
    }
    val org = currentOrg!!

    // Back button returns to Personal Diver profile mode
    BackHandler {
        viewModel.selectOrganization(null)
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val primaryAccentColor = if (org.isClub) SeaGreen else OceanPrimary

    val tabs = listOf(
        Triple(0, "👥", strings.tabDiveOpsRoster),
        Triple(1, "🚤", strings.tabDiveOpsEvents),
        Triple(2, "🤿", strings.tabDiveOpsBuddyBoard),
        Triple(3, "📋", strings.tabDiveOpsManifest),
        Triple(4, "⚙️", strings.tabDiveOpsAssets)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            WorkspaceSwitcherPill(viewModel = viewModel)
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = primaryAccentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (org.isClub) "CLUB" else "BASE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccentColor
                                )
                            }
                        }
                    },
                    actions = {
                        // Language Switcher
                        LanguageSwitchButton(
                            currentLanguage = currentLanguage,
                            onLanguageChange = { viewModel.setLanguage(it) },
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        // Return to Personal Profile
                        IconButton(
                            onClick = { viewModel.selectOrganization(null) },
                            modifier = Modifier.testTag("exit_diveops_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.personalDiverProfile,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // 4-Tab Navigation Bar
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = primaryAccentColor,
                                height = 3.dp
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = primaryAccentColor
                ) {
                    tabs.forEach { (index, icon, title) ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = icon, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            },
                            selectedContentColor = primaryAccentColor,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "diveops_tab_transition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DiveOpsRosterTab(
                        viewModel = viewModel,
                        organization = org,
                        isGerman = isGerman,
                        primaryAccentColor = primaryAccentColor
                    )
                    1 -> DiveOpsEventsTab(
                        viewModel = viewModel,
                        organization = org,
                        primaryAccentColor = primaryAccentColor,
                        onNavigateToBuddyBoard = { event ->
                            viewModel.selectDiveEvent(event)
                            selectedTab = 2
                        },
                        onNavigateToManifest = { event ->
                            viewModel.selectDiveEvent(event)
                            selectedTab = 3
                        }
                    )
                    2 -> DiveOpsBuddyBoardTab(
                        viewModel = viewModel,
                        organization = org,
                        primaryAccentColor = primaryAccentColor,
                        onNavigateToTrips = { selectedTab = 1 }
                    )
                    3 -> DiveOpsManifestTab(
                        viewModel = viewModel,
                        organization = org,
                        primaryAccentColor = primaryAccentColor,
                        onNavigateToTrips = { selectedTab = 1 }
                    )
                    4 -> DiveOpsAssetsTab(
                        viewModel = viewModel,
                        organization = org,
                        isGerman = isGerman,
                        primaryAccentColor = primaryAccentColor
                    )
                }
            }
        }
    }
}
