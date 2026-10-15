@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens.excursions

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiveEventEntity
import com.example.data.model.DiveEventParticipantEntity
import com.example.data.model.OrganizationEntity
import com.example.ui.screens.diveops.DiveEventDossierDialog
import com.example.ui.screens.diveops.DiverEventRegistrationDialog
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

/**
 * Diver Excursions Screen:
 * Allows active divers to browse partner dive centers and clubs, connect/link with bases,
 * explore scheduled excursions, inspect deep site dossiers (including dive site maps),
 * and register/manage their dive excursion bookings.
 */
@Composable
fun DiverExcursionsScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allEvents by viewModel.allUpcomingEvents.collectAsState()
    val allOrganizations by viewModel.allOrganizations.collectAsState()
    val myRegistrations by viewModel.myDiverRegistrations.collectAsState()
    val myMemberships by viewModel.myMemberships.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var eventTypeFilter by remember { mutableStateOf("ALL") }

    // Dialog state
    var selectedEventForDossier by remember { mutableStateOf<DiveEventEntity?>(null) }
    var selectedEventForRegistration by remember { mutableStateOf<DiveEventEntity?>(null) }
    var registrationToCancel by remember { mutableStateOf<DiveEventParticipantEntity?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val activeRegistrationsEventIds = remember(myRegistrations) {
        myRegistrations.map { it.eventId }.toSet()
    }

    val connectedOrgIds = remember(myMemberships) {
        myMemberships.filter { it.status == "ACTIVE" }.map { it.orgId }.toSet()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ausfahrten & Tauchbasen",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                        Text(
                            text = "Vernetze dich mit Basen & melde dich direkt zu Tauchgängen an",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OceanPrimary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${myRegistrations.size} Gebucht",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("🤿 Ausfahrten (${allEvents.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("🏛️ Basen & Clubs (${allOrganizations.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("📋 Meine Buchungen (${myRegistrations.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        // Toast Banner
        toastMessage?.let { msg ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SeaGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SeaGreen)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SeaGreen)
                    }
                    TextButton(onClick = { toastMessage = null }) {
                        Text("OK", color = SeaGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Content Body
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> UpcomingEventsTab(
                    events = allEvents,
                    organizations = allOrganizations,
                    activeRegisteredEventIds = activeRegistrationsEventIds,
                    filterType = eventTypeFilter,
                    onFilterChange = { eventTypeFilter = it },
                    onViewDossier = { selectedEventForDossier = it },
                    onRegister = { selectedEventForRegistration = it }
                )
                1 -> PartnerOrganizationsTab(
                    organizations = allOrganizations,
                    connectedOrgIds = connectedOrgIds,
                    onToggleConnection = { org -> viewModel.toggleDiverOrgConnection(org) }
                )
                2 -> MyRegistrationsTab(
                    registrations = myRegistrations,
                    events = allEvents,
                    onViewDossier = { selectedEventForDossier = it },
                    onCancelRegistration = { registrationToCancel = it }
                )
            }
        }
    }

    // 1. Dossier Dialog (includes Dive Site Map & EAP)
    selectedEventForDossier?.let { targetEvent ->
        val isRegistered = activeRegistrationsEventIds.contains(targetEvent.id)
        DiveEventDossierDialog(
            event = targetEvent,
            primaryAccentColor = OceanPrimary,
            onDismiss = { selectedEventForDossier = null },
            onUpdateSiteMapImage = { uri ->
                viewModel.updateEventSiteMapImage(targetEvent.id, uri)
                selectedEventForDossier = targetEvent.copy(siteMapImageUri = uri)
            },
            onRegisterClick = {
                selectedEventForRegistration = targetEvent
                selectedEventForDossier = null
            },
            isAlreadyRegistered = isRegistered
        )
    }

    // 2. Registration Dialog
    selectedEventForRegistration?.let { targetEvent ->
        DiverEventRegistrationDialog(
            event = targetEvent,
            currentUser = currentUser,
            onDismiss = { selectedEventForRegistration = null },
            onConfirmRegistration = { tank, gas, gear, notes ->
                viewModel.registerDiverForEvent(
                    event = targetEvent,
                    tankSelection = tank,
                    gasType = gas,
                    rentalGear = gear,
                    carpoolNotes = notes,
                    onSuccess = {
                        toastMessage = "Erfolgreich für '${targetEvent.title}' angemeldet!"
                        selectedEventForRegistration = null
                    },
                    onError = { error ->
                        toastMessage = error
                    }
                )
            }
        )
    }

    // 3. Cancel Registration Confirmation Dialog
    registrationToCancel?.let { participant ->
        val associatedEvent = allEvents.find { it.id == participant.eventId }
        AlertDialog(
            onDismissRequest = { registrationToCancel = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Anmeldung stornieren?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Möchtest du deinen Platz bei '${associatedEvent?.title ?: "Ausfahrt"}' wirklich freigeben? Deine Reservierung wird damit aufgehoben.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelDiverEventRegistration(participant.eventId, participant.id) {
                            toastMessage = "Anmeldung wurde erfolgreich storniert."
                            registrationToCancel = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 7.dp)
                ) {
                    Text("Platz freigeben", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { registrationToCancel = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

/**
 * Tab 0: Upcoming Excursions List
 */
@Composable
private fun UpcomingEventsTab(
    events: List<DiveEventEntity>,
    organizations: List<OrganizationEntity>,
    activeRegisteredEventIds: Set<String>,
    filterType: String,
    onFilterChange: (String) -> Unit,
    onViewDossier: (DiveEventEntity) -> Unit,
    onRegister: (DiveEventEntity) -> Unit
) {
    val orgMap = remember(organizations) { organizations.associateBy { it.id } }

    val filteredEvents = remember(events, filterType) {
        if (filterType == "ALL") events
        else events.filter { it.eventType == filterType }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = filterType == "ALL",
                onClick = { onFilterChange("ALL") },
                label = { Text("Alle", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterType == "BOAT_TRIP",
                onClick = { onFilterChange("BOAT_TRIP") },
                label = { Text("🚤 Boot", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterType == "SHORE_DIVE",
                onClick = { onFilterChange("SHORE_DIVE") },
                label = { Text("🌊 Hausriff", fontSize = 11.sp) }
            )
            FilterChip(
                selected = filterType == "CLUB_OUTING",
                onClick = { onFilterChange("CLUB_OUTING") },
                label = { Text("🏛️ Club", fontSize = 11.sp) }
            )
        }

        if (filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Keine Ausfahrten in dieser Kategorie",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredEvents, key = { it.id }) { event ->
                    val org = orgMap[event.orgId]
                    val isRegistered = activeRegisteredEventIds.contains(event.id)
                    val freeSlots = (event.maxCapacity - event.registeredCount).coerceAtLeast(0)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            if (isRegistered) SeaGreen.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Top Row: Type & Org Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = OceanPrimary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = when (event.eventType) {
                                                "BOAT_TRIP" -> "🚤 Bootsausfahrt"
                                                "SHORE_DIVE" -> "🌊 Hausriff & See"
                                                "COURSE_TRAINING" -> "🎓 Kurs"
                                                else -> "🏛️ Vereinsausfahrt"
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = OceanPrimary
                                        )
                                    }

                                    if (org != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = org.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (isRegistered) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SeaGreen.copy(alpha = 0.18f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Angemeldet",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = SeaGreen
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (freeSlots > 0) Color(0xFFD69E2E).copy(alpha = 0.18f) else Color(0xFFE53E3E).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (freeSlots > 0) "$freeSlots Plätze frei" else "Ausgebucht",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (freeSlots > 0) Color(0xFF975A16) else Color(0xFFE53E3E)
                                        )
                                    }
                                }
                            }

                            // Event Title & Dive Site
                            Column {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${event.siteName} • ${event.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Date & Parameter Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = "📅 ${event.eventDate} • ⏰ ${event.meetingTime}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = "Max. ${event.maxDepthMeters.toInt()}m",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = "Brevet: ${event.requiredMinCert}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onViewDossier(event) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Briefing & Karte", fontSize = 12.sp)
                                }

                                if (isRegistered) {
                                    OutlinedButton(
                                        onClick = { onViewDossier(event) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SeaGreen),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Details ansehen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Button(
                                        onClick = { onRegister(event) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                        enabled = freeSlots > 0
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Anmelden", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

/**
 * Tab 1: Partner Organizations & Dive Centers List
 */
@Composable
private fun PartnerOrganizationsTab(
    organizations: List<OrganizationEntity>,
    connectedOrgIds: Set<String>,
    onToggleConnection: (OrganizationEntity) -> Unit
) {
    if (organizations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Keine Partnerbasen gefunden",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = OceanPrimary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Vernetze dich mit deiner Tauchbasis oder deinem Verein, um direkten Zugriff auf exklusive Ausfahrten, Leihmaterial und Ausbildungspläne zu erhalten.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(organizations, key = { it.id }) { org ->
                val isConnected = connectedOrgIds.contains(org.id)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (isConnected) SeaGreen.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (org.orgType == "CLUB") OceanPrimary.copy(alpha = 0.12f) else AquaCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (org.orgType == "CLUB") "🏛️ Tauchverein / Club" else "🤿 Tauchbasis & Resort",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (org.orgType == "CLUB") OceanPrimary else Color(0xFF007799)
                                )
                            }

                            if (isConnected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SeaGreen.copy(alpha = 0.18f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Verbunden",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SeaGreen
                                        )
                                    }
                                }
                            }
                        }

                        // Organization Name & Location
                        Column {
                            Text(
                                text = org.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${org.location} • ${org.waterBody}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (org.affiliations.isNotBlank()) {
                            Text(
                                text = "Verbands-Akkreditierung: ${org.affiliations}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                        // Footer Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✉️ ${org.contactEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )

                            if (isConnected) {
                                OutlinedButton(
                                    onClick = { onToggleConnection(org) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53E3E)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Trennen", fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = { onToggleConnection(org) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Mit Basis vernetzen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tab 2: Diver's Active Bookings & Registrations
 */
@Composable
private fun MyRegistrationsTab(
    registrations: List<DiveEventParticipantEntity>,
    events: List<DiveEventEntity>,
    onViewDossier: (DiveEventEntity) -> Unit,
    onCancelRegistration: (DiveEventParticipantEntity) -> Unit
) {
    val eventsMap = remember(events) { events.associateBy { it.id } }

    if (registrations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Noch keine aktiven Ausfahrts-Buchungen",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Wechsle zum Tab 'Ausfahrten' um geplante Bootstrips oder See-Exkursionen zu buchen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(registrations, key = { it.id }) { participant ->
                val event = eventsMap[participant.eventId]

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SeaGreen.copy(alpha = 0.18f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when (participant.checkInStatus) {
                                            "SAFETY_CHECKED" -> "Sicherheits-Check OK"
                                            "ON_BOARD" -> "An Bord / Ausfahrt läuft"
                                            "IN_WATER" -> "Im Wasser"
                                            "SURFACED_OK" -> "Sicher aufgetaucht"
                                            else -> "Verbindlich gebucht"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SeaGreen
                                    )
                                }
                            }

                            Text(
                                text = "Ref: ${participant.id.takeLast(6).uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Event Title & Dive Site
                        Column {
                            Text(
                                text = event?.title ?: "Geplante Ausfahrt",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (event != null) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${event.siteName} • ${event.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "📅 ${event.eventDate} • ⏰ Treffen: ${event.meetingTime} | Start: ${event.departureTime}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Diver's Selected Equipment
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Flasche & Gas: ${participant.tankSelection} • ${participant.gasType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )

                                val rentalGear = participant.parseRentalGear()
                                if (rentalGear.isNotEmpty()) {
                                    Text(
                                        text = "Leih-Ausrüstung: ${rentalGear.joinToString(", ")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (participant.transferCarPoolNote.isNotBlank()) {
                                    Text(
                                        text = "Notizen: ${participant.transferCarPoolNote}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (event != null) {
                                OutlinedButton(
                                    onClick = { onViewDossier(event) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Briefing & Karte", fontSize = 12.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            TextButton(
                                onClick = { onCancelRegistration(participant) },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53E3E))
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stornieren", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
