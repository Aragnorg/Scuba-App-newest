@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DirectionsBoat
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiveEventEntity
import com.example.data.model.UserAccountEntity
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.SeaGreen

/**
 * Clean, modern dialog allowing a registered diver to sign up for an upcoming excursion/trip.
 * Collects tank size, gas mix, equipment rental preferences, and carpool notes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiverEventRegistrationDialog(
    event: DiveEventEntity,
    currentUser: UserAccountEntity?,
    onDismiss: () -> Unit,
    onConfirmRegistration: (
        tankSelection: String,
        gasType: String,
        rentalGear: List<String>,
        carpoolNotes: String
    ) -> Unit
) {
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    val strings = LocalAppStrings.current

    var selectedTank by remember { mutableStateOf(if (isGerman) "12L Aluminium" else "12L Aluminum") }
    var selectedGas by remember { mutableStateOf(if (isGerman) "Pressluft (21% O2)" else "Air (21% O2)") }
    val selectedRentalItems = remember { mutableStateListOf<String>() }
    var carpoolNotes by remember { mutableStateOf("") }
    var safetyConfirmed by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tankOptions = if (isGerman) {
        listOf("12L Aluminium", "12L Stahl", "15L Stahl", "Doppel-12L Stahl")
    } else {
        listOf("12L Aluminum", "12L Steel", "15L Steel", "Twin-12L Steel")
    }

    val gasOptions = if (isGerman) {
        listOf("Pressluft (21% O2)", "Nitrox 32% (EAN32)", "Nitrox 36% (EAN36)")
    } else {
        listOf("Air (21% O2)", "Nitrox 32% (EAN32)", "Nitrox 36% (EAN36)")
    }

    val rentalGearOptions = if (isGerman) {
        listOf(
            "Tarierweste (BCD)",
            "Kaltwasser-Reglerset (DIN)",
            "7mm Tauchanzug",
            "Flossen & Füßlinge",
            "Tauchcomputer",
            "Blei & Bleigurt"
        )
    } else {
        listOf(
            "BCD / Wing",
            "Cold Water Regulator (DIN)",
            "7mm Wetsuit",
            "Fins & Booties",
            "Dive Computer",
            "Weights & Belt"
        )
    }

    val remainingSpots = (event.maxCapacity - event.registeredCount).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsBoat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isGerman) "Zur Ausfahrt anmelden" else "Register for Excursion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isGerman) "${event.title} • $remainingSpots freie Plätze" else "${event.title} • $remainingSpots spots left",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Event Quick Summary Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${event.siteName} (${event.location})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📅 ${event.eventDate} • ⏰ ${event.meetingTime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Max. ${event.maxDepthMeters.toInt()}m",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // Diver Information Preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (isGerman) "Angemeldeter Taucher" else "Registered Diver",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentUser?.fullName ?: (if (isGerman) "Aktiver Taucher" else "Active Diver"),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // 1. Tank Configuration
                Text(
                    text = if (isGerman) "1. Flaschen-Konfiguration" else "1. Tank Configuration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tankOptions.forEach { tank ->
                        FilterChip(
                            selected = selectedTank == tank,
                            onClick = { selectedTank = tank },
                            shape = RoundedCornerShape(10.dp),
                            label = { Text(tank, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedTank == tank,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // 2. Gas Selection
                Text(
                    text = if (isGerman) "2. Atemgas" else "2. Breathing Gas",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gasOptions.forEach { gas ->
                        FilterChip(
                            selected = selectedGas == gas,
                            onClick = { selectedGas = gas },
                            shape = RoundedCornerShape(10.dp),
                            label = { Text(gas, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedGas == gas,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // 3. Rental Gear (Optional)
                Text(
                    text = if (isGerman) "3. Leih-Ausrüstung der Basis" else "3. Rental Equipment",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rentalGearOptions.forEach { gear ->
                        val isSelected = selectedRentalItems.contains(gear)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedRentalItems.remove(gear)
                                else selectedRentalItems.add(gear)
                            },
                            shape = RoundedCornerShape(10.dp),
                            label = { Text(gear, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                // 4. Carpool & Notes
                OutlinedTextField(
                    value = carpoolNotes,
                    onValueChange = { carpoolNotes = it },
                    label = { Text(if (isGerman) "Mitfahrgelegenheit & Anmerkungen" else "Carpool & Notes") },
                    placeholder = { Text(if (isGerman) "z. B. Fahre ab Konstanz oder brauche Abholung" else "e.g. Driving from town or need a ride") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 2
                )

                // 5. Safety Declaration Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { safetyConfirmed = !safetyConfirmed }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = safetyConfirmed,
                        onCheckedChange = { safetyConfirmed = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGerman)
                            "Ich bestätige eine gültige TTU & entsprechende Taucherfahrung für diesen Spot."
                        else
                            "I confirm valid medical clearance (TTU) & required dive experience for this site.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!safetyConfirmed) {
                        errorMessage = if (isGerman)
                            "Bitte die Sicherheits- und Tauchtauglichkeitsbestätigung anhaken."
                        else
                            "Please accept the medical clearance confirmation."
                        return@Button
                    }
                    onConfirmRegistration(
                        selectedTank,
                        selectedGas,
                        selectedRentalItems.toList(),
                        carpoolNotes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 9.dp)
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isGerman) "Verbindlich anmelden" else "Confirm Booking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
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
