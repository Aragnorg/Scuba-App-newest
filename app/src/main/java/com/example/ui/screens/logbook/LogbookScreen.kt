package com.example.ui.screens.logbook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiveLogEntity
import com.example.ui.components.BuddyPushNotificationBanner
import com.example.ui.components.ScubaCompactButton
import com.example.ui.components.ScubaFilterChip
import com.example.ui.components.ScubaPillButton
import com.example.ui.components.SignaturePreview
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
// import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
// import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

/**
 * Sort options for scuba dive logs
 */
enum class DiveSortOption(val labelEn: String, val labelDe: String) {
    DATE_DESC("Newest First", "Neueste zuerst"),
    DATE_ASC("Oldest First", "Älteste zuerst"),
    DEPTH_DESC("Deepest First", "Tiefste zuerst"),
    DURATION_DESC("Longest First", "Längste zuerst")
}

/**
 * Full Scuba Diving Logbook Screen:
 * - Summary statistics (Total dives, bottom time, max depth, verified badges)
 * - Complete telemetry capture (Date, time in/out, duration, max/avg depths, water/air temps, gas mix, tank bar, weights, suit, safety stop, sightings, notes)
 * - Two Ways to Approve:
 *   1. Dive Center / Instructor Approval via QR Code (scanning or official stamp)
 *   2. Buddy Approval:
 *      a) Finger Signature Pad (Sign with hands on screen)
 *      b) Send Dive to Buddy for real-time Push Notification approval
 */
