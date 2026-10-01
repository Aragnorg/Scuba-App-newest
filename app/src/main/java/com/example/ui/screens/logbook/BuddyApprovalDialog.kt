package com.example.ui.screens.logbook

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen

val sampleBuddies = listOf(
    Pair("Alex Rivera", "alex.rivera@divebuddy.org"),
    Pair("Elena Rostova", "elena.rostova@diver.com"),
    Pair("Sarah Connor", "sarah.c@ocean.net")
)

/**
 * Buddy Approval Dialog featuring both requested mechanisms:
 * 1. Finger Signature Pad (Sign with hands on screen)
 * 2. Send Push Notification message to Buddy for approval
 */
@Composable
fun BuddyApprovalDialog(
    dive: DiveLogEntity,
    onSignApprove: (diveId: String, buddyName: String, signatureJson: String, note: String) -> Unit,
    onSendPush: (diveId: String, buddyEmail: String, buddyName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Hand Signature, 1: Send Push Message

    // Hand Signature State
    val signaturePoints = remember { mutableStateListOf<SignaturePoint>() }
    var buddyNameInput by remember { mutableStateOf(dive.buddyName.ifBlank { "Alex Rivera" }) }
    var buddyNoteInput by remember { mutableStateOf("Verified buddy dive. Excellent buoyancy, trim, and gas management!") }

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
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp))
                .testTag("buddy_approval_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SeaGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = SeaGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.buddyVerificationTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${strings.diveNumberPrefix}${dive.diveNumber}: ${dive.siteName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_buddy_dialog_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = strings.close)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.tabHandSignature, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_hand_signature")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.tabPushNotification, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        modifier = Modifier.testTag("tab_push_to_buddy")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // TAB 1: HAND SIGNATURE
                    Text(
                        text = strings.signaturePadTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
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
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("clear_signature_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
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
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = buddyNoteInput,
                        onValueChange = { buddyNoteInput = it },
                        label = { Text(strings.buddyVerificationNoteLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("buddy_signature_note_input"),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val json = serializeSignaturePoints(signaturePoints)
                            onSignApprove(dive.id, buddyNameInput, json, buddyNoteInput)
                        },
                        enabled = buddyNameInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SeaGreen,
                            contentColor = Color(0xFF00382E)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_buddy_signature_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.signAndVerifyButton,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                } else {
                    // TAB 2: SEND PUSH NOTIFICATION TO BUDDY
                    Text(
                        text = strings.sendPushTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
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
                        shape = RoundedCornerShape(10.dp)
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
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSendPush(dive.id, pushBuddyEmail, pushBuddyName)
                            pushSentSuccess = true
                        },
                        enabled = pushBuddyEmail.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OceanPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("send_buddy_push_request_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.sendPushRequestButton,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
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
