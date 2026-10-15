package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Signpost
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Upload
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
import com.example.ui.i18n.AppLanguage
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
                    colors = CardDefaults.cardColors(containerColor = SeaGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SeaGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = SeaGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SeaGreen)
                        }
                        IconButton(onClick = { enrollmentSuccessMessage = null }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = SeaGreen, modifier = Modifier.size(16.dp))
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
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        color = AquaCyan.copy(alpha = 0.2f),
                        border = BorderStroke(2.dp, AquaCyan)
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
                                    Icons.Default.DriveFileRenameOutline,
                                    contentDescription = strings.updateStudentName,
                                    tint = OceanPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = strings.studentEnvironmentCandidate,
                            style = MaterialTheme.typography.bodySmall,
                            color = OceanSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Streak Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OceanTertiary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = OceanTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${progress?.studyStreakDays ?: 1}d",
                                fontWeight = FontWeight.Black,
                                color = OceanTertiary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // Premium Segmented Sub-Tab Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
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
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) OceanPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSubTab = index }
                            .testTag("student_sub_tab_$index")
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(enrolledCourses, key = { it.id }) { course ->
                        val isSelected = course.id == selectedCourseId
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { selectedCourseId = course.id }
                                .testTag("enrolled_course_${course.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OceanPrimary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(if (isSelected) 2.5.dp else 1.dp, if (isSelected) OceanPrimary else OceanPrimary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = if (isSelected) OceanPrimary else OceanPrimary.copy(alpha = 0.1f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = course.iconEmoji, fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = course.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                color = if (isSelected) OceanPrimary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                        }
                                        Text(
                                            text = course.subtitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 9.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) OceanPrimary.copy(alpha = 0.15f) else SeaGreen.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = if (isSelected) "SELECTED" else "ACTIVE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) OceanPrimary else SeaGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "${course.lessonsCount} lessons • ${course.duration}",
                                        fontSize = 9.sp,
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        QuickActionTile(
                            title = strings.tileHandSignalsTitle,
                            subtitle = "20+ Signals in Course",
                            icon = Icons.Default.Signpost,
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
                            icon = Icons.Default.School,
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = OceanPrimary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.5.dp, OceanPrimary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = OceanPrimary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = selectedCourse?.iconEmoji ?: "🤿", fontSize = 22.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${selectedCourse?.title ?: "Open Water Diver"} Curriculum",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
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
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("nitrox_curriculum_module"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Specialty Unit: Oxygen Analysis & Gas Mixing",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                            Text(
                                text = "Theory and safety procedures for diving with oxygen concentrations up to 40% Enriched Air Nitrox (EANx).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ModuleItemRow(
                                title = "Nitrox Gas Analysis & Max Depth (MOD) calculations",
                                type = "Slide Deck (2 Slides)",
                                icon = Icons.Default.Slideshow,
                                isDone = nitroxPres?.isCompleted == true,
                                onClick = {
                                    if (nitroxPres != null) {
                                        viewModel.openPresentation(nitroxPres)
                                    }
                                }
                            )
                            ModuleItemRow(
                                title = "Enriched Air Safety & Oxygen Toxicity Exams",
                                type = "Practice Quiz (1 Question)",
                                icon = Icons.Default.Quiz,
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
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("deep_curriculum_module"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Specialty Unit: Deep Diving Limits & Narcosis Safety",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                            Text(
                                text = "Master safe descent procedures, redundant gas planning, and narcosis management for deep diving to 40m/130ft.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ModuleItemRow(
                                title = "Decompression Safety & Redundant Gas Systems",
                                type = "Slide Deck (1 Slide)",
                                icon = Icons.Default.Slideshow,
                                isDone = deepPres?.isCompleted == true,
                                onClick = {
                                    if (deepPres != null) {
                                        viewModel.openPresentation(deepPres)
                                    }
                                }
                            )
                            ModuleItemRow(
                                title = "Narcosis Symptoms & Safety Protocol Quiz",
                                type = "Practice Quiz (1 Question)",
                                icon = Icons.Default.Quiz,
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
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("wreck_curriculum_module"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Specialty Unit: Wreck Surveying & Line Laying",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                            Text(
                                text = "Procedures and protocols for mapping artificial reefs, deploying penetration guidelines, and avoiding silt-outs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ModuleItemRow(
                                title = "Wreck Hazards, Navigation & Penetration Lines",
                                type = "Slide Deck (1 Slide)",
                                icon = Icons.Default.Slideshow,
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
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("night_curriculum_module"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Specialty Unit: Dark Navigation & Light Communication",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                            Text(
                                text = "Master night dive navigation, specialized buddy communication signals, and explore nocturnal reef environments safely.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ModuleItemRow(
                                title = "Nocturnal Wildlife & Dive Torch Management",
                                type = "Slide Deck (1 Slide)",
                                icon = Icons.Default.Slideshow,
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = OceanTertiary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, OceanTertiary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = strings.studyStreak, tint = OceanTertiary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(strings.studyStreak, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${progress?.studyStreakDays ?: 1} ${strings.daysUnit}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = OceanPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = strings.studyTime, tint = OceanPrimary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(strings.studyTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${progress?.totalStudyMinutes ?: 35} ${strings.minsUnit}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleSmall)
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
                    color = MaterialTheme.colorScheme.primary,
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
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = CircleShape,
                                    color = OceanPrimary.copy(alpha = 0.1f)
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
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = course.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (percentProgress == 100) SeaGreen.copy(alpha = 0.15f) else OceanPrimary.copy(alpha = 0.1f)
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
                                    color = if (percentProgress == 100) SeaGreen else OceanPrimary
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { percentProgress.toFloat() / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (percentProgress == 100) SeaGreen else OceanPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                                        fontWeight = FontWeight.Bold
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
                                        fontWeight = FontWeight.Bold
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
                                        fontWeight = FontWeight.Bold
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
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DepthNavy
                        )
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
                                    Icon(Icons.Default.EmojiEvents, contentDescription = "Certificate", tint = SandGold, modifier = Modifier.size(30.dp))
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
                                shape = RoundedCornerShape(8.dp),
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
                        color = MaterialTheme.colorScheme.primary
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
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            if (quiz.isPassed) SeaGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                        )
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
                                color = if (quiz.isPassed) SeaGreen.copy(alpha = 0.15f) else OceanPrimary.copy(alpha = 0.12f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (quiz.isPassed) Icons.Default.CheckCircle else Icons.Default.Quiz,
                                        contentDescription = "Quiz Status",
                                        tint = if (quiz.isPassed) SeaGreen else OceanPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quiz.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
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
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "Privacy Shield",
                            tint = OceanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
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
                    color = MaterialTheme.colorScheme.primary
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
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
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                            fontWeight = FontWeight.Bold
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
                                        else -> OceanPrimary.copy(alpha = 0.12f)
                                    }
                                ) {
                                    Text(
                                        text = course.difficulty.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when (course.difficulty.lowercase()) {
                                            "easy" -> SeaGreen
                                            "medium" -> SandGold
                                            else -> OceanPrimary
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
                                    shape = RoundedCornerShape(20.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
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
                            Icon(Icons.Default.Close, contentDescription = strings.close)
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
            title = { Text(strings.updateStudentName) },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    label = { Text(strings.studentNameLabel) },
                    singleLine = true,
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
                    }
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
            .width(160.dp)
            .height(110.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp))
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val totalItems = presentations.size + videos.size + quizzes.size
    val completedItems = presentations.count { it.isCompleted } +
            videos.count { it.isCompleted } +
            quizzes.count { it.isPassed }
    val progressFraction = if (totalItems > 0) completedItems.toFloat() / totalItems.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module_card_${module.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        text = "${strings.tabCurriculum.take(6)} ${module.orderIndex}: ${module.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = module.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
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
                                border = BorderStroke(1.dp, SeaGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
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
                                    imageVector = Icons.Default.Delete,
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
                                imageVector = Icons.Default.Download,
                                contentDescription = strings.downloadOfflineLabel,
                                tint = OceanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = module.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = isDownloaded) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SeaGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "✈️ " + strings.offlineReadyNotice,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp),
                        color = SeaGreen
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progressFraction >= 1f) SeaGreen else OceanPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                        title = pres.title,
                        type = "${strings.slideDeckType} (${pres.totalSlides} ${strings.slidesCountLabel})",
                        icon = Icons.Default.Slideshow,
                        isDone = pres.isCompleted,
                        isInstructorUploaded = pres.isInstructorUploaded,
                        onClick = { onOpenPresentation(pres) }
                    )
                }

                videos.forEach { vid ->
                    ModuleItemRow(
                        title = vid.title,
                        type = "${strings.videoType} (${vid.durationSeconds / 60} ${strings.durationMinutesSuffix})",
                        icon = Icons.Default.Movie,
                        isDone = vid.isCompleted,
                        isInstructorUploaded = vid.isInstructorUploaded,
                        onClick = { onOpenVideo(vid) }
                    )
                }

                quizzes.forEach { quiz ->
                    ModuleItemRow(
                        title = quiz.title,
                        type = if (quiz.isMockExam) strings.mockExamType else "${strings.practiceQuizType} (${quiz.parseQuestions().size}Q)",
                        icon = Icons.Default.Quiz,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type,
            tint = if (isDone) SeaGreen else OceanPrimary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
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
                    shape = RoundedCornerShape(4.dp),
                    color = if (isInstructorUploaded) AquaCyan.copy(alpha = 0.2f) else OceanPrimary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isInstructorUploaded) strings.instructorDeployedTag else strings.generalMaterialTag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isInstructorUploaded) OceanPrimary else OceanSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        if (isDone) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = strings.completedBadge,
                tint = SeaGreen,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = strings.startButton,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SpecPill(label: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
