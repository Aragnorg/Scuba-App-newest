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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.SafetyCheck
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Shield
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
import com.example.data.repository.DiveSpotsData
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
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

    val allSpots = remember { DiveSpotsData.allDiveSpots }

    val filteredSpots = remember(searchQuery, selectedDifficultyFilter, selectedCountryFilter) {
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

    Column(
        modifier = modifier
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

                // Sub-Tabs for Dive Planning
                TabRow(
                    selectedTabIndex = selectedSubTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                ) {
                    Tab(
                        selected = selectedSubTab == 0,
                        onClick = { selectedSubTab = 0 },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                strings.subTabDiveSpots,
                                fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        icon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_planning_spots")
                    )
                    Tab(
                        selected = selectedSubTab == 1,
                        onClick = { selectedSubTab = 1 },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                strings.subTabEmergencyInfo,
                                fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        icon = { Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_planning_emergency")
                    )
                    Tab(
                        selected = selectedSubTab == 2,
                        onClick = { selectedSubTab = 2 },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                strings.subTabGasPlanner,
                                fontWeight = if (selectedSubTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        icon = { Icon(Icons.Rounded.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_planning_calculator")
                    )
                    Tab(
                        selected = selectedSubTab == 3,
                        onClick = { selectedSubTab = 3 },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                strings.subTabPreDiveChecklist,
                                fontWeight = if (selectedSubTab == 3) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        icon = { Icon(Icons.Rounded.SafetyCheck, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_planning_checklist")
                    )
                }
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
                onEmergencyClick = { spotForEmergencyModal = it }
            )
            1 -> EmergencyInfoDossierTab(
                spots = allSpots,
                onSelectSpotEmergency = { spotForEmergencyModal = it }
            )
            2 -> GasAndModCalculatorTab(
                isImperial = isImperial
            )
            3 -> PreDiveBuddyChecklistTab()
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
    onEmergencyClick: (DiveSpot) -> Unit
) {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(strings.searchSpotsPlaceholder, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.searchSpotsPlaceholder) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = if (isGerman) "Löschen" else "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spot_search_input")
            )
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
                            text = if (isGerman) "Keine Tauchplätze entsprechen deinem Filter" else "No dive spots match your filter",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isGerman) "Suchfilter zurücksetzen oder 'Alle Erfahrungsstufen' wählen" else "Try clearing search filters or selecting All Levels",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    val difficultyColor = when (spot.difficulty) {
        "BEGINNER" -> SeaGreen
        "INTERMEDIATE" -> OceanPrimary
        "ADVANCED" -> Color(0xFFD97706)
        else -> Color(0xFFC53030)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("spot_card_${spot.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = difficultyColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, difficultyColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = spot.difficulty,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = difficultyColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "${spot.spotType} • ${spot.entryType}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

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

                // Mini Map Visual Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Map, contentDescription = "View Map", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Key Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpotMetricPill(title = if (isGerman) "Tiefe" else "Depth", value = depthLabel, modifier = Modifier.weight(1f))
                SpotMetricPill(title = if (isGerman) "Sicht" else "Visibility", value = visibilityLabel, modifier = Modifier.weight(1f))
                val currentText = if (isGerman) when (spot.currentStrength.uppercase()) {
                    "STRONG" -> "Stark"
                    "MODERATE" -> "Mäßig"
                    "MILD", "LOW", "LIGHT" -> "Leicht"
                    "NONE" -> "Keine"
                    else -> spot.currentStrength
                } else spot.currentStrength
                SpotMetricPill(title = if (isGerman) "Strömung" else "Current", value = currentText, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Highlights snippet
            Text(
                text = "✨ " + spot.highlights.take(2).joinToString(" • "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emergency info fast access
                TextButton(
                    onClick = onEmergencyClick,
                    modifier = Modifier.testTag("spot_emergency_btn_${spot.id}"),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isGerman) "Notfalldossier" else "Emergency Dossier", color = Color(0xFFE53E3E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("spot_view_map_btn_${spot.id}")
                ) {
                    Icon(Icons.Rounded.Map, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isGerman) "Briefing & Karte" else "Briefing & Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
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
            shape = RoundedCornerShape(24.dp),
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
                    shape = RoundedCornerShape(16.dp),
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
                                text = "${String.format("%.4f", spot.latitude)}° N, ${String.format("%.4f", spot.longitude)}° E",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = {
                                openExternalMap(context, spot.latitude, spot.longitude, spot.name)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("open_external_map_btn")
                        ) {
                            Icon(Icons.Rounded.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Google Maps", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
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
                    shape = RoundedCornerShape(16.dp),
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
                            Text(if (isGerman) "Strömung & Drift:" else "Current & Drift:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Button(
                        onClick = onEmergencyClick,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("spot_detail_emergency_btn")
                    ) {
                        Icon(Icons.Rounded.HealthAndSafety, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isGerman) "Notfalldossier" else "Emergency Dossier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("spot_detail_close_btn")
                    ) {
                        Text(strings.close)
                    }
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

            drawPath(contour1, color = AquaCyan.copy(alpha = 0.35f), style = Stroke(width = 2f))
            drawPath(contour2, color = OceanPrimary.copy(alpha = 0.45f), style = Stroke(width = 2f))
            drawPath(contour3, color = Color(0xFF0A2540).copy(alpha = 0.6f), style = Stroke(width = 2f))

            // 2. Draw Compass Rose in top right
            val cx = w - 40f
            val cy = 40f
            drawCircle(color = AquaCyan.copy(alpha = 0.2f), radius = 22f, center = Offset(cx, cy))
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
                    "MOORING" -> AquaCyan
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
                    border = BorderStroke(0.5.dp, AquaCyan.copy(alpha = 0.5f))
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
            shape = RoundedCornerShape(24.dp),
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
                        Icon(Icons.Rounded.Close, contentDescription = if (isGerman) "Schließen" else "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Chamber Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.LocalHospital, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isGerman) "NÄCHSTE DRUCKKAMMER" else "NEAREST HYPERBARIC CHAMBER",
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
                            text = "${emergency.chamberLocation} • ~${emergency.chamberDistanceKm} km ${if (isGerman) "entfernt" else "away"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { dialPhoneNumber(context, emergency.chamberPhone) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("call_chamber_btn")
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${if (isGerman) "Druckkammer anrufen" else "Call Chamber"}: ${emergency.chamberPhone}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Radio & Maritime Contacts Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        EmergencyRowItem(
                            icon = Icons.Rounded.Radio,
                            title = if (isGerman) "Küstenwache / Seefunk (VHF):" else "Coast Guard / Marine VHF:",
                            value = emergency.coastGuardVHF,
                            color = OceanPrimary
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
                            color = AquaCyan
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

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("close_emergency_modal_btn")
                    ) {
                        Text(if (isGerman) "Dossier schließen" else "Close Dossier", fontWeight = FontWeight.Bold)
                    }
                }
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
                shape = RoundedCornerShape(22.dp),
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
                        Button(
                            onClick = { dialPhoneNumber(context, "+19196849111") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DAN America", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { dialPhoneNumber(context, "+39066954000") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DAN Europe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
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
                shape = RoundedCornerShape(18.dp),
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
                        text = "🏥 ${emergency.nearestChamberName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "📻 ${emergency.coastGuardVHF}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { dialPhoneNumber(context, emergency.chamberPhone) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${if (isGerman) "Anrufen" else "Call"}: ${emergency.chamberPhone}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GasAndModCalculatorTab(
    isImperial: Boolean
) {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    var o2Percent by remember { mutableIntStateOf(32) } // Standard EAN32
    var ppo2Limit by remember { mutableDoubleStateOf(1.4) } // Standard 1.4 bar bottom limit
    var plannedDepth by remember { mutableDoubleStateOf(28.0) } // 28m
    var plannedBottomTime by remember { mutableIntStateOf(35) } // 35 min
    var tankVolume by remember { mutableIntStateOf(12) } // 12L
    var tankStartPressure by remember { mutableIntStateOf(200) } // 200 bar

    val result = remember(o2Percent, ppo2Limit, plannedDepth, plannedBottomTime, tankVolume, tankStartPressure) {
        DiveSpotsData.calculateGasPlan(
            o2Percent = o2Percent,
            ppo2Limit = ppo2Limit,
            plannedDepthMeters = plannedDepth,
            plannedBottomTimeMin = plannedBottomTime,
            tankStartPressureBar = tankStartPressure,
            tankVolumeLiters = tankVolume
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Calculator Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = strings.gasPlannerTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = strings.gasPlannerSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key Calculated Results Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = if (result.isPlanSafe) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, if (result.isPlanSafe) SeaGreen.copy(alpha = 0.6f) else MaterialTheme.colorScheme.error)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isGerman) "MAXIMALE EINSATZTIEFE (MOD)" else "MAX OPERATING DEPTH (MOD)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isImperial) "${(result.maxOperatingDepthMeters * 3.28084).toInt()} ft" else "${result.maxOperatingDepthMeters.toInt()} m",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (result.isPlanSafe) SeaGreen else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (isGerman) "WENDEDRUCK (1/3-REGEL)" else "TURN PRESSURE (1/3 RULE)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isImperial) "${(result.turnPressureBar * 14.5038).toInt()} PSI" else "${result.turnPressureBar} bar",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (result.warningMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = result.warningMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
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
                    // 1. Oxygen Percentage
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(strings.nitroxO2Fraction, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                                color = if (ppo2Limit > 1.4) Color(0xFFD97706) else SeaGreen
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
                                color = MaterialTheme.colorScheme.primary
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
    }
}

@Composable
private fun PreDiveBuddyChecklistTab() {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    val checklistItems = remember(isGerman) {
        mutableStateListOf(
            if (isGerman) {
                ChecklistItem("B", "Tarierung (BCD)", "Inflator, Mundstück und Schnellablässe prüfen. Tarierweste hält dicht und füllt sich einwandfrei.", false)
            } else {
                ChecklistItem("B", "Buoyancy (BCD)", "Check power inflator, oral inflation, and dump valves. Verify BCD holds air and inflates effortlessly.", false)
            },
            if (isGerman) {
                ChecklistItem("W", "Blei (Weights)", "Bleigurt oder integrierte Bleitaschen mit Rechtsauslösung prüfen. Trimmblei sichern.", false)
            } else {
                ChecklistItem("W", "Weights", "Verify weight belt or integrated pouches right-hand release. Check trim weights and security.", false)
            },
            if (isGerman) {
                ChecklistItem("R", "Verschlüsse (Releases)", "Schultergurte, Brustgurt, Bauchgurt und Flaschenspanngurt auf festen Sitz prüfen.", false)
            } else {
                ChecklistItem("R", "Releases", "Check shoulder straps, chest clip, cummerbund, and tank cylinder cam band tightness.", false)
            },
            if (isGerman) {
                ChecklistItem("A", "Atemregler (Air)", "3 tiefe Atemzüge aus Hauptregler und Oktopus bei gleichzeitigem Blick auf das Finimeter (SPG).", false)
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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAllChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(
                    1.dp,
                    if (isAllChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAllChecked) Icons.Rounded.CheckCircle else Icons.Rounded.SafetyCheck,
                                contentDescription = null,
                                tint = if (isAllChecked) SeaGreen else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = strings.preDiveChecklistTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isGerman) "$completedCount von ${checklistItems.size} Sicherheitsprüfungen mit Buddy bestätigt" else "$completedCount of ${checklistItems.size} checks verified with buddy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        items(checklistItems, key = { it.letter }) { item ->
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isChecked) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(
                    1.dp,
                    if (item.isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (item.isChecked) SeaGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(item.letter, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isChecked) SeaGreen else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { checked ->
                            val index = checklistItems.indexOf(item)
                            if (index != -1) {
                                checklistItems[index] = item.copy(isChecked = checked)
                            }
                        },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = {
                        for (i in 0 until checklistItems.size) {
                            checklistItems[i] = checklistItems[i].copy(isChecked = false)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(if (isGerman) "Checkliste zurücksetzen" else "Reset Checklist")
                }
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

private fun openExternalMap(context: Context, lat: Double, lng: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            context.startActivity(browserIntent)
        } catch (_: Exception) {}
    }
}

private fun dialPhoneNumber(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.replace(" ", "")}"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

