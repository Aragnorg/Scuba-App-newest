package com.example.ui.screens.planning

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SafetyCheck
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiveSpot
import com.example.data.model.DiveSpotMapAnnotation
import com.example.data.model.EmergencyInfo
import com.example.data.model.GasPlanResult
import com.example.data.model.PlannedDiveDraft
import com.example.data.repository.DiveSpotsData
import com.example.ui.components.DaltonsLawBestMixCard
import com.example.ui.components.SacRateCalculatorCard
import com.example.ui.components.ScubaCompactButton
import com.example.ui.components.ScubaFilterChip
import com.example.ui.components.ScubaPrimaryButton
import com.example.ui.components.ScubaSecondaryButton
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
// import com.example.ui.theme.AquaCyan
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.DepthNavy
// import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.OceanTertiary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

@Composable
fun DivePlanningScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    val isImperial by viewModel.isImperial.collectAsState()
    val context = LocalContext.current

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Spots & Maps, 1: Emergency Info, 2: Gas & MOD Planner, 3: BWRAF Checklist
    var searchQuery by remember { mutableStateOf("") }
    var selectedDifficultyFilter by remember { mutableStateOf("ALL") }
    var selectedCountryFilter by remember { mutableStateOf("ALL") }

    var spotForDetails by remember { mutableStateOf<DiveSpot?>(null) }
    var spotForEmergencyModal by remember { mutableStateOf<DiveSpot?>(null) }
    var showAddCustomSpotDialog by remember { mutableStateOf(false) }
    var draftToSaveAsLog by remember { mutableStateOf<PlannedDiveDraft?>(null) }
    var logSuccessNotice by remember { mutableStateOf<String?>(null) }

    val allSpots by viewModel.allDiveSpots.collectAsState()

    val filteredSpots = remember(allSpots, searchQuery, selectedDifficultyFilter, selectedCountryFilter) {
        allSpots.filter { spot ->
            val matchesSearch = searchQuery.isBlank() ||
                    spot.name.contains(searchQuery, ignoreCase = true) ||
                    spot.region.contains(searchQuery, ignoreCase = true) ||
                    spot.country.contains(searchQuery, ignoreCase = true) ||
                    spot.highlights.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesDiff = selectedDifficultyFilter == "ALL" || spot.difficulty == selectedDifficultyFilter
            val matchesCountry = selectedCountryFilter == "ALL" || spot.country == selectedCountryFilter

            matchesSearch && matchesDiff && matchesCountry
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 0.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.divePlanningTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.divePlanningSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Explore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sub-Tabs for Dive Planning (Segmented Pill Style)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val planningTabs = listOf(
                                Triple(0, strings.subTabDiveSpots, Icons.Rounded.LocationOn to "tab_planning_spots"),
                                Triple(1, strings.subTabEmergencyInfo, Icons.Rounded.HealthAndSafety to "tab_planning_emergency"),
                                Triple(2, strings.subTabGasPlanner, Icons.Rounded.Calculate to "tab_planning_calculator"),
                                Triple(3, strings.subTabPreDiveChecklist, Icons.Rounded.SafetyCheck to "tab_planning_checklist")
                            )
                            planningTabs.forEach { (index, title, iconAndTag) ->
                                val (icon, tag) = iconAndTag
                                val isSelected = selectedSubTab == index
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(11.dp))
                                        .clickable { selectedSubTab = index }
                                        .testTag(tag),
                                    shape = RoundedCornerShape(11.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Sub-Tab Content Switcher
            when (selectedSubTab) {
                0 -> DiveSpotsListTab(
                    spots = filteredSpots,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedDifficulty = selectedDifficultyFilter,
                    onDifficultyChange = { selectedDifficultyFilter = it },
                    isImperial = isImperial,
                    onSelectSpot = { spotForDetails = it },
                    onEmergencyClick = { spotForEmergencyModal = it },
                    onAddCustomSpotClick = { showAddCustomSpotDialog = true }
                )
                1 -> EmergencyInfoDossierTab(
                    spots = allSpots,
                    onSelectSpotEmergency = { spotForEmergencyModal = it }
                )
                2 -> GasAndModCalculatorTab(
                    isImperial = isImperial,
                    allSpots = allSpots,
                    onSaveAsPlannedDraft = { draft ->
                        draftToSaveAsLog = draft
                    }
                )
                3 -> PreDiveBuddyChecklistTab()
            }
        }

        // Notification Banner if dive logged
        logSuccessNotice?.let { notice ->
            LaunchedEffect(notice) {
                kotlinx.coroutines.delay(4000)
                logSuccessNotice = null
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SeaGreen,
                contentColor = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notice,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Modal: Detailed Dive Spot Briefing & Interactive Site Map
    spotForDetails?.let { spot ->
        DiveSpotDetailDialog(
            spot = spot,
            isImperial = isImperial,
            onEmergencyClick = {
                spotForEmergencyModal = spot
            },
            onDismiss = { spotForDetails = null }
        )
    }

    // Modal: Spot Emergency Information & Decompression Chamber Hotline
    spotForEmergencyModal?.let { spot ->
        SpotEmergencyDossierDialog(
            spot = spot,
            onDismiss = { spotForEmergencyModal = null }
        )
    }

    // Modal: Add Custom Dive Spot Dialog
    if (showAddCustomSpotDialog) {
        AddCustomDiveSpotDialog(
            onDismiss = { showAddCustomSpotDialog = false },
            onSave = { name, region, country, minDepth, maxDepth, difficulty, entryType, waterType, notes, emergencyContact, lat, lng ->
                viewModel.addCustomDiveSpot(
                    name = name,
                    region = region,
                    country = country,
                    minDepthMeters = minDepth,
                    maxDepthMeters = maxDepth,
                    difficulty = difficulty,
                    entryType = entryType,
                    waterType = waterType,
                    notes = notes,
                    emergencyContact = emergencyContact,
                    latitude = lat,
                    longitude = lng
                )
                showAddCustomSpotDialog = false
            }
        )
    }

    // Modal: Convert Plan to Dive Log Dialog
    draftToSaveAsLog?.let { draft ->
        SaveDraftDiveLogDialog(
            draft = draft,
            allSpots = allSpots,
            onDismiss = { draftToSaveAsLog = null },
            onConfirmSave = { siteName, location, notes ->
                viewModel.savePlannedDiveAsLog(draft, siteName, location, notes)
                logSuccessNotice = if (isGerman) {
                    "Plan als Entwurf im Logbuch gesichert! ($siteName)"
                } else {
                    "Plan saved to Logbook as draft! ($siteName)"
                }
                draftToSaveAsLog = null
            }
        )
    }
}

@Composable
private fun DiveSpotsListTab(
    spots: List<DiveSpot>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedDifficulty: String,
    onDifficultyChange: (String) -> Unit,
    isImperial: Boolean,
    onSelectSpot: (DiveSpot) -> Unit,
    onEmergencyClick: (DiveSpot) -> Unit,
    onAddCustomSpotClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search bar & Add Custom Spot Action Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onSearchChange,
                    placeholder = { Text(strings.searchSpotsPlaceholder, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.searchSpotsPlaceholder) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Rounded.Clear, contentDescription = if (isGerman) "LÃ¶schen" else "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("spot_search_input")
                )

                // Quick Add Custom Spot row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGerman) "${spots.size} TauchplÃ¤tze verfÃ¼gbar" else "${spots.size} Dive spots available",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ScubaCompactButton(
                        text = if (isGerman) "+ Eigener Spot" else "+ Add Spot",
                        icon = Icons.Rounded.Add,
                        onClick = onAddCustomSpotClick,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        testTag = "btn_add_custom_spot"
                    )
                }
            }
        }

        // Filter chips (Difficulty)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "BEGINNER", "INTERMEDIATE", "ADVANCED").forEach { diff ->
                    val isSelected = selectedDifficulty == diff
                    val label = when (diff) {
                        "BEGINNER" -> strings.difficultyBeginner
                        "INTERMEDIATE" -> strings.difficultyIntermediate
                        "ADVANCED" -> strings.difficultyAdvanced
                        else -> strings.allDifficulties
                    }
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        label = "chip_bg_$diff"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "chip_fg_$diff"
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clickable { onDifficultyChange(diff) }
                            .testTag("filter_diff_$diff")
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }

        if (spots.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isGerman) "Keine TauchplÃ¤tze entsprechen deinem Filter" else "No dive spots match your filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isGerman) "Suchfilter anpassen oder eigenen Tauchplatz erstellen" else "Try clearing filters or add your own custom dive site",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        ScubaPrimaryButton(
                            text = if (isGerman) "Jetzt eigenen Spot anlegen" else "Add Custom Spot Now",
                            icon = Icons.Rounded.Add,
                            onClick = onAddCustomSpotClick,
                            modifier = Modifier.fillMaxWidth(0.9f),
                            testTag = "btn_empty_add_spot"
                        )
                    }
                }
            }
        } else {
            items(spots, key = { it.id }) { spot ->
                DiveSpotSummaryCard(
                    spot = spot,
                    isImperial = isImperial,
                    onClick = { onSelectSpot(spot) },
                    onEmergencyClick = { onEmergencyClick(spot) }
                )
            }
        }
    }
}

