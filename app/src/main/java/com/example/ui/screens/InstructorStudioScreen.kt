package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Visibility
import com.example.util.GoogleDriveHelper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.CourseModuleEntity
import com.example.data.model.LearningVideoEntity
import com.example.data.model.PresentationDeckEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestion
import com.example.data.model.SlideItem
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorStudioScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Presentations, 1: Videos, 2: Quizzes
    var showCreatePresentationDialog by remember { mutableStateOf(false) }
    var showCreateVideoDialog by remember { mutableStateOf(false) }
    var showCreateQuizDialog by remember { mutableStateOf(false) }

    val modules by viewModel.allModules.collectAsState()
    val presentations by viewModel.allPresentations.collectAsState()
    val videos by viewModel.allVideos.collectAsState()
    val quizzes by viewModel.allQuizzes.collectAsState()
    val strings = LocalAppStrings.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> showCreatePresentationDialog = true
                        1 -> showCreateVideoDialog = true
                        2 -> showCreateQuizDialog = true
                    }
                },
                containerColor = OceanPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = {
                    Text(
                        when (selectedTab) {
                            0 -> strings.uploadPresentation
                            1 -> strings.uploadVideo
                            else -> strings.createQuiz
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("instructor_fab_action")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Instructor Header Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.tabDeploy,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanPrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = strings.instructorLabel,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                        }
                    }
                }
            }

            // Tab bar
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("${strings.presentationsTab} (${presentations.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Rounded.Slideshow, contentDescription = strings.presentationsTab) },
                    modifier = Modifier.testTag("tab_presentations")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("${strings.videosTab} (${videos.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Rounded.VideoLibrary, contentDescription = strings.videosTab) },
                    modifier = Modifier.testTag("tab_videos")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("${strings.quizzesTab} (${quizzes.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Rounded.Quiz, contentDescription = strings.quizzesTab) },
                    modifier = Modifier.testTag("tab_quizzes")
                )
            }

            // Content List based on selected tab
            when (selectedTab) {
                0 -> PresentationsList(
                    presentations = presentations,
                    onOpen = { viewModel.openPresentation(it) },
                    onDelete = { viewModel.deleteUploadedItem(it.id, "presentation") },
                    onToggleDeployment = { id, deployed -> viewModel.togglePresentationDeployment(id, deployed) }
                )
                1 -> VideosList(
                    videos = videos,
                    onOpen = { viewModel.openVideo(it) },
                    onDelete = { viewModel.deleteUploadedItem(it.id, "video") },
                    onToggleDeployment = { id, deployed -> viewModel.toggleVideoDeployment(id, deployed) }
                )
                2 -> QuizzesList(
                    quizzes = quizzes,
                    onOpen = { viewModel.openQuiz(it) },
                    onDelete = { viewModel.deleteUploadedItem(it.id, "quiz") },
                    onToggleDeployment = { id, deployed -> viewModel.toggleQuizDeployment(id, deployed) }
                )
            }
        }
    }

    // Dialogs for Uploading Content
    if (showCreatePresentationDialog) {
        CreatePresentationDialog(
            modules = modules,
            onDismiss = { showCreatePresentationDialog = false },
            onSave = { moduleId, title, desc, slides, uri ->
                viewModel.createPresentation(moduleId, title, desc, slides, uri)
                showCreatePresentationDialog = false
            }
        )
    }

    if (showCreateVideoDialog) {
        CreateVideoDialog(
            modules = modules,
            onDismiss = { showCreateVideoDialog = false },
            onSave = { moduleId, title, desc, duration, uriOrUrl, chapters, takeaways ->
                viewModel.uploadVideoLesson(moduleId, title, desc, duration, uriOrUrl, chapters, takeaways)
                showCreateVideoDialog = false
            }
        )
    }

    if (showCreateQuizDialog) {
        CreateQuizDialog(
            modules = modules,
            onDismiss = { showCreateQuizDialog = false },
            onSave = { moduleId, title, desc, passScore, questions, isMock ->
                viewModel.createQuiz(moduleId, title, desc, passScore, questions, isMock)
                showCreateQuizDialog = false
            }
        )
    }
}