@Composable
fun LogbookScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dives by viewModel.allDiveLogs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activePush by viewModel.activePushNotification.collectAsState()
    val activeEmailVerification by viewModel.activeEmailVerification.collectAsState()
    val isImperial by viewModel.isImperial.collectAsState()
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE

    var showEmailApprovalDialog by remember { mutableStateOf(false) }

    // Dialog & Inspection State
    var showAddDiveDialog by remember { mutableStateOf(false) }
    var selectedDiveForInspection by remember { mutableStateOf<DiveLogEntity?>(null) }
    var diveForQrApproval by remember { mutableStateOf<DiveLogEntity?>(null) }
    var diveForBuddyApproval by remember { mutableStateOf<DiveLogEntity?>(null) }
    var diveForIncomingPushReview by remember { mutableStateOf<DiveLogEntity?>(null) }

    // Live Search & Sort State
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedSortOption by rememberSaveable { mutableStateOf(DiveSortOption.DATE_DESC) }
    var isAnalyticsExpanded by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Filter Chips: 0 = All, 1 = Approved Only, 2 = Pending Only
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    // Multi-field filtered & sorted dives
    val filteredAndSortedDives = remember(dives, selectedFilterIndex, searchQuery, selectedSortOption) {
        val query = searchQuery.trim().lowercase()
        val list = dives.filter { dive ->
            // Filter chips
            val matchesFilter = when (selectedFilterIndex) {
                1 -> dive.approvalStatus != "PENDING"
                2 -> dive.approvalStatus == "PENDING"
                else -> true
            }
            if (!matchesFilter) return@filter false

            // Multi-field search
            if (query.isBlank()) {
                true
            } else {
                dive.siteName.lowercase().contains(query) ||
                dive.location.lowercase().contains(query) ||
                dive.buddyName.lowercase().contains(query) ||
                dive.notes.lowercase().contains(query) ||
                dive.gasType.lowercase().contains(query) ||
                dive.sightings.lowercase().contains(query)
            }
        }

        when (selectedSortOption) {
            DiveSortOption.DATE_DESC -> list.sortedWith(compareByDescending<DiveLogEntity> { it.date }.thenByDescending { it.diveNumber })
            DiveSortOption.DATE_ASC -> list.sortedWith(compareBy<DiveLogEntity> { it.date }.thenBy { it.diveNumber })
            DiveSortOption.DEPTH_DESC -> list.sortedByDescending { it.maxDepthMeters }
            DiveSortOption.DURATION_DESC -> list.sortedByDescending { it.durationMinutes }
        }
    }

    // Aggregate Stats
    val totalDivesCount = dives.size
    val maxDepthAchieved = dives.maxOfOrNull { it.maxDepthMeters } ?: 0f
    val totalMinutes = dives.sumOf { it.durationMinutes }
    val totalHours = totalMinutes / 60
    val remMinutes = totalMinutes % 60
    val verifiedCount = dives.count { it.approvalStatus != "PENDING" }

    // Analytics Breakdown Stats
    val avgDepthOfAll = if (dives.isNotEmpty()) dives.map { it.avgDepthMeters }.average().toFloat() else 0f
    val avgDurationOfAll = if (dives.isNotEmpty()) dives.map { it.durationMinutes }.average().toInt() else 0
    val favoriteSiteEntry = remember(dives) {
        if (dives.isEmpty()) null
        else dives.groupBy { it.siteName.trim() }
            .filterKeys { it.isNotBlank() }
            .maxByOrNull { it.value.size }
    }
    val favoriteSiteName = favoriteSiteEntry?.key ?: (if (isGerman) "Noch keine" else "None yet")
    val favoriteSiteCount = favoriteSiteEntry?.value?.size ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ScubaPillButton(
                text = strings.logNewDive,
                icon = Icons.Rounded.Add,
                onClick = { showAddDiveDialog = true },
                testTag = "fab_log_new_dive"
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Heads-up real-time Push Notification Banner (if any active buddy push message)
            BuddyPushNotificationBanner(
                notification = activePush,
                onReviewClick = { pushNotification ->
                    val targetDive = dives.find { it.id == pushNotification.diveId }
                    if (targetDive != null) {
                        diveForIncomingPushReview = targetDive
                    }
                },
                onDismiss = { viewModel.dismissPushNotification() }
            )

            // Heads-up real-time Email Approval Banner (Target: Adrian Scheid)
            activeEmailVerification?.let { emailMsg ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("email_approval_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✉️ E-Mail-Freigabe: ${emailMsg.recipientName}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "TG #${emailMsg.diveNumber} (${emailMsg.siteName}) • ${emailMsg.recipientEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showEmailApprovalDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .heightIn(min = 36.dp)
                                .testTag("review_email_approval_btn")
                        ) {
                            Text(
                                text = if (isGerman) "Freigeben" else "Approve",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissEmailVerification() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.close,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                // HERO STATS BANNER
                item {
                    LogbookHeaderHero(
                        diverName = currentUser?.fullName ?: "Leo Chen",
                        totalDives = totalDivesCount,
                        maxDepth = maxDepthAchieved,
                        totalHours = totalHours,
                        totalMinutes = remMinutes,
                        verifiedDives = verifiedCount,
                        isImperial = isImperial
                    )
                }

                // LOGBOOK ANALYTICS BREAKDOWN CARD
                item {
                    LogbookAnalyticsBreakdownCard(
                        avgDepth = avgDepthOfAll,
                        avgDuration = avgDurationOfAll,
                        totalHours = totalHours,
                        totalMinutes = remMinutes,
                        totalDives = totalDivesCount,
                        favoriteSiteName = favoriteSiteName,
                        favoriteSiteCount = favoriteSiteCount,
                        isExpanded = isAnalyticsExpanded,
                        onToggleExpand = { isAnalyticsExpanded = !isAnalyticsExpanded },
                        isImperial = isImperial,
                        isGerman = isGerman
                    )
                }

                // FILTER CHIPS ROW
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    FilterChipsRow(
                        selectedIndex = selectedFilterIndex,
                        onSelectIndex = { selectedFilterIndex = it },
                        totalCount = dives.size,
                        verifiedCount = verifiedCount,
                        pendingCount = dives.count { it.approvalStatus == "PENDING" }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // LIVE SEARCH & SORT ROW
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Search Input Field
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("logbook_search_field"),
                                placeholder = {
                                    Text(
                                        text = if (isGerman) "Tauchplatz, Ort, Buddy, Gas..." else "Search site, location, buddy...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Clear Search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )

                            // Sort Selector Dropdown Button
                            Box {
                                ScubaCompactButton(
                                    text = if (isGerman) "Sortieren" else "Sort",
                                    icon = Icons.Rounded.Sort,
                                    onClick = { showSortMenu = true },
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                    testTag = "logbook_sort_button"
                                )

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DiveSortOption.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = if (isGerman) option.labelDe else option.labelEn,
                                                    fontWeight = if (selectedSortOption == option) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (selectedSortOption == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                            },
                                            onClick = {
                                                selectedSortOption = option
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Search & Sort Status Feedback Bar
                        if (searchQuery.isNotBlank() || selectedSortOption != DiveSortOption.DATE_DESC) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isGerman) {
                                        "${filteredAndSortedDives.size} von ${dives.size} Tauchgängen"
                                    } else {
                                        "Showing ${filteredAndSortedDives.size} of ${dives.size} dives"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (isGerman) selectedSortOption.labelDe else selectedSortOption.labelEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // EMPTY STATE
                if (filteredAndSortedDives.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(56.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Water,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) {
                                        if (isGerman) "Keine Tauchgänge gefunden" else "No matching dives found"
                                    } else strings.noDivesFound,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) {
                                        if (isGerman) "Keine Treffer für \"$searchQuery\". Bitte Suchbegriff anpassen."
                                        else "No dives match \"$searchQuery\". Try adjusting your search query or filter."
                                    } else strings.noDivesFoundDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // DIVE LOG LIST ITEMS
                    items(filteredAndSortedDives, key = { it.id }) { dive ->
                        DiveLogCard(
                            dive = dive,
                            onClick = { selectedDiveForInspection = dive },
                            onApproveQrClick = { diveForQrApproval = dive },
                            onApproveBuddyClick = { diveForBuddyApproval = dive },
                            isImperial = isImperial
                        )
                    }
                }
            }
        }
    }

    // --- MODALS & DIALOGS ---

    // 1. Log New Dive Dialog
    if (showAddDiveDialog) {
        val nextNumber = (dives.maxOfOrNull { it.diveNumber } ?: 0) + 1
        AddEditDiveDialog(
            nextDiveNumber = nextNumber,
            onSave = { num, date, tIn, tOut, dur, site, loc, water, entry, maxD, avgD, wTemp, aTemp, vis, gas, tank, pIn, pOut, suit, wt, stop, sights, nts, bName, bEmail, dCenter ->
                viewModel.createDiveLog(
                    diveNumber = num,
                    date = date,
                    timeIn = tIn,
                    timeOut = tOut,
                    durationMinutes = dur,
                    siteName = site,
                    location = loc,
                    bodyOfWater = water,
                    entryType = entry,
                    maxDepthMeters = maxD,
                    avgDepthMeters = avgD,
                    waterTempCelsius = wTemp,
                    airTempCelsius = aTemp,
                    visibilityMeters = vis,
                    gasType = gas,
                    tankType = tank,
                    pressureInBar = pIn,
                    pressureOutBar = pOut,
                    suitType = suit,
                    weightKg = wt,
                    safetyStopCompleted = stop,
                    sightings = sights,
                    notes = nts,
                    buddyName = bName,
                    buddyEmail = bEmail,
                    diveCenterName = dCenter
                )
                showAddDiveDialog = false
            },
            onDismiss = { showAddDiveDialog = false }
        )
    }

    // 2. Dive Details Dialog
    selectedDiveForInspection?.let { dive ->
        DiveDetailDialog(
            dive = dive,
            isImperial = isImperial,
            onApproveWithQrClick = {
                selectedDiveForInspection = null
                diveForQrApproval = dive
            },
            onApproveWithBuddyClick = {
                selectedDiveForInspection = null
                diveForBuddyApproval = dive
            },
            onApproveWithEmailClick = {
                selectedDiveForInspection = null
                diveForBuddyApproval = dive
            },
            onSimulatePushClick = {
                viewModel.triggerSimulatedPushForDemo(dive)
            },
            onDeleteClick = { id ->
                viewModel.deleteDiveLog(id)
            },
            onDismiss = { selectedDiveForInspection = null }
        )
    }

    // 3. QR Approval Dialog (Dive Center / Instructor)
    diveForQrApproval?.let { dive ->
        InstructorQrApprovalDialog(
            dive = dive,
            onApprove = { diveId, stampCode, center, inst, lic ->
                viewModel.approveDiveWithInstructorQr(diveId, stampCode, center, inst, lic)
                diveForQrApproval = null
            },
            onDismiss = { diveForQrApproval = null }
        )
    }

    // 4. Buddy Approval Dialog (Email, Hand Signature or Push Message)
    diveForBuddyApproval?.let { dive ->
        BuddyApprovalDialog(
            dive = dive,
            onSignApprove = { diveId, buddyName, sigJson, note ->
                viewModel.approveDiveWithBuddySignature(diveId, buddyName, sigJson, note)
                diveForBuddyApproval = null
            },
            onSendPush = { diveId, buddyEmail, buddyName ->
                viewModel.sendDiveToBuddyForApproval(diveId, buddyEmail, buddyName)
                diveForBuddyApproval = null
            },
            onSendEmail = { diveId, email, name, note ->
                viewModel.sendDiveForEmailApproval(context, diveId, email, name, note)
                diveForBuddyApproval = null
            },
            onDismiss = { diveForBuddyApproval = null }
        )
    }

    // 4b. Incoming Email Review Dialog (Adrian Scheid 1-Tap Verification)
    if (showEmailApprovalDialog && activeEmailVerification != null) {
        val emailMsg = activeEmailVerification!!
        Dialog(
            onDismissRequest = { showEmailApprovalDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("email_approval_dialog"),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.MarkEmailRead, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGerman) "E-Mail-Freigabe prüfen" else "Review Email Approval",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { showEmailApprovalDialog = false }) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.close)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Von: ${emailMsg.diverName} (${emailMsg.diverEmail})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "An: ${emailMsg.recipientName} (${emailMsg.recipientEmail})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Betreff: ${emailMsg.subject}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = emailMsg.emailBodyText,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.approveDiveViaEmail(
                                diveId = emailMsg.diveId,
                                approverName = emailMsg.recipientName,
                                approverEmail = emailMsg.recipientEmail,
                                note = "Offiziell freigegeben per E-Mail an ${emailMsg.recipientEmail} von Adrian Scheid."
                            )
                            showEmailApprovalDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 46.dp)
                            .testTag("confirm_email_approval_btn")
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGerman) "✓ Als Adrian Scheid freigeben" else "✓ Approve as Adrian Scheid",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // 5. Incoming Push Review Dialog
    diveForIncomingPushReview?.let { dive ->
        BuddyPushApprovalDialog(
            dive = dive,
            onApprove = { diveId, buddyName, note ->
                viewModel.approveBuddyDiveRequest(diveId, buddyName, note)
                diveForIncomingPushReview = null
            },
            onDismiss = { diveForIncomingPushReview = null }
        )
    }
}

/**
 * Diver Logbook Header with key certification stats
 */
@Composable
private fun LogbookHeaderHero(
    diverName: String,
    totalDives: Int,
    maxDepth: Float,
    totalHours: Int,
    totalMinutes: Int,
    verifiedDives: Int,
    isImperial: Boolean = false
) {
    val strings = LocalAppStrings.current
    val verifiedRatioPercent = if (totalDives > 0) (verifiedDives * 100 / totalDives) else 0
    val maxDepthStr = if (isImperial) {
        "${kotlin.math.round(maxDepth * 3.28084f * 10f) / 10f} ft"
    } else {
        "${maxDepth}m"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    SeaGreen.copy(alpha = 0.5f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Cockpit Bar: Telemetry Status & Verified Ratio Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SeaGreen, CircleShape)
                        )
                        Text(
                            text = "DIVE COMPUTER // TELEMETRY LINKED",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = diverName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SeaGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = SeaGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "$verifiedDives/$totalDives ${strings.verifiedDives}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SeaGreen,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "$verifiedRatioPercent% VERIFIED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = SeaGreen.copy(alpha = 0.8f),
                                fontSize = 8.sp
                            )
                        }
                    }
                }
            }

            // Tactile Metric Tiles (2x2 Cockpit Grid)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroStatItem(
                        modifier = Modifier.weight(1f),
                        label = strings.totalDives,
                        value = "$totalDives",
                        subLabel = "LOGBOOK DIVES",
                        icon = Icons.Rounded.Water,
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    HeroStatItem(
                        modifier = Modifier.weight(1f),
                        label = strings.maxDepth,
                        value = maxDepthStr,
                        subLabel = "DEEPEST RECORD",
                        icon = Icons.Rounded.Speed,
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HeroStatItem(
                        modifier = Modifier.weight(1f),
                        label = strings.bottomTime,
                        value = "${totalHours}h ${totalMinutes}m",
                        subLabel = "DEPLOYED TIME",
                        icon = Icons.Rounded.Timer,
                        accentColor = SeaGreen
                    )
                    HeroStatItem(
                        modifier = Modifier.weight(1f),
                        label = strings.verifiedDives,
                        value = "$verifiedDives / $totalDives",
                        subLabel = "$verifiedRatioPercent% CLEAR",
                        icon = Icons.Rounded.Shield,
                        accentColor = SandGold
                    )
                }
            }

            // Cockpit Safety & Algorithm Status Strip
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ZH-L16C DECO ENGINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "CE EN 13319 TELEMETRY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Expandable Cockpit Analytics Breakdown Card:
 * - Average depth across all dives
 * - Average bottom time per dive
 * - Total accumulated dive hours & minutes
 * - Top / Favorite dive site with dive count
 */
