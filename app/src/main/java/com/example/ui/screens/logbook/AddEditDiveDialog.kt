package com.example.ui.screens.logbook

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen

val defaultSightingsOptions = listOf(
    "Green Sea Turtle",
    "Whale Shark",
    "Manta Ray",
    "Bluespotted Ray",
    "Giant Moray Eel",
    "Clownfish",
    "Barracuda School",
    "Nudibranch",
    "Lionfish",
    "Coral Garden"
)

val entryTypeOptions = listOf("Boat", "Shore", "Drift", "Wreck", "Night")
val bodyOfWaterOptions = listOf("Ocean", "Sea", "Lake", "Quarry", "Cave")
val gasTypeOptions = listOf("Air 21%", "EANx 32%", "EANx 36%", "Trimix")
val tankTypeOptions = listOf("12L Aluminum", "12L Steel", "15L Steel", "10L Steel")
val suitTypeOptions = listOf("5mm Full Suit", "3mm Shorty", "7mm Semi-Dry", "Drysuit", "Rashguard")

@Composable
fun AddEditDiveDialog(
    nextDiveNumber: Int,
    onSave: (
        diveNumber: Int,
        date: String,
        timeIn: String,
        timeOut: String,
        durationMinutes: Int,
        siteName: String,
        location: String,
        bodyOfWater: String,
        entryType: String,
        maxDepthMeters: Float,
        avgDepthMeters: Float,
        waterTempCelsius: Float,
        airTempCelsius: Float,
        visibilityMeters: Float,
        gasType: String,
        tankType: String,
        pressureInBar: Int,
        pressureOutBar: Int,
        suitType: String,
        weightKg: Float,
        safetyStopCompleted: Boolean,
        sightings: String,
        notes: String,
        buddyName: String,
        buddyEmail: String,
        diveCenterName: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var diveNumber by remember { mutableIntStateOf(nextDiveNumber) }
    var date by remember { mutableStateOf("2026-09-29") }
    var timeIn by remember { mutableStateOf("10:15 AM") }
    var timeOut by remember { mutableStateOf("11:03 AM") }
    var durationMinutes by remember { mutableIntStateOf(48) }

    var siteName by remember { mutableStateOf("Gordon Reef") }
    var location by remember { mutableStateOf("Straits of Tiran, Sharm El Sheikh") }
    var bodyOfWater by remember { mutableStateOf("Ocean") }
    var entryType by remember { mutableStateOf("Boat") }

    var maxDepthMeters by remember { mutableFloatStateOf(18.5f) }
    var avgDepthMeters by remember { mutableFloatStateOf(12.3f) }
    var waterTempCelsius by remember { mutableFloatStateOf(26.0f) }
    var airTempCelsius by remember { mutableFloatStateOf(31.0f) }
    var visibilityMeters by remember { mutableFloatStateOf(25.0f) }

    var gasType by remember { mutableStateOf("Air 21%") }
    var tankType by remember { mutableStateOf("12L Aluminum") }
    var pressureInBar by remember { mutableIntStateOf(200) }
    var pressureOutBar by remember { mutableIntStateOf(50) }

    var suitType by remember { mutableStateOf("5mm Full Suit") }
    var weightKg by remember { mutableFloatStateOf(6.0f) }
    var safetyStopCompleted by remember { mutableStateOf(true) }

    val selectedSightings = remember { mutableStateListOf("Green Sea Turtle", "Clownfish") }
    var customSightings by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("Beautiful plateau dive with great drift along the reef wall. Safety stop completed at 5 meters for 3 minutes.") }

    var buddyName by remember { mutableStateOf("Alex Rivera") }
    var buddyEmail by remember { mutableStateOf("alex.rivera@divebuddy.org") }
    var diveCenterName by remember { mutableStateOf("AquaLearn PADI 5-Star Dive Academy") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_edit_dive_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(OceanPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${strings.logNewDiveTitle} #$diveNumber",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.logNewDiveSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_dive_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 1: SITE & LOCATION
                SectionHeader(title = strings.siteAndLocationSection, icon = Icons.Rounded.Place)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = siteName,
                    onValueChange = { siteName = it },
                    label = { Text(strings.diveSiteNamePlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dive_site_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(strings.locationPlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dive_location_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = strings.entryTypeLabel, style = MaterialTheme.typography.labelSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        ChipSelector(
                            options = entryTypeOptions,
                            selected = entryType,
                            onSelect = { entryType = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 2: DEPTH & TIME
                SectionHeader(title = strings.depthTimeSection, icon = Icons.Rounded.Speed)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = maxDepthMeters.toString(),
                        onValueChange = { it.toFloatOrNull()?.let { v -> maxDepthMeters = v } },
                        label = { Text(strings.maxDepthMetersLabel) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("max_depth_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = avgDepthMeters.toString(),
                        onValueChange = { it.toFloatOrNull()?.let { v -> avgDepthMeters = v } },
                        label = { Text(strings.avgDepthMetersLabel) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("avg_depth_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = durationMinutes.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> durationMinutes = v } },
                        label = { Text(strings.durationMinutesLabel) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("duration_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text(strings.dateLabel) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dive_date_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = timeIn,
                        onValueChange = { timeIn = it },
                        label = { Text(strings.timeInLabel) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = timeOut,
                        onValueChange = { timeOut = it },
                        label = { Text(strings.timeOutLabel) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 3: TEMPERATURE & CONDITIONS
                SectionHeader(title = strings.tempConditionsSection, icon = Icons.Rounded.Thermostat)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = waterTempCelsius.toString(),
                        onValueChange = { it.toFloatOrNull()?.let { v -> waterTempCelsius = v } },
                        label = { Text(strings.waterTempCelsiusLabel) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("water_temp_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = airTempCelsius.toString(),
                        onValueChange = { it.toFloatOrNull()?.let { v -> airTempCelsius = v } },
                        label = { Text(strings.airTempCelsiusLabel) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("air_temp_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = visibilityMeters.toString(),
                        onValueChange = { it.toFloatOrNull()?.let { v -> visibilityMeters = v } },
                        label = { Text(strings.visibilityMetersLabel) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 4: GAS & EQUIPMENT
                SectionHeader(title = strings.gasCylinderSection, icon = Icons.Rounded.Air)
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = strings.gasMixLabel, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                ChipSelector(options = gasTypeOptions, selected = gasType, onSelect = { gasType = it })

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pressureInBar.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> pressureInBar = v } },
                        label = { Text(strings.startPressureBarLabel) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pressure_in_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = pressureOutBar.toString(),
                        onValueChange = { it.toIntOrNull()?.let { v -> pressureOutBar = v } },
                        label = { Text(strings.endPressureBarLabel) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pressure_out_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OceanPrimary.copy(alpha = 0.1f),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(6.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = strings.usedPressure, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
                            Text(
                                text = "${(pressureInBar - pressureOutBar).coerceAtLeast(0)} bar",
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Safety Stop Checkbox
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (safetyStopCompleted) SeaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { safetyStopCompleted = !safetyStopCompleted }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = safetyStopCompleted,
                            onCheckedChange = { safetyStopCompleted = it },
                            modifier = Modifier.testTag("safety_stop_checkbox")
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.safetyStopCompletedLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (safetyStopCompleted) SeaGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 5: MARINE LIFE SIGHTINGS
                SectionHeader(title = strings.sightingsNotesSection, icon = Icons.Rounded.Water)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column {
                        defaultSightingsOptions.chunked(3).forEach { rowSightings ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowSightings.forEach { sighting ->
                                    val isSelected = selectedSightings.contains(sighting)
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) SeaGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = if (isSelected) BorderStroke(1.dp, SeaGreen) else null,
                                        modifier = Modifier
                                            .clickable {
                                                if (isSelected) selectedSightings.remove(sighting) else selectedSightings.add(sighting)
                                            }
                                            .testTag("sighting_$sighting")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isSelected) {
                                                Icon(Icons.Rounded.Check, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                            }
                                            Text(
                                                text = sighting,
                                                fontSize = 11.sp,
                                                color = if (isSelected) SeaGreen else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SECTION 6: BUDDY & DIVE CENTER INFO
                SectionHeader(title = strings.buddyCenterSection, icon = Icons.Rounded.Navigation)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = buddyName,
                    onValueChange = { buddyName = it },
                    label = { Text(strings.buddyNameLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_dive_buddy_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = buddyEmail,
                    onValueChange = { buddyEmail = it },
                    label = { Text(strings.buddyEmailLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_dive_buddy_email"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = diveCenterName,
                    onValueChange = { diveCenterName = it },
                    label = { Text(strings.diveCenterNameLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.diverNotesJournalLabel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_dive_notes_input"),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Save Dive Button
                Button(
                    onClick = {
                        val sightingsCombined = (selectedSightings + if (customSightings.isNotBlank()) listOf(customSightings) else emptyList())
                            .joinToString(", ")

                        onSave(
                            diveNumber,
                            date,
                            timeIn,
                            timeOut,
                            durationMinutes,
                            siteName.ifBlank { "Dive Site #$diveNumber" },
                            location.ifBlank { "Open Water" },
                            bodyOfWater,
                            entryType,
                            maxDepthMeters,
                            avgDepthMeters,
                            waterTempCelsius,
                            airTempCelsius,
                            visibilityMeters,
                            gasType,
                            tankType,
                            pressureInBar,
                            pressureOutBar,
                            suitType,
                            weightKg,
                            safetyStopCompleted,
                            sightingsCombined,
                            notes,
                            buddyName,
                            buddyEmail,
                            diveCenterName
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_save_dive_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${strings.saveDiveLogButton} #$diveNumber",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(26.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ChipSelector(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { opt ->
            val isSelected = opt == selected
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelect(opt) }
            ) {
                Text(
                    text = opt,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