@Composable
private fun DiveSpotSummaryCard(
    spot: DiveSpot,
    isImperial: Boolean,
    onClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    val depthLabel = if (isImperial) {
        val minFt = (spot.minDepthMeters * 3.28084).toInt()
        val maxFt = (spot.maxDepthMeters * 3.28084).toInt()
        "$minFt - $maxFt ft"
    } else {
        "${spot.minDepthMeters} - ${spot.maxDepthMeters} m"
    }

    val visibilityLabel = if (isImperial) {
        "${(spot.avgVisibilityMeters * 3.28084).toInt()} ft"
    } else {
        "${spot.avgVisibilityMeters} m"
    }

    val (difficultyColor, difficultyLabel) = when (spot.difficulty.uppercase()) {
        "BEGINNER" -> Pair(SeaGreen, if (isGerman) "ðŸŸ¢ EINSTEIGER" else "ðŸŸ¢ BEGINNER")
        "INTERMEDIATE" -> Pair(MaterialTheme.colorScheme.primary, if (isGerman) "ðŸ”µ FORTGESCHRITTEN" else "ðŸ”µ INTERMEDIATE")
        "ADVANCED" -> Pair(SandGold, if (isGerman) "ðŸŸ¡ ERFAHREN" else "ðŸŸ¡ ADVANCED")
        else -> Pair(CoralAlert, if (isGerman) "ðŸ”´ TECHNISCH / EXPERTE" else "ðŸ”´ TECHNICAL / EXPERT")
    }

    val entryTypeIcon = when {
        spot.entryType.contains("BOAT", ignoreCase = true) -> "ðŸš¤"
        spot.entryType.contains("SHORE", ignoreCase = true) || spot.entryType.contains("BEACH", ignoreCase = true) -> "ðŸ–ï¸"
        spot.entryType.contains("DRIFT", ignoreCase = true) -> "ðŸŒŠ"
        else -> "ðŸ¤¿"
    }

    val spotTypeIcon = when {
        spot.spotType.contains("WRECK", ignoreCase = true) -> "âš“"
        spot.spotType.contains("REEF", ignoreCase = true) -> "ðŸª¸"
        spot.spotType.contains("CAVE", ignoreCase = true) || spot.spotType.contains("CAVERN", ignoreCase = true) -> "ðŸ•³ï¸"
        spot.spotType.contains("WALL", ignoreCase = true) -> "ðŸ§±"
        else -> "ðŸ“"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("spot_card_${spot.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Difficulty Badge + Entry Type + Mini Map Visual Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Difficulty & Tags Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = difficultyColor.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, difficultyColor.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = difficultyLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                color = difficultyColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        // Custom Spot Tag
                        if (spot.isCustom) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SandGold.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, SandGold.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "â­ " + if (isGerman) "EIGENER SPOT" else "CUSTOM SPOT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = SandGold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Entry Type Tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "$entryTypeIcon ${spot.entryType}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        // Spot Type Tag
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "$spotTypeIcon ${spot.spotType}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${spot.region}, ${spot.country}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mini Map Visual Badge / Quick Map launcher
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { openExternalMap(context, spot.latitude, spot.longitude, spot.name) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Navigation,
                            contentDescription = "Open in Maps",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Depth Range Pill & Metric Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Polished Depth Range Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isGerman) "TIEFENBEREICH" else "DEPTH RANGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "â¬‡ $depthLabel",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Visibility Metric
                SpotMetricPill(
                    title = if (isGerman) "Sicht" else "Visibility",
                    value = visibilityLabel,
                    modifier = Modifier.weight(1f)
                )

                // Current Metric
                val currentText = if (isGerman) when (spot.currentStrength.uppercase()) {
                    "STRONG" -> "Stark"
                    "MODERATE" -> "MÃ¤ÃŸig"
                    "MILD", "LOW", "LIGHT" -> "Leicht"
                    "NONE" -> "Keine"
                    else -> spot.currentStrength
                } else spot.currentStrength
                SpotMetricPill(
                    title = if (isGerman) "StrÃ¶mung" else "Current",
                    value = currentText,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlights snippet
            Text(
                text = "âœ¨ " + spot.highlights.take(2).joinToString(" â€¢ "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(10.dp))

            // Emergency Quick-Action, Open in Maps & Briefing Button Row (Canonical ScubaCompactButton 36dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emergency Quick-Action Button
                ScubaCompactButton(
                    text = "SOS",
                    icon = Icons.Rounded.HealthAndSafety,
                    onClick = onEmergencyClick,
                    containerColor = CoralAlert.copy(alpha = 0.12f),
                    contentColor = CoralAlert,
                    borderColor = CoralAlert.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f),
                    testTag = "spot_emergency_btn_${spot.id}"
                )

                // Open in Maps Action Button (geo: URI navigation)
                ScubaCompactButton(
                    text = "Maps",
                    icon = Icons.Rounded.Navigation,
                    onClick = { openExternalMap(context, spot.latitude, spot.longitude, spot.name) },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary,
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1.1f),
                    testTag = "spot_open_maps_btn_${spot.id}"
                )

                // Briefing & Map Button
                ScubaCompactButton(
                    text = if (isGerman) "Briefing" else "Briefing",
                    icon = Icons.Rounded.Map,
                    onClick = onClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    borderColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1.3f),
                    testTag = "spot_view_map_btn_${spot.id}"
                )
            }
        }
    }
}

