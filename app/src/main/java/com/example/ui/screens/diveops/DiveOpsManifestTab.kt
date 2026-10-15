package com.example.ui.screens.diveops

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Phone
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiveEventEntity
import com.example.data.model.DiveEventParticipantEntity
import com.example.data.model.OrganizationEntity
import com.example.data.model.OrganizationMemberEntity
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import org.json.JSONObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Tab 3: Boot-/See-Manifest (Offline-Ready)
 * Tablet- and mobile-ready operational roster for dive leaders & skippers at the lake or on board.
 * Real-time roll-call: On Board, In Water (Splash), Surfaced OK, Air In/Out tracker, and Emergency Dispatch.
 */
@Composable
fun DiveOpsManifestTab(
    viewModel: AquaViewModel,
    organization: OrganizationEntity,
    primaryAccentColor: Color,
    onNavigateToTrips: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.currentOrgEvents.collectAsState()
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val participants by viewModel.selectedEventParticipants.collectAsState()
    val members by viewModel.currentOrgMembers.collectAsState()
    val context = LocalContext.current

    // Active event selection
    val activeEvent = selectedEvent ?: events.firstOrNull()
    if (activeEvent != null && selectedEvent == null) {
        viewModel.selectDiveEvent(activeEvent)
    }

    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var participantToSurface by remember { mutableStateOf<DiveEventParticipantEntity?>(null) }

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
                    Text(text = "📋", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Keine Ausfahrten aktiv",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Erstelle zuerst eine Ausfahrt in 'Ausfahrten & Trips', um das Manifest zu führen.",
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

    // Counters
    val totalCheckedIn = participants.size
    val inWaterCount = participants.count { it.checkInStatus == "IN_WATER" }
    val surfacedCount = participants.count { it.checkInStatus == "SURFACED_OK" }
    val onBoardCount = participants.count { it.checkInStatus in listOf("ON_BOARD", "SAFETY_CHECKED", "REGISTERED") }

    val filteredParticipants = remember(participants, selectedStatusFilter) {
        when (selectedStatusFilter) {
            "ALL" -> participants
            "IN_WATER" -> participants.filter { it.checkInStatus == "IN_WATER" }
            "SURFACED_OK" -> participants.filter { it.checkInStatus == "SURFACED_OK" }
            "ON_BOARD" -> participants.filter { it.checkInStatus in listOf("ON_BOARD", "SAFETY_CHECKED", "REGISTERED") }
            else -> participants
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
            // Section Header & Event Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Boot-/See-Manifest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sicherheits- & Anwesenheitsliste (Offline-Ready)",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SeaGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⚡ LIVE READY",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeaGreen
                    )
                }
            }
        }

        item {
            // Event Selector row
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
                                text = "${ev.title} (${ev.registeredCount})",
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
                // Live Operational Safety KPI Dashboard Bar
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
                            Text(
                                text = "Operativer Sicherheitsstatus (${activeEvent.siteName}):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Treffen: ${activeEvent.meetingTime} • Start: ${activeEvent.departureTime}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 4 KPI Counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Total Checked In
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "$totalCheckedIn", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(text = "Eingecheckt", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // On Board
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = OceanPrimary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "$onBoardCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = OceanPrimary)
                                    Text(text = "🚤 An Bord", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OceanPrimary)
                                }
                            }

                            // In Water (Emphasized!)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (inWaterCount > 0) AquaCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(if (inWaterCount > 0) 1.5.dp else 1.dp, if (inWaterCount > 0) Color(0xFF007799) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
                                modifier = Modifier.weight(1.1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$inWaterCount",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (inWaterCount > 0) Color(0xFF007799) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "🌊 Im Wasser",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (inWaterCount > 0) Color(0xFF007799) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Surfaced OK
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SeaGreen.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "$surfacedCount", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SeaGreen)
                                    Text(text = "🟢 Ausgetaucht", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SeaGreen)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Status Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == "ALL",
                        onClick = { selectedStatusFilter = "ALL" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("Alle ($totalCheckedIn)", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "ON_BOARD",
                        onClick = { selectedStatusFilter = "ON_BOARD" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("An Bord ($onBoardCount)", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "IN_WATER",
                        onClick = { selectedStatusFilter = "IN_WATER" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("Im Wasser ($inWaterCount)", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == "SURFACED_OK",
                        onClick = { selectedStatusFilter = "SURFACED_OK" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("Ausgetaucht ($surfacedCount)", fontSize = 10.sp) }
                    )
                }
            }

            // Diver Rows
            if (filteredParticipants.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Keine Taucher in dieser Kategorie erfasst.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredParticipants, key = { it.id }) { participant ->
                    val member = members.find { it.id == participant.memberId }
                    ManifestParticipantCard(
                        participant = participant,
                        member = member,
                        onRecordSplash = {
                            val now = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                            viewModel.recordDiverSplash(participant.id, now)
                        },
                        onRecordSurfaced = {
                            participantToSurface = participant
                        },
                        onEmergencyDial = { phone ->
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${phone.filter { it.isDigit() || it == '+' }}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Operational Emergency Dispatch Banner
            item {
                Spacer(modifier = Modifier.height(10.dp))
                EmergencyDispatchBar(
                    organization = organization,
                    onEmergencyDial = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${phone.filter { it.isDigit() || it == '+' }}")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- Record Surfaced Dialog ---
    if (participantToSurface != null) {
        RecordSurfacedDialog(
            participant = participantToSurface!!,
            onDismiss = { participantToSurface = null },
            onConfirm = { pressureOut, timeOut ->
                viewModel.recordDiverSurfaced(participantToSurface!!.id, pressureOut, timeOut)
                participantToSurface = null
            }
        )
    }
}

/**
 * Manifest Diver Row Card:
 * Displays air pressure, time stamps, buddy team, status pill, and 1-click splash/surfaced transitions.
 */
@Composable
fun ManifestParticipantCard(
    participant: DiveEventParticipantEntity,
    member: OrganizationMemberEntity?,
    onRecordSplash: () -> Unit,
    onRecordSurfaced: () -> Unit,
    onEmergencyDial: (phone: String) -> Unit
) {
    val isInWater = participant.checkInStatus == "IN_WATER"
    val isSurfaced = participant.checkInStatus == "SURFACED_OK"
    val isOnBoard = !isInWater && !isSurfaced

    val statusBg = when {
        isInWater -> Color(0xFF007799).copy(alpha = 0.12f)
        isSurfaced -> SeaGreen.copy(alpha = 0.12f)
        else -> OceanPrimary.copy(alpha = 0.1f)
    }

    val statusBorder = when {
        isInWater -> Color(0xFF007799)
        isSurfaced -> SeaGreen
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(
            if (isInWater) 1.5.dp else 1.dp,
            if (isInWater) Color(0xFF007799) else if (isSurfaced) SeaGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Diver Name, Team, Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = (if (isInWater) Color(0xFF007799) else if (isSurfaced) SeaGreen else OceanPrimary).copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = participant.diverName.take(1).uppercase(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isInWater) Color(0xFF007799) else if (isSurfaced) SeaGreen else OceanPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = participant.diverName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (participant.buddyTeamId != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OceanPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = participant.buddyTeamId,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanPrimary
                                )
                            }
                        }
                    }
                    Text(
                        text = "${participant.diverCertLevel} • ${participant.tankSelection} • ${participant.gasType}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg,
                    border = BorderStroke(1.dp, statusBorder)
                ) {
                    Text(
                        text = when {
                            isInWater -> "🌊 IM WASSER"
                            isSurfaced -> "🟢 OK (SURFACED)"
                            else -> "🚤 AN BORD"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInWater) Color(0xFF007799) else if (isSurfaced) SeaGreen else OceanPrimary
                    )
                }
            }

            // Telemetry & Timing Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pressure In / Out
                Text(
                    text = "Druck: ${participant.tankPressureStartBar} bar Start" +
                        if (participant.timeOut != null) " • ${participant.tankPressureEndBar} bar Ende (${participant.tankPressureStartBar - participant.tankPressureEndBar} bar verbraucht)" else "",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Times
                if (participant.timeIn != null) {
                    Text(
                        text = "Splash: ${participant.timeIn}" + if (participant.timeOut != null) " • Auftauchen: ${participant.timeOut}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isInWater) Color(0xFF007799) else SeaGreen
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            // Operational Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emergency Contact Quick-Dial
                val emergencyPhone = member?.emergencyContactPhone ?: ""
                val emergencyName = member?.emergencyContactName ?: ""
                if (emergencyPhone.isNotBlank()) {
                    OutlinedButton(
                        onClick = { onEmergencyDial(emergencyPhone) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFC53030))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Notfall: $emergencyName", fontSize = 10.sp, color = Color(0xFFC53030))
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Water Entry / Exit Transition Buttons
                if (isOnBoard) {
                    Button(
                        onClick = onRecordSplash,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007799)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🌊 Splash / Start", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isInWater) {
                    Button(
                        onClick = onRecordSurfaced,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "🟢 Auftauchen (OK)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SeaGreen.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "✅ Sicher ausgetaucht",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SeaGreen
                        )
                    }
                }
            }
        }
    }
}

/**
 * Record Surfaced Dialog:
 * Captures end pressure and surface timestamp.
 */
@Composable
fun RecordSurfacedDialog(
    participant: DiveEventParticipantEntity,
    onDismiss: () -> Unit,
    onConfirm: (pressureOut: Int, timeOut: String) -> Unit
) {
    var endPressureText by remember { mutableStateOf("50") }
    var timeOutText by remember {
        mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("🟢 Auftauchen erfassen") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tauchgang beendet für '${participant.diverName}':",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = timeOutText,
                    onValueChange = { timeOutText = it },
                    label = { Text("Auftauchzeit (HH:mm)") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = endPressureText,
                    onValueChange = { endPressureText = it },
                    label = { Text("Flaschendruck Ende (bar)") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pOut = endPressureText.toIntOrNull() ?: 50
                    onConfirm(pOut, timeOutText)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
            ) {
                Text("Speichern (Surfaced OK)")
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

/**
 * Bottom Emergency Dispatch Bar on Manifest:
 * Immediate one-click dialing of SAR/Chamber, and display of GPS point and O2 kit.
 */
@Composable
fun EmergencyDispatchBar(
    organization: OrganizationEntity,
    onEmergencyDial: (phone: String) -> Unit
) {
    val emergencyJson = try {
        JSONObject(organization.emergencyPlanJson)
    } catch (_: Exception) {
        JSONObject()
    }
    val emergencyPhone = emergencyJson.optString("emergencyPhone", "112 / DAN Europe +39 06 42118685")
    val nearestChamber = emergencyJson.optString("nearestChamber", "Lokales Druckkammerzentrum")
    val gpsEvacPoint = emergencyJson.optString("gpsEvacuationPoint", organization.location.ifBlank { "Basis Marina" })
    val o2KitLocation = emergencyJson.optString("o2KitLocation", "Basis Notfallkoffer & Bord-O2")

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Emergency, contentDescription = null, tint = Color(0xFFC53030), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🚨 Notfall-Schnellzugriff & Rettungskette",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC53030)
                    )
                }

                Button(
                    onClick = { onEmergencyDial(emergencyPhone) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53030)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Notruf", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "📞 SAR / DAN: $emergencyPhone • 🏥 Kammer: $nearestChamber",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = Color(0xFFC53030),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "📍 Evakuierung: $gpsEvacPoint • 🫁 O2-Koffer: $o2KitLocation",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
