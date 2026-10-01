package com.example.ui.screens.logbook

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiveLogEntity
import com.example.ui.components.BuddyPushNotificationBanner
import com.example.ui.components.SignaturePreview
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

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
    val dives by viewModel.allDiveLogs.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activePush by viewModel.activePushNotification.collectAsState()
    val isImperial by viewModel.isImperial.collectAsState()
    val strings = LocalAppStrings.current

    // Dialog & Inspection State
    var showAddDiveDialog by remember { mutableStateOf(false) }
    var selectedDiveForInspection by remember { mutableStateOf<DiveLogEntity?>(null) }
    var diveForQrApproval by remember { mutableStateOf<DiveLogEntity?>(null) }
    var diveForBuddyApproval by remember { mutableStateOf<DiveLogEntity?>(null) }
    var diveForIncomingPushReview by remember { mutableStateOf<DiveLogEntity?>(null) }

    // Filter Chips: 0 = All, 1 = Approved Only, 2 = Pending Only
    var selectedFilterIndex by remember { mutableIntStateOf(0) }

    val filteredDives = remember(dives, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> dives.filter { it.approvalStatus != "PENDING" }
            2 -> dives.filter { it.approvalStatus == "PENDING" }
            else -> dives
        }
    }

    // Aggregate Stats
    val totalDivesCount = dives.size
    val maxDepthAchieved = dives.maxOfOrNull { it.maxDepthMeters } ?: 0f
    val totalMinutes = dives.sumOf { it.durationMinutes }
    val totalHours = totalMinutes / 60
    val remMinutes = totalMinutes % 60
    val verifiedCount = dives.count { it.approvalStatus != "PENDING" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDiveDialog = true },
                containerColor = OceanPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_log_new_dive")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.logNewDive, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = strings.logNewDive, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
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

                // FILTER CHIPS ROW
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    FilterChipsRow(
                        selectedIndex = selectedFilterIndex,
                        onSelectIndex = { selectedFilterIndex = it },
                        totalCount = dives.size,
                        verifiedCount = verifiedCount,
                        pendingCount = dives.count { it.approvalStatus == "PENDING" }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // EMPTY STATE
                if (filteredDives.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp),
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
                                Icon(
                                    imageVector = Icons.Default.Water,
                                    contentDescription = null,
                                    tint = OceanPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = strings.noDivesFound,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = strings.noDivesFoundDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // DIVE LOG LIST ITEMS
                    items(filteredDives, key = { it.id }) { dive ->
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

    // 4. Buddy Approval Dialog (Hand Signature or Push Message)
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
            onDismiss = { diveForBuddyApproval = null }
        )
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0A2333),
        contentColor = Color.White,
        tonalElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.logbookHeroTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AquaCyan
                    )
                    Text(
                        text = diverName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SeaGreen.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$verifiedDives/$totalDives ${strings.verifiedDives}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SeaGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3 Key Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeroStatItem(
                    label = strings.totalDives,
                    value = "$totalDives",
                    icon = Icons.Default.Water
                )
                HeroStatItem(
                    label = strings.maxDepth,
                    value = if (isImperial) {
                        "${kotlin.math.round(maxDepth * 3.28084f * 10f) / 10f} ft"
                    } else {
                        "${maxDepth}m"
                    },
                    icon = Icons.Default.Speed
                )
                HeroStatItem(
                    label = strings.bottomTime,
                    value = "${totalHours}h ${totalMinutes}m",
                    icon = Icons.Default.Timer
                )
            }
        }
    }
}

@Composable
private fun HeroStatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF13364C),
        modifier = Modifier.width(96.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = AquaCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color.LightGray
            )
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

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters.size) { index ->
            val label = filters[index]
            val isSelected = selectedIndex == index
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) OceanPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .clickable { onSelectIndex(index) }
                    .testTag("filter_chip_$index")
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
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
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("dive_card_${dive.diveNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Dive Number pill + Date + Entry Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OceanPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${strings.diveNumberPrefix}${dive.diveNumber}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dive.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "${dive.entryType} • ${dive.bodyOfWater}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Site Name & Location
            Text(
                text = dive.siteName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "📍 ${dive.location}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Telemetry Pills Row
            val maxDepthStr = if (isImperial) {
                "${kotlin.math.round(dive.maxDepthMeters * 3.28084f * 10f) / 10f} ft"
            } else {
                "${dive.maxDepthMeters}m"
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MiniPill(icon = Icons.Default.Water, label = maxDepthStr)
                MiniPill(icon = Icons.Default.Timer, label = "${dive.durationMinutes}m")
                MiniPill(icon = Icons.Default.Thermostat, label = waterTempStr)
                MiniPill(icon = Icons.Default.Air, label = pressureUsedStr)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // VERIFICATION BADGE & ACTION ROW
            when (dive.approvalStatus) {
                "APPROVED_BY_INSTRUCTOR" -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.verifiedByInstructor}: ${dive.instructorName.ifBlank { "Adrian Scheid" }} (${strings.qrVerifiedLabel})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                "APPROVED_BY_BUDDY_SIGNATURE" -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE0F7FA),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00ACC1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Draw, contentDescription = null, tint = Color(0xFF00838F), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.verifiedByBuddySig} ${dive.approvedBy.ifBlank { dive.buddyName }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006064),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                "APPROVED_BY_BUDDY_PUSH" -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SeaGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.verifiedByBuddyPush} ${dive.approvedBy.ifBlank { dive.buddyName }}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                else -> {
                    // PENDING: Show Two Quick Approval Action Buttons
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF8E1),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.pendingVerification,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OceanPrimary,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onApproveQrClick() }
                                        .testTag("card_action_qr_${dive.diveNumber}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = strings.scanQr, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SeaGreen,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onApproveBuddyClick() }
                                        .testTag("card_action_buddy_${dive.diveNumber}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 5.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Draw, contentDescription = null, tint = Color(0xFF00382E), modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = strings.buddySign, color = Color(0xFF00382E), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp), tint = OceanPrimary)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