@Composable
private fun SpotMetricPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DiveSpotDetailDialog(
    spot: DiveSpot,
    isImperial: Boolean,
    onEmergencyClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    val depthLabel = if (isImperial) {
        val minFt = (spot.minDepthMeters * 3.28084).toInt()
        val maxFt = (spot.maxDepthMeters * 3.28084).toInt()
        "$minFt - $maxFt ft"
    } else {
        "${spot.minDepthMeters} - ${spot.maxDepthMeters} m"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("spot_detail_dialog"),
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = spot.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${spot.region}, ${spot.country}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_spot_dialog_btn")) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // GPS Coordinates & Open in Google Maps Banner
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "GPS Coordinates:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = "${String.format("%.4f", spot.latitude)}Â° N, ${String.format("%.4f", spot.longitude)}Â° E",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        ScubaCompactButton(
                            text = "Google Maps",
                            icon = Icons.Rounded.Navigation,
                            onClick = { openExternalMap(context, spot.latitude, spot.longitude, spot.name) },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            borderColor = MaterialTheme.colorScheme.primary,
                            testTag = "open_external_map_btn"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Dive Site Vector Map Canvas
                Text(
                    text = strings.diveSpotMapSection,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                InteractiveDiveSiteCanvasMap(
                    spot = spot,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(18.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Parameters Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isGerman) "Tiefenbereich:" else "Depth Range:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(depthLabel, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isGerman) "Einstiegsart:" else "Entry Type:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${spot.entryType} (${spot.waterType})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isGerman) "StrÃ¶mung & Drift:" else "Current & Drift:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(spot.currentStrength, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (isGerman) "Beste Tauchsaison:" else "Best Diving Season:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(spot.bestSeason, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                Text(
                    text = if (isGerman) "Beschreibung & Topografie:" else "Description & Topography:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = spot.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Route & Navigation Advice
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.diveSpotRouteAdvice,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = spot.routeAdvice,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Emergency Info & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ScubaPrimaryButton(
                        text = if (isGerman) "Notfalldossier" else "Emergency Dossier",
                        icon = Icons.Rounded.HealthAndSafety,
                        onClick = onEmergencyClick,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.weight(1.1f),
                        testTag = "spot_detail_emergency_btn"
                    )

                    ScubaSecondaryButton(
                        text = strings.close,
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.9f)
                            .height(52.dp),
                        testTag = "spot_detail_close_btn"
                    )
                }
            }
        }
    }
}

/**
 * Interactive Vector Dive Site Map with Depth Contours, Compass Rose & Annotation Pins
 */
@Composable
private fun InteractiveDiveSiteCanvasMap(
    spot: DiveSpot,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF00384D), DepthNavy, Color(0xFF05111B))
                )
            )
            .testTag("interactive_dive_map_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Depth Contour Bathymetry lines
            val contour1 = Path().apply {
                moveTo(0f, h * 0.35f)
                cubicTo(w * 0.3f, h * 0.2f, w * 0.7f, h * 0.5f, w, h * 0.4f)
            }
            val contour2 = Path().apply {
                moveTo(0f, h * 0.6f)
                cubicTo(w * 0.35f, h * 0.5f, w * 0.65f, h * 0.75f, w, h * 0.65f)
            }
            val contour3 = Path().apply {
                moveTo(0f, h * 0.85f)
                cubicTo(w * 0.25f, h * 0.75f, w * 0.75f, h * 0.95f, w, h * 0.88f)
            }

            drawPath(contour1, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), style = Stroke(width = 2f))
            drawPath(contour2, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), style = Stroke(width = 2f))
            drawPath(contour3, color = Color(0xFF0A2540).copy(alpha = 0.6f), style = Stroke(width = 2f))

            // 2. Draw Compass Rose in top right
            val cx = w - 40f
            val cy = 40f
            drawCircle(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), radius = 22f, center = Offset(cx, cy))
            drawLine(color = Color.White.copy(alpha = 0.8f), start = Offset(cx, cy + 18f), end = Offset(cx, cy - 18f), strokeWidth = 2f)
            drawLine(color = Color.White.copy(alpha = 0.4f), start = Offset(cx - 18f, cy), end = Offset(cx + 18f, cy), strokeWidth = 2f)

            // 3. Draw Spot Reef or Wreck Structure
            if (spot.spotType == "WRECK") {
                val wreckPath = Path().apply {
                    moveTo(w * 0.4f, h * 0.5f)
                    lineTo(w * 0.65f, h * 0.45f)
                    lineTo(w * 0.7f, h * 0.65f)
                    lineTo(w * 0.45f, h * 0.7f)
                    close()
                }
                drawPath(wreckPath, color = SandGold.copy(alpha = 0.4f))
                drawPath(wreckPath, color = SandGold, style = Stroke(width = 2f))
            } else {
                // Natural Reef Wall / Pinnacles
                drawCircle(color = SeaGreen.copy(alpha = 0.3f), radius = 35f, center = Offset(w * 0.45f, h * 0.55f))
                drawCircle(color = SeaGreen.copy(alpha = 0.25f), radius = 45f, center = Offset(w * 0.65f, h * 0.65f))
            }

            // 4. Draw Map Annotations
            spot.mapAnnotations.forEach { annotation ->
                val px = (annotation.xPercent / 100f) * w
                val py = (annotation.yPercent / 100f) * h

                val pinColor = when (annotation.type) {
                    "ENTRY", "EXIT" -> SeaGreen
                    "MOORING" -> MaterialTheme.colorScheme.primary
                    "WRECK" -> SandGold
                    "SAFETY_STOP" -> OceanTertiary
                    "HAZARD" -> Color(0xFFE53E3E)
                    else -> Color.White
                }

                // Outer pulsing circle
                drawCircle(color = pinColor.copy(alpha = 0.3f), radius = 10f, center = Offset(px, py))
                // Inner solid pin
                drawCircle(color = pinColor, radius = 5f, center = Offset(px, py))
            }
        }

        // Overlay Annotation labels accurately positioned over Canvas markers
        spot.mapAnnotations.forEach { annotation ->
            val labelStart = (maxWidth * (annotation.xPercent / 100f) - 20.dp).coerceAtLeast(4.dp)
            val labelTop = (maxHeight * (annotation.yPercent / 100f) + 8.dp).coerceAtMost(maxHeight - 24.dp)

            Box(
                modifier = Modifier.padding(start = labelStart, top = labelTop)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DepthNavy.copy(alpha = 0.85f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = annotation.title,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Bottom Map Legend
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(color = SeaGreen, label = "Entry/Exit")
                LegendDot(color = SandGold, label = "Reef/Wreck")
                LegendDot(color = OceanTertiary, label = "Safety Stop")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color, modifier = Modifier.size(6.dp)) {}
        Spacer(modifier = Modifier.width(3.dp))
        Text(label, color = Color.White, fontSize = 8.sp)
    }
}

