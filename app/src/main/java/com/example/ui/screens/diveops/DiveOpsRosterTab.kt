package com.example.ui.screens.diveops

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrganizationEntity
import com.example.data.model.OrganizationMemberEntity
import com.example.ui.components.EditOrganizationDialog
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import com.example.util.DocumentExpirationHelper
import com.example.util.ExpirationAlertLevel
import org.json.JSONObject

/**
 * Tab 0: DiveOps Roster & Safety Dossier
 * Shows organization hero banner, TTU compliance traffic lights,
 * searchable member directory with role filtering, and fast emergency plan access.
 */
@Composable
fun DiveOpsRosterTab(
    viewModel: AquaViewModel,
    organization: OrganizationEntity,
    isGerman: Boolean,
    primaryAccentColor: Color,
    modifier: Modifier = Modifier
) {
    val members by viewModel.currentOrgMembers.collectAsState()
    val strings = LocalAppStrings.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("ALL") }
    var showEmergencyDialog by remember { mutableStateOf(false) }
    var showEditOrgDialog by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var memberToEditTtu by remember { mutableStateOf<OrganizationMemberEntity?>(null) }
    var memberToChangeRole by remember { mutableStateOf<OrganizationMemberEntity?>(null) }
    var memberToDelete by remember { mutableStateOf<OrganizationMemberEntity?>(null) }

    // TTU Traffic Light Statistics
    val ttuStats = remember(members, isGerman) {
        var validCount = 0
        var expiringSoonCount = 0
        var expiredCount = 0
        members.forEach { m ->
            val info = DocumentExpirationHelper.computeDateExpiration(m.ttuExpiryDate, isGerman)
            when (info.level) {
                ExpirationAlertLevel.VALID -> validCount++
                ExpirationAlertLevel.EXPIRING_SOON -> expiringSoonCount++
                ExpirationAlertLevel.EXPIRED, ExpirationAlertLevel.LIFETIME -> {
                    if (m.ttuExpiryDate.isNullOrBlank() || info.level == ExpirationAlertLevel.EXPIRED) {
                        expiredCount++
                    } else {
                        validCount++
                    }
                }
            }
        }
        Triple(validCount, expiringSoonCount, expiredCount)
    }

    // Filter members
    val filteredMembers = remember(members, searchQuery, selectedRoleFilter) {
        members.filter { m ->
            val matchesSearch = searchQuery.isBlank() ||
                m.fullName.contains(searchQuery, ignoreCase = true) ||
                m.email.contains(searchQuery, ignoreCase = true) ||
                m.membershipNumber.contains(searchQuery, ignoreCase = true) ||
                m.certLevel.contains(searchQuery, ignoreCase = true)

            val matchesRole = when (selectedRoleFilter) {
                "ALL" -> true
                "OWNER" -> m.role == "OWNER"
                "INSTRUCTOR" -> m.role == "INSTRUCTOR"
                "TECH_MANAGER" -> m.role == "TECH_MANAGER"
                "MEMBER_GUEST" -> m.role == "MEMBER" || m.role == "GUEST"
                else -> true
            }
            matchesSearch && matchesRole
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
            // 1. Organization Hero & Emergency Action Card
            OrgHeroBanner(
                organization = organization,
                primaryAccentColor = primaryAccentColor,
                onEditOrganization = { showEditOrgDialog = true },
                onOpenEmergencyPlan = { showEmergencyDialog = true }
            )
        }

        item {
            // 2. Safety Gate & TTU Traffic Light Dashboard
            SafetyGateTrafficLightBar(
                validCount = ttuStats.first,
                expiringSoonCount = ttuStats.second,
                expiredCount = ttuStats.third,
                totalCount = members.size,
                strings = strings
            )
        }

        item {
            // 3. Search and Role Filter Chips
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            if (organization.isClub) "Mitglied, Brevet, E-Mail suchen..." else "Kunde, Gast, Brevet suchen..."
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = primaryAccentColor)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("diveops_search_field"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryAccentColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    singleLine = true
                )

                // Role filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedRoleFilter == "ALL",
                        onClick = { selectedRoleFilter = "ALL" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text("Alle (${members.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryAccentColor
                        )
                    )
                    FilterChip(
                        selected = selectedRoleFilter == "OWNER",
                        onClick = { selectedRoleFilter = "OWNER" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text(if (organization.isClub) "Vorstand" else "Leitung", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryAccentColor
                        )
                    )
                    FilterChip(
                        selected = selectedRoleFilter == "INSTRUCTOR",
                        onClick = { selectedRoleFilter = "INSTRUCTOR" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text(if (organization.isClub) "Ausbilder" else "Guides", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryAccentColor
                        )
                    )
                    FilterChip(
                        selected = selectedRoleFilter == "TECH_MANAGER",
                        onClick = { selectedRoleFilter = "TECH_MANAGER" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text(if (organization.isClub) "Gerätewart" else "Technik", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryAccentColor
                        )
                    )
                    FilterChip(
                        selected = selectedRoleFilter == "MEMBER_GUEST",
                        onClick = { selectedRoleFilter = "MEMBER_GUEST" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text(if (organization.isClub) "Mitglieder" else "Gäste", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                            selectedLabelColor = primaryAccentColor
                        )
                    )
                }
            }
        }

        item {
            // Roster Section Header with Add Member Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (organization.isClub) strings.rosterTitleClub else strings.rosterTitleCenter,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${filteredMembers.size} Personen erfasst",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { viewModel.seedSampleMembersForCurrentOrg() },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, primaryAccentColor.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("⚡ Test-Mitglieder", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryAccentColor)
                    }

                    Button(
                        onClick = { showAddMemberDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (organization.isClub) "+ Mitglied" else "+ Gast/Diver", fontSize = 12.sp)
                    }
                }
            }
        }

        // 4. Member Cards List
        if (filteredMembers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Text("🔍", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Keine Personen gefunden",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Passe die Filter oder den Suchbegriff an.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredMembers, key = { it.id }) { member ->
                MemberRosterCard(
                    member = member,
                    organization = organization,
                    isGerman = isGerman,
                    onUpdateTtu = { memberToEditTtu = member },
                    onChangeRole = { memberToChangeRole = member },
                    onDelete = { memberToDelete = member }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- Dialogs ---
    if (showEmergencyDialog) {
        EmergencyPlanDialog(
            organization = organization,
            onDismiss = { showEmergencyDialog = false }
        )
    }

    if (showAddMemberDialog) {
        AddOrganizationMemberDialog(
            organization = organization,
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name, email, role, cert, phone, number, ttuDate, notes ->
                viewModel.addOrganizationMember(
                    fullName = name,
                    email = email,
                    role = role,
                    certLevel = cert,
                    phoneNumber = phone,
                    membershipNumber = number,
                    ttuExpiryDate = ttuDate,
                    notes = notes
                )
                showAddMemberDialog = false
            }
        )
    }

    if (memberToEditTtu != null) {
        UpdateTtuDialog(
            member = memberToEditTtu!!,
            onDismiss = { memberToEditTtu = null },
            onConfirm = { expiryDate, doctorName ->
                viewModel.updateMemberTtu(
                    memberId = memberToEditTtu!!.id,
                    docId = null,
                    expiryDate = expiryDate,
                    doctorName = doctorName
                )
                memberToEditTtu = null
            }
        )
    }

    if (memberToChangeRole != null) {
        ChangeMemberRoleDialog(
            member = memberToChangeRole!!,
            isClub = organization.isClub,
            onDismiss = { memberToChangeRole = null },
            onConfirm = { newRole ->
                viewModel.updateMemberRole(memberToChangeRole!!.id, newRole)
                memberToChangeRole = null
            }
        )
    }

    if (memberToDelete != null) {
        AlertDialog(
            onDismissRequest = { memberToDelete = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = { Text("Person entfernen") },
            text = {
                Text("Möchtest du '${memberToDelete!!.fullName}' wirklich aus dieser Organisation entfernen?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeOrganizationMember(memberToDelete!!.id)
                        memberToDelete = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
                ) {
                    Text("Entfernen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { memberToDelete = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (showEditOrgDialog) {
        EditOrganizationDialog(
            organization = organization,
            onSave = { updated -> viewModel.updateOrganizationDetails(updated) },
            onDismiss = { showEditOrgDialog = false }
        )
    }
}

/**
 * Organization Header Hero Banner:
 * Displays organization identity, associations, region, and fast emergency plan access.
 */
@Composable
fun OrgHeroBanner(
    organization: OrganizationEntity,
    primaryAccentColor: Color,
    onEditOrganization: () -> Unit,
    onOpenEmergencyPlan: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, primaryAccentColor.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = primaryAccentColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (organization.isClub) "🏛️" else "⚓",
                            fontSize = 24.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = organization.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (organization.isClub) "Eingetragener Tauchverein (e.V.)" else "Kommerzielle Tauchbasis & Resort",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryAccentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Region & Water Body
            if (organization.waterBody.isNotBlank() || organization.location.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(text = "🌊", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = listOf(organization.waterBody, organization.location).filter { it.isNotBlank() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Affiliations & Emergency Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (organization.affiliations.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "Verband: ${organization.affiliations}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onEditOrganization,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, primaryAccentColor.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "✏️", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bearbeiten",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryAccentColor
                        )
                    }

                    // Emergency Plan Dossier Button
                    Button(
                        onClick = onOpenEmergencyPlan,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC53030)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "🚨", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Notfallplan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Safety Gate Traffic Light KPI Bar:
 * Prominently surfaces TTU compliance: Valid (Green), Expiring in <30d (Yellow), Expired (Red).
 */
@Composable
fun SafetyGateTrafficLightBar(
    validCount: Int,
    expiringSoonCount: Int,
    expiredCount: Int,
    totalCount: Int,
    strings: com.example.ui.i18n.Strings
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🩺", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.ttuComplianceStatus,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "$validCount von $totalCount freigegeben",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Traffic Light Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Green: Valid
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF38A169).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF38A169).copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$validCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF276749)
                        )
                        Text(
                            text = "🟢 Gültig",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF276749)
                        )
                    }
                }

                // Yellow: Expiring Soon
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFD69E2E).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFD69E2E).copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$expiringSoonCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB7791F)
                        )
                        Text(
                            text = "🟡 <30 Tage",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB7791F)
                        )
                    }
                }

                // Red: Expired / Missing
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE53E3E).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFE53E3E).copy(alpha = 0.6f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$expiredCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFC53030)
                        )
                        Text(
                            text = "🔴 Abgelaufen",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC53030)
                        )
                    }
                }
            }

            // Safety Gate Active Alert
            if (expiredCount > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE53E3E).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFE53E3E).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "⛔ Safety Gate: $expiredCount Person(en) ohne gültige TTU gesperrt für Tief-/Bootsausfahrten!",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC53030)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Member Roster Card:
 * Displays full diver profile, role pill, brevet, TTU traffic-light status, and management actions.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberRosterCard(
    member: OrganizationMemberEntity,
    organization: OrganizationEntity,
    isGerman: Boolean,
    onUpdateTtu: () -> Unit,
    onChangeRole: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isClub = organization.isClub

    // TTU Expiration Analysis
    val ttuInfo = remember(member.ttuExpiryDate, isGerman) {
        DocumentExpirationHelper.computeDateExpiration(member.ttuExpiryDate, isGerman)
    }

    val roleColor = when (member.role) {
        "OWNER" -> Color(0xFF805AD5)
        "INSTRUCTOR" -> OceanPrimary
        "TECH_MANAGER" -> Color(0xFFDD6B20)
        else -> SeaGreen
    }

    val roleTitle = when (member.role) {
        "OWNER" -> if (isClub) "Vereinsvorstand" else "Basisleiter"
        "INSTRUCTOR" -> if (isClub) "Tauchgruppenleiter" else "Senior Guide / TL"
        "TECH_MANAGER" -> if (isClub) "Gerätewart" else "Basistechniker"
        "GUEST" -> "Kunde / Gast"
        else -> if (isClub) "Vereinsmitglied" else "Gasttaucher"
    }

    val ttuColor = when (ttuInfo.level) {
        ExpirationAlertLevel.VALID -> Color(0xFF2F855A)
        ExpirationAlertLevel.EXPIRING_SOON -> Color(0xFFD69E2E)
        ExpirationAlertLevel.EXPIRED, ExpirationAlertLevel.LIFETIME -> {
            if (member.ttuExpiryDate.isNullOrBlank() || ttuInfo.level == ExpirationAlertLevel.EXPIRED) {
                Color(0xFFE53E3E)
            } else {
                Color(0xFF2F855A)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = if (ttuInfo.level == ExpirationAlertLevel.EXPIRED) 1.5.dp else 1.dp,
            color = if (ttuInfo.level == ExpirationAlertLevel.EXPIRED) Color(0xFFE53E3E).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Row: Avatar, Name, Role Badge, Expand Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = roleColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.5.dp, roleColor.copy(alpha = 0.7f)),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = member.fullName.take(1).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = roleColor,
                            fontSize = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = member.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = roleColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = roleTitle,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = roleColor
                            )
                        }
                    }
                    Text(
                        text = "ID: ${member.membershipNumber} • ${member.email}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Brevet & Logged Dives Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OceanPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "🤿 ${member.certLevel}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanPrimary
                    )
                }
                if (member.totalLoggedDives > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = if (isGerman) "${member.totalLoggedDives} Tauchgänge" else "${member.totalLoggedDives} Dives",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Specialties pills
            val specialties = member.parseSpecialties()
            if (specialties.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    specialties.forEach { spec ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = spec,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // TTU / Medical Traffic Light Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ttuColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, ttuColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = when (ttuInfo.level) {
                                ExpirationAlertLevel.VALID -> "🟢"
                                ExpirationAlertLevel.EXPIRING_SOON -> "🟡"
                                else -> "🔴"
                            },
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = ttuInfo.badgeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = ttuColor
                            )
                            if (member.ttuDoctorName.isNotBlank()) {
                                Text(
                                    text = "Arzt: ${member.ttuDoctorName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Update TTU button
                    TextButton(
                        onClick = onUpdateTtu,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text("🩺 TTU anpassen", fontSize = 11.sp, color = ttuColor, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Expandable Details (Insurance, Contact, Rental gear, Notes)
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    if (member.emergencyContactName.isNotBlank() || member.emergencyContactPhone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📇", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Notfallkontakt: ${member.emergencyContactName} (${member.emergencyContactPhone})",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (member.insuranceProvider.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🛡️", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Versicherung: ${member.insuranceProvider} (${member.insuranceNumber})",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (member.rentalPreferences.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🤿", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isGerman) "Leih-Ausrüstung: ${member.rentalPreferences}" else "Rental Gear: ${member.rentalPreferences}",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (member.notes.isNotBlank()) {
                        Text(
                            text = if (isGerman) "Notizen: ${member.notes}" else "Notes: ${member.notes}",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Management Action Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onChangeRole,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isGerman) "Rolle ändern" else "Change Role", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = if (isGerman) "Löschen" else "Delete", tint = Color(0xFFE53E3E), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Emergency Plan Dossier Dialog:
 * Shows immediate chamber location, emergency phone with quick-dial, GPS assembly point, and O2 kit.
 */
@Composable
fun EmergencyPlanDialog(
    organization: OrganizationEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val emergencyJson = try {
        JSONObject(organization.emergencyPlanJson)
    } catch (_: Exception) {
        JSONObject()
    }
    val nearestChamber = emergencyJson.optString("nearestChamber", "Lokales Druckkammerzentrum")
    val emergencyPhone = emergencyJson.optString("emergencyPhone", "112 / DAN Europe +39 06 42118685")
    val gpsEvacPoint = emergencyJson.optString("gpsEvacuationPoint", organization.location.ifBlank { "Basis Marina" })
    val o2KitLocation = emergencyJson.optString("o2KitLocation", "Basis Tresen & Notfallkoffer")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🚨", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notfallplan & Rettungskette",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC53030)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Operativer Notfallplan für ${organization.name}:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Chamber
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🏥 Nächste Dekompressionskammer:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nearestChamber,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 2. Emergency Phone / SAR
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE53E3E).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFFE53E3E).copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📞 Notruf / Seenotrettung (SAR):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC53030)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = emergencyPhone,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC53030)
                            )
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${emergencyPhone.filter { it.isDigit() || it == '+' }}")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Rounded.Phone, contentDescription = "Dial", tint = Color(0xFFC53030))
                        }
                    }
                }

                // 3. GPS Evacuation Point
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📍 Rettungstreffpunkt / Slipstelle:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SeaGreen
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = gpsEvacPoint,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 4. Oxygen Kit Location
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🫁 100% Notfall-Sauerstoff (O2-Koffer):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = o2KitLocation,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Schließen")
            }
        }
    )
}

