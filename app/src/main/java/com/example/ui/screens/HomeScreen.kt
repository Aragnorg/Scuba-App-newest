package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Signpost
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CourseModuleEntity
import com.example.data.model.LearningVideoEntity
import com.example.data.model.PresentationDeckEntity
import com.example.data.model.QuizEntity
import com.example.ui.components.DocumentExpirationAlertBanner
import com.example.ui.components.ExamReadinessGauge
import com.example.ui.components.TacticalMetricChip
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppLocalizer
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.DepthNavy
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.OceanTertiary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import com.example.util.DocumentExpirationHelper

@Composable
fun HomeScreen(
    viewModel: AquaViewModel,
    onNavigateToDocuments: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val modules by viewModel.allModules.collectAsState()
    val presentations by viewModel.deployedPresentations.collectAsState()
    val videos by viewModel.deployedVideos.collectAsState()
    val quizzes by viewModel.deployedQuizzes.collectAsState()
    val progress by viewModel.userProgress.collectAsState()
    val readiness by viewModel.examReadiness.collectAsState()
    val documents by viewModel.allDocuments.collectAsState()
    val downloadedModules by viewModel.downloadedModules.collectAsState()
    val downloadingModules by viewModel.downloadingModules.collectAsState()
    val courses by viewModel.scubaCourses.collectAsState()
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current

    val alertDocuments = remember(documents, currentLanguage) {
        DocumentExpirationHelper.getAlertDocuments(documents, currentLanguage == AppLanguage.DE)
    }

    val studentName = currentUser?.fullName ?: progress?.studentName ?: "Diver Candidate"

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf("") }
    var showHandSignalsTrainerDialog by remember { mutableStateOf(false) }
    
    // Sub-tab selection state (0: Classroom, 1: My Progress, 2: Browse Specialties)
    var activeSubTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedCourseId by rememberSaveable { mutableStateOf("course_owd") }
    var enrollmentSuccessMessage by remember { mutableStateOf<String?>(null) }
    val enrolledCourses = remember(courses) { courses.filter { it.isEnrolled } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Expiration Alert Banner for near expiration / expired documents
        if (alertDocuments.isNotEmpty()) {
            item {
                DocumentExpirationAlertBanner(
                    alertDocuments = alertDocuments,
                    onDocumentClick = { onNavigateToDocuments() }
                )
            }
        }

        // Enrollment Success Toast
        enrollmentSuccessMessage?.let { msg ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SeaGreen.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = msg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SeaGreen)
                        }
                        IconButton(onClick = { enrollmentSuccessMessage = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.close, tint = SeaGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Unified Environment Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diver_header_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "🤿", fontSize = 28.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = studentName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(
                                onClick = {
                                    editNameText = studentName
                                    showEditNameDialog = true
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.DriveFileRenameOutline,
                                    contentDescription = strings.updateStudentName,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = strings.studentEnvironmentCandidate,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Streak Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${progress?.studyStreakDays ?: 1}d",
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // Tactical Diver Telemetry HUD Row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("diver_telemetry_hud_row"),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TacticalMetricChip(
                        label = if (currentLanguage == AppLanguage.DE) "STATUS" else "STATUS",
                        value = if (currentLanguage == AppLanguage.DE) "AKTIV" else "ACTIVE",
                        statusColor = SeaGreen
                    )
                }
                item {
                    TacticalMetricChip(
                        label = if (currentLanguage == AppLanguage.DE) "FORTSCHRITT" else "PROGRESS",
                        value = "${progress?.overallProgressPercentage ?: 0}%",
                        statusColor = AquaCyan
                    )
                }
                item {
                    TacticalMetricChip(
                        label = if (currentLanguage == AppLanguage.DE) "DOKUMENTE" else "DOCS",
                        value = if (alertDocuments.isNotEmpty()) (if (currentLanguage == AppLanguage.DE) "WARNUNG" else "ALERT") else (if (currentLanguage == AppLanguage.DE) "GÜLTIG" else "VALID"),
                        statusColor = if (alertDocuments.isNotEmpty()) CoralAlert else SeaGreen
                    )
                }
                item {
                    TacticalMetricChip(
                        label = if (currentLanguage == AppLanguage.DE) "STREAK" else "STREAK",
                        value = "${progress?.studyStreakDays ?: 1} TAGE",
                        statusColor = SandGold
                    )
                }
            }
        }

        // Premium Segmented Sub-Tab Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabsList = listOf(
                    if (currentLanguage == AppLanguage.DE) "Klassenzimmer" else "Classroom",
                    if (currentLanguage == AppLanguage.DE) "Mein Fortschritt" else "My Progress",
                    if (currentLanguage == AppLanguage.DE) "Kurskatalog" else "Browse Specialties"
                )
                tabsList.forEachIndexed { index, label ->
                    val isSelected = activeSubTab == index
                    val tabBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        animationSpec = tween(200),
                        label = "tab_bg_$index"
                    )
                    val tabTextColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(200),
                        label = "tab_text_$index"
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tabBgColor,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSubTab = index }
                            .testTag("student_sub_tab_$index")
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = tabTextColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SUB-TAB 0: CLASSROOM (Core Scuba Modules & Active Courses)
        // =========================================================================
        if (activeSubTab == 0) {
            // Active Enrolled Courses horizontal listing
            item {
                Text(
                    text = if (currentLanguage == AppLanguage.DE) "Meine aktiven Kurse" else "My Enrolled Courses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(enrolledCourses, key = { it.id }) { course ->
                        val isSelected = course.id == selectedCourseId
                        Card(
                            modifier = Modifier
                                .width(230.dp)
                                .clickable { selectedCourseId = course.id }
                                .testTag("enrolled_course_${course.id}"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = course.iconEmoji, fontSize = 18.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = course.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Text(
                                            text = course.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else SeaGreen.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (isSelected) (if (currentLanguage == AppLanguage.DE) "AUSGEWÄHLT" else "SELECTED") else (if (currentLanguage == AppLanguage.DE) "AKTIV" else "ACTIVE"),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else SeaGreen,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "${course.lessonsCount} ${if (currentLanguage == AppLanguage.DE) "Einheiten" else "lessons"} • ${course.duration}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Exam Readiness Gauge Integration
            item {
                ExamReadinessGauge(readinessPercentage = readiness)
            }

            // Self-Paced Hand Signals Shortcut & Mock Exam tiles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        QuickActionTile(
                            title = strings.tileHandSignalsTitle,
                            subtitle = if (currentLanguage == AppLanguage.DE) "20+ Signale im Kurs" else "20+ Signals in Course",
                            icon = Icons.Rounded.Signpost,
                            color = OceanSecondary,
                            onClick = { showHandSignalsTrainerDialog = true },
                            testTag = "tile_hand_signals"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val mockExam = quizzes.find { it.isMockExam }
                        QuickActionTile(
                            title = strings.tileMockExamTitle,
                            subtitle = if (mockExam?.isPassed == true) "${strings.tileMockExamSubtitlePassed} (${mockExam.highestScore}%)" else strings.tileMockExamSubtitle50Q,
                            icon = Icons.Rounded.School,
                            color = SandGold,
                            onClick = {
                                if (mockExam != null) {
                                    viewModel.openQuiz(mockExam)
                                }
                            },
                            testTag = "tile_mock_exam"
                        )
                    }
                }
            }

            // Dynamic curriculum display based on the selected course
            val selectedCourse = enrolledCourses.find { it.id == selectedCourseId } ?: enrolledCourses.firstOrNull()

            // Visual wrapper/header showing modules are PART of this selected course
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("course_curriculum_wrapper_header"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = selectedCourse?.iconEmoji ?: "🤿", fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${AppLocalizer.localizeCourseTitle(selectedCourse?.title ?: "Open Water Diver", currentLanguage == AppLanguage.DE)} ${if (currentLanguage == AppLanguage.DE) "Lehrplan" else "Curriculum"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentLanguage == AppLanguage.DE) "Kurslehrplan & Module" else "Active Course Curriculum & Study Units",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (selectedCourseId == "course_owd") {
                // Scuba modules list for Open Water Diver
                items(modules, key = { it.id }) { module ->
                    val modPres = presentations.filter { it.moduleId == module.id }
                    val modVids = videos.filter { it.moduleId == module.id }
                    val modQuizzes = quizzes.filter { it.moduleId == module.id }

                    ModuleCard(
                        module = module,
                        presentations = modPres,
                        videos = modVids,
                        quizzes = modQuizzes,
                        isDownloaded = downloadedModules.contains(module.id),
                        downloadProgress = downloadingModules[module.id],
                        onDownloadClick = { viewModel.downloadModule(module.id) },
                        onRemoveClick = { viewModel.removeDownloadedModule(module.id) },
                        onOpenPresentation = { viewModel.openPresentation(it) },
                        onOpenVideo = { viewModel.openVideo(it) },
                        onOpenQuiz = { viewModel.openQuiz(it) }
                    )
                }
            } else if (selectedCourseId == "course_nitrox") {
                item {
                    val nitroxPres = presentations.find { it.id == "pres_nitrox" }
                    val nitroxQuiz = quizzes.find { it.id == "quiz_nitrox" }
                    val isGerman = currentLanguage == AppLanguage.DE
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("nitrox_curriculum_module"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (isGerman) "Spezialkurs: Sauerstoffanalyse & Gasmischung" else "Specialty Unit: Oxygen Analysis & Gas Mixing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGerman) "Theorie und Sicherheitsverfahren für das Tauchen mit Sauerstoffkonzentrationen bis 40% Enriched Air Nitrox (EANx)." else "Theory and safety procedures for diving with oxygen concentrations up to 40% Enriched Air Nitrox (EANx).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ModuleItemRow(
                                title = if (isGerman) "Nitrox-Gasanalyse & Maximaltiefen-Berechnung (MOD)" else "Nitrox Gas Analysis & Max Depth (MOD) calculations",
                                type = if (isGerman) "Präsentation (2 Folien)" else "Slide Deck (2 Slides)",
                                icon = Icons.Rounded.Slideshow,
                                isDone = nitroxPres?.isCompleted == true,
                                onClick = {
                                    if (nitroxPres != null) {
                                        viewModel.openPresentation(nitroxPres)
                                    }
                                }
                            )
                            ModuleItemRow(
                                title = if (isGerman) "Enriched Air Sicherheit & Sauerstofftoxizität" else "Enriched Air Safety & Oxygen Toxicity Exams",
                                type = if (isGerman) "Übungsquiz (1 Frage)" else "Practice Quiz (1 Question)",
                                icon = Icons.Rounded.Quiz,
                                isDone = nitroxQuiz?.isPassed == true,
                                onClick = {
                                    if (nitroxQuiz != null) {
                                        viewModel.openQuiz(nitroxQuiz)
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (selectedCourseId == "course_deep") {
                item {
                    val deepPres = presentations.find { it.id == "pres_deep" }
                    val deepQuiz = quizzes.find { it.id == "quiz_deep" }
                    val isGerman = currentLanguage == AppLanguage.DE
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("deep_curriculum_module"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (isGerman) "Spezialkurs: Tieftauchgrenzen & Tiefenrausch" else "Specialty Unit: Deep Diving Limits & Narcosis Safety",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGerman) "Sichere Abstiegsverfahren, redundante Gasplanung und Tiefenrausch-Management bis 40m Tiefe." else "Master safe descent procedures, redundant gas planning, and narcosis management for deep diving to 40m/130ft.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ModuleItemRow(
                                title = if (isGerman) "Dekompressionssicherheit & redundante Gassysteme" else "Decompression Safety & Redundant Gas Systems",
                                type = if (isGerman) "Präsentation (1 Folie)" else "Slide Deck (1 Slide)",
                                icon = Icons.Rounded.Slideshow,
                                isDone = deepPres?.isCompleted == true,
                                onClick = {
                                    if (deepPres != null) {
                                        viewModel.openPresentation(deepPres)
                                    }
                                }
                            )
                            ModuleItemRow(
                                title = if (isGerman) "Tiefenrausch-Symptome & Sicherheitsprotokoll" else "Narcosis Symptoms & Safety Protocol Quiz",
                                type = if (isGerman) "Übungsquiz (1 Frage)" else "Practice Quiz (1 Question)",
                                icon = Icons.Rounded.Quiz,
                                isDone = deepQuiz?.isPassed == true,
                                onClick = {
                                    if (deepQuiz != null) {
                                        viewModel.openQuiz(deepQuiz)
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (selectedCourseId == "course_wreck") {
                item {
                    val wreckPres = presentations.find { it.id == "pres_wreck" }
                    val isGerman = currentLanguage == AppLanguage.DE
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("wreck_curriculum_module"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (isGerman) "Spezialkurs: Wrackvermessung & Führungsleinen" else "Specialty Unit: Wreck Surveying & Line Laying",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGerman) "Verfahren zur Kartierung von Wracks, Verlegen von Penetrationsleinen und Vermeidung von Sedimentaufwirbelung." else "Procedures and protocols for mapping artificial reefs, deploying penetration guidelines, and avoiding silt-outs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ModuleItemRow(
                                title = if (isGerman) "Wrackgefahren, Navigation & Penetrationsleinen" else "Wreck Hazards, Navigation & Penetration Lines",
                                type = if (isGerman) "Präsentation (1 Folie)" else "Slide Deck (1 Slide)",
                                icon = Icons.Rounded.Slideshow,
                                isDone = wreckPres?.isCompleted == true,
                                onClick = {
                                    if (wreckPres != null) {
                                        viewModel.openPresentation(wreckPres)
                                    }
                                }
                            )
                        }
                    }
                }
            } else if (selectedCourseId == "course_night") {
                item {
                    val nightPres = presentations.find { it.id == "pres_night" }
                    val isGerman = currentLanguage == AppLanguage.DE
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("night_curriculum_module"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = if (isGerman) "Spezialkurs: Nachtnavigation & Lampenkommunikation" else "Specialty Unit: Dark Navigation & Light Communication",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isGerman) "Nachttauchnavigation, Buddy-Signale mit der Lampe und sicheres Erkunden nachtaktiver Rifflebewesen." else "Master night dive navigation, specialized buddy communication signals, and explore nocturnal reef environments safely.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ModuleItemRow(
                                title = if (isGerman) "Nachtaktive Meeresbewohner & Lampen-Management" else "Nocturnal Wildlife & Dive Torch Management",
                                type = if (isGerman) "Präsentation (1 Folie)" else "Slide Deck (1 Slide)",
                                icon = Icons.Rounded.Slideshow,
                                isDone = nightPres?.isCompleted == true,
                                onClick = {
                                    if (nightPres != null) {
                                        viewModel.openPresentation(nightPres)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // SUB-TAB 1: MY PROGRESS / PROGRESSION (Personal Stats & Quiz Analytics)
        // =========================================================================
        if (activeSubTab == 1) {
            // Personal Study Stats Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.LocalFireDepartment, contentDescription = strings.studyStreak, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(strings.studyStreak, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${progress?.studyStreakDays ?: 1} ${strings.daysUnit}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = strings.studyTime, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(strings.studyTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${progress?.totalStudyMinutes ?: 35} ${strings.minsUnit}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Header for Courses Progress
            item {
                Text(
                    text = if (currentLanguage == AppLanguage.DE) "Kursfortschritt im Klassenzimmer" else "Classroom Course Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Progress Card per Enrolled Course
            items(enrolledCourses, key = { "progress_course_${it.id}" }) { course ->
                // Identify items belonging to this specific course
                val coursePres = when (course.id) {
                    "course_owd" -> presentations.filter { it.moduleId in listOf("mod_physics", "mod_equipment", "mod_physiology", "mod_emergencies", "mod_planning") }
                    "course_nitrox" -> presentations.filter { it.id == "pres_nitrox" }
                    "course_deep" -> presentations.filter { it.id == "pres_deep" }
                    "course_wreck" -> presentations.filter { it.id == "pres_wreck" }
                    "course_night" -> presentations.filter { it.id == "pres_night" }
                    else -> emptyList()
                }

                val courseVids = when (course.id) {
                    "course_owd" -> videos.filter { it.moduleId in listOf("mod_physics", "mod_equipment", "mod_physiology", "mod_emergencies", "mod_planning") }
                    else -> emptyList()
                }

                val courseQuizzes = when (course.id) {
                    "course_owd" -> quizzes.filter { it.moduleId in listOf("mod_physics", "mod_equipment", "mod_physiology", "mod_emergencies", "mod_planning") }
                    "course_nitrox" -> quizzes.filter { it.id == "quiz_nitrox" }
                    "course_deep" -> quizzes.filter { it.id == "quiz_deep" }
                    else -> emptyList()
                }

                val presTotal = coursePres.size
                val presDone = coursePres.count { it.isCompleted }

                val vidsTotal = courseVids.size
                val vidsDone = courseVids.count { it.isCompleted }

                val quizzesTotal = courseQuizzes.size
                val quizzesDone = courseQuizzes.count { it.isPassed }

                val totalElements = presTotal + vidsTotal + quizzesTotal
                val doneElements = presDone + vidsDone + quizzesDone

                val percentProgress = if (totalElements > 0) {
                    (doneElements.toFloat() / totalElements * 100).toInt()
                } else {
                    100
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("course_progress_card_${course.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = course.iconEmoji, fontSize = 20.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = course.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = course.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (percentProgress == 100) SeaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (percentProgress == 100) {
                                        if (currentLanguage == AppLanguage.DE) "ABGESCHLOSSEN" else "COMPLETED"
                                    } else {
                                        "$percentProgress%"
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = if (percentProgress == 100) SeaGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { percentProgress.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (percentProgress == 100) SeaGreen else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        // Breakdown lists
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (presTotal > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = strings.presentationsRead,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$presDone / $presTotal",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            if (vidsTotal > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = strings.trainingVideosWatched,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$vidsDone / $vidsTotal",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            if (quizzesTotal > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = strings.quizzesAndExamsPassed,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$quizzesDone / $quizzesTotal",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Gold Certificate Banner
            if (readiness >= 80) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("certificate_qualification_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DepthNavy
                        ),
                        border = BorderStroke(1.dp, SandGold.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                modifier = Modifier.size(56.dp),
                                shape = CircleShape,
                                color = SandGold.copy(alpha = 0.2f),
                                border = BorderStroke(2.dp, SandGold)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.EmojiEvents, contentDescription = "Certificate", tint = SandGold, modifier = Modifier.size(30.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = strings.officialKnowledgeClearance,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SandGold,
                                letterSpacing = 1.2.sp
                            )

                            Text(
                                text = strings.openWaterKnowledgeVerified,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = strings.certificateDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.1f)
                              ) {
                                Text(
                                    text = strings.diverAcademyLabel,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AquaCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Quiz Performance Title
            item {
                Column {
                    Text(
                        text = strings.myQuizPerformance,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (currentLanguage == AppLanguage.DE) 
                            "Nur vergangene Prüfungen. Um eine neue Prüfung abzulegen, gehe bitte in den Klassenzimmer-Bereich." 
                        else 
                            "Past exams only. To take or retake an exam, please visit the Classroom section.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Quizzes Scores History List (Only show attempted/past exams)
            val attemptedQuizzes = quizzes.filter { it.timesAttempted > 0 }
            if (attemptedQuizzes.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("no_exam_attempts_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.DE) "Noch keine Prüfungsversuche. Absolviere deine erste Prüfung im Klassenzimmer!" else "No exam attempts yet. Take your first exam in the Classroom section!",
                            modifier = Modifier.padding(20.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(attemptedQuizzes, key = { "progress_quiz_${it.id}" }) { quiz ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_quiz_card_${quiz.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(
                            1.dp,
                            if (quiz.isPassed) SeaGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = if (quiz.isPassed) SeaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (quiz.isPassed) Icons.Rounded.CheckCircle else Icons.Rounded.Quiz,
                                        contentDescription = "Quiz Status",
                                        tint = if (quiz.isPassed) SeaGreen else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quiz.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (quiz.isMockExam) "${strings.finalExamPrefix} • ${strings.passReqPrefix}: ${quiz.passScorePercentage}%" else "${strings.passReqPrefix}: ${quiz.passScorePercentage}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${strings.highestScorePrefix}: ${quiz.highestScore}% (${quiz.timesAttempted} ${strings.attemptsSuffix})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (quiz.isPassed) SeaGreen else SandGold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (quiz.isPassed) SeaGreen.copy(alpha = 0.12f) else SandGold.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, if (quiz.isPassed) SeaGreen.copy(alpha = 0.4f) else SandGold.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "${quiz.highestScore}%",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (quiz.isPassed) SeaGreen else SandGold
                                )
                            }
                        }
                    }
                }
            }

            // Student Environment Privacy Disclosure
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.Shield,
                                    contentDescription = "Privacy Shield",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = strings.studentEnvironmentDisclosure,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // =========================================================================
        // SUB-TAB 2: BROWSE SPECIALTIES (Specialty Scuba Courses Catalog)
        // =========================================================================
        if (activeSubTab == 2) {
            item {
                Text(
                    text = if (currentLanguage == AppLanguage.DE) "Verfügbare Spezialkurse" else "Available Specialty Courses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (currentLanguage == AppLanguage.DE) "Erweitern Sie Ihre Grenzen mit zertifizierten Zusatzausbildungen." else "Expand your limits and acquire advanced certifications with our premium specialties.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val availableCourses = courses.filter { !it.isEnrolled }

            if (availableCourses.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎓🎉", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.DE) "Sie sind in alle Kurse eingeschrieben!" else "You are Enrolled in All Available Courses!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (currentLanguage == AppLanguage.DE) "Gehen Sie zu 'Klassenzimmer', um die Theorie zu studieren." else "Review your active classes under 'Classroom' to study their units.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(availableCourses, key = { it.id }) { course ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("available_course_card_${course.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Row with Icon + Title + Difficulty
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(44.dp),
                                        shape = CircleShape,
                                        color = OceanSecondary.copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = course.iconEmoji, fontSize = 24.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = course.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = course.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when (course.difficulty.lowercase()) {
                                        "easy" -> SeaGreen.copy(alpha = 0.12f)
                                        "medium" -> SandGold.copy(alpha = 0.12f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    }
                                ) {
                                    Text(
                                        text = course.difficulty.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when (course.difficulty.lowercase()) {
                                            "easy" -> SeaGreen
                                            "medium" -> SandGold
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Specs Row (depth limit, duration, prerequisites)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SpecPill(label = if (currentLanguage == AppLanguage.DE) "Tiefe: ${course.depthLimit}" else "Depth: ${course.depthLimit}")
                                SpecPill(label = if (currentLanguage == AppLanguage.DE) "Dauer: ${course.duration}" else "Duration: ${course.duration}")
                                SpecPill(label = if (currentLanguage == AppLanguage.DE) "Voraussetzung: ${course.prerequisites}" else "Req: ${course.prerequisites}")
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive Enroll Now button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.enrollInCourse(course.id)
                                        enrollmentSuccessMessage = if (currentLanguage == AppLanguage.DE) {
                                             "Erfolgreich in den Spezialkurs '${course.title}' eingeschrieben!"
                                        } else {
                                            "Enrolled in specialty course '${course.title}' successfully!"
                                        }
                                    },
                                    modifier = Modifier.testTag("enroll_btn_${course.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Rounded.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLanguage == AppLanguage.DE) "Einschreiben" else "Enroll",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Integrated Hand Signals Interactive Trainer Dialog
    if (showHandSignalsTrainerDialog) {
        Dialog(
            onDismissRequest = { showHandSignalsTrainerDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("hand_signals_trainer_dialog"),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👌", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = strings.handSignalsTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Interactive Course Module Unit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { showHandSignalsTrainerDialog = false },
                            modifier = Modifier.testTag("close_hand_signals_dialog_btn")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.close)
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        HandSignalsScreen(signals = viewModel.getHandSignals(currentLanguage))
                    }
                }
            }
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            title = {
                Text(
                    text = strings.updateStudentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    label = { Text(strings.studentNameLabel) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameText.isNotBlank()) {
                            viewModel.updateStudentName(editNameText.trim())
                        }
                        showEditNameDialog = false
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ModuleCard(
    module: CourseModuleEntity,
    presentations: List<PresentationDeckEntity>,
    videos: List<LearningVideoEntity>,
    quizzes: List<QuizEntity>,
    isDownloaded: Boolean,
    downloadProgress: Float?,
    onDownloadClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onOpenPresentation: (PresentationDeckEntity) -> Unit,
    onOpenVideo: (LearningVideoEntity) -> Unit,
    onOpenQuiz: (QuizEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE
    val totalItems = presentations.size + videos.size + quizzes.size
    val completedItems = presentations.count { it.isCompleted } +
            videos.count { it.isCompleted } +
            quizzes.count { it.isPassed }
    val progressFraction = if (totalItems > 0) completedItems.toFloat() / totalItems.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("module_card_${module.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Module Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${strings.tabCurriculum.take(6)} ${module.orderIndex}: ${AppLocalizer.localizeModuleTitle(module.title, isGerman)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = AppLocalizer.localizeModuleSubtitle(module.subtitle, isGerman),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "~${module.estimatedMin}m",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Download State Management UI
                    if (downloadProgress != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = OceanSecondary
                            )
                            Text(
                                text = String.format(strings.downloadingLabel, (downloadProgress * 100).toInt()),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = OceanSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (isDownloaded) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SeaGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = strings.downloadedOfflineLabel,
                                        tint = SeaGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = strings.downloadedOfflineLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = SeaGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            IconButton(
                                onClick = onRemoveClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = strings.deleteOfflineLabel,
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onDownloadClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = strings.downloadOfflineLabel,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = AppLocalizer.localizeModuleDescription(module.description, isGerman),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = isDownloaded) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SeaGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✈️ " + strings.offlineReadyNotice,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(10.dp),
                        color = SeaGreen
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progressFraction >= 1f) SeaGreen else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$completedItems ${strings.completedOfTotal} $totalItems ${strings.completedItemsSuffix}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (progressFraction >= 1f) {
                        Text(
                            text = strings.moduleMastered,
                            style = MaterialTheme.typography.labelSmall,
                            color = SeaGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick launch buttons for items in module
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presentations.forEach { pres ->
                    ModuleItemRow(
                        title = AppLocalizer.localizePresentationTitle(pres.title, isGerman),
                        type = "${strings.slideDeckType} (${pres.totalSlides} ${strings.slidesCountLabel})",
                        icon = Icons.Rounded.Slideshow,
                        isDone = pres.isCompleted,
                        isInstructorUploaded = pres.isInstructorUploaded,
                        onClick = { onOpenPresentation(pres) }
                    )
                }

                videos.forEach { vid ->
                    ModuleItemRow(
                        title = AppLocalizer.localizeVideoTitle(vid.title, isGerman),
                        type = "${strings.videoType} (${vid.durationSeconds / 60} ${strings.durationMinutesSuffix})",
                        icon = Icons.Rounded.Movie,
                        isDone = vid.isCompleted,
                        isInstructorUploaded = vid.isInstructorUploaded,
                        onClick = { onOpenVideo(vid) }
                    )
                }

                quizzes.forEach { quiz ->
                    ModuleItemRow(
                        title = AppLocalizer.localizeQuizTitle(quiz.title, isGerman),
                        type = if (quiz.isMockExam) strings.mockExamType else "${strings.practiceQuizType} (${quiz.parseQuestions().size}Q)",
                        icon = Icons.Rounded.Quiz,
                        isDone = quiz.isPassed,
                        isInstructorUploaded = quiz.isInstructorUploaded,
                        onClick = { onOpenQuiz(quiz) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModuleItemRow(
    title: String,
    type: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isDone: Boolean,
    isInstructorUploaded: Boolean = false,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = if (isDone) SeaGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = type,
                        tint = if (isDone) SeaGreen else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Tag distinguishing General Material vs Instructor Deployed Material
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isInstructorUploaded) AquaCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = if (isInstructorUploaded) strings.instructorDeployedTag else strings.generalMaterialTag,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isInstructorUploaded) OceanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (isDone) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = strings.completedBadge,
                    tint = SeaGreen,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = strings.startButton,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SpecPill(label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            maxLines = 1
        )
    }
}