@Composable
private fun SpotEmergencyDossierDialog(
    spot: DiveSpot,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val emergency = spot.emergencyInfo
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("spot_emergency_dossier_dialog"),
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
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isGerman) "Notfall- & Sicherheitsdossier" else "Emergency Safety Dossier",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = spot.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = if (isGerman) "SchlieÃŸen" else "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Chamber Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGerman) "NÃ„CHSTE DRUCKKAMMER" else "NEAREST HYPERBARIC CHAMBER",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = emergency.nearestChamberName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${emergency.chamberLocation} â€¢ ~${emergency.chamberDistanceKm} km ${if (isGerman) "entfernt" else "away"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        ScubaPrimaryButton(
                            text = "${if (isGerman) "Druckkammer anrufen" else "Call Chamber"}: ${emergency.chamberPhone}",
                            icon = Icons.Rounded.Call,
                            onClick = { dialPhoneNumber(context, emergency.chamberPhone) },
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "call_chamber_btn"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Radio & Maritime Contacts Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        EmergencyRowItem(
                            icon = Icons.Rounded.Radio,
                            title = if (isGerman) "KÃ¼stenwache / Seefunk (VHF):" else "Coast Guard / Marine VHF:",
                            value = emergency.coastGuardVHF,
                            color = MaterialTheme.colorScheme.primary
                        )
                        EmergencyRowItem(
                            icon = Icons.Rounded.Phone,
                            title = if (isGerman) "DAN 24/7 Internationale Notfall-Hotline:" else "DAN 24/7 International Diving Hotline:",
                            value = emergency.danHotline,
                            color = OceanSecondary,
                            onActionClick = { dialPhoneNumber(context, emergency.danHotline.split(" ").firstOrNull() ?: "") }
                        )
                        EmergencyRowItem(
                            icon = Icons.Rounded.LocalHospital,
                            title = if (isGerman) "Krankenhaus & Notaufnahme:" else "Local Hospital & Trauma Center:",
                            value = "${emergency.localHospitalName} (${emergency.localHospitalPhone})",
                            color = SeaGreen
                        )
                        EmergencyRowItem(
                            icon = Icons.Rounded.Air,
                            title = if (isGerman) "Notfall-Sauerstoffversorgung:" else "Oxygen Resuscitation Protocol:",
                            value = if (emergency.oxygenOnSite) {
                                if (isGerman) "100% Demand-Ventil Sauerstoffkoffer einsatzbereit an Bord & Basis." else "100% Demand Valve O2 Kit on standby on boat & base."
                            } else {
                                if (isGerman) "O2-Koffer bei lokaler Basis angefordert." else "O2 Kit requested from local base."
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Evacuation Notes
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isGerman) "Taucher-Evakuierungsprotokoll:" else "Diver Evacuation Protocol:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = emergency.emergencyNotes,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ScubaPrimaryButton(
                    text = if (isGerman) "Dossier schlieÃŸen" else "Close Dossier",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "close_emergency_modal_btn"
                )
            }
        }
    }
}

