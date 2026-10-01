package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.i18n.LocalAppStrings
import org.json.JSONArray
import org.json.JSONObject

data class SignaturePoint(val x: Float, val y: Float, val isBreak: Boolean = false)

/**
 * Interactive touch signature pad where a diving buddy can sign directly with their finger.
 */
@Composable
fun InteractiveSignaturePad(
    points: List<SignaturePoint>,
    onAddPoint: (SignaturePoint) -> Unit,
    onEndStroke: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 160.dp,
    strokeColor: Color = Color(0xFF004F5D),
    backgroundColor: Color = Color(0xFFF0F9FF)
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.5.dp, strokeColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onAddPoint(SignaturePoint(offset.x, offset.y, isBreak = false))
                    },
                    onDragEnd = {
                        onEndStroke()
                    },
                    onDragCancel = {
                        onEndStroke()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onAddPoint(SignaturePoint(change.position.x, change.position.y, isBreak = false))
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (points.isEmpty()) {
            Text(
                text = strings.signHereWithFinger,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            if (points.size > 1) {
                val path = Path()
                var isFirst = true

                for (pt in points) {
                    if (isFirst) {
                        path.moveTo(pt.x, pt.y)
                        isFirst = false
                    } else if (pt.isBreak) {
                        isFirst = true
                    } else {
                        path.lineTo(pt.x, pt.y)
                    }
                }

                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

/**
 * Preview component for saved buddy signature on a dive log card.
 */
@Composable
fun SignaturePreview(
    signatureJson: String,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp,
    strokeColor: Color = Color(0xFF005B60)
) {
    val strings = LocalAppStrings.current
    val points = remember(signatureJson) {
        parseSignatureJson(signatureJson)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (points.isEmpty()) {
            Text(
                text = strings.signatureVerified,
                style = MaterialTheme.typography.labelSmall,
                color = strokeColor
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Determine bounding box to scale signature inside canvas
                var minX = Float.MAX_VALUE
                var maxX = Float.MIN_VALUE
                var minY = Float.MAX_VALUE
                var maxY = Float.MIN_VALUE

                points.forEach { pt ->
                    if (pt.x < minX) minX = pt.x
                    if (pt.x > maxX) maxX = pt.x
                    if (pt.y < minY) minY = pt.y
                    if (pt.y > maxY) maxY = pt.y
                }

                val srcWidth = (maxX - minX).coerceAtLeast(10f)
                val srcHeight = (maxY - minY).coerceAtLeast(10f)

                val scaleX = (size.width * 0.85f) / srcWidth
                val scaleY = (size.height * 0.85f) / srcHeight
                val scale = minOf(scaleX, scaleY, 2.5f)

                val offsetX = (size.width - srcWidth * scale) / 2f - minX * scale
                val offsetY = (size.height - srcHeight * scale) / 2f - minY * scale

                val path = Path()
                var isFirst = true

                for (pt in points) {
                    val targetX = pt.x * scale + offsetX
                    val targetY = pt.y * scale + offsetY

                    if (isFirst) {
                        path.moveTo(targetX, targetY)
                        isFirst = false
                    } else if (pt.isBreak) {
                        isFirst = true
                    } else {
                        path.lineTo(targetX, targetY)
                    }
                }

                drawPath(
                    path = path,
                    color = strokeColor,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

fun serializeSignaturePoints(points: List<SignaturePoint>): String {
    val array = JSONArray()
    for (pt in points) {
        val obj = JSONObject()
        obj.put("x", pt.x)
        obj.put("y", pt.y)
        if (pt.isBreak) obj.put("b", true)
        array.put(obj)
    }
    return array.toString()
}

fun parseSignatureJson(json: String): List<SignaturePoint> {
    val list = mutableListOf<SignaturePoint>()
    if (json.isBlank()) return list
    try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                SignaturePoint(
                    x = obj.optDouble("x", 0.0).toFloat(),
                    y = obj.optDouble("y", 0.0).toFloat(),
                    isBreak = obj.optBoolean("b", false)
                )
            )
        }
    } catch (_: Exception) {}
    return list
}