/**
 * Add Organization Member Dialog:
 * Adds a new diver/guest to the roster with role, TTU date, and membership number.
 */
@Composable
fun AddOrganizationMemberDialog(
    organization: OrganizationEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, role: String, cert: String, phone: String, number: String, ttuDate: String?, notes: String) -> Unit
) {
    val isClub = organization.isClub
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(if (isClub) "MEMBER" else "GUEST") }
    var certLevel by remember { mutableStateOf("Open Water Diver (18m)") }
    var phoneNumber by remember { mutableStateOf("") }
    var membershipNumber by remember { mutableStateOf("") }
    var ttuExpiryDate by remember { mutableStateOf("2027-05-15") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text(if (isClub) "Mitglied anlegen" else "Kunde / Gast anlegen") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Vollständiger Name *") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-Mail-Adresse *") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefonnummer") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = membershipNumber,
                    onValueChange = { membershipNumber = it },
                    label = { Text(if (isClub) "Mitgliedsnummer" else "Kundennummer / Pass") },
                    placeholder = { Text(if (isClub) "z.B. TCD-099" else "z.B. GST-4421") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = certLevel,
                    onValueChange = { certLevel = it },
                    label = { Text("Brevet-Stufe") },
                    placeholder = { Text("z.B. CMAS * / OWD / AOWD") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ttuExpiryDate,
                    onValueChange = { ttuExpiryDate = it },
                    label = { Text("TTU Ablaufdatum (YYYY-MM-DD)") },
                    placeholder = { Text("2027-05-15") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Selector
                Text("Rolle auswählen:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = role == (if (isClub) "MEMBER" else "GUEST"),
                        onClick = { role = if (isClub) "MEMBER" else "GUEST" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text(if (isClub) "Mitglied" else "Gast", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = role == "INSTRUCTOR",
                        onClick = { role = "INSTRUCTOR" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text(if (isClub) "Ausbilder" else "Guide", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = role == "TECH_MANAGER",
                        onClick = { role = "TECH_MANAGER" },
                        shape = RoundedCornerShape(10.dp),
                        label = { Text(if (isClub) "Gerätewart" else "Technik", fontSize = 11.sp) }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank() || email.isBlank()) {
                        errorMessage = "Bitte Name und E-Mail ausfüllen."
                        return@Button
                    }
                    onConfirm(fullName, email, role, certLevel, phoneNumber, membershipNumber, ttuExpiryDate, notes)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Speichern")
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
 * Update TTU Expiry Dialog:
 * Allows quick updating of medical certificate date and doctor.
 */
@Composable
fun UpdateTtuDialog(
    member: OrganizationMemberEntity,
    onDismiss: () -> Unit,
    onConfirm: (expiryDate: String, doctorName: String) -> Unit
) {
    var expiryDate by remember { mutableStateOf(member.ttuExpiryDate.ifBlank { "2027-06-30" }) }
    var doctorName by remember { mutableStateOf(member.ttuDoctorName.ifBlank { "Dr. med. Thomas Weber" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("🩺 TTU / Attest aktualisieren") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Tauchtauglichkeitsuntersuchung (TTU) für ${member.fullName}:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = { expiryDate = it },
                    label = { Text("Neues Ablaufdatum (YYYY-MM-DD)") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Ausstellender Arzt / Praxis") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(expiryDate, doctorName) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Aktualisieren")
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
 * Change Member Role Dialog:
 */
@Composable
fun ChangeMemberRoleDialog(
    member: OrganizationMemberEntity,
    isClub: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newRole: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(member.role) }
    val roles = listOf(
        "OWNER" to if (isClub) "🏛️ Vereinsvorstand / Leitung" else "⚓ Basisleiter / Inhaber",
        "INSTRUCTOR" to if (isClub) "🤿 Ausbilder / Tauchgruppenleiter" else "🤿 Tauchlehrer / Senior Guide",
        "TECH_MANAGER" to if (isClub) "🔧 Gerätewart (Flaschen/TÜV)" else "🔧 Basistechniker / Werkstatt",
        (if (isClub) "MEMBER" else "GUEST") to if (isClub) "🤿 Reguläres Vereinsmitglied" else "🤿 Kunde / Safari-Gast"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = { Text("Rolle ändern") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Wähle eine neue Rolle für ${member.fullName}:", style = MaterialTheme.typography.bodySmall)
                roles.forEach { (roleKey, roleLabel) ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRole == roleKey) OceanPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainer
                        ),
                        border = BorderStroke(1.dp, if (selectedRole == roleKey) OceanPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRole = roleKey }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = roleLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedRole == roleKey) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedRole == roleKey) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedRole) },
                shape = RoundedCornerShape(12.dp)
            ) { Text("Übernehmen") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) { Text("Abbrechen") }
        }
    )
}