@Composable
private fun EmergencyRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(34.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        if (onActionClick != null) {
            IconButton(onClick = onActionClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Call, contentDescription = "Call", tint = color, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EmergencyInfoDossierTab(
    spots: List<DiveSpot>,
    onSelectSpotEmergency: (DiveSpot) -> Unit
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Global SOS Emergency Dispatch Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isGerman) "WELTWEITER TAUCHNOTRUF & DAN" else "GLOBAL DIVE EMERGENCY & DAN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = if (isGerman) "24/7 Internationale Notfall-Hotlines" else "24/7 International Emergency Response Hotlines",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScubaSecondaryButton(
                            text = "DAN America",
                            icon = Icons.Rounded.Call,
                            onClick = { dialPhoneNumber(context, "+19196849111") },
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.error,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        )

                        ScubaSecondaryButton(
                            text = "DAN Europe",
                            icon = Icons.Rounded.Call,
                            onClick = { dialPhoneNumber(context, "+39066954000") },
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.error,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Section Title: Regional Chambers
        item {
            Text(
                text = if (isGerman) "Regionale Druckkammern & Notfallzentren:" else "Regional Decompression Chambers & Medical Bases:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(spots, key = { "emergency_${it.id}" }) { spot ->
            val emergency = spot.emergencyInfo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSpotEmergency(spot) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = spot.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = spot.region,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SeaGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "~${emergency.chamberDistanceKm} km",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SeaGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ðŸ¥ ${emergency.nearestChamberName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ðŸ“» ${emergency.coastGuardVHF}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ScubaCompactButton(
                        text = "${if (isGerman) "Anrufen" else "Call"}: ${emergency.chamberPhone}",
                        icon = Icons.Rounded.Call,
                        onClick = { dialPhoneNumber(context, emergency.chamberPhone) },
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        contentColor = MaterialTheme.colorScheme.error,
                        borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun GasAndModCalculatorTab(
    isImperial: Boolean,
    allSpots: List<DiveSpot> = emptyList(),
    onSaveAsPlannedDraft: (PlannedDiveDraft) -> Unit
) {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    var activeCalculatorIndex by remember { mutableIntStateOf(0) } // 0: MOD Computer, 1: SAC Rate & RMV, 2: Dalton's Best Mix
    var o2Percent by remember { mutableIntStateOf(32) } // Standard EAN32
    var ppo2Limit by remember { mutableDoubleStateOf(1.4) } // Standard 1.4 bar bottom limit
    var plannedDepth by remember { mutableDoubleStateOf(28.0) } // 28m
    var plannedBottomTime by remember { mutableIntStateOf(35) } // 35 min
    var tankVolume by remember { mutableIntStateOf(12) } // 12L
    var tankStartPressure by remember { mutableIntStateOf(200) } // 200 bar

    val result = remember(o2Percent, ppo2Limit, plannedDepth, plannedBottomTime, tankVolume, tankStartPressure) {
        DiveSpotsData.calculateGasPlan(
            o2Percent = o2Percent.coerceIn(21, 100),
            ppo2Limit = ppo2Limit.coerceAtLeast(0.5),
            plannedDepthMeters = plannedDepth.coerceAtLeast(0.0),
            plannedBottomTimeMin = plannedBottomTime.coerceAtLeast(1),
            tankStartPressureBar = tankStartPressure.coerceAtLeast(10),
            tankVolumeLiters = tankVolume.coerceAtLeast(1)
        )
    }

    // Actual PO2 at planned depth: PO2 = (Depth/10 + 1) * (O2% / 100)
    val actualPO2 = remember(o2Percent, plannedDepth) {
        val ambientPressure = ((plannedDepth.coerceAtLeast(0.0) / 10.0) + 1.0).coerceAtLeast(1.0)
        ambientPressure * (o2Percent.coerceIn(21, 100) / 100.0)
    }

    val (po2Color, po2BadgeText, po2StatusAlert) = when {
        actualPO2 <= 1.40 -> Triple(
            SeaGreen,
            if (isGerman) "SICHER (SPORTTAUCHEN < 1.4)" else "SAFE (RECREATIONAL < 1.4)",
            false
        )
        actualPO2 <= 1.60 -> Triple(
            SandGold,
            if (isGerman) "WARNUNG (DEKO-MAX 1.4 - 1.6)" else "CAUTION (DECO MAX 1.4 - 1.6)",
            false
        )
        else -> Triple(
            CoralAlert,
            if (isGerman) "ðŸš¨ RED ALERT: SAUERSTOFF-TOXIZITÃ„T (> 1.6)" else "ðŸš¨ RED ALERT: CNS TOXICITY (> 1.6)",
            true
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 3-Way Tactical Calculator Tool Switcher (Uniform 44dp height)
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("calculator_tool_switcher"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val calcOptions = listOf(
                        Triple(0, if (isGerman) "MOD Computer" else "MOD Computer", Icons.Rounded.Calculate),
                        Triple(1, if (isGerman) "SAC & RMV" else "SAC & RMV", Icons.Rounded.Air),
                        Triple(2, if (isGerman) "Beste Mischung" else "Best Mix", Icons.Rounded.Water)
                    )

                    calcOptions.forEach { (index, title, icon) ->
                        val isSelected = activeCalculatorIndex == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(11.dp))
                                .clickable { activeCalculatorIndex = index }
                                .testTag("calc_mode_$index"),
                            shape = RoundedCornerShape(11.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.5.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        when (activeCalculatorIndex) {
            0 -> {
                // Technical Dive Computer HUD Card
                item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), Color(0x15FFFFFF)))),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Computer Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isGerman) "PRO DIVE COMPUTER HUD" else "PRO DIVE COMPUTER HUD",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isGerman) "Live Gas- & MOD-Telemetrie" else "Live Gas & MOD Telemetry",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Current Mix Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = if (o2Percent == 21) "AIR 21%" else "EAN$o2Percent",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key Calculated Results Banner (Instant MOD & Turn Pressure)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Instant MOD Readout
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = if (result.isPlanSafe && !po2StatusAlert) MaterialTheme.colorScheme.surfaceContainerLow else CoralAlert.copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.5.dp,
                                if (result.isPlanSafe && !po2StatusAlert) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else CoralAlert
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isGerman) "MAX. EINSATZTIEFE (MOD)" else "MAX OPERATING DEPTH (MOD)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isImperial) "${(result.maxOperatingDepthMeters * 3.28084).toInt()} ft" else "${String.format("%.1f", result.maxOperatingDepthMeters)} m",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (result.isPlanSafe && !po2StatusAlert) MaterialTheme.colorScheme.primary else CoralAlert
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Contingency (1.6): ${if (isImperial) "${(result.contingencyDepthMeters * 3.28084).toInt()} ft" else "${result.contingencyDepthMeters.toInt()}m"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Turn Pressure (1/3 Rule) Readout
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isGerman) "WENDEDRUCK (1/3-REGEL)" else "TURN PRESSURE (1/3 RULE)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isImperial) "${(result.turnPressureBar * 14.5038).toInt()} PSI" else "${result.turnPressureBar} bar",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Reserve: ${if (isImperial) "${(result.reservePressureBar * 14.5038).toInt()} PSI" else "${result.reservePressureBar} bar"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Color-Coded PO2 Safety Gauge
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.2.dp, po2Color.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isGerman) "SAUERSTOFFPARTIALDRUCK (PO2)" else "OXYGEN PARTIAL PRESSURE (PO2)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "${String.format("%.2f", actualPO2)} ",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black,
                                            color = po2Color
                                        )
                                        Text(
                                            text = "bar @ ${if (isImperial) "${(plannedDepth * 3.28084).toInt()} ft" else "${plannedDepth.toInt()}m"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = po2Color.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, po2Color.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = po2BadgeText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = po2Color
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Segmented PO2 Safety Bar Gauge (10 blocks: 0.2 to 2.0 bar)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (segment in 1..10) {
                                    val segmentPO2Threshold = segment * 0.2 // 0.2, 0.4, 0.6, 0.8, 1.0, 1.2, 1.4, 1.6, 1.8, 2.0
                                    val isActive = actualPO2 >= segmentPO2Threshold - 0.1

                                    val blockColor = when {
                                        segmentPO2Threshold <= 1.4 -> SeaGreen
                                        segmentPO2Threshold <= 1.6 -> SandGold
                                        else -> CoralAlert
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp),
                                        shape = RoundedCornerShape(3.dp),
                                        color = if (isActive) blockColor else blockColor.copy(alpha = 0.15f),
                                        border = if (isActive) BorderStroke(0.5.dp, blockColor) else null
                                    ) {}
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Scale Markers
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0.21 (Air)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "1.4 bar (Rec Safe)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeaGreen
                                )
                                Text(
                                    text = "1.6 bar (Deco)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SandGold
                                )
                                Text(
                                    text = "2.0 bar (Danger)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralAlert
                                )
                            }
                        }
                    }

                    if (result.warningMessage != null || po2StatusAlert) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CoralAlert.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = CoralAlert,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = result.warningMessage ?: (if (isGerman) "Achtung: Geplante Tiefe Ã¼berschreitet MOD-Sicherheitsgrenze!" else "Warning: Planned depth exceeds safe MOD limit!"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoralAlert,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sliders & Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 1. Interactive Nitrox O2% Slider & Quick Presets
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.nitroxO2Fraction,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (o2Percent == 21) {
                                    if (isGerman) "21% (Standardluft)" else "21% (Standard Air)"
                                } else {
                                    "EAN$o2Percent (${o2Percent}% O2)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Mix Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(21, 28, 32, 36, 40).forEach { mix ->
                                val isSelected = o2Percent == mix
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { o2Percent = mix },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (mix == 21) "AIR" else "EAN$mix",
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Slider(
                            value = o2Percent.toFloat(),
                            onValueChange = { o2Percent = it.toInt() },
                            valueRange = 21f..40f,
                            steps = 18,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("slider_o2_percent")
                        )
                    }

                    // 2. PPO2 Limit
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.maxPPO2Limit, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${String.format("%.1f", ppo2Limit)} bar ${if (ppo2Limit == 1.4) (if (isGerman) "(Sporttauchen)" else "(Recreational)") else if (ppo2Limit == 1.6) (if (isGerman) "(Deko-Max)" else "(Deco Max)") else ""}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (ppo2Limit > 1.4) SandGold else SeaGreen
                            )
                        }
                        Slider(
                            value = ppo2Limit.toFloat(),
                            onValueChange = { ppo2Limit = Math.round(it * 10.0) / 10.0 },
                            valueRange = 1.2f..1.6f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = OceanSecondary,
                                activeTrackColor = OceanSecondary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("slider_ppo2_limit")
                        )
                    }

                    // 3. Planned Depth
                    Column {
                        val depthUnit = if (isImperial) "ft" else "m"
                        val displayDepth = if (isImperial) (plannedDepth * 3.28084).toInt() else plannedDepth.toInt()
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.plannedDepth, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$displayDepth $depthUnit",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = plannedDepth.toFloat(),
                            onValueChange = { plannedDepth = it.toDouble() },
                            valueRange = 5f..40f,
                            steps = 34,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("slider_planned_depth")
                        )
                    }

                    // 4. Planned Bottom Time
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.plannedBottomTime, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$plannedBottomTime min",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanTertiary
                            )
                        }
                        Slider(
                            value = plannedBottomTime.toFloat(),
                            onValueChange = { plannedBottomTime = it.toInt() },
                            valueRange = 10f..60f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = OceanTertiary,
                                activeTrackColor = OceanTertiary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.testTag("slider_bottom_time")
                        )
                    }
                }
            }
        }

        // 5. Convert Plan to Dive Log Action Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_save_plan_to_log"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.BookmarkAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isGerman) "Plan in Logbuch Ã¼bertragen" else "Convert Plan to Dive Log",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGerman) {
                                    "Tiefe (${plannedDepth.toInt()}m), Nitrox (${if (o2Percent == 21) "Air 21%" else "EAN$o2Percent"}) & ${plannedBottomTime} min Grundzeit direkt als Logbuchentwurf anlegen"
                                } else {
                                    "Save planned depth (${plannedDepth.toInt()}m), mix (${if (o2Percent == 21) "Air 21%" else "EAN$o2Percent"}) & ${plannedBottomTime} min duration as draft dive log"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScubaSecondaryButton(
                            text = if (isGerman) "Reset" else "Reset",
                            icon = Icons.Rounded.Refresh,
                            onClick = {
                                o2Percent = 32
                                ppo2Limit = 1.4
                                plannedDepth = 28.0
                                plannedBottomTime = 35
                                tankVolume = 12
                                tankStartPressure = 200
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            testTag = "btn_reset_calculator_defaults"
                        )

                        ScubaPrimaryButton(
                            text = if (isGerman) "Als Entwurf speichern" else "Save as Draft Log",
                            icon = Icons.Rounded.BookmarkAdd,
                            onClick = {
                                val gasMix = if (o2Percent == 21) "Air 21%" else "EAN$o2Percent"
                                val defaultSpot = allSpots.firstOrNull()
                                val draft = PlannedDiveDraft(
                                    maxDepthMeters = plannedDepth.toFloat(),
                                    gasType = gasMix,
                                    plannedBottomTimeMin = plannedBottomTime,
                                    turnPressureBar = result.turnPressureBar,
                                    tankStartPressureBar = tankStartPressure,
                                    siteName = defaultSpot?.name ?: "Planned Nitrox Dive",
                                    location = defaultSpot?.let { "${it.region}, ${it.country}" } ?: "Open Water",
                                    notes = "Planned with Nitrox ($gasMix). Calculated MOD: ${String.format("%.1f", result.maxOperatingDepthMeters)}m, Turn Pressure: ${result.turnPressureBar} bar."
                                )
                                onSaveAsPlannedDraft(draft)
                            },
                            modifier = Modifier.weight(2f),
                            testTag = "btn_save_planned_dive_log"
                        )
                    }
                }
            }
        }
    }
    1 -> {
        item {
            SacRateCalculatorCard(
                isImperial = isImperial
            )
        }
    }
    2 -> {
        item {
            DaltonsLawBestMixCard(
                isImperial = isImperial
            )
        }
    }
}
    }
}

