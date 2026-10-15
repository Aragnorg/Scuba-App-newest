package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CorporateFare
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrganizationEntity
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

/**
 * Universal Workspace / Organization Switcher:
 * Allows diver to toggle seamlessly between Personal Profile Mode ("Mein Taucher-Profil")
 * and Multi-Tenant Organizations ("Tauchclub Delphin e.V." or "Blue Horizon Dive Resort").
 */
@Composable
fun WorkspaceSwitcherPill(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val currentOrg by viewModel.currentOrganization.collectAsState()
    val allOrgs by viewModel.allOrganizations.collectAsState()
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    var showDialog by remember { mutableStateOf(false) }

    // Pill Button in TopAppBar
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (currentOrg != null) {
            if (currentOrg!!.isClub) SeaGreen.copy(alpha = 0.18f) else OceanPrimary.copy(alpha = 0.18f)
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = BorderStroke(
            1.dp,
            if (currentOrg != null) {
                if (currentOrg!!.isClub) SeaGreen.copy(alpha = 0.6f) else OceanPrimary.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            }
        ),
        modifier = modifier
            .testTag("workspace_switcher_pill")
            .clip(RoundedCornerShape(16.dp))
            .clickable { showDialog = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconEmoji = when {
                currentOrg == null -> "👤"
                currentOrg!!.isClub -> "🏛️"
                currentOrg!!.isDiveCenter -> "⚓"
                else -> "🤿"
            }
            Text(
                text = iconEmoji,
                fontSize = 13.sp,
                modifier = Modifier.padding(end = 6.dp)
            )
            Column {
                Text(
                    text = currentOrg?.name?.take(22) ?: strings.personalDiverProfile,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (currentOrg != null) {
                        if (currentOrg!!.isClub) SeaGreen else OceanPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (currentOrg != null) {
                    Text(
                        text = if (currentOrg!!.isClub) {
                            if (isGerman) "Verein • ${currentOrg!!.activeMembersCount} Mgl." else "Club • ${currentOrg!!.activeMembersCount} Mem."
                        } else {
                            if (isGerman) "Basis • ${currentOrg!!.activeMembersCount} Staff/Gäste" else "Base • ${currentOrg!!.activeMembersCount} Staff/Guests"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = strings.switchOrganization,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (showDialog) {
        WorkspaceSelectionDialog(
            viewModel = viewModel,
            currentOrg = currentOrg,
            allOrgs = allOrgs,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun WorkspaceSelectionDialog(
    viewModel: AquaViewModel,
    currentOrg: OrganizationEntity?,
    allOrgs: List<OrganizationEntity>,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    var showCreateDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CorporateFare,
                    contentDescription = null,
                    tint = OceanPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.workspaceSwitcherTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
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
                Text(
                    text = "Wähle deine Arbeitsumgebung: Persönlicher Modus oder eine Tauchorganisation:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Personal Diver Mode
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentOrg == null) {
                            OceanPrimary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(
                        width = if (currentOrg == null) 1.5.dp else 1.dp,
                        color = if (currentOrg == null) OceanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectOrganization(null)
                            onDismiss()
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = OceanPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👤", fontSize = 18.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.personalDiverProfile,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.personalDiverSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (currentOrg == null) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Active",
                                tint = OceanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                Text(
                    text = "Tauchorganisationen & Basen (${allOrgs.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // 2. Organization List
                allOrgs.forEach { org ->
                    val isSelected = currentOrg?.id == org.id
                    val isClub = org.isClub
                    val accentColor = if (isClub) SeaGreen else OceanPrimary

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                accentColor.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectOrganization(org)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = accentColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isClub) "🏛️" else "⚓",
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = org.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = accentColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = if (isGerman) (if (isClub) "VEREIN" else "BASIS") else (if (isClub) "CLUB" else "BASE"),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = accentColor
                                        )
                                    }
                                }
                                if (org.waterBody.isNotBlank() || org.location.isNotBlank()) {
                                    Text(
                                        text = listOf(org.waterBody, org.location).filter { it.isNotBlank() }.joinToString(" • "),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (org.affiliations.isNotBlank()) {
                                    Text(
                                        text = "Verband: ${org.affiliations}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = accentColor
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Active",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 3. Register New Organization
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OceanPrimary
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.createNewOrg)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.close)
            }
        }
    )

    if (showCreateDialog) {
        CreateOrganizationDialog(
            viewModel = viewModel,
            onDismiss = {
                showCreateDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun CreateOrganizationDialog(
    viewModel: AquaViewModel,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var orgType by remember { mutableStateOf("CLUB") } // CLUB, DIVE_CENTER, DIVE_SCHOOL
    var affiliations by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var waterBody by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var chamberLocation by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = strings.createNewOrg,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Erstelle eine neue Mandanten-Instanz für einen Tauchverein, eine Tauchbasis oder Tauchschule:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Test-Fill Sample Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            name = "Tauchclub Delphin Bodensee e.V."
                            orgType = "CLUB"
                            affiliations = "VDST, CMAS, BLTV"
                            location = "Überlingen am Bodensee, DE"
                            waterBody = "Bodensee / Überlinger See"
                            contactEmail = "vorstand@tc-delphin-bodensee.de"
                            contactPhone = "+49 7551 987654"
                            chamberLocation = "Druckkammerzentrum Überlingen (Tel: 07551 8370)"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("⚡ Test-Verein", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SeaGreen)
                    }

                    OutlinedButton(
                        onClick = {
                            name = "Red Sea Blue Horizon Dive Resort"
                            orgType = "DIVE_CENTER"
                            affiliations = "PADI 5-Star CDC, SSI Diamond Resort"
                            location = "Sharm El Sheikh, Süd-Sinai, EG"
                            waterBody = "Rotes Meer / Ras Mohammed"
                            contactEmail = "ops@bluehorizon-dive.com"
                            contactPhone = "+20 69 360 4420"
                            chamberLocation = "Hyperbaric Medical Center Sharm (Tel: +20 69 366 0922)"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text("⚡ Test-Basis", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OceanPrimary)
                    }
                }

                // Organization Type Radio Group
                Text(
                    text = strings.orgTypeLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Club
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (orgType == "CLUB") SeaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, if (orgType == "CLUB") SeaGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { orgType = "CLUB" }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🏛️", fontSize = 20.sp)
                            Text("Verein", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Dive Center
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (orgType == "DIVE_CENTER") OceanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, if (orgType == "DIVE_CENTER") OceanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { orgType = "DIVE_CENTER" }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚓", fontSize = 20.sp)
                            Text("Basis", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Dive School
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (orgType == "DIVE_SCHOOL") AquaCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, if (orgType == "DIVE_SCHOOL") AquaCyan else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { orgType = "DIVE_SCHOOL" }
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🤿", fontSize = 20.sp)
                            Text("Schule", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.orgNameLabel + " *") },
                    placeholder = { Text(if (orgType == "CLUB") "z.B. Tauchclub Seelöwe e.V." else "z.B. Deep Blue Dive Center") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = affiliations,
                    onValueChange = { affiliations = it },
                    label = { Text(strings.affiliationsLabel) },
                    placeholder = { Text("z.B. VDST, CMAS, PADI, SSI") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(strings.locationCityLabel) },
                    placeholder = { Text("z.B. Konstanz am Bodensee, DE") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = waterBody,
                    onValueChange = { waterBody = it },
                    label = { Text(strings.waterBodyLabel) },
                    placeholder = { Text("z.B. Bodensee / Überlinger See") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Kontakt E-Mail") },
                    placeholder = { Text("info@org-diving.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = chamberLocation,
                    onValueChange = { chamberLocation = it },
                    label = { Text(strings.nearestChamber) },
                    placeholder = { Text("z.B. Druckkammer Überlingen Tel 07551 8370") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Bitte gib einen Namen für die Organisation an."
                        return@Button
                    }
                    val emergencyJson = org.json.JSONObject().apply {
                        put("nearestChamber", chamberLocation.ifBlank { "Lokales Druckkammerzentrum" })
                        put("emergencyPhone", "112 / DAN HotLine")
                        put("gpsEvacuationPoint", location.ifBlank { "Basis Marina" })
                        put("o2KitLocation", "Basis Tresen / O2-Koffer")
                    }.toString()

                    viewModel.createOrganization(
                        name = name,
                        orgType = orgType,
                        affiliations = affiliations,
                        location = location,
                        waterBody = waterBody,
                        contactEmail = contactEmail,
                        contactPhone = contactPhone,
                        emergencyPlanJson = emergencyJson
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun EditOrganizationDialog(
    organization: OrganizationEntity,
    onSave: (OrganizationEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(organization.name) }
    var orgType by remember { mutableStateOf(organization.orgType) }
    var affiliations by remember { mutableStateOf(organization.affiliations) }
    var location by remember { mutableStateOf(organization.location) }
    var waterBody by remember { mutableStateOf(organization.waterBody) }
    var contactEmail by remember { mutableStateOf(organization.contactEmail) }
    var contactPhone by remember { mutableStateOf(organization.contactPhone) }
    var website by remember { mutableStateOf(organization.website) }

    var chamberLocation by remember {
        mutableStateOf(
            try {
                org.json.JSONObject(organization.emergencyPlanJson).optString("nearestChamber", "")
            } catch (_: Exception) { "" }
        )
    }
    var emergencyPhone by remember {
        mutableStateOf(
            try {
                org.json.JSONObject(organization.emergencyPlanJson).optString("emergencyPhone", "+49 112 / DAN")
            } catch (_: Exception) { "+49 112 / DAN" }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = "Organisation bearbeiten",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Test-Fill Sample Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            name = "Tauchclub Delphin Bodensee e.V."
                            orgType = "CLUB"
                            affiliations = "VDST, CMAS, BLTV"
                            location = "Überlingen am Bodensee, DE"
                            waterBody = "Bodensee / Überlinger See"
                            contactEmail = "vorstand@tc-delphin-bodensee.de"
                            contactPhone = "+49 7551 987654"
                            website = "www.tc-delphin-bodensee.de"
                            chamberLocation = "Druckkammerzentrum Überlingen (Tel: +49 7551 8370)"
                            emergencyPhone = "+49 112 (DLRG / Wasserwacht)"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text("⚡ Test-Verein", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SeaGreen)
                    }

                    OutlinedButton(
                        onClick = {
                            name = "Red Sea Blue Horizon Dive Resort"
                            orgType = "DIVE_CENTER"
                            affiliations = "PADI 5-Star CDC, SSI Diamond Resort"
                            location = "Sharm El Sheikh, Süd-Sinai, EG"
                            waterBody = "Rotes Meer / Ras Mohammed"
                            contactEmail = "ops@bluehorizon-dive.com"
                            contactPhone = "+20 69 360 4420"
                            website = "www.bluehorizon-dive.com"
                            chamberLocation = "Hyperbaric Medical Center Sharm (Tel: +20 69 366 0922)"
                            emergencyPhone = "+20 122 218 4800 (Red Sea SAR)"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Text("⚡ Test-Basis", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OceanPrimary)
                    }
                }

                // Org Type Selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (orgType == "CLUB") SeaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, if (orgType == "CLUB") SeaGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.weight(1f).clickable { orgType = "CLUB" }
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏛️", fontSize = 18.sp)
                            Text("Verein", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (orgType == "DIVE_CENTER") OceanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, if (orgType == "DIVE_CENTER") OceanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.weight(1f).clickable { orgType = "DIVE_CENTER" }
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⚓", fontSize = 18.sp)
                            Text("Basis", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (orgType == "DIVE_SCHOOL") AquaCyan.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, if (orgType == "DIVE_SCHOOL") AquaCyan else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.weight(1f).clickable { orgType = "DIVE_SCHOOL" }
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🤿", fontSize = 18.sp)
                            Text("Schule", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Organisationsname *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = affiliations,
                    onValueChange = { affiliations = it },
                    label = { Text("Verbände / Zugehörigkeiten") },
                    placeholder = { Text("z.B. VDST, CMAS, PADI, SSI") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Standort / Ort") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = waterBody,
                    onValueChange = { waterBody = it },
                    label = { Text("Hausgewässer / Region") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Kontakt E-Mail") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Telefonnummer") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = chamberLocation,
                    onValueChange = { chamberLocation = it },
                    label = { Text("Nächste Druckkammer") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = emergencyPhone,
                    onValueChange = { emergencyPhone = it },
                    label = { Text("Notruf / Hotline") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedPlan = org.json.JSONObject().apply {
                        put("nearestChamber", chamberLocation.ifBlank { "Lokales Druckkammerzentrum" })
                        put("emergencyPhone", emergencyPhone.ifBlank { "+49 112 / DAN" })
                        put("gpsEvacuationPoint", location.ifBlank { "Basis Marina" })
                        put("o2KitLocation", "Basis Tresen / O2-Koffer")
                    }.toString()

                    val updated = organization.copy(
                        name = name.trim(),
                        orgType = orgType,
                        affiliations = affiliations.trim(),
                        location = location.trim(),
                        waterBody = waterBody.trim(),
                        contactEmail = contactEmail.trim(),
                        contactPhone = contactPhone.trim(),
                        website = website.trim(),
                        emergencyPlanJson = updatedPlan
                    )
                    onSave(updated)
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Abbrechen")
            }
        }
    )
}

