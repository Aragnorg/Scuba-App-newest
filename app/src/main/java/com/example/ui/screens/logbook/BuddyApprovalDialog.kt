package com.example.ui.screens.logbook

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DiveLogEntity
import com.example.ui.components.InteractiveSignaturePad
import com.example.ui.components.SignaturePoint
import com.example.ui.components.serializeSignaturePoints
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen

val sampleBuddies = listOf(
    Pair("Adrian Scheid (Instructor)", "adrian.scheid01@gmail.com"),
    Pair("Alex Rivera (Divemaster)", "alex.rivera@divebuddy.org"),
    Pair("Elena Rostova (Master Diver)", "elena.rostova@diver.com"),
    Pair("Sarah Connor (Rescue Diver)", "sarah.c@ocean.net")
)

/**
 * Buddy Approval Dialog featuring three robust verification mechanisms:
 * 1. Email Approval Dispatch (Official email to adrian.scheid01@gmail.com)
 * 2. Finger Signature Pad (Sign with hands on screen)
 * 3. Send Push Notification message to Buddy for in-app approval
 */
@Composable
fun BuddyApprovalDialog(
    dive: DiveLogEntity,
    onSignApprove: (diveId: String, buddyName: String, signatureJson: String, note: String) -> Unit,
    onSendPush: (diveId: String, buddyEmail: String, buddyName: String) -> Unit,
    onSendEmail: (diveId: String, recipientEmail: String, recipientName: String, note: String) -> Unit = { _, _, _, _ -> },
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    var selectedTab by remember { mutableIntStateOf(0) } // 0: E-Mail (Adrian Scheid), 1: Hand Signature, 2: Push

    // Email Approval State
    var emailRecipientName by remember { mutableStateOf("Adrian Scheid (Instructor)") }
    var emailRecipientEmail by remember { mutableStateOf("adrian.scheid01@gmail.com") }
    var emailNoteInput by remember(isGerman) {
        mutableStateOf(
            if (isGerman) "Bitte um offizielle Bestätigung meines Tauchgangs #${dive.diveNumber} (${dive.siteName}) für mein Ausbildungslogbuch."
            else "Requesting official verification for dive #${dive.diveNumber} (${dive.siteName}) for my training logbook."
        )
    }
    var emailSentSuccess by remember { mutableStateOf(false) }

    // Hand Signature State
    val signaturePoints = remember { mutableStateListOf<SignaturePoint>() }
    var buddyNameInput by remember { mutableStateOf(dive.buddyName.ifBlank { "Alex Rivera" }) }
    var buddyNoteInput by remember(isGerman) {
        mutableStateOf(
            if (isGerman) "Bestätigter Buddy-Tauchgang. Hervorragende Tarierung, Trimm und Gasmanagement!"
            else "Verified buddy dive. Excellent buoyancy, trim, and gas management!"
        )
    }

    // Push Message State
    var pushBuddyName by remember { mutableStateOf(dive.buddyName.ifBlank { "Elena Rostova" }) }
    var pushBuddyEmail by remember { mutableStateOf(dive.buddyEmail.ifBlank { "elena.rostova@diver.com" }) }
    var pushSentSuccess by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("buddy_approval_dialog"),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.buddyVerificationTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${strings.diveNumberPrefix}${dive.diveNumber}: ${dive.siteName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_buddy_dialog_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Tab Bar
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (strings.tabEmailApproval.isNotBlank()) strings.tabEmailApproval else "E-Mail",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_email_approval")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.tabHandSignature, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_hand_signature")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.tabPushNotification, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_push_to_buddy")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // TAB 0: EMAIL APPROVAL TO ADRIAN SCHEID / BUDDY
                        Text(
                            text = strings.emailApprovalTitle.ifBlank { "E-Mail-Bestätigung anfordern" },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.emailApprovalPrompt.ifBlank {
                                "Senden Sie die vollständigen Tauchgangsdaten direkt an Adrian Scheid (adrian.scheid01@gmail.com) zur offiziellen Abzeichnung."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick selection chips
                        Text(
                            text = if (isGerman) "Empfänger schnell auswählen:" else "Quick Select Recipient:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            sampleBuddies.forEach { (name, email) ->
                                val isSelected = emailRecipientEmail == email
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            emailRecipientName = name
                                            emailRecipientEmail = email
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = emailRecipientName,
                            onValueChange = { emailRecipientName = it },
                            label = { Text(if (isGerman) "Name des Empfängers / Tauchlehrers" else "Recipient / Instructor Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_recipient_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = emailRecipientEmail,
                            onValueChange = { emailRecipientEmail = it },
                            label = { Text(if (isGerman) "E-Mail-Adresse für Bestätigung" else "Verification Email Address") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_recipient_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = emailNoteInput,
                            onValueChange = { emailNoteInput = it },
                            label = { Text(if (isGerman) "Persönliche Nachricht (optional)" else "Personal Note (optional)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_note_input"),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Telemetry Summary Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isGerman) "📋 Enthaltene Telemetriedaten:" else "📋 Included Telemetry:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• ${strings.diveNumberPrefix}${dive.diveNumber}: ${dive.siteName} (${dive.location})\n" +
                                            "• Tiefe: ${dive.maxDepthMeters} m | Zeit: ${dive.durationMinutes} min | Gas: ${dive.gasType}\n" +
                                            "• Flaschendruck: ${dive.pressureInBar} bar -> ${dive.pressureOutBar} bar (Δ ${dive.pressureInBar - dive.pressureOutBar} bar)\n" +
                                            "• Sicherheitsstopp: ${if (dive.safetyStopCompleted) "Absolviert (3 min @ 5m) ✓" else "Nicht geloggt"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onSendEmail(dive.id, emailRecipientEmail, emailRecipientName, emailNoteInput)
                                emailSentSuccess = true
                            },
                            enabled = emailRecipientEmail.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("send_email_approval_button")
                        ) {
                            Icon(Icons.Rounded.MarkEmailRead, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.sendEmailApprovalButton.ifBlank { "Bestätigungs-E-Mail senden" },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (emailSentSuccess) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SeaGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = SeaGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${strings.emailSentSuccess.ifBlank { "E-Mail versendet an:" }} $emailRecipientEmail",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SeaGreen
                                        )
                                        Text(
                                            text = if (isGerman) "In-App Freigabebanner für Adrian Scheid wurde zusätzlich aktiviert."
                                            else "In-app approval banner for Adrian Scheid was also activated.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: HAND SIGNATURE
                        Text(
                            text = strings.signaturePadTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.drawSignaturePrompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        InteractiveSignaturePad(
                            points = signaturePoints,
                            onAddPoint = { signaturePoints.add(it) },
                            onEndStroke = {
                                if (signaturePoints.isNotEmpty() && !signaturePoints.last().isBreak) {
                                    signaturePoints.add(SignaturePoint(0f, 0f, isBreak = true))
                                }
                            },
                            height = 150.dp,
                            modifier = Modifier.testTag("interactive_signature_pad")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (signaturePoints.isEmpty()) "" else "✓",
                                style = MaterialTheme.typography.labelSmall,
                                color = SeaGreen,
                                fontWeight = FontWeight.Bold
                            )

                            OutlinedButton(
                                onClick = { signaturePoints.clear() },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .heightIn(min = 36.dp)
                                    .testTag("clear_signature_button")
                            ) {
                                Icon(Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.clearSignature, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = buddyNameInput,
                            onValueChange = { buddyNameInput = it },
                            label = { Text(strings.buddyNameLabel) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("buddy_signature_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = buddyNoteInput,
                            onValueChange = { buddyNoteInput = it },
                            label = { Text(strings.buddyVerificationNoteLabel) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("buddy_signature_note_input"),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val json = serializeSignaturePoints(signaturePoints)
                                onSignApprove(dive.id, buddyNameInput, json, buddyNoteInput)
                            },
                            enabled = buddyNameInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("save_buddy_signature_button")
                        ) {
                            Icon(Icons.Rounded.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.signAndVerifyButton,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    2 -> {
                        // TAB 2: SEND PUSH NOTIFICATION TO BUDDY
                        Text(
                            text = strings.sendPushTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.sendPushPrompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pushBuddyName,
                            onValueChange = { pushBuddyName = it },
                            label = { Text(strings.buddyNameLabel) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("push_buddy_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = pushBuddyEmail,
                            onValueChange = { pushBuddyEmail = it },
                            label = { Text(strings.buddyEmailLabel) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("push_buddy_email_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onSendPush(dive.id, pushBuddyEmail, pushBuddyName)
                                pushSentSuccess = true
                            },
                            enabled = pushBuddyEmail.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("send_buddy_push_request_button")
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.sendPushRequestButton,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (pushSentSuccess) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ ${strings.pushRequestSentSuccess}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SeaGreen,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}
