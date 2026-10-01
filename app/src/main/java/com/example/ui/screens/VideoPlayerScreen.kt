package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningVideoEntity
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    viewModel: AquaViewModel,
    video: LearningVideoEntity,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeVideo()
    }

    val strings = LocalAppStrings.current
    val isPlaying by viewModel.isVideoPlaying.collectAsState()
    val currentSeconds by viewModel.videoCurrentSeconds.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()

    // Smooth playback ticker
    LaunchedEffect(isPlaying, playbackSpeed) {
        while (isPlaying) {
            val stepMs = (1000L / playbackSpeed).toLong().coerceAtLeast(200L)
            delay(stepMs)
            if (currentSeconds < video.durationSeconds) {
                viewModel.seekVideo(currentSeconds + 1)
            } else {
                viewModel.toggleVideoPlay()
            }
        }
    }

    val chapters = video.parseChapters()
    val takeaways = video.parseTakeaways()

    fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "bubbles")
    val bubbleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_anim"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = strings.videoTrainingLesson,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeVideo() },
                        modifier = Modifier.testTag("close_video_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = strings.close)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Video Canvas Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(DepthNavy, Color(0xFF03253D), Color(0xFF05445E))
                        )
                    )
                    .testTag("video_player_viewport")
            ) {
                // Ocean Water Animation
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Ascending Diver Bubbles
                    val bubbleY1 = (1f - ((bubbleOffset + 0.1f) % 1f)) * h
                    val bubbleY2 = (1f - ((bubbleOffset + 0.5f) % 1f)) * h
                    val bubbleY3 = (1f - ((bubbleOffset + 0.8f) % 1f)) * h

                    drawCircle(AquaCyan.copy(alpha = 0.5f), radius = 8f, center = Offset(w * 0.25f, bubbleY1))
                    drawCircle(Color.White.copy(alpha = 0.4f), radius = 14f, center = Offset(w * 0.7f, bubbleY2))
                    drawCircle(AquaCyan.copy(alpha = 0.6f), radius = 10f, center = Offset(w * 0.45f, bubbleY3))
                }

                // Center visual & video status
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = CircleShape,
                        color = AquaCyan.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(2.dp, AquaCyan)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🤿",
                                fontSize = 32.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = video.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPlaying) "${strings.playingLesson} (${playbackSpeed}x)" else strings.pausedLesson,
                        color = AquaCyan,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Big play/pause overlay click
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { viewModel.toggleVideoPlay() }
                )
            }

            // Player Controls Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Scrubber Slider
                    Slider(
                        value = currentSeconds.toFloat(),
                        onValueChange = { viewModel.seekVideo(it.toInt()) },
                        valueRange = 0f..video.durationSeconds.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = AquaCyan,
                            activeTrackColor = OceanPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_progress_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatTime(currentSeconds)} / ${formatTime(video.durationSeconds)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Playback speed button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .clickable { viewModel.cyclePlaybackSpeed() }
                                .testTag("speed_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Speed,
                                    contentDescription = "Playback Speed",
                                    modifier = Modifier.size(16.dp),
                                    tint = OceanPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${playbackSpeed}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Main Control Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.skipVideo(-10) },
                            modifier = Modifier.testTag("rewind_button")
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Rewind 10s")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        FilledIconButton(
                            onClick = { viewModel.toggleVideoPlay() },
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("play_pause_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = OceanPrimary)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        IconButton(
                            onClick = { viewModel.skipVideo(10) },
                            modifier = Modifier.testTag("fast_forward_button")
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Forward 10s")
                        }
                    }
                }
            }

            // Video Details & Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Description Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.lessonOverview,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = video.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interactive Chapter Markers
                if (chapters.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Bookmark,
                                    contentDescription = "Chapters",
                                    tint = OceanPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.videoChapters,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            chapters.forEach { ch ->
                                val isCurrentChapter = currentSeconds >= ch.timeSec &&
                                        (chapters.indexOf(ch) == chapters.size - 1 || currentSeconds < (chapters.getOrNull(chapters.indexOf(ch) + 1)?.timeSec ?: Int.MAX_VALUE))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isCurrentChapter) AquaCyan.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                        .clickable {
                                            viewModel.seekVideo(ch.timeSec)
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isCurrentChapter) OceanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = formatTime(ch.timeSec),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isCurrentChapter) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ch.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCurrentChapter) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (ch.summary.isNotBlank()) {
                                            Text(
                                                text = ch.summary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Key Exam Takeaways
                if (takeaways.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SandGold.copy(alpha = 0.12f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.examTakeawaysLesson,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            takeaways.forEach { takeaway ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Check",
                                        tint = SeaGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = takeaway,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Mark Completed Button
                Button(
                    onClick = {
                        viewModel.seekVideo(video.durationSeconds)
                        viewModel.closeVideo()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .testTag("mark_video_completed_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Complete")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.markVideoCompleted)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
