@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ui.screens.diveops

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Security
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
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
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
    var selectedTank by remember { mutableStateOf("12L Aluminium") }
    var selectedGas by remember { mutableStateOf("Pressluft (21% O2)") }
    val selectedRentalItems = remember { mutableStateListOf<String>() }
    var carpoolNotes by remember { mutableStateOf("") }
    var safetyConfirmed by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tankOptions = listOf("12L Aluminium", "12L Stahl", "15L Stahl", "Doppel-12L Stahl")
    val gasOptions = listOf("Pressluft (21% O2)", "Nitrox 32% (EAN32)", "Nitrox 36% (EAN36)")
    val rentalGearOptions = listOf(
        "Tarierweste (BCD)",
        "Kaltwasser-Reglerset (DIN)",
        "7mm Tauchanzug",
        "Flossen & Füßlinge",
        "Tauchcomputer",
        "Blei & Bleigurt"
    )

    val remainingSpots = (event.maxCapacity - event.registeredCount).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = null,
                    tint = OceanPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Zur Ausfahrt anmelden",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${event.title} • $remainingSpots freie Plätze",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Event Quick Summary Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${event.siteName} (${event.location})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📅 ${event.eventDate} • ⏰ ${event.meetingTime} Uhr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = OceanPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Max. ${event.maxDepthMeters.toInt()}m",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanPrimary
                                )
                            }
                        }
                    }
                }

                // Diver Information Preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Angemeldeter Taucher", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currentUser?.fullName ?: "Aktiver Taucher",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // 1. Tank Configuration
                Text(
                    text = "1. Flaschen-Konfiguration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tankOptions.forEach { tank ->
                        FilterChip(
                            selected = selectedTank == tank,
                            onClick = { selectedTank = tank },
                            label = { Text(tank, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OceanPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = OceanPrimary
                            )
                        )
                    }
                }

                // 2. Gas Selection
                Text(
                    text = "2. Atemgas",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    gasOptions.forEach { gas ->
                        FilterChip(
                            selected = selectedGas == gas,
                            onClick = { selectedGas = gas },
                            label = { Text(gas, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (gas.contains("Nitrox")) AquaCyan.copy(alpha = 0.2f) else OceanPrimary.copy(alpha = 0.15f),
                                selectedLabelColor = if (gas.contains("Nitrox")) Color(0xFF007799) else OceanPrimary
                            )
                        )
                    }
                }

                HorizontalDivider()

                // 3. Rental Gear (Optional)
                Text(
                    text = "3. Leih-Ausrüstung der Basis",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rentalGearOptions.forEach { gear ->
                        val isSelected = selectedRentalItems.contains(gear)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedRentalItems.remove(gear)
                                else selectedRentalItems.add(gear)
                            },
                            label = { Text(gear, fontSize = 11.sp) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }

                // 4. Carpool & Notes
                OutlinedTextField(
                    value = carpoolNotes,
                    onValueChange = { carpoolNotes = it },
                    label = { Text("Mitfahrgelegenheit & Anmerkungen") },
                    placeholder = { Text("z. B. Fahre ab Konstanz oder brauche Abholung") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // 5. Safety Declaration Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { safetyConfirmed = !safetyConfirmed },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = safetyConfirmed,
                        onCheckedChange = { safetyConfirmed = it },
                        colors = CheckboxDefaults.colors(checkedColor = SeaGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ich bestätige eine gültige TTU & entsprechende Taucherfahrung für diesen Spot.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFE53E3E),
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
                        errorMessage = "Bitte die Sicherheits- und Tauchtauglichkeitsbestätigung anhaken."
                        return@Button
                    }
                    onConfirmRegistration(
                        selectedTank,
                        selectedGas,
                        selectedRentalItems.toList(),
                        carpoolNotes.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Verbindlich anmelden", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