@Composable
fun PresentationsList(
    presentations: List<PresentationDeckEntity>,
    onOpen: (PresentationDeckEntity) -> Unit,
    onDelete: (PresentationDeckEntity) -> Unit,
    onToggleDeployment: (String, Boolean) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE

    if (presentations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isGerman) "Noch keine Präsentationen vorhanden. Tippe auf +, um Folien hochzuladen oder zu erstellen!" else "No presentations yet. Tap + to upload or build a slide deck!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(presentations, key = { it.id }) { pres ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("presentation_item_${pres.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = CircleShape,
                                color = OceanPrimary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Slideshow,
                                        contentDescription = "Slides",
                                        tint = OceanPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pres.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${pres.totalSlides} slides • ${pres.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }

                            IconButton(
                                onClick = { onOpen(pres) },
                                modifier = Modifier.testTag("preview_presentation_${pres.id}")
                            ) {
                                Icon(Icons.Rounded.Visibility, contentDescription = "View Deck", tint = OceanPrimary)
                            }

                            if (pres.isInstructorUploaded) {
                                IconButton(
                                    onClick = { onDelete(pres) },
                                    modifier = Modifier.testTag("delete_presentation_${pres.id}")
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = CoralAlert)
                                }
                            }
                        }

                        // Deployment Controls & Origin Badges
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (pres.isInstructorUploaded) AquaCyan.copy(alpha = 0.18f) else OceanPrimary.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        text = if (pres.isInstructorUploaded) (if (isGerman) "👨‍🏫 Eigene Inhalte" else "👨‍🏫 Instructor Custom") else (if (isGerman) "🌐 App-Standard" else "🌐 General App Material"),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pres.isInstructorUploaded) OceanPrimary else OceanSecondary
                                    )
                                }
                                if (GoogleDriveHelper.isGoogleDriveUrl(pres.documentUri)) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AquaCyan.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "☁️ Google Drive",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OceanPrimary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (pres.isDeployed) SeaGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (pres.isDeployed) "🟢 Deployed" else "⚪ Draft (Hidden)",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pres.isDeployed) SeaGreen else Color.Gray
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (pres.isDeployed) "Deployed" else "Deploy",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = if (pres.isDeployed) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = pres.isDeployed,
                                    onCheckedChange = { onToggleDeployment(pres.id, it) },
                                    modifier = Modifier.testTag("deploy_switch_pres_${pres.id}")
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun VideosList(
    videos: List<LearningVideoEntity>,
    onOpen: (LearningVideoEntity) -> Unit,
    onDelete: (LearningVideoEntity) -> Unit,
    onToggleDeployment: (String, Boolean) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE

    if (videos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isGerman) "Noch keine Lehrvideos vorhanden. Tippe auf +, um Videos für Schüler bereitzustellen!" else "No learning videos yet. Tap + to upload video lessons for your students!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(videos, key = { it.id }) { vid ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_item_${vid.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = OceanSecondary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Movie,
                                        contentDescription = "Video",
                                        tint = OceanSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = vid.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val durMin = vid.durationSeconds / 60
                                val durSec = vid.durationSeconds % 60
                                Text(
                                    text = "%d:%02d • ${vid.description}".format(durMin, durSec),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }

                            IconButton(
                                onClick = { onOpen(vid) },
                                modifier = Modifier.testTag("play_video_${vid.id}")
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", tint = OceanPrimary)
                            }

                            if (vid.isInstructorUploaded) {
                                IconButton(
                                    onClick = { onDelete(vid) },
                                    modifier = Modifier.testTag("delete_video_${vid.id}")
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = CoralAlert)
                                }
                            }
                        }

                        // Deployment Controls & Origin Badges
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (vid.isInstructorUploaded) AquaCyan.copy(alpha = 0.18f) else OceanPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (vid.isInstructorUploaded) (if (isGerman) "👨‍🏫 Eigene Inhalte" else "👨‍🏫 Instructor Custom") else (if (isGerman) "🌐 App-Standard" else "🌐 General App Material"),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (vid.isInstructorUploaded) OceanPrimary else OceanSecondary
                                    )
                                }
                                if (GoogleDriveHelper.isGoogleDriveUrl(vid.videoUrlOrUri)) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = SeaGreen.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "☁️ Google Drive",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SeaGreen
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (vid.isDeployed) SeaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = if (vid.isDeployed) "🟢 Deployed" else "⚪ Draft (Hidden)",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (vid.isDeployed) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (vid.isDeployed) "Deployed" else "Deploy",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = if (vid.isDeployed) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = vid.isDeployed,
                                    onCheckedChange = { onToggleDeployment(vid.id, it) },
                                    modifier = Modifier.testTag("deploy_switch_vid_${vid.id}")
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun QuizzesList(
    quizzes: List<QuizEntity>,
    onOpen: (QuizEntity) -> Unit,
    onDelete: (QuizEntity) -> Unit,
    onToggleDeployment: (String, Boolean) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE

    if (quizzes.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isGerman) "Noch keine Quizze vorhanden. Tippe auf +, um Prüfungsfragen und Tests zu erstellen!" else "No quizzes yet. Tap + to create questions and exams!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(quizzes, key = { it.id }) { qz ->
                val qCount = qz.parseQuestions().size
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_item_${qz.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (qz.isMockExam) SandGold.copy(alpha = 0.2f) else SeaGreen.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Rounded.Quiz,
                                        contentDescription = "Quiz",
                                        tint = if (qz.isMockExam) SandGold else SeaGreen
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = qz.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (qz.isMockExam) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = SandGold.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = "MOCK EXAM",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$qCount Questions • Pass: ${qz.passScorePercentage}% • Best: ${qz.highestScore}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            IconButton(
                                onClick = { onOpen(qz) },
                                modifier = Modifier.testTag("preview_quiz_${qz.id}")
                            ) {
                                Icon(Icons.Rounded.Visibility, contentDescription = "Test Quiz", tint = OceanPrimary)
                            }

                            if (qz.isInstructorUploaded) {
                                IconButton(
                                    onClick = { onDelete(qz) },
                                    modifier = Modifier.testTag("delete_quiz_${qz.id}")
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = strings.delete, tint = CoralAlert)
                                }
                            }
                        }

                        // Deployment Controls & Origin Badges
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (qz.isInstructorUploaded) AquaCyan.copy(alpha = 0.18f) else OceanPrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (qz.isInstructorUploaded) (if (isGerman) "👨‍🏫 Eigene Inhalte" else "👨‍🏫 Instructor Custom") else (if (isGerman) "🌐 App-Standard" else "🌐 General App Material"),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (qz.isInstructorUploaded) OceanPrimary else OceanSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (qz.isDeployed) SeaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest
                                ) {
                                    Text(
                                        text = if (qz.isDeployed) (if (isGerman) "🟢 Bereitgestellt" else "🟢 Deployed") else (if (isGerman) "⚪ Entwurf (Versteckt)" else "⚪ Draft (Hidden)"),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (qz.isDeployed) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (qz.isDeployed) "Deployed" else "Deploy",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = if (qz.isDeployed) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = qz.isDeployed,
                                    onCheckedChange = { onToggleDeployment(qz.id, it) },
                                    modifier = Modifier.testTag("deploy_switch_qz_${qz.id}")
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ---------------- DIALOGS ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePresentationDialog(
    modules: List<CourseModuleEntity>,
    onDismiss: () -> Unit,
    onSave: (moduleId: String, title: String, desc: String, slides: List<SlideItem>, documentUri: String?) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedModuleId by remember { mutableStateOf(modules.firstOrNull()?.id ?: "mod_physics") }
    var selectedDocumentUri by remember { mutableStateOf<String?>(null) }
    var googleDriveUrlText by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Slides in deck
    val slides = remember(isGerman) {
        mutableStateListOf(
            SlideItem(
                slideNumber = 1,
                title = if (isGerman) "Einführung in die Tauchtechnik" else "Introduction to Dive Technique",
                subtitle = if (isGerman) "Grundlagenkonzepte für Schüler" else "Foundational concepts for candidates",
                bulletPoints = if (isGerman) {
                    listOf("Grundlegende Buddy-Prinzipien wiederholen", "Finimeter- und Tauchgangsplanung bestätigen")
                } else {
                    listOf("Review basic buddy principles", "Confirm depth gauges and dive plan")
                },
                diagramType = "GENERAL_CHECKLIST",
                instructorNotes = if (isGerman) "Buddy-Kommunikation und Handzeichen hervorheben." else "Emphasize buddy communication.",
                keyTakeaway = if (isGerman) "Niemals alleine tauchen." else "Never dive alone."
            )
        )
    }

    // File / Document Picker for real device files
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedDocumentUri = uri.toString()
            if (title.isBlank()) {
                title = if (isGerman) "Hochgeladene Präsentation" else "Uploaded Presentation Deck"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = strings.uploadCreatePresentationTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pick Presentation Document
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { filePicker.launch("*/*") },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = OceanPrimary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.FileUpload,
                                    contentDescription = if (isGerman) "Dokument hochladen" else "Upload Document",
                                    tint = OceanPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (selectedDocumentUri != null) strings.fileSelectedBadge else strings.uploadDeckFileOrPdf,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedDocumentUri ?: strings.tapToChooseFilePrompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Google Drive / Cloud Presentation Link
                OutlinedTextField(
                    value = googleDriveUrlText,
                    onValueChange = { googleDriveUrlText = it },
                    label = { Text("☁️ Google Drive / Slides Link") },
                    placeholder = { Text("https://drive.google.com/... oder docs.google.com/presentation/...") },
                    leadingIcon = { Icon(Icons.Rounded.Cloud, contentDescription = null, tint = OceanPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        if (GoogleDriveHelper.isGoogleDriveUrl(googleDriveUrlText)) {
                            Text("✓ Google Drive / Slides Link erkannt", color = SeaGreen, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Optional: Freigabelink zu Präsentation oder PDF einfügen")
                        }
                    }
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.presentationTitleLabel) },
                    placeholder = { Text(strings.presentationTitlePlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("presentation_title_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.descriptionLabel) },
                    placeholder = { Text(strings.presentationDescPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Module selector
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = modules.find { it.id == selectedModuleId }?.title ?: strings.selectModulePrompt,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.courseModuleLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        modules.forEach { mod ->
                            DropdownMenuItem(
                                text = { Text(mod.title) },
                                onClick = {
                                    selectedModuleId = mod.id
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Add slide card
                Text(
                    text = "${strings.presentationSlidesCount} (${slides.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                slides.forEachIndexed { index, slide ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${strings.slidePrefix} ${index + 1}: ${slide.title}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = slide.keyTakeaway,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        val num = slides.size + 1
                        slides.add(
                            SlideItem(
                                slideNumber = num,
                                title = if (isGerman) "Folie $num: Sicherheits-Checkliste" else "Slide $num: Safety Checklist",
                                subtitle = if (isGerman) "Wiederholungspunkte für Schüler" else "Candidate Review Points",
                                bulletPoints = if (isGerman) {
                                    listOf("Atemgasverbrauch prüfen", "Sicherheitsstopp auf 5 m korrekt durchführen")
                                } else {
                                    listOf("Check air consumption rate", "Safety stop execution at 5m")
                                },
                                diagramType = if (num % 2 == 0) "ASCENT_PROFILE" else "BWRAF_BUDDY",
                                instructorNotes = if (isGerman) "Praxiserfahrungen mit den Schülern diskutieren." else "Discuss real-life scenarios with students.",
                                keyTakeaway = if (isGerman) "Aufstiegsgeschwindigkeit von max. 9 m/min strikt einhalten." else "Always ascend at 9m/min or slower."
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = if (isGerman) "Folie hinzufügen" else "Add Slide")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.addAnotherSlideButton)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalUri = googleDriveUrlText.ifBlank { selectedDocumentUri }
                    onSave(
                        selectedModuleId,
                        title.ifBlank { if (isGerman) "Tauchlehrer-Präsentation" else "Instructor Deck" },
                        description.ifBlank { if (isGerman) "Präsentation für Schüler." else "Presentation for students." },
                        slides,
                        finalUri
                    )
                },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                modifier = Modifier.testTag("save_presentation_button")
            ) {
                Text(strings.publishToStudentsButton)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVideoDialog(
    modules: List<CourseModuleEntity>,
    onDismiss: () -> Unit,
    onSave: (
        moduleId: String,
        title: String,
        desc: String,
        durationMin: Int,
        uriOrUrl: String,
        chapters: List<Pair<Int, String>>,
        takeaways: List<String>
    ) -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var durationMinutesText by remember { mutableStateOf("4") }
    var videoUriText by remember { mutableStateOf("") }
    var googleDriveUrlText by remember { mutableStateOf("") }
    var selectedModuleId by remember { mutableStateOf(modules.firstOrNull()?.id ?: "mod_physics") }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Zero-permission Android Photo/Media Picker for videos
    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            videoUriText = uri.toString()
            if (title.isBlank()) {
                title = "Uploaded Dive Skill Video"
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = strings.uploadVideo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Video Picker Card (Local Device)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            videoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = OceanSecondary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Movie,
                                    contentDescription = "Pick Video",
                                    tint = OceanSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (videoUriText.isNotBlank()) strings.videoAttachedBadge else strings.selectVideoFromDevice,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (videoUriText.isNotBlank()) videoUriText else strings.zeroPermMediaPickerDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Google Drive / Cloud Video Option
                OutlinedTextField(
                    value = googleDriveUrlText,
                    onValueChange = { googleDriveUrlText = it },
                    label = { Text("☁️ Google Drive Video-Link (Cloud Stream)") },
                    placeholder = { Text("https://drive.google.com/file/d/...") },
                    leadingIcon = { Icon(Icons.Rounded.Cloud, contentDescription = null, tint = OceanSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        if (GoogleDriveHelper.isGoogleDriveUrl(googleDriveUrlText)) {
                            Text("✓ Google Drive Video erkannt (Streaming aktiv)", color = SeaGreen, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Freigabelink zu deinem MP4-Video in Google Drive einfügen")
                        }
                    }
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.videoTitleLabel) },
                    placeholder = { Text(strings.videoTitlePlaceholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_title_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.videoDescInstructionsLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = durationMinutesText,
                    onValueChange = { durationMinutesText = it },
                    label = { Text(strings.approxDurationMinutesLabel) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Module selector
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = modules.find { it.id == selectedModuleId }?.title ?: strings.selectModulePrompt,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.courseModuleLabel) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        modules.forEach { mod ->
                            DropdownMenuItem(
                                text = { Text(mod.title) },
                                onClick = {
                                    selectedModuleId = mod.id
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val durationMin = durationMinutesText.toIntOrNull() ?: 4
                    val chapters = listOf(
                        Pair(0, "Einführung & Übungsbriefing"),
                        Pair(45, "Demonstration im Flachwasser / Pool"),
                        Pair(120, "Häufige Fehler & Korrektur")
                    )
                    val takeaways = listOf(
                        "Stetige und ruhige Atmung während der Übungsausführung beibehalten.",
                        "Buddy beobachtet aufmerksam und signalisiert Bestätigung."
                    )
                    val finalVideoUri = googleDriveUrlText.ifBlank { videoUriText.ifBlank { "scuba://video/uploaded_${System.currentTimeMillis()}" } }
                    onSave(
                        selectedModuleId,
                        title.ifBlank { "Training Video" },
                        description.ifBlank { "Diver training video." },
                        durationMin,
                        finalVideoUri,
                        chapters,
                        takeaways
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("save_video_button")
            ) {
                Text(strings.publishVideoButton)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuizDialog(
    modules: List<CourseModuleEntity>,
    onDismiss: () -> Unit,
    onSave: (
        moduleId: String,
        title: String,
        desc: String,
        passScore: Int,
        questions: List<QuizQuestion>,
        isMock: Boolean
    ) -> Unit
) {
    val strings = LocalAppStrings.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var passScoreText by remember { mutableStateOf("80") }
    var isMockExam by remember { mutableStateOf(false) }
    var selectedModuleId by remember { mutableStateOf(modules.firstOrNull()?.id ?: "mod_physics") }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Dynamic question list
    val questions = remember {
        mutableStateListOf(
            QuizQuestion(
                id = "custom_q1",
                question = "Was sollte ein Taucher tun, wenn er beim Abtauchen einen Druckschmerz in den Nebenhöhlen spürt?",
                options = listOf(
                    "Leicht aufsteigen bis der Schmerz nachlässt, Druckausgleich durchführen und langsam weitertauchen",
                    "Schneller abtauchen, um den Hohlraum zu öffnen",
                    "Unter Wasser die Maske abnehmen",
                    "Tarierweste voll aufblasen"
                ),
                correctIndex = 0,
                explanation = "Durch leichtes Aufsteigen verringert sich der Umgebungsdruck, sodass der Druckausgleich ohne Gewebetrauma gelingt.",
                safetyRule = "Niemals gegen Schmerzen abtauchen.",
                depthOrContext = "Abtauchprotokoll"
            )
        )
    }

    // Temporary inputs for new question
    var newQText by remember { mutableStateOf("") }
    var newOpt0 by remember { mutableStateOf("") }
    var newOpt1 by remember { mutableStateOf("") }
    var newOpt2 by remember { mutableStateOf("") }
    var newOpt3 by remember { mutableStateOf("") }
    var newCorrectIdx by remember { mutableIntStateOf(0) }
    var newExpl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = "Quiz oder Probeprüfung erstellen",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Quiz-Titel") },
                    placeholder = { Text("z.B. Navigation & Kompass-Quiz") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_title_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beschreibung") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Offizieller Probeprüfungs-Modus", fontWeight = FontWeight.Bold)
                    Switch(
                        checked = isMockExam,
                        onCheckedChange = { isMockExam = it }
                    )
                }

                OutlinedTextField(
                    value = passScoreText,
                    onValueChange = { passScoreText = it },
                    label = { Text("Bestehensgrenze (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Module selector
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = modules.find { it.id == selectedModuleId }?.title ?: "Modul auswählen",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kursmodul") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(14.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        modules.forEach { mod ->
                            DropdownMenuItem(
                                text = { Text(mod.title) },
                                onClick = {
                                    selectedModuleId = mod.id
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Existing Questions
                Text(
                    text = "Fragen in diesem Quiz (${questions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                questions.forEachIndexed { i, q ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Frage ${i + 1}: ${q.question}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Richtig: Option ${q.correctIndex + 1} (${q.options.getOrNull(q.correctIndex) ?: ""})",
                                style = MaterialTheme.typography.bodySmall,
                                color = SeaGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Add Question Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "+ Frage zum Quiz hinzufügen",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        OutlinedTextField(
                            value = newQText,
                            onValueChange = { newQText = it },
                            label = { Text("Fragentext") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = newOpt0,
                            onValueChange = { newOpt0 = it },
                            label = { Text("Option A") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = newOpt1,
                            onValueChange = { newOpt1 = it },
                            label = { Text("Option B") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = newOpt2,
                            onValueChange = { newOpt2 = it },
                            label = { Text("Option C") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = newOpt3,
                            onValueChange = { newOpt3 = it },
                            label = { Text("Option D") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Text("Richtige Option auswählen:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("A", "B", "C", "D").forEachIndexed { idx, letter ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = newCorrectIdx == idx,
                                        onClick = { newCorrectIdx = idx }
                                    )
                                    Text(letter)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newExpl,
                            onValueChange = { newExpl = it },
                            label = { Text("Erklärung für Schüler / Prüflinge") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        Button(
                            onClick = {
                                if (newQText.isNotBlank()) {
                                    questions.add(
                                        QuizQuestion(
                                            id = "q_${UUID.randomUUID().toString().take(6)}",
                                            question = newQText,
                                            options = listOf(
                                                newOpt0.ifBlank { "Option A" },
                                                newOpt1.ifBlank { "Option B" },
                                                newOpt2.ifBlank { "Option C" },
                                                newOpt3.ifBlank { "Option D" }
                                            ),
                                            correctIndex = newCorrectIdx,
                                            explanation = newExpl.ifBlank { "Tauchverfahren sorgfältig wiederholen." },
                                            safetyRule = "Sicherheitsregeln stets einhalten.",
                                            depthOrContext = "Ausbildungsbewertung"
                                        )
                                    )
                                    newQText = ""
                                    newOpt0 = ""
                                    newOpt1 = ""
                                    newOpt2 = ""
                                    newOpt3 = ""
                                    newExpl = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanSecondary)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Frage hinzufügen")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pass = passScoreText.toIntOrNull() ?: 80
                    onSave(
                        selectedModuleId,
                        title.ifBlank { "Tauch-Quiz" },
                        description.ifBlank { "Wissensüberprüfung" },
                        pass,
                        questions,
                        isMockExam
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.testTag("save_quiz_button")
            ) {
                Text("Quiz veröffentlichen")
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
