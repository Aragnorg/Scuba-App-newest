@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens.diveops

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.DiveEventEntity
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
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
    onEditEvent: (DiveEventEntity) -> Unit = {},
    onCheckIn: (DiveEventEntity) -> Unit = {},
    onNavigateToBuddyBoard: (DiveEventEntity) -> Unit = {},
    onNavigateToManifest: (DiveEventEntity) -> Unit = {},
    onUpdateSiteMapImage: ((String) -> Unit)? = null,
    onRegisterClick: ((DiveEventEntity) -> Unit)? = null,
    isAlreadyRegistered: Boolean = false
) {
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showFullscreenMap by remember { mutableStateOf(false) }

    val mapPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && onUpdateSiteMapImage != null) {
            onUpdateSiteMapImage(uri.toString())
        }
    }

    if (showFullscreenMap && event.siteMapImageUri.isNotBlank()) {
        Dialog(
            onDismissRequest = { showFullscreenMap = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                color = Color.Black
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = event.siteMapImageUri,
                        contentDescription = "Tauchplatzkarte Vollbild",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = { showFullscreenMap = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.close, tint = Color.White)
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = when (event.eventType) {
                                        "BOAT_TRIP" -> if (isGerman) "🚤 Bootsausfahrt" else "🚤 Boat Trip"
                                        "SHORE_DIVE" -> if (isGerman) "🌊 Hausriff / See" else "🌊 Shore Dive"
                                        "COURSE_TRAINING" -> if (isGerman) "🎓 Ausbildung / Kurs" else "🎓 Training"
                                        else -> if (isGerman) "🏛️ Vereinsausfahrt" else "🏛️ Club Outing"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SeaGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Status: ${event.status}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
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
                            text = "📅 ${event.eventDate} • ⏰ ${if (isGerman) "Treffen:" else "Meeting:"} ${event.meetingTime} | ${if (isGerman) "Start:" else "Departure:"} ${event.departureTime}",
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
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = if (isGerman) "Bearbeiten" else "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Close Dialog
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.close,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(selectedTab),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = if (isGerman) "📋 Briefing" else "📋 Briefing",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = if (isGerman) "🏥 Notfallplan (EAP)" else "🏥 EAP / Safety",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = if (isGerman) "👥 Organisation" else "👥 Organization",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                            )
                        }
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
                        0 -> BriefingTabContent(
                            event = event,
                            primaryAccentColor = MaterialTheme.colorScheme.primary,
                            onOpenMapPicker = {
                                mapPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onShowFullscreenMap = { showFullscreenMap = true },
                            canUploadMap = onUpdateSiteMapImage != null
                        )
                        1 -> EmergencyPlanTabContent(event = event, context = context)
                        2 -> OrganizationTabContent(
                            event = event,
                            primaryAccentColor = MaterialTheme.colorScheme.primary,
                            onCheckIn = { onCheckIn(event) },
                            onNavigateToBuddyBoard = { onNavigateToBuddyBoard(event) },
                            onNavigateToManifest = { onNavigateToManifest(event) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Actions
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (onRegisterClick != null) {
                            if (isAlreadyRegistered) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = SeaGreen.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = SeaGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isGerman) "Angemeldet" else "Booked",
                                            fontWeight = FontWeight.Bold,
                                            color = SeaGreen,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { onRegisterClick(event) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 9.dp)
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isGerman) "Anmelden" else "Register",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 9.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = if (isGerman) "Dossier schließen" else "Close Dossier",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
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
    primaryAccentColor: Color,
    onOpenMapPicker: () -> Unit,
    onShowFullscreenMap: () -> Unit,
    canUploadMap: Boolean
) {
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    // 1. Site Location & Target Parameters
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = primaryAccentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${event.siteName} • ${event.location}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isGerman) "Geplante Maximaltiefe" else "Planned Max Depth",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${event.maxDepthMeters.toInt()} Meter",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryAccentColor
                    )
                }
                Column {
                    Text(
                        text = if (isGerman) "Mindest-Brevet" else "Min. Certification",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.requiredMinCert,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = if (isGerman) "Umkehrdruck (Turn)" else "Turn Pressure",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${event.turnPressureBar} bar",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }

    // 2. Dive Site Map & Underwater Topography (Tauchplatzkarte)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Map,
                        contentDescription = null,
                        tint = primaryAccentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGerman) "🗺️ Tauchplatzkarte & Topografie" else "🗺️ Site Map & Topography",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (canUploadMap) {
                    OutlinedButton(
                        onClick = onOpenMapPicker,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (event.siteMapImageUri.isNotBlank())
                                (if (isGerman) "Karte ändern" else "Change Map")
                            else
                                (if (isGerman) "Karte hochladen" else "Upload Map"),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (event.siteMapImageUri.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onShowFullscreenMap() }
                ) {
                    AsyncImage(
                        model = event.siteMapImageUri,
                        contentDescription = "Tauchplatzkarte",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 8.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = if (isGerman) "🔍 Zum Vergrößern tippen" else "🔍 Tap to zoom",
                            color = Color.White,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isGerman)
                                "Keine Tauchplatzkarte hinterlegt. Das Geländeprofil wird vor Ort im Briefing erläutert."
                            else
                                "No site map uploaded. Underwater topography will be explained during site briefing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // 3. Entry & Exit Info
    DossierSectionCard(
        icon = Icons.Rounded.Water,
        iconTint = primaryAccentColor,
        title = if (isGerman) "Einstieg, Ausstieg & Tauchgangsverlauf" else "Entry, Exit & Profile",
        content = event.entryExitInfo.ifBlank {
            if (isGerman)
                "Einstieg und Ausstieg nach Einweisung vor Ort durch den Tauchgangsleiter. Sicherer Notausstieg in Briefing-Skizze verzeichnet."
            else
                "Entry and exit according to briefing on site. Emergency exit points marked on briefing slate."
        }
    )

    // 4. Environmental Conditions
    DossierSectionCard(
        icon = Icons.Rounded.Air,
        iconTint = AquaCyan,
        title = if (isGerman) "Sichtverhältnisse, Wassertemperatur & Strömung" else "Conditions, Temp & Current",
        content = event.waterConditions.ifBlank {
            if (isGerman)
                "Lokale Bedingungen werden vor dem Einstieg final geprüft. Kaltwasser- und Strömungserfahrung entsprechend empfohlen."
            else
                "Local conditions verified before splash. Cold water & current experience recommended."
        }
    )

    // 5. Equipment Requirements
    DossierSectionCard(
        icon = Icons.Rounded.Security,
        iconTint = SeaGreen,
        title = if (isGerman) "Ausrüstungs-Anforderungen" else "Equipment Requirements",
        content = event.equipmentRequirements.ifBlank {
            if (isGerman)
                "Vollständige Kaltwasser-Ausrüstung gemäß Verbandsrichtlinien, Tauchcomputer sowie Signalboje (SMB) pro Buddy-Team erforderlich."
            else
                "Cold-water gear compliant with guidelines, dive computer and SMB per buddy team required."
        }
    )

    // 6. Hazards & Safety Rules
    if (event.hazardsAndRules.isNotBlank()) {
        DossierSectionCard(
            icon = Icons.Rounded.Warning,
            iconTint = MaterialTheme.colorScheme.error,
            title = if (isGerman) "Besondere Gefahren & Verhaltensregeln" else "Hazards & Rules",
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
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    // 1. Hyperbaric Chamber Highlight Card
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (isGerman) "Nächste Dekompressionskammer" else "Nearest Hyperbaric Chamber",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (event.chamberDistanceTime.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "⏱️ ${event.chamberDistanceTime}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = event.chamberName.ifBlank { if (isGerman) "Keine Druckkammer hinterlegt" else "No chamber configured" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (event.chamberPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { dialPhoneNumber(context, event.chamberPhone) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${if (isGerman) "Kammer anrufen:" else "Call Chamber:"} ${event.chamberPhone}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // 2. Coast Guard & Radio Channel
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Radio,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isGerman) "Seefunk & Notfall-Kommunikation" else "Maritime VHF & Radio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${if (isGerman) "Funkkanal:" else "VHF Channel:"} ${event.coastGuardVHF.ifBlank { "VHF 16 (Distress & Safety)" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isGerman)
                    "Allgemeiner Rettungsdienst: Notruf 112 (Europa) oder zuständige Seenotleitung"
                else
                    "Emergency Services: Dial 112 / 911 or local MRCC / Coast Guard",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // 3. First Aid & Oxygen Provider
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.HealthAndSafety,
                    contentDescription = null,
                    tint = SeaGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isGerman) "Sauerstoff-Versorgung & Erste Hilfe vor Ort" else "Oxygen & First Aid On-Site",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = event.emergencyNotes.ifBlank {
                    event.emergencyBriefing.ifBlank {
                        if (isGerman)
                            "Medizinischer Notfallsauerstoff (100% O2) und Erste-Hilfe-Ausrüstung einsatzbereit vor Ort."
                        else
                            "Medical oxygen kit (100% O2) and first aid kit ready on-site."
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
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.People,
                    contentDescription = null,
                    tint = primaryAccentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isGerman) "Einsatz-Leitung & Belegung" else "Lead Guide & Capacity",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isGerman) "Tauchgangsleiter / Guide" else "Lead Guide",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.leadGuideName.ifBlank { if (isGerman) "Noch nicht zugewiesen" else "Unassigned" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = if (isGerman) "Angemeldete Taucher" else "Booked Divers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${event.registeredCount} / ${event.maxCapacity} ${if (isGerman) "Plätze" else "spots"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = if (isGerman) "Schnell-Aktionen für diesen Tauchgang:" else "Quick actions for this excursion:",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    // Direct Action Buttons
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onCheckIn,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryAccentColor,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (isGerman) "➕ Einchecken" else "➕ Check In",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedButton(
            onClick = onNavigateToBuddyBoard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "🤿 Buddy-Board",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedButton(
            onClick = onNavigateToManifest,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "📋 Manifest",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
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
