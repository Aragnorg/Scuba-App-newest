package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DiverDocumentEntity
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.util.DocumentExpirationInfo
import com.example.util.ExpirationAlertLevel

/**
 * Color-coded Expiration Badge for Document Vault Cards and Details.
 */
@Composable
fun DocumentExpirationBadge(
    expirationInfo: DocumentExpirationInfo,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, icon) = when (expirationInfo.level) {
        ExpirationAlertLevel.EXPIRED -> Tuple4(
            Color(0xFFE53E3E).copy(alpha = 0.15f),
            Color(0xFFE53E3E),
            Color(0xFFE53E3E),
            Icons.Rounded.Error
        )
        ExpirationAlertLevel.EXPIRING_SOON -> Tuple4(
            SandGold.copy(alpha = 0.18f),
            SandGold,
            Color(0xFFB7791F),
            Icons.Rounded.Warning
        )
        ExpirationAlertLevel.VALID -> Tuple4(
            SeaGreen.copy(alpha = 0.12f),
            SeaGreen.copy(alpha = 0.5f),
            SeaGreen,
            Icons.Rounded.CheckCircle
        )
        ExpirationAlertLevel.LIFETIME -> Tuple4(
            AquaCyan.copy(alpha = 0.12f),
            AquaCyan.copy(alpha = 0.4f),
            AquaCyan,
            Icons.Rounded.Verified
        )
    }

    Surface(
        modifier = modifier.testTag("doc_expiration_badge_${expirationInfo.level.name}"),
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = expirationInfo.badgeLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

/**
 * High-visibility Expiration Alert Banner shown at the top of Documents Vault Hub & Home Dashboard.
 */
@Composable
fun DocumentExpirationAlertBanner(
    alertDocuments: List<Pair<DiverDocumentEntity, DocumentExpirationInfo>>,
    onDocumentClick: (DiverDocumentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (alertDocuments.isEmpty()) return

    val strings = LocalAppStrings.current
    val hasExpired = alertDocuments.any { it.second.level == ExpirationAlertLevel.EXPIRED }
    var isExpanded by remember { mutableStateOf(true) }

    val bannerBg = if (hasExpired) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF741212),
                Color(0xFF4A0E17)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF7A4B05),
                Color(0xFF422800)
            )
        )
    }

    val borderColor = if (hasExpired) Color(0xFFFC8181) else Color(0xFFF6E05E)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("document_expiration_alert_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.8f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bannerBg)
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (hasExpired) Color(0xFFE53E3E) else SandGold,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasExpired) Icons.Rounded.Error else Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (hasExpired) {
                                    "${strings.expiredDocsAlertHeader} (${alertDocuments.size})"
                                } else {
                                    "${strings.expiringSoonAlertHeader} (${alertDocuments.size})"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (hasExpired) {
                                    strings.expiredDocsBannerDesc
                                } else {
                                    strings.expiringSoonBannerDesc
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    ) {
                        Text(
                            text = if (isExpanded) "Hide" else "Show (${alertDocuments.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        alertDocuments.forEach { (doc, info) ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black.copy(alpha = 0.35f),
                                border = BorderStroke(
                                    1.dp,
                                    if (info.level == ExpirationAlertLevel.EXPIRED) Color(0xFFE53E3E).copy(alpha = 0.6f)
                                    else SandGold.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDocumentClick(doc) }
                                    .testTag("alert_item_${doc.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${doc.issuingOrganization} • ${doc.expiryDate}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    DocumentExpirationBadge(expirationInfo = info)

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = "Inspect",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detailed Expiration Analysis Card for DocumentDetailDialog.
 */
@Composable
fun DocumentExpirationDetailCard(
    expirationInfo: DocumentExpirationInfo,
    document: DiverDocumentEntity,
    onRenewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val (cardBg, borderColor, titleColor) = when (expirationInfo.level) {
        ExpirationAlertLevel.EXPIRED -> Tuple3(
            Color(0xFFE53E3E).copy(alpha = 0.1f),
            Color(0xFFE53E3E),
            Color(0xFFE53E3E)
        )
        ExpirationAlertLevel.EXPIRING_SOON -> Tuple3(
            SandGold.copy(alpha = 0.12f),
            SandGold,
            Color(0xFFD69E2E)
        )
        ExpirationAlertLevel.VALID -> Tuple3(
            SeaGreen.copy(alpha = 0.08f),
            SeaGreen.copy(alpha = 0.3f),
            SeaGreen
        )
        ExpirationAlertLevel.LIFETIME -> Tuple3(
            AquaCyan.copy(alpha = 0.08f),
            AquaCyan.copy(alpha = 0.3f),
            AquaCyan
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("doc_detail_expiration_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (expirationInfo.level) {
                            ExpirationAlertLevel.EXPIRED -> Icons.Rounded.Error
                            ExpirationAlertLevel.EXPIRING_SOON -> Icons.Rounded.HourglassTop
                            ExpirationAlertLevel.VALID -> Icons.Rounded.CheckCircle
                            ExpirationAlertLevel.LIFETIME -> Icons.Rounded.Verified
                        },
                        contentDescription = null,
                        tint = titleColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.expiryDateLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = titleColor
                    )
                }

                DocumentExpirationBadge(expirationInfo = expirationInfo)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = expirationInfo.detailStatusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (expirationInfo.level == ExpirationAlertLevel.EXPIRED || expirationInfo.level == ExpirationAlertLevel.EXPIRING_SOON) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, borderColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = titleColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.diveCenterWarningExpired,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = onRenewClick,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (expirationInfo.level == ExpirationAlertLevel.EXPIRED) Color(0xFFE53E3E) else SandGold
                        ),
                        modifier = Modifier.testTag("renew_document_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.renewNowAction,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (expirationInfo.level == ExpirationAlertLevel.EXPIRED) Color.White else DepthNavy
                        )
                    }
                }
            }
        }
    }
}

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Tuple3<A, B, C>(val first: A, val second: B, val third: C)
