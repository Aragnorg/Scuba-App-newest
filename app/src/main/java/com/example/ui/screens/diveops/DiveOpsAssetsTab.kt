@file:OptIn(ExperimentalLayoutApi::class)

package com.example.ui.screens.diveops

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InventoryItemEntity
import com.example.data.model.OrganizationEntity
import com.example.data.model.OrganizationMemberEntity
import com.example.data.model.RentalTransactionEntity
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import com.example.util.DocumentExpirationHelper
import com.example.util.ExpirationAlertLevel
import java.time.LocalDate

/**
 * Tab 4: Inventar, TÜV & Verleih (Asset & Risk Control)
 * Universal asset management for cylinders, regulators, BCDs, emergency kits and compressors.
 * Features automatic TÜV/service expiration tracking, rental lock-outs, and rapid check-out/check-in workflows.
 */
@Composable
fun DiveOpsAssetsTab(
    viewModel: AquaViewModel,
    organization: OrganizationEntity,
    isGerman: Boolean,
    primaryAccentColor: Color,
    modifier: Modifier = Modifier
) {
    val inventory by viewModel.currentOrgInventory.collectAsState()
    val activeRentals by viewModel.currentOrgActiveRentals.collectAsState()
    val members by viewModel.currentOrgMembers.collectAsState()
    val strings = LocalAppStrings.current

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemForCheckOut by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var itemForCheckIn by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var itemForServiceUpdate by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var compressorForTelemetry by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<InventoryItemEntity?>(null) }

    // Statistics & Traffic Lights
    val todayStr = remember { LocalDate.now().toString() }
    val assetStats = remember(inventory, todayStr) {
        var availableCount = 0
        var rentedCount = 0
        var maintenanceCount = 0
        var lockedExpiredCount = 0

        inventory.forEach { item ->
            val isExpired = item.nextServiceDate.isNotBlank() && item.nextServiceDate < todayStr
            when {
                item.status == "RENTED" -> rentedCount++
                item.status == "MAINTENANCE" -> maintenanceCount++
                isExpired || item.status == "LOCKED_EXPIRED" -> lockedExpiredCount++
                else -> availableCount++
            }
        }
        Tuple4(availableCount, rentedCount, maintenanceCount, lockedExpiredCount)
    }

    // Filter items
    val filteredInventory = remember(inventory, selectedCategoryFilter) {
        when (selectedCategoryFilter) {
            "ALL" -> inventory
            "TANK" -> inventory.filter { it.isTank }
            "REGULATOR" -> inventory.filter { it.isRegulator }
            "BCD_SUIT" -> inventory.filter { it.isBcd || it.isSuit }
            "COMPUTER" -> inventory.filter { it.isComputer }
            "O2_KIT" -> inventory.filter { it.isO2Kit }
            "COMPRESSOR" -> inventory.filter { it.isCompressor }
            else -> inventory
        }
    }

    val compressorItem = remember(inventory) { inventory.find { it.isCompressor } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Section Header & + Equipment Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.assetControlTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = strings.assetControlSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddItemDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryAccentColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+ Equipment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // KPI Traffic Lights Bar
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ausrüstungs- & Prüffristen-Status:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${inventory.size} Gegenstände erfasst",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Ready
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SeaGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "${assetStats.first}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SeaGreen)
                                Text(text = "🟢 Bereit", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SeaGreen)
                            }
                        }

                        // Rented
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OceanPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "${assetStats.second}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = OceanPrimary)
                                Text(text = "📦 Verliehen", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OceanPrimary)
                            }
                        }

                        // Maintenance
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFD69E2E).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFD69E2E).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "${assetStats.third}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB7791F))
                                Text(text = "🟡 Wartung", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB7791F))
                            }
                        }

                        // Locked / Expired TÜV
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE53E3E).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFE53E3E).copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "${assetStats.fourth}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFC53030))
                                Text(text = "⛔ Gesperrt", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC53030))
                            }
                        }
                    }

                    // Active Rental Lock Alert
                    if (assetStats.fourth > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE53E3E).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFFE53E3E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE53E3E), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "⛔ Verleih-Sperre aktiv: ${assetStats.fourth} Gegenstand/Flasche mit abgelaufenem TÜV für Ausgabe gesperrt!",
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

        // Compressor Station Card (if present)
        if (compressorItem != null) {
            item {
                CompressorStationCard(
                    compressor = compressorItem,
                    onLogHours = { compressorForTelemetry = compressorItem },
                    onResetFilter = {
                        viewModel.updateCompressorTelemetry(
                            itemId = compressorItem.id,
                            addedHours = 0f,
                            resetFilter = true,
                            airAnalysisDate = compressorItem.airAnalysisDate
                        )
                    }
                )
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == "ALL",
                    onClick = { selectedCategoryFilter = "ALL" },
                    label = { Text("Alle (${inventory.size})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
                FilterChip(
                    selected = selectedCategoryFilter == "TANK",
                    onClick = { selectedCategoryFilter = "TANK" },
                    label = { Text("🫧 Flaschen", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
                FilterChip(
                    selected = selectedCategoryFilter == "REGULATOR",
                    onClick = { selectedCategoryFilter = "REGULATOR" },
                    label = { Text("🤿 Regler", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
                FilterChip(
                    selected = selectedCategoryFilter == "BCD_SUIT",
                    onClick = { selectedCategoryFilter = "BCD_SUIT" },
                    label = { Text("🦺 Jackets", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
                FilterChip(
                    selected = selectedCategoryFilter == "O2_KIT",
                    onClick = { selectedCategoryFilter = "O2_KIT" },
                    label = { Text("🫁 O2-Koffer", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryAccentColor.copy(alpha = 0.2f),
                        selectedLabelColor = primaryAccentColor
                    )
                )
            }
        }

        // Equipment List Items
        if (filteredInventory.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔧", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Kein Equipment in dieser Kategorie",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            items(filteredInventory, key = { it.id }) { item ->
                    val isExpired = item.nextServiceDate.isNotBlank() && item.nextServiceDate < todayStr
                    val rentalTx = activeRentals.find { it.itemId == item.id }

                    InventoryItemCard(
                        item = item,
                        isExpired = isExpired,
                        rentalTransaction = rentalTx,
                        primaryAccentColor = primaryAccentColor,
                        onCheckOut = { itemForCheckOut = item },
                        onCheckIn = { itemForCheckIn = item },
                        onUpdateService = { itemForServiceUpdate = item },
                        onDelete = { itemToDelete = item }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // --- Dialogs ---
    if (showAddItemDialog) {
        AddInventoryItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { type, code, brand, model, spec, serial, nextDate, notes ->
                viewModel.addInventoryItem(type, code, brand, model, spec, serial, nextDate, notes)
                showAddItemDialog = false
            }
        )
    }

    if (itemForCheckOut != null) {
        RentalCheckOutDialog(
            item = itemForCheckOut!!,
            members = members,
            onDismiss = { itemForCheckOut = null },
            onConfirm = { member, dueDate, notes ->
                viewModel.checkOutRentalItem(itemForCheckOut!!, member, dueDate, notes)
                itemForCheckOut = null
            }
        )
    }

    if (itemForCheckIn != null) {
        val rentalTx = activeRentals.find { it.itemId == itemForCheckIn!!.id }
        RentalCheckInDialog(
            item = itemForCheckIn!!,
            rentalTransaction = rentalTx,
            onDismiss = { itemForCheckIn = null },
            onConfirm = { condition, notes ->
                viewModel.checkInRentalItem(itemForCheckIn!!, rentalTx?.id, condition, notes)
                itemForCheckIn = null
            }
        )
    }

    if (itemForServiceUpdate != null) {
        UpdateServiceTuvDialog(
            item = itemForServiceUpdate!!,
            onDismiss = { itemForServiceUpdate = null },
            onConfirm = { nextDate, lastDate, notes ->
                viewModel.updateInventoryServiceTuv(itemForServiceUpdate!!.id, nextDate, lastDate, notes)
                itemForServiceUpdate = null
            }
        )
    }

    if (compressorForTelemetry != null) {
        CompressorTelemetryDialog(
            compressor = compressorForTelemetry!!,
            onDismiss = { compressorForTelemetry = null },
            onConfirm = { addedHours, resetFilter, airDate ->
                viewModel.updateCompressorTelemetry(compressorForTelemetry!!.id, addedHours, resetFilter, airDate)
                compressorForTelemetry = null
            }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Equipment entfernen") },
            text = { Text("Möchtest du '${itemToDelete!!.internalCode} - ${itemToDelete!!.brand} ${itemToDelete!!.model}' wirklich aus dem Inventar löschen?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInventoryItem(itemToDelete!!.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53E3E))
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Abbrechen") }
            }
        )
    }
}

/**
 * Compressor Station Card:
 * Displays operating hours, filter cartridge lifetime gauge, and DIN EN 12021 air analysis status.
 */
@Composable
fun CompressorStationCard(
    compressor: InventoryItemEntity,
    onLogHours: () -> Unit,
    onResetFilter: () -> Unit
) {
    val filterRatio = if (compressor.compressorFilterMaxHours > 0) {
        (compressor.compressorFilterCurrentHours / compressor.compressorFilterMaxHours).coerceIn(0f, 1f)
    } else 0f

    val filterColor = when {
        compressor.compressorFilterCurrentHours >= compressor.compressorFilterMaxHours -> Color(0xFFE53E3E)
        filterRatio >= 0.8f -> Color(0xFFD69E2E)
        else -> SeaGreen
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.5f)),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = OceanPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "⚙️", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${compressor.brand} ${compressor.model}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kompressoranlage • ${compressor.compressorHours} Gesamt-Betriebsstunden",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = SeaGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "DIN EN 12021: OK",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeaGreen
                    )
                }
            }

            // Filter Lifetime Gauge
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Filterpatronen-Laufzeit: ${compressor.compressorFilterCurrentHours} / ${compressor.compressorFilterMaxHours} Std.",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = filterColor
                    )
                    Text(
                        text = if (filterRatio >= 1f) "TAUSCH DRINGEND!" else "${((1f - filterRatio) * compressor.compressorFilterMaxHours).toInt()}h verbleibend",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = filterColor
                    )
                }
                LinearProgressIndicator(
                    progress = { filterRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = filterColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Compressor Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onResetFilter,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = "🔄 Filter gewechselt", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onLogHours,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(text = "⏱️ + Stunden loggen", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Individual Equipment Item Card:
 * Displays TÜV/Service status, rental assignment, and management actions.
 */
@Composable
fun InventoryItemCard(
    item: InventoryItemEntity,
    isExpired: Boolean,
    rentalTransaction: RentalTransactionEntity?,
    primaryAccentColor: Color,
    onCheckOut: () -> Unit,
    onCheckIn: () -> Unit,
    onUpdateService: () -> Unit,
    onDelete: () -> Unit
) {
    val typeIcon = when (item.itemType) {
        "TANK" -> "🫧"
        "REGULATOR" -> "🤿"
        "BCD" -> "🦺"
        "SUIT" -> "🥋"
        "COMPUTER" -> "⏱️"
        "O2_KIT" -> "🫁"
        "COMPRESSOR" -> "⚙️"
        else -> "🔧"
    }

    val isRented = item.isRented
    val isLocked = isExpired || item.isLocked

    val statusBadgeText = when {
        isRented -> "📦 Verliehen an ${item.currentRenterName ?: "Taucher"}"
        isLocked -> "⛔ VERLEIH GESPERRT (TÜV/Wartung fällig)"
        item.status == "MAINTENANCE" -> "🟡 In Revision"
        else -> "🟢 Einsatzbereit"
    }

    val statusBadgeColor = when {
        isLocked -> Color(0xFFC53030)
        isRented -> OceanPrimary
        item.status == "MAINTENANCE" -> Color(0xFFB7791F)
        else -> SeaGreen
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (isLocked) 1.5.dp else 1.dp, if (isLocked) Color(0xFFE53E3E).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Icon, Code, Brand/Model, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = primaryAccentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = typeIcon, fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.internalCode,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.brand} ${item.model}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (item.sizeOrSpec.isNotBlank()) {
                        Text(
                            text = item.sizeOrSpec,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                }
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusBadgeColor.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, statusBadgeColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = statusBadgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusBadgeColor
                    )
                    if (isRented && item.currentRentalDueDate != null) {
                        Text(
                            text = "Fällig: ${item.currentRentalDueDate}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = statusBadgeColor
                        )
                    }
                }
            }

            // TÜV / Revision Date Details
            if (item.nextServiceDate.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TÜV / Revision fällig:",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.nextServiceDate,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpired) Color(0xFFC53030) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (item.serviceNotes.isNotBlank()) {
                Text(
                    text = "Prüfbericht: ${item.serviceNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Management Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onUpdateService,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "TÜV / Service", fontSize = 10.sp)
                }

                if (isRented) {
                    Button(
                        onClick = onCheckIn,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(text = "📥 Rückgabe", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onCheckOut,
                        enabled = !isLocked,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) Color.Gray else OceanPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isLocked) "⛔ Gesperrt" else "📤 Verleihen",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add New Inventory Item Dialog:
 */
@Composable
fun AddInventoryItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        itemType: String,
        internalCode: String,
        brand: String,
        model: String,
        sizeOrSpec: String,
        serialNumber: String,
        nextServiceDate: String,
        serviceNotes: String
    ) -> Unit
) {
    var itemType by remember { mutableStateOf("TANK") }
    var internalCode by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var sizeOrSpec by remember { mutableStateOf("") }
    var serialNumber by remember { mutableStateOf("") }
    var nextServiceDate by remember { mutableStateOf("2028-06-30") }
    var serviceNotes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val typeOptions = listOf(
        "TANK" to "🫧 Flasche",
        "REGULATOR" to "🤿 Regler",
        "BCD" to "🦺 Jacket",
        "SUIT" to "🥋 Anzug",
        "COMPUTER" to "⏱️ Computer",
        "O2_KIT" to "🫁 O2-Koffer",
        "COMPRESSOR" to "⚙️ Kompressor"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("+ Neues Equipment anlegen") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Kategorie:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    typeOptions.forEach { (typeKey, typeLabel) ->
                        FilterChip(
                            selected = itemType == typeKey,
                            onClick = { itemType = typeKey },
                            label = { Text(typeLabel, fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = internalCode,
                    onValueChange = { internalCode = it },
                    label = { Text("Inventarnummer / Kennung *") },
                    placeholder = { Text("z.B. T-12-05 oder REG-APX-03") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Hersteller *") },
                        placeholder = { Text("z.B. Apeks, Faber") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it },
                        label = { Text("Modell *") },
                        placeholder = { Text("z.B. XTX 50") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = sizeOrSpec,
                    onValueChange = { sizeOrSpec = it },
                    label = { Text("Größe / Spezifikation") },
                    placeholder = { Text("z.B. 12L Stahl lang, 232 bar oder Größe L") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = serialNumber,
                    onValueChange = { serialNumber = it },
                    label = { Text("Seriennummer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextServiceDate,
                    onValueChange = { nextServiceDate = it },
                    label = { Text("Nächster TÜV / Revision (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = serviceNotes,
                    onValueChange = { serviceNotes = it },
                    label = { Text("Prüfbericht / Notizen") },
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
                    if (internalCode.isBlank() || brand.isBlank() || model.isBlank()) {
                        errorMessage = "Bitte Kennung, Hersteller und Modell ausfüllen."
                        return@Button
                    }
                    onConfirm(itemType, internalCode, brand, model, sizeOrSpec, serialNumber, nextServiceDate, serviceNotes)
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

/**
 * Rental Check-Out Dialog:
 */
@Composable
fun RentalCheckOutDialog(
    item: InventoryItemEntity,
    members: List<OrganizationMemberEntity>,
    onDismiss: () -> Unit,
    onConfirm: (member: OrganizationMemberEntity, dueDate: String, notes: String) -> Unit
) {
    if (members.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Keine Personen vorhanden") },
            text = { Text("Bitte lege zuerst Mitglieder oder Kunden im Roster an.") },
            confirmButton = { Button(onClick = onDismiss) { Text("OK") } }
        )
        return
    }

    var selectedMember by remember { mutableStateOf(members.first()) }
    var dueDate by remember { mutableStateOf(LocalDate.now().plusWeeks(2).toString()) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📤 Equipment ausgeben (Verleih)") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Gegenstand: ${item.internalCode} - ${item.brand} ${item.model} (${item.sizeOrSpec})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )

                Text("Ausleiher auswählen:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
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
                                    Text(text = m.fullName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                    Text(text = "${m.membershipNumber} • ${m.certLevel}", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Rückgabe fällig am (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Verleih-Bemerkungen") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMember, dueDate, notes) }) {
                Text("Ausgeben")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

/**
 * Rental Check-In / Return Dialog with Damage Report:
 */
@Composable
fun RentalCheckInDialog(
    item: InventoryItemEntity,
    rentalTransaction: RentalTransactionEntity?,
    onDismiss: () -> Unit,
    onConfirm: (condition: String, damageNotes: String) -> Unit
) {
    var condition by remember { mutableStateOf("INTACT") }
    var damageNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📥 Equipment-Rückgabe erfassen") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Rückgabe für: ${item.internalCode} (${item.brand} ${item.model})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                if (rentalTransaction != null) {
                    Text(
                        text = "Ausgeliehen an: ${rentalTransaction.renterName} seit ${rentalTransaction.checkOutDate}",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text("Zustand bei Rückgabe:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = condition == "INTACT",
                        onClick = { condition = "INTACT" },
                        label = { Text("🟢 Einwandfrei", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = condition == "DAMAGED",
                        onClick = { condition = "DAMAGED" },
                        label = { Text("🔴 Mangel / Defekt", fontSize = 11.sp) }
                    )
                }

                if (condition == "DAMAGED") {
                    OutlinedTextField(
                        value = damageNotes,
                        onValueChange = { damageNotes = it },
                        label = { Text("Schadensbericht / Mängelbeschreibung *") },
                        placeholder = { Text("z.B. Mundstück gerissen, Flossenband defekt") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(condition, damageNotes) }) {
                Text("Rücknahme bestätigen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

/**
 * Update TÜV / Revision Dialog:
 */
@Composable
fun UpdateServiceTuvDialog(
    item: InventoryItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (nextDate: String, lastDate: String, notes: String) -> Unit
) {
    var nextDate by remember { mutableStateOf(LocalDate.now().plusYears(2).toString()) }
    var lastDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var notes by remember { mutableStateOf(item.serviceNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🛠️ TÜV / Revision aktualisieren") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Prüffrist für ${item.internalCode} (${item.brand} ${item.model}):",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = nextDate,
                    onValueChange = { nextDate = it },
                    label = { Text("Neues Ablaufdatum (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastDate,
                    onValueChange = { lastDate = it },
                    label = { Text("Prüfdatum (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Prüfprotokoll / Prüfstelle") },
                    placeholder = { Text("z.B. TÜV Süd Prüfprotokoll #9941") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(nextDate, lastDate, notes) }) {
                Text("Aktualisieren (Freigeben)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

/**
 * Compressor Operating Hours & Filter Dialog:
 */
@Composable
fun CompressorTelemetryDialog(
    compressor: InventoryItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (addedHours: Float, resetFilter: Boolean, airDate: String) -> Unit
) {
    var addedHoursText by remember { mutableStateOf("2.5") }
    var resetFilter by remember { mutableStateOf(false) }
    var airDate by remember { mutableStateOf(compressor.airAnalysisDate.ifBlank { LocalDate.now().toString() }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("⏱️ Kompressor-Stunden loggen") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Kompressor: ${compressor.brand} ${compressor.model}\nAktuell: ${compressor.compressorHours} Std. (Filter: ${compressor.compressorFilterCurrentHours}/${compressor.compressorFilterMaxHours} Std.)",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = addedHoursText,
                    onValueChange = { addedHoursText = it },
                    label = { Text("Hinzugefügte Betriebsstunden (h)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { resetFilter = !resetFilter }
                ) {
                    Checkbox(checked = resetFilter, onCheckedChange = { resetFilter = it })
                    Text(text = "Filterpatrone gewechselt (Stunden auf 0)", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = airDate,
                    onValueChange = { airDate = it },
                    label = { Text("Datum Luftanalyse DIN EN 12021") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val addH = addedHoursText.toFloatOrNull() ?: 0f
                    onConfirm(addH, resetFilter, airDate)
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
