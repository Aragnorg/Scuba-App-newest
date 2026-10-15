package com.example.ui.screens.diveops

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiveEventEntity
import com.example.data.model.OrganizationEntity
import com.example.data.model.OrganizationMemberEntity
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import com.example.util.DocumentExpirationHelper
import com.example.util.ExpirationAlertLevel

/**
 * Tab 1: Ausfahrten & Trips (DiveOps Engine)
 * Operational planner for boat trips, shore dives, courses, and club outings.
 * Features 1-Click Check-in with real-time Auto-Safety-Gate verification.
 */
@Composable
fun DiveOpsEventsTab(
    viewModel: AquaViewModel,
    organization: OrganizationEntity,
    primaryAccentColor: Color,
    onNavigateToBuddyBoard: (DiveEventEntity) -> Unit,
    onNavigateToManifest: (DiveEventEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.currentOrgEvents.collectAsState()
    val members by viewModel.currentOrgMembers.collectAsState()
    val strings = LocalAppStrings.current

    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var eventForCheckIn by remember { mutableStateOf<DiveEventEntity?>(null) }
    var eventToDelete by remember { mutableStateOf<DiveEventEntity?>(null) }
    var eventForDossier by remember { mutableStateOf<DiveEventEntity?>(null) }
    var eventForEdit by remember { mutableStateOf<DiveEventEntity?>(null) }

    val filteredEvents = remember(events, selectedTypeFilter) {
        when (selectedTypeFilter) {
            "ALL" -> events
            "BOAT_TRIP" -> events.filter { it.isBoatTrip }
            "SHORE_DIVE" -> events.filter { it.isShoreDive }
            "COURSE_TRAINING" -> events.filter { it.isCourse }
            "CLUB_OUTING" -> events.filter { it.isClubOuting }
            else -> events
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Section Title & + Ausfahrt planen Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.tabDiveOpsEvents,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ausfahrten, Hausriff-Tauchgänge & Kurse",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+ Ausfahrt planen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedTypeFilter == "ALL",
                    onClick = { selectedTypeFilter = "ALL" },
                    label = { Text("Alle (${events.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
                FilterChip(
                    selected = selectedTypeFilter == "BOAT_TRIP",
                    onClick = { selectedTypeFilter = "BOAT_TRIP" },
                    label = { Text("🚤 Boot", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
                FilterChip(
                    selected = selectedTypeFilter == "SHORE_DIVE",
                    onClick = { selectedTypeFilter = "SHORE_DIVE" },
                    label = { Text("🌊 Hausriff/See", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
                FilterChip(
                    selected = selectedTypeFilter == "COURSE_TRAINING",
                    onClick = { selectedTypeFilter = "COURSE_TRAINING" },
                    label = { Text("🎓 Ausbildung", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
                FilterChip(
                    selected = selectedTypeFilter == "CLUB_OUTING",
                    onClick = { selectedTypeFilter = "CLUB_OUTING" },
                    label = { Text("🏛️ Club", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
            }
        }

        // Events List
        if (filteredEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⚓", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Keine Ausfahrten geplant",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Plane die nächste Bootsausfahrt oder ein See-Event.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor)
                        ) {
                            Text("+ Erste Ausfahrt anlegen")
                        }
                    }
                }
            }
        } else {
            items(filteredEvents, key = { it.id }) { event ->
                EventCard(
                    event = event,
                    primaryAccentColor = primaryAccentColor,
                    onOpenDossier = { eventForDossier = event },
                    onCheckIn = { eventForCheckIn = event },
                    onNavigateToBuddyBoard = { onNavigateToBuddyBoard(event) },
                    onNavigateToManifest = { onNavigateToManifest(event) },
                    onDelete = { eventToDelete = event }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- Dialogs ---
    if (showCreateDialog) {
        CreateDiveEventDialog(
            organization = organization,
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, eventType, siteName, location, eventDate, meetingTime, departureTime, maxCap, leadGuide, maxDepth, minCert, emergencyBriefing, chamberName, chamberPhone, chamberDistanceTime, coastGuardVHF, emergencyNotes, entryExitInfo, waterConditions, turnPressureBar, equipmentRequirements, hazardsAndRules ->
                viewModel.createDiveEvent(
                    title = title,
                    eventType = eventType,
                    siteName = siteName,
                    location = location,
                    eventDate = eventDate,
                    meetingTime = meetingTime,
                    departureTime = departureTime,
                    maxCapacity = maxCap,
                    leadGuideName = leadGuide,
                    maxDepthMeters = maxDepth,
                    requiredMinCert = minCert,
                    emergencyBriefing = emergencyBriefing,
                    chamberName = chamberName,
                    chamberPhone = chamberPhone,
                    chamberDistanceTime = chamberDistanceTime,
                    coastGuardVHF = coastGuardVHF,
                    emergencyNotes = emergencyNotes,
                    entryExitInfo = entryExitInfo,
                    waterConditions = waterConditions,
                    turnPressureBar = turnPressureBar,
                    equipmentRequirements = equipmentRequirements,
                    hazardsAndRules = hazardsAndRules
                )
                showCreateDialog = false
            }
        )
    }

    eventForDossier?.let { dossierEvent ->
        val liveEvent = events.find { it.id == dossierEvent.id } ?: dossierEvent
        DiveEventDossierDialog(
            event = liveEvent,
            primaryAccentColor = primaryAccentColor,
            onDismiss = { eventForDossier = null },
            onEditEvent = { targetEvent ->
                eventForEdit = targetEvent
            },
            onCheckIn = { targetEvent ->
                eventForCheckIn = targetEvent
                eventForDossier = null
            },
            onNavigateToBuddyBoard = { targetEvent ->
                onNavigateToBuddyBoard(targetEvent)
                eventForDossier = null
            },
            onNavigateToManifest = { targetEvent ->
                onNavigateToManifest(targetEvent)
                eventForDossier = null
            }
        )
    }

    eventForEdit?.let { editTarget ->
        EditDiveEventDialog(
            event = editTarget,
            onDismiss = { eventForEdit = null },
            onConfirm = { updatedEvent ->
                viewModel.updateDiveEvent(updatedEvent)
                eventForEdit = null
            }
        )
    }

    if (eventForCheckIn != null) {
        CheckInDiverDialog(
            event = eventForCheckIn!!,
            members = members,
            onDismiss = { eventForCheckIn = null },
            onConfirm = { member, tank, gas, rentalGear, transferNote ->
                viewModel.checkInDiverForEvent(
                    event = eventForCheckIn!!,
                    member = member,
                    tankSelection = tank,
                    gasType = gas,
                    rentalGear = rentalGear,
                    transferCarPoolNote = transferNote
                )
                eventForCheckIn = null
            }
        )
    }

    if (eventToDelete != null) {
        AlertDialog(
            onDismissRequest = { eventToDelete = null },
            title = { Text("Ausfahrt löschen") },
            text = { Text("Möchtest du '${eventToDelete!!.title}' wirklich absagen und entfernen?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDiveEvent(eventToDelete!!.id)
                        eventToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { eventToDelete = null }) { Text("Abbrechen") }
            }
        )
    }
}

/**
 * Event Card displaying operational details, capacity, and quick actions.
 */
@Composable
fun EventCard(
    event: DiveEventEntity,
    primaryAccentColor: Color,
    onOpenDossier: () -> Unit,
    onCheckIn: () -> Unit,
    onNavigateToBuddyBoard: () -> Unit,
    onNavigateToManifest: () -> Unit,
    onDelete: () -> Unit
) {
    val typeIcon = when (event.eventType) {
        "BOAT_TRIP" -> "🚤"
        "SHORE_DIVE" -> "🌊"
        "COURSE_TRAINING" -> "🎓"
        else -> "🏛️"
    }

    val typeLabel = when (event.eventType) {
        "BOAT_TRIP" -> "Bootsausfahrt"
        "SHORE_DIVE" -> "Hausriff / See"
        "COURSE_TRAINING" -> "Kurs / Freiwasser"
        else -> "Vereinsausfahrt"
    }

    val capacityRatio = if (event.maxCapacity > 0) {
        (event.registeredCount.toFloat() / event.maxCapacity.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val capacityColor = when {
        event.registeredCount >= event.maxCapacity -> Color(0xFFE53E3E)
        capacityRatio >= 0.8f -> Color(0xFFD69E2E)
        else -> SeaGreen
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDossier() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Type pill, Title, Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = primaryAccentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$typeIcon $typeLabel",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccentColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Date & Meeting / Departure Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${event.eventDate}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "⏰ Treffen: ${event.meetingTime} • Start: ${event.departureTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Location & Depth / Cert Limits
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.siteName} (${event.location})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "Max ${event.maxDepthMeters.toInt()}m • Min: ${event.requiredMinCert}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Capacity indicator bar
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Kapazität: ${event.registeredCount} von ${event.maxCapacity} belegt",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = capacityColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (event.leadGuideName.isNotBlank()) {
                        Text(
                            text = "Guide: ${event.leadGuideName}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { capacityRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = capacityColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Micro-Badges for EAP & Briefing (Clean, uncluttered indicator)
            if (event.hasEmergencyPlan || event.hasBriefing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (event.hasEmergencyPlan) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFC53030).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "🏥 Kammer: ${event.chamberName.ifBlank { "EAP aktiv" }.take(28)}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC53030)
                            )
                        }
                    }
                    if (event.hasBriefing) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SeaGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "📋 Briefing hinterlegt",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SeaGreen
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Operational Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dossier & Briefing Shortcut
                OutlinedButton(
                    onClick = onOpenDossier,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Text(text = "ℹ️ Dossier", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // 1-Click Check-in
                Button(
                    onClick = onCheckIn,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1.1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "Check-in", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Buddy-Board shortcut
                OutlinedButton(
                    onClick = onNavigateToBuddyBoard,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "🤿 Board", fontSize = 10.sp)
                }

                // Manifest shortcut
                OutlinedButton(
                    onClick = onNavigateToManifest,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "📋 Manifest", fontSize = 10.sp)
                }
            }
        }
    }
}

/**
 * Create Dive Event Dialog:
 * Configures new trip, max depth, minimum cert requirement, guide, and briefing.
 */
@Composable
fun CreateDiveEventDialog(
    organization: OrganizationEntity,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        eventType: String,
        siteName: String,
        location: String,
        eventDate: String,
        meetingTime: String,
        departureTime: String,
        maxCap: Int,
        leadGuide: String,
        maxDepth: Float,
        minCert: String,
        emergencyBriefing: String,
        chamberName: String,
        chamberPhone: String,
        chamberDistanceTime: String,
        coastGuardVHF: String,
        emergencyNotes: String,
        entryExitInfo: String,
        waterConditions: String,
        turnPressureBar: Int,
        equipmentRequirements: String,
        hazardsAndRules: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf(if (organization.isClub) "CLUB_OUTING" else "BOAT_TRIP") }
    var siteName by remember { mutableStateOf(organization.waterBody.ifBlank { "Hausriff Ostseite" }) }
    var location by remember { mutableStateOf(organization.location.ifBlank { "Basis Steg" }) }
    var eventDate by remember { mutableStateOf("2026-10-20") }
    var meetingTime by remember { mutableStateOf("08:30") }
    var departureTime by remember { mutableStateOf("09:00") }
    var maxCapacity by remember { mutableStateOf("12") }
    var leadGuide by remember { mutableStateOf("Adrian Scheid") }
    var maxDepth by remember { mutableStateOf("25") }
    var minCert by remember { mutableStateOf("Open Water Diver") }
    var turnPressureBar by remember { mutableStateOf("120") }
    var emergencyBriefing by remember { mutableStateOf("VHF Funk Kanal 16, O2 Koffer an Bord, Nächste Kammer 20 Min.") }

    // Chamber & EAP data
    var chamberName by remember { mutableStateOf(if (organization.isClub) "Druckkammerzentrum Überlingen (Helios Spital)" else "Hyperbaric Medical Center Sharm El Sheikh") }
    var chamberPhone by remember { mutableStateOf(if (organization.isClub) "+49 7551 88-0" else "+20 69 3660922") }
    var chamberDistanceTime by remember { mutableStateOf(if (organization.isClub) "4 km / 7 Min." else "12 NM / 25 Min.") }
    var coastGuardVHF by remember { mutableStateOf("VHF Kanal 16") }
    var emergencyNotes by remember { mutableStateOf("O2-Koffer 100% Demand-System & AED vor Ort einsatzbereit.") }

    // Briefing data
    var entryExitInfo by remember { mutableStateOf("Einstieg über Steg/Treppe. Sicherer Notausstieg in Briefing-Skizze markiert.") }
    var waterConditions by remember { mutableStateOf("Sicht: 8-12m • Wassertemperatur: ca. 16°C • Leichte Strömung") }
    var equipmentRequirements by remember { mutableStateOf("Tauchcomputer, Kaltwasser-Regler & Signalboje (SMB) pro Team obligatorisch.") }
    var hazardsAndRules by remember { mutableStateOf("Maximaltiefe und Umkehrdruck strikt einhalten. Bootsverkehr beachten.") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("➕ Neue Tauchausfahrt planen", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel der Ausfahrt *") },
                    placeholder = { Text("z.B. Wracktauchen St. Peter") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Event Type Selector
                Text("Art des Events:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = eventType == "BOAT_TRIP",
                        onClick = { eventType = "BOAT_TRIP" },
                        label = { Text("🚤 Boot", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = eventType == "SHORE_DIVE",
                        onClick = { eventType = "SHORE_DIVE" },
                        label = { Text("🌊 Hausriff", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = eventType == "COURSE_TRAINING",
                        onClick = { eventType = "COURSE_TRAINING" },
                        label = { Text("🎓 Kurs", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = eventType == "CLUB_OUTING",
                        onClick = { eventType = "CLUB_OUTING" },
                        label = { Text("🏛️ Club", fontSize = 10.sp) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = siteName,
                        onValueChange = { siteName = it },
                        label = { Text("Tauchplatz *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Ort / Slipstelle") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Datum (YYYY-MM-DD) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = meetingTime,
                        onValueChange = { meetingTime = it },
                        label = { Text("Treffen") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                    OutlinedTextField(
                        value = departureTime,
                        onValueChange = { departureTime = it },
                        label = { Text("Start") },
                        singleLine = true,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = maxDepth,
                        onValueChange = { maxDepth = it },
                        label = { Text("Max Tiefe (m)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = turnPressureBar,
                        onValueChange = { turnPressureBar = it },
                        label = { Text("Turn (bar)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxCapacity,
                        onValueChange = { maxCapacity = it },
                        label = { Text("Max Plätze") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minCert,
                        onValueChange = { minCert = it },
                        label = { Text("Mindestbrevet") },
                        placeholder = { Text("OWD / CMAS*") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = leadGuide,
                        onValueChange = { leadGuide = it },
                        label = { Text("Dive Leader / Guide") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider()

                // Emergency & Chamber Section
                Text("🏥 Druckkammer & Notfallrettung", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC53030))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            chamberName = "Druckkammerzentrum Überlingen (Helios Spital)"
                            chamberPhone = "+49 7551 88-0"
                            chamberDistanceTime = "3.8 km / 6 Min."
                            coastGuardVHF = "Kanal 16 / Wasserschutzpolizei (112)"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("⚡ Bodensee", fontSize = 10.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            chamberName = "Hyperbaric Medical Center Sharm El Sheikh"
                            chamberPhone = "+20 69 3660922"
                            chamberDistanceTime = "12 NM / 25 Min."
                            coastGuardVHF = "VHF Kanal 16 / DSC CH 70"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("⚡ Sharm", fontSize = 10.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            chamberName = "Druckkammerzentrum Berlin Helios"
                            chamberPhone = "+49 30 9401-0"
                            chamberDistanceTime = "15 km / 20 Min."
                            coastGuardVHF = "VHF Kanal 16 / Leitstelle 112"
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("⚡ Berlin", fontSize = 10.sp)
                    }
                }

                OutlinedTextField(
                    value = chamberName,
                    onValueChange = { chamberName = it },
                    label = { Text("Nächste Druckkammer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chamberPhone,
                        onValueChange = { chamberPhone = it },
                        label = { Text("Kammer Tel / Notruf") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = chamberDistanceTime,
                        onValueChange = { chamberDistanceTime = it },
                        label = { Text("Fahrzeit / Distanz") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = coastGuardVHF,
                    onValueChange = { coastGuardVHF = it },
                    label = { Text("Seefunk VHF Kanal / Notruf") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = entryExitInfo,
                    onValueChange = { entryExitInfo = it },
                    label = { Text("Einstieg & Ausstieg") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = waterConditions,
                    onValueChange = { waterConditions = it },
                    label = { Text("Sicht, Wassertemperatur & Strömung") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || siteName.isBlank() || eventDate.isBlank()) {
                        errorMessage = "Bitte Titel, Tauchplatz und Datum eingeben."
                        return@Button
                    }
                    val capInt = maxCapacity.toIntOrNull() ?: 12
                    val depthFloat = maxDepth.toFloatOrNull() ?: 20f
                    val turnInt = turnPressureBar.toIntOrNull() ?: 120
                    onConfirm(
                        title, eventType, siteName, location, eventDate, meetingTime, departureTime,
                        capInt, leadGuide, depthFloat, minCert, emergencyBriefing,
                        chamberName, chamberPhone, chamberDistanceTime, coastGuardVHF, emergencyNotes,
                        entryExitInfo, waterConditions, turnInt, equipmentRequirements, hazardsAndRules
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("Ausfahrt planen", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

/**
 * 1-Click Check-in Dialog with Real-Time Auto-Safety-Gate:
 * Verifies TTU expiration on event date, diver depth limit vs planned event depth, and Nitrox MOD limits.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInDiverDialog(
    event: DiveEventEntity,
    members: List<OrganizationMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        member: OrganizationMemberEntity,
        tank: String,
        gas: String,
        rentalGear: List<String>,
        transferNote: String
    ) -> Unit
) {
    if (members.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Keine Personen vorhanden") },
            text = { Text("Bitte lege zuerst Mitglieder oder Gäste im Roster an.") },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var selectedMember by remember { mutableStateOf(members.first()) }
    var selectedTank by remember { mutableStateOf("12L Stahl lang (200 bar)") }
    var selectedGas by remember { mutableStateOf("Air 21%") }
    var selectedRentalGear by remember { mutableStateOf(setOf<String>()) }
    var transferNote by remember { mutableStateOf("") }

    val tankOptions = listOf(
        "10L Stahl (200 bar)",
        "12L Stahl lang (200 bar)",
        "12L Stahl kurz (200 bar)",
        "15L Stahl (200 bar)",
        "12L Alu (200 bar)",
        "Alu 80 cuft (11.1L)",
        "Doppel-7 (200 bar)",
        "Doppel-8.5 (232 bar)",
        "Doppel-12 (200 bar)"
    )

    val gasOptions = listOf("Air 21%", "Nitrox 32%", "Nitrox 36%", "Nitrox 40%")

    val rentalOptions = listOf(
        "Tarierjacket / Wing",
        "Atemregler-Set (1.+2. Stufe + Oktopus)",
        "Neoprenanzug (5mm/7mm)",
        "Tauchcomputer",
        "Flossen + Füßlinge",
        "Maske + Schnorchel",
        "Blei / Bleigurt"
    )

    // --- Live Auto-Safety-Gate Evaluation ---
    val safetyIssues = remember(selectedMember, selectedGas, event) {
        val list = mutableListOf<String>()

        // 1. TTU validity check
        val rawTtu = selectedMember.ttuExpiryDate.trim()
        if (rawTtu.isBlank()) {
            list.add("Keine TTU / ärztliches Tauchtauglichkeitsattest hinterlegt!")
        } else {
            val ttuInfo = DocumentExpirationHelper.computeDateExpiration(rawTtu, isGerman = true)
            if (ttuInfo.level == ExpirationAlertLevel.EXPIRED) {
                list.add("TTU ist abgelaufen ($rawTtu)!")
            } else if (rawTtu < event.eventDate) {
                list.add("TTU läuft vor dem Ausfahrtstag ab ($rawTtu < ${event.eventDate})!")
            }
        }

        // 2. Brevet Depth Limit
        val certLower = selectedMember.certLevel.lowercase()
        val maxAllowedDepth = when {
            certLower.contains("scuba diver") -> 12.0f
            certLower.contains("open water") || certLower.contains("cmas *") || certLower.contains("owd") -> 18.0f
            certLower.contains("advanced") || certLower.contains("cmas **") || certLower.contains("aowd") -> 30.0f
            else -> 40.0f
        }
        if (event.maxDepthMeters > maxAllowedDepth) {
            list.add("Brevet '${selectedMember.certLevel}' (max ${maxAllowedDepth.toInt()}m) reicht nicht für geplante Ausfahrtstiefe (${event.maxDepthMeters.toInt()}m)!")
        }

        // 3. Nitrox Gas MOD Calculation
        val calculatedMod = when (selectedGas) {
            "Nitrox 32%" -> 33.7f
            "Nitrox 36%" -> 28.8f
            "Nitrox 40%" -> 25.0f
            else -> 56.6f
        }
        if (event.maxDepthMeters > calculatedMod) {
            list.add("Geplante Tiefe (${event.maxDepthMeters.toInt()}m) überschreitet MOD von $selectedGas (${calculatedMod.toInt()}m)!")
        }

        list
    }

    val safetyPassed = safetyIssues.isEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "1-Klick Check-in & Safety-Gate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ausfahrt: ${event.title} (${event.siteName}, max ${event.maxDepthMeters.toInt()}m)",
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
                // 1. Select Member
                Text("1. Taucher auswählen:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    members.forEach { m ->
                        val isSelected = m.id == selectedMember.id
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OceanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            border = BorderStroke(1.dp, if (isSelected) OceanPrimary else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMember = m }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = m.fullName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${m.certLevel} • ${m.totalLoggedDives} Dives • TTU: ${m.ttuExpiryDate.ifBlank { "Keine" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 2. Tank Choice
                Text("2. Flaschen-Konfiguration:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tankOptions.forEach { tank ->
                        FilterChip(
                            selected = selectedTank == tank,
                            onClick = { selectedTank = tank },
                            label = { Text(tank, fontSize = 10.sp) }
                        )
                    }
                }

                // 3. Gas Mix & MOD
                Text("3. Atemgas (PPO2 1.4 bar Limit):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    gasOptions.forEach { gas ->
                        val mod = when (gas) {
                            "Nitrox 32%" -> "33m"
                            "Nitrox 36%" -> "28m"
                            "Nitrox 40%" -> "25m"
                            else -> "56m"
                        }
                        FilterChip(
                            selected = selectedGas == gas,
                            onClick = { selectedGas = gas },
                            label = { Text("$gas (MOD $mod)", fontSize = 10.sp) }
                        )
                    }
                }

                HorizontalDivider()

                // 4. Rental Gear Checklist
                Text("4. Benötigtes Leih-Equipment:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    rentalOptions.forEach { gear ->
                        val checked = selectedRentalGear.contains(gear)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedRentalGear = if (checked) {
                                        selectedRentalGear - gear
                                    } else {
                                        selectedRentalGear + gear
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    selectedRentalGear = if (isChecked) {
                                        selectedRentalGear + gear
                                    } else {
                                        selectedRentalGear - gear
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = OceanPrimary)
                            )
                            Text(text = gear, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = transferNote,
                    onValueChange = { transferNote = it },
                    label = { Text("Transfer / Fahrgemeinschaft") },
                    placeholder = { Text("z.B. Biete 2 Plätze ab Parkplatz") },
                    modifier = Modifier.fillMaxWidth()
                )

                // --- 5. Auto-Safety-Gate Live Status Display ---
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (safetyPassed) SeaGreen.copy(alpha = 0.12f) else Color(0xFFE53E3E).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (safetyPassed) SeaGreen else Color(0xFFE53E3E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (safetyPassed) "✅" else "⛔", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (safetyPassed) "Safety Gate: Freigegeben" else "Safety Gate: Blockiert!",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (safetyPassed) SeaGreen else Color(0xFFC53030)
                            )
                        }

                        if (safetyPassed) {
                            Text(
                                text = "• TTU am Ausfahrtstag (${event.eventDate}) gültig\n• Brevet-Tiefenlimit ausreichend für ${event.maxDepthMeters.toInt()}m\n• Gas MOD sicher kalkuliert",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = SeaGreen
                            )
                        } else {
                            safetyIssues.forEach { issue ->
                                Text(
                                    text = "• $issue",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = Color(0xFFC53030),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedMember, selectedTank, selectedGas, selectedRentalGear.toList(), transferNote)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (safetyPassed) SeaGreen else Color(0xFFC53030)
                )
            ) {
                Text(if (safetyPassed) "Einchecken (Freigegeben)" else "Trotzdem einchecken (Gesperrt)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
