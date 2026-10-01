package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SeaGreen

/**
 * Modern Diver User Avatar component supporting:
 * 1. Base64 captured photo
 * 2. File / Content URI from photo picker
 * 3. Emoji avatar string (e.g. "avatar_🤿", "avatar_🐬")
 * 4. Initials fallback
 */
@Composable
fun UserAvatarView(
    photoUri: String?,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    borderWidth: Dp = 2.dp,
    borderColor: Color = AquaCyan,
    hasAlertBadge: Boolean = false,
    alertBadgeColor: Color = Color(0xFFE53E3E),
    onClick: (() -> Unit)? = null
) {
    val base64Bitmap = remember(photoUri) {
        if (photoUri != null && photoUri.startsWith("data:image")) {
            try {
                val base64Str = photoUri.substringAfter("base64,")
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val initials = remember(displayName) {
        displayName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
            .ifBlank { "D" }
    }

    Box(
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag("user_avatar_container")
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("user_avatar_surface"),
            shape = CircleShape,
            color = DepthNavy,
            border = BorderStroke(borderWidth, borderColor)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    base64Bitmap != null -> {
                        Image(
                            bitmap = base64Bitmap.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    photoUri != null && (photoUri.startsWith("content://") || photoUri.startsWith("file://") || photoUri.startsWith("http")) -> {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        // Initials with stylish scuba gradient background
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        listOf(AquaCyan.copy(alpha = 0.8f), OceanPrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = (size.value * 0.38f).sp
                            )
                        }
                    }
                }
            }
        }

        if (hasAlertBadge) {
            Surface(
                shape = CircleShape,
                color = alertBadgeColor,
                border = BorderStroke(1.5.dp, Color.White),
                modifier = Modifier
                    .size(size * 0.32f)
                    .align(Alignment.TopEnd)
                    .testTag("avatar_expiration_alert_dot")
            ) {}
        }
    }
}
