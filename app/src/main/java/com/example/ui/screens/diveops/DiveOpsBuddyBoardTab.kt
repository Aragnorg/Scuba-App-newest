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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiveEventEntity
import com.example.data.model.DiveEventParticipantEntity
import com.example.data.model.OrganizationEntity
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

/**
 * Tab 2: Smart Buddy- & Gruppen-Board
 * Visual board to organize checked-in divers into buddy pairs and guide groups.
 * Includes the ⚡ Auto-Suggest algorithm which balances experience and enforces safety-gate blocks.
 */
@Composable
fun DiveOpsBuddyBoardTab(
    viewModel: AquaViewModel,
    organization: OrganizationEntity,
    primaryAccentColor: Color,
    onNavigateToTrips: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    val events by viewModel.currentOrgEvents.collectAsState()
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val participants by viewModel.selectedEventParticipants.collectAsState()

    // Active event selection
    val activeEvent = selectedEvent ?: events.firstOrNull()
    if (activeEvent != null && selectedEvent == null) {
        viewModel.selectDiveEvent(activeEvent)
    }

    var participantToAssignTeam by remember { mutableStateOf<DiveEventParticipantEntity?>(null) }
    var participantToRemove by remember { mutableStateOf<DiveEventParticipantEntity?>(null) }

    if (events.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🤿", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Keine Ausfahrten vorhanden",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Erstelle zuerst eine Ausfahrt in 'Ausfahrten & Trips', um Buddy-Teams einzuteilen.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToTrips,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Zu den Ausfahrten")
                    }
                }
            }
        }
        return
    }

    // Group participants
    val assignedTeams = remember(participants) {
        participants
            .filter { !it.buddyTeamId.isNullOrBlank() }
            .groupBy { it.buddyTeamId!! }
    }

    val unassignedParticipants = remember(participants) {
        participants.filter { it.buddyTeamId.isNullOrBlank() }
    }

    val blockedParticipants = remember(participants) {
        participants.filter { !it.safetyGatePassed }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Section Header & Event Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Smart Buddy- & Gruppen-Board",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sicheres Buddy-Matching nach Erfahrungslevel & Guide-Gruppierung",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            // Event Selector horizontal pill row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                events.forEach { ev ->
                    val isSelected = activeEvent?.id == ev.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectDiveEvent(ev) },
                        shape = RoundedCornerShape(10.dp),
                        label = {
                            Text(
                                text = "${ev.title} (${ev.registeredCount} P.)",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryAccentColor
                        )
                    )
                }
            }
        }

        if (activeEvent != null) {
            item {
                // Event Quick Summary Card & Auto-Suggest Button
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = activeEvent.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeEvent.eventDate} • ${activeEvent.siteName} (Max ${activeEvent.maxDepthMeters.toInt()}m)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Auto-Suggest Button
                            Button(
                                onClick = { viewModel.autoSuggestBuddyTeams(activeEvent.id) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B46C1)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isGerman) "⚡ Auto-Zuteilung" else "⚡ Auto-Suggest",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Safety summary
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SeaGreen.copy(alpha = 0.12f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "🟢 ${participants.count { it.safetyGatePassed }} Freigegeben",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeaGreen
                                )
                            }
                            if (blockedParticipants.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE53E3E).copy(alpha = 0.12f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "⛔ ${blockedParticipants.size} Gesperrt",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC53030)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Safety Block Banner
            if (blockedParticipants.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE53E3E).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFFE53E3E).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sicherheits-Gate aktiv: ${blockedParticipants.size} Taucher nicht freigegeben!",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC53030)
                                )
                            }
                            blockedParticipants.forEach { p ->
                                val issues = p.parseSafetyIssues().joinToString(", ")
                                Text(
                                    text = "• ${p.diverName}: $issues",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = Color(0xFFC53030)
                                )
                            }
                        }
                    }
                }
            }

            // Grouped Teams Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Eingeteilte Buddy-Teams (${assignedTeams.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (assignedTeams.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Noch keine Teams eingeteilt. Klicke auf '⚡ Auto-Suggest' oder weise Taucher manuell zu.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(assignedTeams.keys.toList(), key = { it }) { teamName ->
                    val teamMembers = assignedTeams[teamName] ?: emptyList()
                    BuddyTeamCard(
                        teamName = teamName,
                        members = teamMembers,
                        primaryAccentColor = primaryAccentColor,
                        onReassign = { participantToAssignTeam = it },
                        onRemoveFromTeam = { viewModel.assignBuddyTeam(it.id, null) }
                    )
                }
            }

            // Unassigned Divers Section
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nicht zugewiesene Taucher (${unassignedParticipants.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (unassignedParticipants.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SeaGreen.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ Alle eingecheckten Taucher sind Teams zugewiesen!",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = SeaGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                items(unassignedParticipants, key = { it.id }) { participant ->
                    UnassignedDiverCard(
                        participant = participant,
                        onAssign = { participantToAssignTeam = participant },
                        onRemove = { participantToRemove = participant }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- Dialogs ---
    if (participantToAssignTeam != null) {
        AssignBuddyTeamDialog(
            participant = participantToAssignTeam!!,
            existingTeams = assignedTeams.keys.toList(),
            onDismiss = { participantToAssignTeam = null },
            onConfirm = { teamName ->
                viewModel.assignBuddyTeam(participantToAssignTeam!!.id, teamName)
                participantToAssignTeam = null
            }
        )
    }

    if (participantToRemove != null) {
        AlertDialog(
            onDismissRequest = { participantToRemove = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Taucher abmelden") },
            text = { Text("Möchtest du '${participantToRemove!!.diverName}' von dieser Ausfahrt abmelden?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeParticipantFromEvent(participantToRemove!!.id, participantToRemove!!.eventId)
                        participantToRemove = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
                ) {
                    Text("Abmelden")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { participantToRemove = null },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Abbrechen") }
            }
        )
    }
}

/**
 * Card representing a designated buddy team (Alpha, Bravo, Charlie, etc.).
 */
@Composable
fun BuddyTeamCard(
    teamName: String,
    members: List<DiveEventParticipantEntity>,
    primaryAccentColor: Color,
    onReassign: (DiveEventParticipantEntity) -> Unit,
    onRemoveFromTeam: (DiveEventParticipantEntity) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Team Title Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OceanPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "🤿 $teamName (${members.size} Taucher)",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                }

                Text(
                    text = if (members.size == 2) "Duo-Team" else if (members.size == 3) "Trio-Team" else "Solo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Divers in team
            members.forEach { diver ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = primaryAccentColor.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = diver.diverName.take(1).uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccentColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = diver.diverName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                if (diver.safetyGatePassed) {
                                    Text(text = "🟢", fontSize = 9.sp)
                                } else {
                                    Text(text = "🔴", fontSize = 9.sp)
                                }
                            }
                            Text(
                                text = "${diver.diverCertLevel} • ${diver.diverTotalDives} Dives • ${diver.tankSelection} • ${diver.gasType}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Reassign
                        IconButton(
                            onClick = { onReassign(diver) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Team wechseln", modifier = Modifier.size(15.dp))
                        }
                        // Remove from team
                        IconButton(
                            onClick = { onRemoveFromTeam(diver) },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Aus Team entfernen", modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card representing an unassigned diver awaiting team allocation.
 */
@Composable
fun UnassignedDiverCard(
    participant: DiveEventParticipantEntity,
    onAssign: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (participant.safetyGatePassed) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (participant.safetyGatePassed) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = participant.diverName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (participant.safetyGatePassed) "🟢 Freigegeben" else "⛔ Gesperrt",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (participant.safetyGatePassed) SeaGreen else Color(0xFFC53030)
                    )
                }
                Text(
                    text = "${participant.diverCertLevel} • ${participant.diverTotalDives} Dives • ${participant.tankSelection} • ${participant.gasType}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!participant.safetyGatePassed) {
                    val issues = participant.parseSafetyIssues().joinToString("; ")
                    Text(
                        text = "Grund: $issues",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = Color(0xFFC53030)
                    )
                }
            }

            if (participant.safetyGatePassed) {
                Button(
                    onClick = onAssign,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "+ Zuweisen", fontSize = 11.sp)
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = "Entfernen", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * Dialog to assign a diver to an existing team or new team.
 */
@Composable
fun AssignBuddyTeamDialog(
    participant: DiveEventParticipantEntity,
    existingTeams: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (teamName: String) -> Unit
) {
    val defaultTeams = listOf("Team Alpha", "Team Bravo", "Team Charlie", "Team Delta", "Team Echo", "Team Foxtrot")
    val allOptions = (defaultTeams + existingTeams).distinct()
    var selectedTeam by remember { mutableStateOf(participant.buddyTeamId ?: allOptions.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Buddy-Team zuweisen") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Wähle das Team für '${participant.diverName}' (${participant.diverCertLevel}, ${participant.diverTotalDives} Dives):",
                    style = MaterialTheme.typography.bodySmall
                )
                allOptions.forEach { team ->
                    val isSelected = selectedTeam == team
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) OceanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, if (isSelected) OceanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTeam = team }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🤿 $team",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedTeam) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Zuweisen")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text("Abbrechen") }
        }
    )
}
