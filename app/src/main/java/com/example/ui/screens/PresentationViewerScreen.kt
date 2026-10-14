package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalContext
import com.example.util.GoogleDriveHelper
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PresentationDeckEntity
import com.example.ui.components.AscentProfileVisual
import com.example.ui.components.BoylesLawCalculator
import com.example.ui.components.BwrafChecklistCard
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationViewerScreen(
    viewModel: AquaViewModel,
    presentation: PresentationDeckEntity,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closePresentation()
    }

    val strings = LocalAppStrings.current
    val currentSlideIndex by viewModel.currentSlideIndex.collectAsState()
    val slides = presentation.parseSlides()
    val total = slides.size.coerceAtLeast(1)
    val safeIndex = currentSlideIndex.coerceIn(0, total - 1)
    val currentSlide = slides.getOrElse(safeIndex) { slides[0] }

    val progress = (safeIndex + 1).toFloat() / total.toFloat()
    val isLastSlide = safeIndex == total - 1

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = presentation.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${strings.slidePrefix} ${safeIndex + 1} ${strings.slideOf} $total",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.closePresentation() },
                            modifier = Modifier.testTag("close_presentation_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = strings.close)
                        }
                    },
                    actions = {
                        if (presentation.isCompleted) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SeaGreen.copy(alpha = 0.2f),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = strings.completedBadge,
                                        tint = SeaGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.completedBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SeaGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = AquaCyan,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.prevSlide() },
                        enabled = safeIndex > 0,
                        modifier = Modifier.testTag("prev_slide_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.previousButton)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.previousButton)
                    }

                    // Slide indicator dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until total.coerceAtMost(8)) {
                            Box(
                                modifier = Modifier
                                    .size(if (i == safeIndex) 10.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == safeIndex) AquaCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    )
                            )
                        }
                    }

                    if (isLastSlide) {
                        Button(
                            onClick = {
                                viewModel.markPresentationComplete()
                                viewModel.closePresentation()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                            modifier = Modifier.testTag("finish_presentation_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = strings.completeDeck)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.completeDeck)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.nextSlide() },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                            modifier = Modifier.testTag("next_slide_button")
                        ) {
                            Text(strings.nextButton)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = strings.nextButton)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val context = LocalContext.current
            val docUri = presentation.documentUri
            val isDriveDoc = GoogleDriveHelper.isGoogleDriveUrl(docUri)

            if (!docUri.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDriveDoc) AquaCyan.copy(alpha = 0.15f) else OceanPrimary.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, if (isDriveDoc) AquaCyan.copy(alpha = 0.5f) else OceanPrimary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isDriveDoc) "☁️" else "📄", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isDriveDoc) "Google Drive / Slides Vorlage" else "Angehängtes Dokument / Skript",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isDriveDoc) "Original-Präsentation aus Vereins-Drive öffnen" else "Dokument auf Smartphone öffnen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                GoogleDriveHelper.openInGoogleDriveOrBrowser(context, docUri)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Öffnen", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Main Slide Content Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("slide_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = currentSlide.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (currentSlide.subtitle.isNotBlank()) {
                        Text(
                            text = currentSlide.subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Bullet points list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        currentSlide.bulletPoints.forEach { point ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(8.dp),
                                    shape = CircleShape,
                                    color = AquaCyan
                                ) {}
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Diagrams based on Slide Type
            when (currentSlide.diagramType) {
                "BOYLES_LAW" -> BoylesLawCalculator()
                "BWRAF_BUDDY" -> BwrafChecklistCard()
                "ASCENT_PROFILE" -> AscentProfileVisual()
            }

            // Key Takeaway Card (Exam highlight)
            if (currentSlide.keyTakeaway.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SandGold.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SandGold.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Key Exam Takeaway",
                            tint = SandGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.keyTakeaway,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentSlide.keyTakeaway,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Instructor Notes
            if (currentSlide.instructorNotes.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = strings.instructorTeachingNote,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = strings.instructorTeachingNote,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentSlide.instructorNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
