package com.example.ui.screens.diveops

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiveEventEntity
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen

/**
 * Comprehensive Dive Event Dossier Dialog:
 * Keeps the main screen clutter-free by surfacing all deep briefing, hyperbaric chamber,
 * and emergency action plan (EAP) details in a dedicated, tabbed modal.
 */
@Composable
fun DiveEventDossierDialog(
    event: DiveEventEntity,
    primaryAccentColor: Color,
    onDismiss: () -> Unit,
    onEditEvent: (DiveEventEntity) -> Unit,
    onCheckIn: (DiveEventEntity) -> Unit,
    onNavigateToBuddyBoard: (DiveEventEntity) -> Unit,
    onNavigateToManifest: (DiveEventEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = primaryAccentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = when (event.eventType) {
                                        "BOAT_TRIP" -> "🚤 Bootsausfahrt"
                                        "SHORE_DIVE" -> "🌊 Hausriff / See"
                                        "COURSE_TRAINING" -> "🎓 Ausbildung / Kurs"
                                        else -> "🏛️ Vereinsausfahrt"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryAccentColor
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SeaGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Status: ${event.status}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SeaGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "📅 ${event.eventDate} • ⏰ Treffen: ${event.meetingTime} | Start: ${event.departureTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Edit Event Button
                        IconButton(
                            onClick = { onEditEvent(event) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Bearbeiten",
                                tint = primaryAccentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Close Dialog
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Schließen",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = primaryAccentColor,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📋 Briefing", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("🏥 Notfallplan (EAP)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("👥 Organisation", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> BriefingTabContent(event = event, primaryAccentColor = primaryAccentColor)
                        1 -> EmergencyPlanTabContent(event = event, context = context)
                        2 -> OrganizationTabContent(
                            event = event,
                            primaryAccentColor = primaryAccentColor,
                            onCheckIn = { onCheckIn(event) },
                            onNavigateToBuddyBoard = { onNavigateToBuddyBoard(event) },
                            onNavigateToManifest = { onNavigateToManifest(event) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions: Centered Pill Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 9.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
                    ) {
                        Text("Dossier schließen", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Tab 1: Briefing & Dive Site Dossier
 */
@Composable
private fun BriefingTabContent(
    event: DiveEventEntity,
    primaryAccentColor: Color
) {
    // 1. Site Location & Target Parameters
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryAccentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${event.siteName} • ${event.location}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Geplante Maximaltiefe", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${event.maxDepthMeters.toInt()} Meter", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = primaryAccentColor)
                }
                Column {
                    Text("Mindest-Brevet", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.requiredMinCert, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Umkehrdruck (Turn)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${event.turnPressureBar} bar", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD69E2E))
                }
            }
        }
    }

    // 2. Entry & Exit Info
    DossierSectionCard(
        icon = Icons.Default.Water,
        iconTint = OceanPrimary,
        title = "Einstieg, Ausstieg & Tauchgangsverlauf",
        content = event.entryExitInfo.ifBlank {
            "Einstieg und Ausstieg nach Einweisung vor Ort durch den Tauchgangsleiter. Sicherer Notausstieg in Briefing-Skizze verzeichnet."
        }
    )

    // 3. Environmental Conditions (Visibility, Temperature, Currents)
    DossierSectionCard(
        icon = Icons.Default.Air,
        iconTint = AquaCyan,
        title = "Sichtverhältnisse, Wassertemperatur & Strömung",
        content = event.waterConditions.ifBlank {
            "Lokale Bedingungen werden vor dem Einstieg final geprüft. Kaltwasser- und Strömungserfahrung entsprechend empfohlen."
        }
    )

    // 4. Equipment Requirements
    DossierSectionCard(
        icon = Icons.Default.Security,
        iconTint = SeaGreen,
        title = "Ausrüstungs-Anforderungen",
        content = event.equipmentRequirements.ifBlank {
            "Vollständige Kaltwasser-Ausrüstung gemäß Verbandsrichtlinien, Tauchcomputer sowie Signalboje (SMB) pro Buddy-Team erforderlich."
        }
    )

    // 5. Hazards & Safety Rules
    if (event.hazardsAndRules.isNotBlank()) {
        DossierSectionCard(
            icon = Icons.Default.Warning,
            iconTint = Color(0xFFE53E3E),
            title = "Besondere Gefahren & Verhaltensregeln",
            content = event.hazardsAndRules
        )
    }
}

/**
 * Tab 2: Emergency Action Plan (EAP) & Hyperbaric Chamber
 */
@Composable
private fun EmergencyPlanTabContent(
    event: DiveEventEntity,
    context: Context
) {
    // 1. Hyperbaric Chamber Highlight Card
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC53030).copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color(0xFFC53030).copy(alpha = 0.4f))
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
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Color(0xFFC53030),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nächste Dekompressionskammer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC53030)
                    )
                }

                if (event.chamberDistanceTime.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFC53030).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "⏱️ ${event.chamberDistanceTime}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC53030)
                        )
                    }
                }
            }

            Text(
                text = event.chamberName.ifBlank { "Keine Druckkammer hinterlegt" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (event.chamberPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                // 1-Click Call Button
                Button(
                    onClick = { dialPhoneNumber(context, event.chamberPhone) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53030)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kammer Notruf anrufen: ${event.chamberPhone}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // 2. Coast Guard & Radio Channel
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Radio, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Seefunk & Notfall-Kommunikation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Funkkanal: ${event.coastGuardVHF.ifBlank { "VHF Kanal 16 (Notruf & Sicherheitsverkehr)" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Allgemeiner Rettungsdienst: Notruf 112 (Europa) oder zuständige Seenotleitung",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // 3. First Aid & Oxygen Provider
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sauerstoff-Versorgung & Erste Hilfe vor Ort",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = event.emergencyNotes.ifBlank {
                    event.emergencyBriefing.ifBlank {
                        "Medizinischer Notfallsauerstoff (100% O2) und Erste-Hilfe-Ausrüstung einsatzbereit vor Ort."
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Tab 3: Organization, Capacity & Participant Actions
 */
@Composable
private fun OrganizationTabContent(
    event: DiveEventEntity,
    primaryAccentColor: Color,
    onCheckIn: () -> Unit,
    onNavigateToBuddyBoard: () -> Unit,
    onNavigateToManifest: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, contentDescription = null, tint = primaryAccentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Einsatz-Leitung & Belegung", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tauchgangsleiter / Guide", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.leadGuideName.ifBlank { "Noch nicht zugewiesen" }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Angemeldete Taucher", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${event.registeredCount} von ${event.maxCapacity} Plätzen", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text("Schnell-Aktionen für diesen Tauchgang:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

    // Direct Action Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onCheckIn,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("➕ Check-in", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onNavigateToBuddyBoard,
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("🤿 Buddy-Board", fontSize = 11.sp)
        }

        OutlinedButton(
            onClick = onNavigateToManifest,
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text("📋 Manifest", fontSize = 11.sp)
        }
    }
}

@Composable
private fun DossierSectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    content: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

private fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.trim()}"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}