@Composable
private fun LogbookAnalyticsBreakdownCard(
    avgDepth: Float,
    avgDuration: Int,
    totalHours: Int,
    totalMinutes: Int,
    totalDives: Int,
    favoriteSiteName: String,
    favoriteSiteCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isImperial: Boolean = false,
    isGerman: Boolean = false
) {
    val scaleToFt = 3.28084f
    val avgDepthStr = if (isImperial) {
        "${kotlin.math.round(avgDepth * scaleToFt * 10f) / 10f} ft"
    } else {
        "${kotlin.math.round(avgDepth * 10f) / 10f} m"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row with Expand / Collapse Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isGerman) "COCKPIT-STATISTIK" else "COCKPIT ANALYTICS BREAKDOWN",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isGerman) "Durchschnitte & Top-Tauchplätze" else "Fleet Averages & Dive Records",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = "Toggle Analytics",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsMetricTile(
                            modifier = Modifier.weight(1f),
                            title = if (isGerman) "Ø Tauchtiefe" else "Avg Depth",
                            value = avgDepthStr,
                            subLabel = if (isGerman) "Durchschnitt aller Logs" else "All dives mean",
                            icon = Icons.Rounded.Speed,
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                        AnalyticsMetricTile(
                            modifier = Modifier.weight(1f),
                            title = if (isGerman) "Ø Grundzeit" else "Avg Bottom Time",
                            value = "$avgDuration min",
                            subLabel = if (isGerman) "Pro Tauchgang" else "Per dive average",
                            icon = Icons.Rounded.Timer,
                            accentColor = SeaGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnalyticsMetricTile(
                            modifier = Modifier.weight(1f),
                            title = if (isGerman) "Gesamte Tauchzeit" else "Total Dive Time",
                            value = "${totalHours}h ${totalMinutes}m",
                            subLabel = if (isGerman) "$totalDives Tauchgänge geloggt" else "$totalDives dives logged",
                            icon = Icons.Rounded.Water,
                            accentColor = MaterialTheme.colorScheme.primary
                        )
                        AnalyticsMetricTile(
                            modifier = Modifier.weight(1f),
                            title = if (isGerman) "Top Tauchplatz" else "Favorite Site",
                            value = favoriteSiteName,
                            subLabel = if (isGerman) "$favoriteSiteCount Tauchgänge" else "$favoriteSiteCount dives logged",
                            icon = Icons.Rounded.Place,
                            accentColor = SandGold
                        )
                    }
                }
            }

            if (!isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ø $avgDepthStr • Ø $avgDuration min • $favoriteSiteName ($favoriteSiteCount)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isGerman) "Tippen zum Öffnen" else "Tap to expand",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsMetricTile(
    title: String,
    value: String,
    subLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HeroStatItem(
    label: String,
    value: String,
    subLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(accentColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                if (subLabel.isNotBlank()) {
                    Text(
                        text = subLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    totalCount: Int,
    verifiedCount: Int,
    pendingCount: Int
) {
    val strings = LocalAppStrings.current
    val filters = listOf(
        "${strings.filterAll} ($totalCount)",
        "${strings.filterVerified} ($verifiedCount)",
        "${strings.filterPending} ($pendingCount)"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters.size) { index ->
            ScubaFilterChip(
                label = filters[index],
                isSelected = selectedIndex == index,
                onClick = { onSelectIndex(index) },
                testTag = "filter_chip_$index"
            )
        }
    }
}

/**
 * Rich Card displaying an individual dive log
 */
@Composable
fun DiveLogCard(
    dive: DiveLogEntity,
    onClick: () -> Unit,
    onApproveQrClick: () -> Unit,
    onApproveBuddyClick: () -> Unit,
    modifier: Modifier = Modifier,
    isImperial: Boolean = false
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    val context = LocalContext.current

    // Parse tank volume to compute SAC rate
    val tankLiters = remember(dive.tankType) {
        val t = dive.tankType.lowercase()
        when {
            t.contains("15") -> 15.0f
            t.contains("10") -> 10.0f
            t.contains("80") || t.contains("11") -> 11.1f
            t.contains("7") -> 7.0f
            else -> 12.0f
        }
    }

    val sacRateStr = remember(dive, tankLiters, isImperial) {
        if (dive.pressureInBar > dive.pressureOutBar && dive.durationMinutes > 0 && dive.avgDepthMeters > 0) {
            val deltaBar = (dive.pressureInBar - dive.pressureOutBar).toFloat()
            val ambientAta = 1.0f + (dive.avgDepthMeters / 10.0f)
            val sacBarPerMin = deltaBar / (ambientAta * dive.durationMinutes.toFloat())
            val sacLitersPerMin = sacBarPerMin * tankLiters
            if (isImperial) {
                val cuFt = sacLitersPerMin / 28.3168f
                "${kotlin.math.round(cuFt * 100f) / 100f} cu ft/m"
            } else {
                "${kotlin.math.round(sacLitersPerMin * 10f) / 10f} L/m"
            }
        } else null
    }

    val maxDepthStr = if (isImperial) {
        "${kotlin.math.round(dive.maxDepthMeters * 3.28084f * 10f) / 10f} ft"
    } else {
        "${dive.maxDepthMeters}m"
    }
    val avgDepthStr = if (isImperial) {
        "${kotlin.math.round(dive.avgDepthMeters * 3.28084f * 10f) / 10f} ft"
    } else {
        "${dive.avgDepthMeters}m"
    }
    val waterTempStr = if (isImperial) {
        "${kotlin.math.round(dive.waterTempCelsius * 1.8f + 32f).toInt()}°F"
    } else {
        "${dive.waterTempCelsius}°C"
    }
    val pressureUsedStr = if (isImperial) {
        "${kotlin.math.round((dive.pressureInBar - dive.pressureOutBar).coerceAtLeast(0) * 14.5038f).toInt()} PSI"
    } else {
        "${(dive.pressureInBar - dive.pressureOutBar).coerceAtLeast(0)} bar"
    }
    val visibilityStr = if (isImperial) {
        "${kotlin.math.round(dive.visibilityMeters * 3.28084f).toInt()}ft Vis"
    } else {
        "${dive.visibilityMeters}m Vis"
    }
    val isNitrox = dive.gasType.contains("EAN", ignoreCase = true) || dive.gasType.contains("Nitrox", ignoreCase = true)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize()
            .testTag("dive_card_${dive.diveNumber}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Dive Number pill + Date + Entry Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${strings.diveNumberPrefix}${dive.diveNumber}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${dive.date} • ${dive.timeIn}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "${dive.entryType} • ${dive.bodyOfWater}",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Site Name & Location
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = dive.siteName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dive.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Visual Depth Indicator Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Rounded.Speed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DEPTH PROFILE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "MAX: $maxDepthStr • AVG: $avgDepthStr",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (dive.maxDepthMeters / 40f).coerceIn(0.06f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primary,
                                            DepthNavy
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            // Gas Badges Row (Nitrox vs Air + Tank Type)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isNitrox) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF064E3B).copy(alpha = 0.35f),
                        border = BorderStroke(1.2.dp, SandGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(SandGold, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "NITROX: ${dive.gasType.uppercase()}",
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                color = SandGold
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Air,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AIR (21% O₂)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = dive.tankType,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            // Telemetry Pills Row: Water Temp, Visibility, Duration, Gas Pressure Used
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniPill(icon = Icons.Rounded.Thermostat, label = waterTempStr)
                MiniPill(icon = Icons.Rounded.Visibility, label = visibilityStr)
                MiniPill(icon = Icons.Rounded.Timer, label = "${dive.durationMinutes}m")
                MiniPill(icon = Icons.Rounded.Speed, label = pressureUsedStr)
            }

            // SAC Rate Telemetry Pill (if sufficient telemetry recorded)
            if (sacRateStr != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MiniPill(
                        icon = Icons.Rounded.Air,
                        label = "SAC: $sacRateStr",
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // CRISP OFFICIAL VERIFICATION BADGE & ACTION ROW
            when (dive.approvalStatus) {
                "APPROVED_BY_INSTRUCTOR" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SeaGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, SeaGreen.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = SeaGreen.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, SeaGreen),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Rounded.Verified,
                                            contentDescription = null,
                                            tint = SeaGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "OFFICIAL INSTRUCTOR VERIFIED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = SeaGreen,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "${dive.instructorName.ifBlank { "Adrian Scheid" }} • ${dive.instructorLicense.ifBlank { "CMAS / VDST TL2" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SeaGreen.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "SEAL #${dive.qrStampCode.ifBlank { "AQ-8821" }.takeLast(8)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SeaGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                "APPROVED_BY_BUDDY_SIGNATURE" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Draw,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "OFFICIAL BUDDY SIGNATURE SEAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${strings.verifiedByBuddySig} ${dive.approvedBy.ifBlank { dive.buddyName }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                "APPROVED_BY_BUDDY_PUSH" -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SeaGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.2.dp, SeaGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SeaGreen.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, SeaGreen),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.NotificationsActive,
                                        contentDescription = null,
                                        tint = SeaGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TELEMETRY PUSH CONFIRMED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = SeaGreen,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${strings.verifiedByBuddyPush} ${dive.approvedBy.ifBlank { dive.buddyName }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                else -> {
                    // PENDING: Show Two Quick Approval Action Buttons
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = strings.pendingVerification,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ScubaCompactButton(
                                    text = strings.scanQr,
                                    icon = Icons.Rounded.QrCodeScanner,
                                    onClick = { onApproveQrClick() },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    borderColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f),
                                    testTag = "card_action_qr_${dive.diveNumber}"
                                )

                                ScubaCompactButton(
                                    text = strings.buddySign,
                                    icon = Icons.Rounded.Draw,
                                    onClick = { onApproveBuddyClick() },
                                    containerColor = SeaGreen,
                                    contentColor = Color(0xFF00382E),
                                    borderColor = SeaGreen,
                                    modifier = Modifier.weight(1f),
                                    testTag = "card_action_buddy_${dive.diveNumber}"
                                )
                            }
                        }
                    }
                }
            }

            // Standardized Action Buttons: Details & Share (36.dp height, 10.dp radius)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScubaCompactButton(
                    text = strings.showDetails,
                    icon = Icons.Rounded.Info,
                    onClick = { onClick() },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    testTag = "card_details_dive_${dive.diveNumber}"
                )

                ScubaCompactButton(
                    text = if (isGerman) "Teilen" else "Share",
                    icon = Icons.Rounded.Share,
                    onClick = { shareDiveLog(context, dive, isImperial) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    testTag = "card_share_dive_${dive.diveNumber}"
                )
            }
        }
    }
}

@Composable
private fun MiniPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = accentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
