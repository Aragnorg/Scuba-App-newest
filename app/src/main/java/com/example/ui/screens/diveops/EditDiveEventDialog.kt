@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens.diveops

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DiveEventEntity
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen

/**
 * Edit Dive Event Dialog:
 * Allows comprehensive editing of event operational parameters, briefing details,
 * and emergency hyperbaric chamber action plan.
 */
@Composable
fun EditDiveEventDialog(
    event: DiveEventEntity,
    onDismiss: () -> Unit,
    onConfirm: (DiveEventEntity) -> Unit
) {
    var title by remember { mutableStateOf(event.title) }
    var eventType by remember { mutableStateOf(event.eventType) }
    var siteName by remember { mutableStateOf(event.siteName) }
    var location by remember { mutableStateOf(event.location) }
    var eventDate by remember { mutableStateOf(event.eventDate) }
    var meetingTime by remember { mutableStateOf(event.meetingTime) }
    var departureTime by remember { mutableStateOf(event.departureTime) }
    var maxCapacity by remember { mutableStateOf(event.maxCapacity.toString()) }
    var leadGuide by remember { mutableStateOf(event.leadGuideName) }
    var maxDepth by remember { mutableStateOf(event.maxDepthMeters.toInt().toString()) }
    var minCert by remember { mutableStateOf(event.requiredMinCert) }

    // Emergency & Chamber data
    var chamberName by remember { mutableStateOf(event.chamberName) }
    var chamberPhone by remember { mutableStateOf(event.chamberPhone) }
    var chamberDistanceTime by remember { mutableStateOf(event.chamberDistanceTime) }
    var coastGuardVHF by remember { mutableStateOf(event.coastGuardVHF) }
    var emergencyNotes by remember { mutableStateOf(event.emergencyNotes.ifBlank { event.emergencyBriefing }) }

    // Briefing data
    var entryExitInfo by remember { mutableStateOf(event.entryExitInfo) }
    var waterConditions by remember { mutableStateOf(event.waterConditions) }
    var turnPressureBar by remember { mutableStateOf(event.turnPressureBar.toString()) }
    var equipmentRequirements by remember { mutableStateOf(event.equipmentRequirements) }
    var hazardsAndRules by remember { mutableStateOf(event.hazardsAndRules) }
    var siteMapImageUri by remember { mutableStateOf(event.siteMapImageUri) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            siteMapImageUri = uri.toString()
        }
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("✏️ Event & Briefing bearbeiten", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Core Event Details
                Text("Allgemeine Event-Daten", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OceanPrimary)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel des Events *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Event Type Selector
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
                        label = { Text("🏛️ Verein", fontSize = 10.sp) }
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = siteName,
                        onValueChange = { siteName = it },
                        label = { Text("Tauchplatz *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Ort / Region") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = eventDate,
                        onValueChange = { eventDate = it },
                        label = { Text("Datum (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = meetingTime,
                        onValueChange = { meetingTime = it },
                        label = { Text("Treffen") },
                        modifier = Modifier.weight(0.9f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = departureTime,
                        onValueChange = { departureTime = it },
                        label = { Text("Start") },
                        modifier = Modifier.weight(0.9f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = maxDepth,
                        onValueChange = { maxDepth = it },
                        label = { Text("Max. Tiefe (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = turnPressureBar,
                        onValueChange = { turnPressureBar = it },
                        label = { Text("Turn (bar)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxCapacity,
                        onValueChange = { maxCapacity = it },
                        label = { Text("Max. Plätze") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = leadGuide,
                        onValueChange = { leadGuide = it },
                        label = { Text("Guide / TL") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minCert,
                        onValueChange = { minCert = it },
                        label = { Text("Mindest-Brevet") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider()

                // 2. Hyperbaric Chamber & Emergency Action Plan
                Text("🏥 Druckkammer & Notfallrettung (EAP)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC53030))

                // Fast-Fill Chamber Buttons
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
                        Text("⚡ Sharm El Sheikh", fontSize = 10.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            chamberName = "Druckkammerzentrum Berlin Helios Klinikum"
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
                    label = { Text("Nächste Druckkammer (Name & Klinik)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = chamberPhone,
                        onValueChange = { chamberPhone = it },
                        label = { Text("Kammer Notruf / Tel.") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = chamberDistanceTime,
                        onValueChange = { chamberDistanceTime = it },
                        label = { Text("Fahrzeit / Distanz") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = coastGuardVHF,
                        onValueChange = { coastGuardVHF = it },
                        label = { Text("Seefunk VHF Kanal / Notruf") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = emergencyNotes,
                    onValueChange = { emergencyNotes = it },
                    label = { Text("O2-Koffer & Ersthelfer Notizen") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                HorizontalDivider()

                // 3. Dive Site Briefing Details
                Text("📋 Briefing-Daten & Tauchplatz-Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SeaGreen)

                OutlinedTextField(
                    value = entryExitInfo,
                    onValueChange = { entryExitInfo = it },
                    label = { Text("Einstieg, Ausstieg & Notausstieg") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                OutlinedTextField(
                    value = waterConditions,
                    onValueChange = { waterConditions = it },
                    label = { Text("Sicht, Wassertemperatur & Strömung") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = equipmentRequirements,
                    onValueChange = { equipmentRequirements = it },
                    label = { Text("Ausrüstungs-Anforderungen (z. B. Boje, Kaltwasser)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                OutlinedTextField(
                    value = hazardsAndRules,
                    onValueChange = { hazardsAndRules = it },
                    label = { Text("Besondere Gefahren & Verhaltensregeln") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                HorizontalDivider()

                // 4. Dive Site Map & Underwater Topography (Tauchplatzkarte)
                Text("🗺️ Tauchplatzkarte & Unterwasser-Topografie", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OceanPrimary)

                if (siteMapImageUri.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = siteMapImageUri,
                                    contentDescription = "Tauchplatzkarte",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        imagePickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Karte ändern", fontSize = 12.sp)
                                }

                                TextButton(
                                    onClick = { siteMapImageUri = "" },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53E3E))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Entfernen", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "Keine Tauchplatzkarte hinterlegt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Karte hochladen", fontSize = 12.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = siteMapImageUri,
                    onValueChange = { siteMapImageUri = it },
                    label = { Text("Oder Bild-URL / Drive-Link eingeben") },
                    placeholder = { Text("https://... oder content://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color(0xFFE53E3E), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || siteName.isBlank()) {
                        errorMessage = "Bitte Titel und Tauchplatz ausfüllen."
                        return@Button
                    }
                    val updatedEvent = event.copy(
                        title = title.trim(),
                        eventType = eventType,
                        siteName = siteName.trim(),
                        location = location.trim(),
                        eventDate = eventDate.trim(),
                        meetingTime = meetingTime.trim(),
                        departureTime = departureTime.trim(),
                        maxCapacity = maxCapacity.toIntOrNull() ?: event.maxCapacity,
                        leadGuideName = leadGuide.trim(),
                        maxDepthMeters = maxDepth.toFloatOrNull() ?: event.maxDepthMeters,
                        requiredMinCert = minCert.trim(),
                        turnPressureBar = turnPressureBar.toIntOrNull() ?: event.turnPressureBar,
                        chamberName = chamberName.trim(),
                        chamberPhone = chamberPhone.trim(),
                        chamberDistanceTime = chamberDistanceTime.trim(),
                        coastGuardVHF = coastGuardVHF.trim(),
                        emergencyNotes = emergencyNotes.trim(),
                        entryExitInfo = entryExitInfo.trim(),
                        waterConditions = waterConditions.trim(),
                        equipmentRequirements = equipmentRequirements.trim(),
                        hazardsAndRules = hazardsAndRules.trim(),
                        siteMapImageUri = siteMapImageUri.trim()
                    )
                    onConfirm(updatedEvent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("Änderungen speichern", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