@Composable
private fun PreDiveBuddyChecklistTab() {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    val checklistItems = remember(isGerman) {
        mutableStateListOf(
            if (isGerman) {
                ChecklistItem("B", "Tarierung (BCD)", "Inflator, MundstÃ¼ck und SchnellablÃ¤sse prÃ¼fen. Tarierweste hÃ¤lt dicht und fÃ¼llt sich einwandfrei.", false)
            } else {
                ChecklistItem("B", "Buoyancy (BCD)", "Check power inflator, oral inflation, and dump valves. Verify BCD holds air and inflates effortlessly.", false)
            },
            if (isGerman) {
                ChecklistItem("W", "Blei (Weights)", "Bleigurt oder integrierte Bleitaschen mit RechtsauslÃ¶sung prÃ¼fen. Trimmblei sichern.", false)
            } else {
                ChecklistItem("W", "Weights", "Verify weight belt or integrated pouches right-hand release. Check trim weights and security.", false)
            },
            if (isGerman) {
                ChecklistItem("R", "VerschlÃ¼sse (Releases)", "Schultergurte, Brustgurt, Bauchgurt und Flaschenspanngurt auf festen Sitz prÃ¼fen.", false)
            } else {
                ChecklistItem("R", "Releases", "Check shoulder straps, chest clip, cummerbund, and tank cylinder cam band tightness.", false)
            },
            if (isGerman) {
                ChecklistItem("A", "Atemregler (Air)", "3 tiefe AtemzÃ¼ge aus Hauptregler und Oktopus bei gleichzeitigem Blick auf das Finimeter (SPG).", false)
            } else {
                ChecklistItem("A", "Air & Regulators", "Breathe 3 deep cycles from primary and alternate octopus while watching the SPG pressure gauge.", false)
            },
            if (isGerman) {
                ChecklistItem("F", "Finaler Check (Final OK)", "Maske antibeschlagen, Flossen an, Computer aktiv, Kompass peilen, Signale abgestimmt. Bereit zum Abtauchen!", false)
            } else {
                ChecklistItem("F", "Final OK", "Mask defogged, fins on, computer active, compass set, entry signals agreed upon. Ready to splash!", false)
            }
        )
    }

    val completedCount = checklistItems.count { it.isChecked }
    val isAllChecked = completedCount == checklistItems.size
    val progressFraction = completedCount.toFloat() / checklistItems.size.toFloat()

    val headerBorderColor by animateColorAsState(
        targetValue = if (isAllChecked) SeaGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        label = "headerBorderColor"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mission Ready Status Board Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAllChecked) SeaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.5.dp, headerBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isAllChecked) SeaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (isAllChecked) SeaGreen else MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isAllChecked) Icons.Rounded.CheckCircle else Icons.Rounded.SafetyCheck,
                                        contentDescription = null,
                                        tint = if (isAllChecked) SeaGreen else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isAllChecked)
                                        (if (isGerman) "BWRAF: 100% BEREIT ZUM TAUCHEN" else "BWRAF: 100% MISSION READY")
                                    else
                                        strings.preDiveChecklistTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (isAllChecked) SeaGreen else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isGerman)
                                        "$completedCount von ${checklistItems.size} SicherheitsprÃ¼fungen mit Buddy bestÃ¤tigt"
                                    else
                                        "$completedCount of ${checklistItems.size} checks verified with buddy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAllChecked) SeaGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, if (isAllChecked) SeaGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${(progressFraction * 100).toInt()}%",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isAllChecked) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tactical 5-Segment Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 0 until checklistItems.size) {
                            val isStepChecked = checklistItems[i].isChecked
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp),
                                shape = RoundedCornerShape(3.dp),
                                color = if (isStepChecked) SeaGreen else MaterialTheme.colorScheme.surfaceVariant
                            ) {}
                        }
                    }
                }
            }
        }

        // Tactical Checklist Items (B, W, R, A, F)
        items(checklistItems, key = { it.letter }) { item ->
            val cardBgColor by animateColorAsState(
                targetValue = if (item.isChecked) SeaGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceContainerLow,
                label = "cardBgColor"
            )
            val cardBorderColor by animateColorAsState(
                targetValue = if (item.isChecked) SeaGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                label = "cardBorderColor"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val index = checklistItems.indexOf(item)
                        if (index != -1) {
                            checklistItems[index] = item.copy(isChecked = !item.isChecked)
                        }
                    }
                    .testTag("checklist_item_${item.letter}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = cardBgColor),
                border = BorderStroke(1.2.dp, cardBorderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tactical Call-Letter Badge
                    Surface(
                        shape = CircleShape,
                        color = if (item.isChecked) SeaGreen else MaterialTheme.colorScheme.primary,
                        border = BorderStroke(
                            1.dp,
                            if (item.isChecked) SeaGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (item.isChecked) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = item.letter,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isChecked) SeaGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { checked ->
                            val index = checklistItems.indexOf(item)
                            if (index != -1) {
                                checklistItems[index] = item.copy(isChecked = checked)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SeaGreen,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
        }

        // Action Buttons Row: Reset & Quick Complete
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ScubaSecondaryButton(
                    text = if (isGerman) "ZurÃ¼cksetzen" else "Reset All",
                    icon = Icons.Rounded.Refresh,
                    onClick = {
                        for (i in 0 until checklistItems.size) {
                            checklistItems[i] = checklistItems[i].copy(isChecked = false)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                ScubaPrimaryButton(
                    text = if (isGerman) "Alle BestÃ¤tigen" else "Verify All",
                    icon = Icons.Rounded.CheckCircle,
                    onClick = {
                        for (i in 0 until checklistItems.size) {
                            checklistItems[i] = checklistItems[i].copy(isChecked = true)
                        }
                    },
                    containerColor = SeaGreen,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1.2f)
                )
            }
        }
    }
}

private data class ChecklistItem(
    val letter: String,
    val title: String,
    val description: String,
    val isChecked: Boolean
)

@Composable
private fun AddCustomDiveSpotDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        region: String,
        country: String,
        minDepth: Int,
        maxDepth: Int,
        difficulty: String,
        entryType: String,
        waterType: String,
        notes: String,
        emergencyContact: String,
        latitude: Double,
        longitude: Double
    ) -> Unit
) {
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    var siteName by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var minDepthText by remember { mutableStateOf("5") }
    var maxDepthText by remember { mutableStateOf("25") }
    var difficulty by remember { mutableStateOf("INTERMEDIATE") }
    var entryType by remember { mutableStateOf("BOAT") }
    var waterType by remember { mutableStateOf("SALT") } // SALT or FRESH
    var emergencyContact by remember { mutableStateOf("+112 / DAN Europe: +39-06-695-4000") }
    var notes by remember { mutableStateOf("") }
    var latitudeText by remember { mutableStateOf("35.8922") }
    var longitudeText by remember { mutableStateOf("14.4258") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("add_custom_spot_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isGerman) "Eigener Tauchplatz" else "Add Custom Dive Spot",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGerman) "Individuellen Spot anlegen & navigieren" else "Register custom dive site & GPS coords",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Spot Name
                OutlinedTextField(
                    value = siteName,
                    onValueChange = { siteName = it },
                    label = { Text(if (isGerman) "Tauchplatz-Name *" else "Dive Spot Name *") },
                    placeholder = { Text(if (isGerman) "z.B. Secret Reef Pinnacles" else "e.g. Secret Reef Pinnacles") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("input_custom_spot_name"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Region & Country
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = region,
                        onValueChange = { region = it },
                        label = { Text(if (isGerman) "Region" else "Region") },
                        placeholder = { Text("z.B. Gozo") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        label = { Text(if (isGerman) "Land" else "Country") },
                        placeholder = { Text("z.B. Malta") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Min & Max Depth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = minDepthText,
                        onValueChange = { minDepthText = it.filter { ch -> ch.isDigit() } },
                        label = { Text(if (isGerman) "Min Tiefe (m)" else "Min Depth (m)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = maxDepthText,
                        onValueChange = { maxDepthText = it.filter { ch -> ch.isDigit() } },
                        label = { Text(if (isGerman) "Max Tiefe (m)" else "Max Depth (m)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Difficulty Selector
                Column {
                    Text(
                        text = if (isGerman) "Schwierigkeit" else "Difficulty",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("BEGINNER", "INTERMEDIATE", "ADVANCED").forEach { diff ->
                            val isSelected = difficulty == diff
                            val label = when (diff) {
                                "BEGINNER" -> if (isGerman) "Einsteiger" else "Beginner"
                                "INTERMEDIATE" -> if (isGerman) "Mittel" else "Intermed."
                                else -> if (isGerman) "Erfahren" else "Advanced"
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { difficulty = diff },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Entry Type & Water Type Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Entry Type
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isGerman) "Einstieg" else "Entry Type",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("BOAT" to "ðŸš¤ Boat", "SHORE" to "ðŸ–ï¸ Shore").forEach { (type, label) ->
                                val isSelected = entryType == type
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { entryType = type },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) OceanSecondary else MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Water Type
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isGerman) "Wasserart" else "Water Type",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("SALT" to "ðŸŒŠ Salt", "FRESH" to "ðŸ’§ Fresh").forEach { (wType, label) ->
                                val isSelected = waterType == wType
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { waterType = wType },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DepthNavy else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // GPS Coordinates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = { latitudeText = it },
                        label = { Text("Breitengrad (Lat)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = longitudeText,
                        onValueChange = { longitudeText = it },
                        label = { Text("LÃ¤ngengrad (Lng)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Emergency Contact
                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text(if (isGerman) "Notfallkontakt / Druckkammer" else "Emergency Contact / Chamber") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isGerman) "Notizen & Highlights" else "Notes & Highlights") },
                    placeholder = { Text(if (isGerman) "z.B. SchÃ¶ne Korallenwand, Sichtweite meist exzellent" else "e.g. Beautiful wall dive, gorgonian fans") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Save button
                ScubaPrimaryButton(
                    text = if (isGerman) "Tauchplatz anlegen & speichern" else "Create & Save Dive Spot",
                    icon = Icons.Rounded.Check,
                    enabled = siteName.isNotBlank(),
                    onClick = {
                        val minD = minDepthText.toIntOrNull() ?: 5
                        val maxD = maxDepthText.toIntOrNull() ?: 20
                        val lat = latitudeText.toDoubleOrNull() ?: 35.8922
                        val lng = longitudeText.toDoubleOrNull() ?: 14.4258
                        onSave(
                            siteName,
                            region.ifBlank { "Custom Region" },
                            country.ifBlank { "Ocean" },
                            minD,
                            maxD,
                            difficulty,
                            entryType,
                            waterType,
                            notes,
                            emergencyContact,
                            lat,
                            lng
                        )
                    },
                    testTag = "btn_submit_custom_spot"
                )
            }
        }
    }
}

@Composable
private fun SaveDraftDiveLogDialog(
    draft: PlannedDiveDraft,
    allSpots: List<DiveSpot>,
    onDismiss: () -> Unit,
    onConfirmSave: (siteName: String, location: String, notes: String) -> Unit
) {
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    var selectedSiteName by remember { mutableStateOf(draft.siteName) }
    var selectedLocation by remember { mutableStateOf(draft.location) }
    var notesText by remember { mutableStateOf(draft.notes) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("save_draft_dive_log_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isGerman) "Geplanten Tauchgang loggen" else "Save as Planned Dive Log",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGerman) "Ãœbernahme in dein persÃ¶nliches Logbuch" else "Pre-populates your official logbook",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                }

                // Telemetry Summary Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isGerman) "TIEFE" else "DEPTH",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${draft.maxDepthMeters.toInt()} m",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isGerman) "GASMIX" else "GAS MIX",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = draft.gasType,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isGerman) "ZEIT" else "TIME",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${draft.plannedBottomTimeMin} min",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = OceanTertiary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isGerman) "WENDEDRUCK" else "TURN",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${draft.turnPressureBar} bar",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = SandGold
                            )
                        }
                    }
                }

                // Quick spot selector chips
                if (allSpots.isNotEmpty()) {
                    Column {
                        Text(
                            text = if (isGerman) "Schnellauswahl Tauchplatz:" else "Quick Select Dive Site:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allSpots.take(6).forEach { spot ->
                                val isSelected = selectedSiteName == spot.name
                                Surface(
                                    modifier = Modifier.clickable {
                                        selectedSiteName = spot.name
                                        selectedLocation = "${spot.region}, ${spot.country}"
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = spot.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Site Name
                OutlinedTextField(
                    value = selectedSiteName,
                    onValueChange = { selectedSiteName = it },
                    label = { Text(if (isGerman) "Tauchplatz-Name" else "Dive Site Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                )

                // Location
                OutlinedTextField(
                    value = selectedLocation,
                    onValueChange = { selectedLocation = it },
                    label = { Text(if (isGerman) "Ort / Region" else "Location / Region") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                )

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text(if (isGerman) "Planungsnotizen" else "Planning Notes") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Confirm Save Button
                ScubaPrimaryButton(
                    text = if (isGerman) "In mein Logbuch eintragen" else "Save to My Dive Logbook",
                    icon = Icons.Rounded.BookmarkAdd,
                    onClick = {
                        onConfirmSave(
                            selectedSiteName.ifBlank { "Planned Dive" },
                            selectedLocation.ifBlank { "Ocean Spot" },
                            notesText
                        )
                    },
                    testTag = "btn_confirm_save_draft_log"
                )
            }
        }
    }
}

private fun openExternalMap(context: Context, lat: Double, lng: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$lat,$lng?q=${Uri.encode(label)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallbackUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
        } catch (_: Exception) {
            try {
                val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
            } catch (_: Exception) {}
        }
    }
}

private fun dialPhoneNumber(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.replace(" ", "")}"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

