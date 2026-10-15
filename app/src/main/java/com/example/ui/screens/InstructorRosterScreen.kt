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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.StudentRosterEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class StudentFilter {
    ACTIVE,
    PAST,
    ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorRosterScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    var filterMode by remember { mutableStateOf(StudentFilter.ACTIVE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedStudentForDetail by remember { mutableStateOf<StudentRosterEntity?>(null) }

    val allStudents by viewModel.allStudents.collectAsState()
    val activeStudents by viewModel.activeStudents.collectAsState()
    val pastStudents by viewModel.pastStudents.collectAsState()
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current
    val isGerman = currentLanguage == AppLanguage.DE

    val filteredList = when (filterMode) {
        StudentFilter.ACTIVE -> activeStudents
        StudentFilter.PAST -> pastStudents
        StudentFilter.ALL -> allStudents
    }.filter { student ->
        if (searchQuery.isBlank()) true
        else student.fullName.contains(searchQuery, ignoreCase = true) ||
                student.email.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Instructor Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roster_summary_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = strings.rosterTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary Stats Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = SeaGreen.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(strings.activeStudentsTab, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = SeaGreen, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${activeStudents.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = SeaGreen)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = OceanPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(strings.pastStudentsTab, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = OceanPrimary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${pastStudents.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = OceanPrimary)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = SandGold.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SandGold.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(strings.allStudentsTab, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = SandGold, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${allStudents.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = DepthNavy)
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(strings.searchStudentsPlaceholder) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = strings.searchStudentsPlaceholder) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = strings.close)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_search_input"),
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Status Filter Chips (Active Students vs Students from the Past vs All)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterMode == StudentFilter.ACTIVE,
                    onClick = { filterMode = StudentFilter.ACTIVE },
                    label = { Text("${strings.activeStudentsTab} (${activeStudents.size})") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.School,
                            contentDescription = strings.activeStudentsTab,
                            tint = if (filterMode == StudentFilter.ACTIVE) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SeaGreen.copy(alpha = 0.15f),
                        selectedLabelColor = SeaGreen
                    ),
                    modifier = Modifier.testTag("filter_active_students")
                )

                FilterChip(
                    selected = filterMode == StudentFilter.PAST,
                    onClick = { filterMode = StudentFilter.PAST },
                    label = { Text("${strings.pastStudentsTab} (${pastStudents.size})") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.History,
                            contentDescription = strings.pastStudentsTab,
                            tint = if (filterMode == StudentFilter.PAST) OceanPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OceanPrimary.copy(alpha = 0.15f),
                        selectedLabelColor = OceanPrimary
                    ),
                    modifier = Modifier.testTag("filter_past_students")
                )

                FilterChip(
                    selected = filterMode == StudentFilter.ALL,
                    onClick = { filterMode = StudentFilter.ALL },
                    label = { Text("${strings.allStudentsTab} (${allStudents.size})") },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.FilterList,
                            contentDescription = strings.allStudentsTab,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("filter_all_students")
                )
            }
        }

        // Students List
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(strings.noStudentsMatchFilter, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(filteredList, key = { it.id }) { student ->
                StudentRosterItemCard(
                    student = student,
                    onViewDetail = { selectedStudentForDetail = student },
                    onToggleStatus = {
                        val newStatus = if (student.status == "ACTIVE") "PAST_STUDENT" else "ACTIVE"
                        val certDate = if (newStatus == "PAST_STUDENT") {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        } else null
                        viewModel.updateStudentStatus(student.id, newStatus, certDate)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }

    // Detailed Student Progression Dossier Dialog
    if (selectedStudentForDetail != null) {
        val student = selectedStudentForDetail!!
        StudentDetailDialog(
            student = student,
            onDismiss = { selectedStudentForDetail = null },
            onSaveNotes = { newNotes ->
                viewModel.updateStudentNotes(student.id, newNotes)
                selectedStudentForDetail = student.copy(instructorNotes = newNotes)
            },
            onUpdateStatus = { newStatus ->
                val certDate = if (newStatus == "PAST_STUDENT") {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                } else null
                viewModel.updateStudentStatus(student.id, newStatus, certDate)
                selectedStudentForDetail = student.copy(status = newStatus, certifiedDate = certDate)
            }
        )
    }
}

@Composable
fun StudentRosterItemCard(
    student: StudentRosterEntity,
    onViewDetail: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    val isActive = student.status == "ACTIVE"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar, Name, Email, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isActive) SeaGreen.copy(alpha = 0.15f) else OceanPrimary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (isActive) "🤿" else "🎓", fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isGerman) {
                            if (isActive) "Eingeschrieben: ${student.enrolledDate}" else "Zertifiziert: ${student.certifiedDate ?: student.enrolledDate}"
                        } else {
                            if (isActive) "Enrolled: ${student.enrolledDate}" else "Certified: ${student.certifiedDate ?: student.enrolledDate}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) SeaGreen.copy(alpha = 0.15f) else SandGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, (if (isActive) SeaGreen else SandGold).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = if (isGerman) {
                            if (isActive) "AKTIVER SCHÜLER" else "ZERTIFIZIERT"
                        } else {
                            if (isActive) "ACTIVE STUDENT" else "PAST (CERTIFIED)"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (isActive) SeaGreen else DepthNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progression Bar & Readiness Gauge
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.examReadinessLabel,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${student.examReadinessScore}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (student.examReadinessScore >= 80) SeaGreen else OceanPrimary
                    )
                }
                LinearProgressIndicator(
                    progress = { student.examReadinessScore / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (student.examReadinessScore >= 80) SeaGreen else OceanPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Progression Counters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Presentations
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Slideshow, contentDescription = null, tint = OceanPrimary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${student.presentationsCompleted}/${student.totalPresentations}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Videos
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Movie, contentDescription = null, tint = OceanSecondary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${student.videosWatched}/${student.totalVideos}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quizzes
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Quiz, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${student.quizzesPassed}/${student.totalQuizzes}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Final Mock Exam
                Surface(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = SandGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isGerman) {
                                if (student.finalMockExamScore != null) "Prüfung: ${student.finalMockExamScore}%" else "Prüfung: Ausstehend"
                            } else {
                                if (student.finalMockExamScore != null) "Exam: ${student.finalMockExamScore}%" else "Exam: Pending"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (student.instructorNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isGerman) "Notizen: \"${student.instructorNotes}\"" else "Notes: \"${student.instructorNotes}\"",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: View Detailed Progression & Toggle Active/Past Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetail,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("view_student_progress_${student.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.viewProgressionButton, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onToggleStatus,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("toggle_student_status_${student.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) OceanPrimary else SeaGreen
                    )
                ) {
                    Text(
                        text = if (isActive) strings.markAsPast else strings.reactivate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Detailed Student Progression Modal / Dossier:
 * Allows the Instructor to review module breakdown, quiz attempts, and write instructor notes.
 */
@Composable
fun StudentDetailDialog(
    student: StudentRosterEntity,
    onDismiss: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    val strings = LocalAppStrings.current
    val isGerman = LocalCurrentLanguage.current == AppLanguage.DE
    var notesText by remember { mutableStateOf(student.instructorNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎓", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${student.email} • ${student.targetCourse}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Readiness Score Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(strings.examReadinessLabel, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${student.examReadinessScore}% ${strings.masterySuffix}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (student.examReadinessScore >= 80) SeaGreen else OceanPrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (student.status == "ACTIVE") SeaGreen else SandGold
                        ) {
                            Text(
                                text = student.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Module by Module Learning Breakdown
                Text(strings.courseProgressionBreakdown, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ModuleProgressLine(
                        if (isGerman) "Modul 1: Physik & Druck" else "Module 1: Physics & Pressure",
                        if (student.presentationsCompleted >= 1) (if (isGerman) "Abgeschlossen ✓" else "Completed ✓") else (if (isGerman) "Nicht begonnen" else "Not Started"),
                        student.presentationsCompleted >= 1
                    )
                    ModuleProgressLine(
                        if (isGerman) "Modul 2: Ausrüstungsgrundlagen" else "Module 2: Equipment Essentials",
                        if (student.presentationsCompleted >= 2) (if (isGerman) "Abgeschlossen ✓" else "Completed ✓") else (if (isGerman) "In Bearbeitung" else "In Progress"),
                        student.presentationsCompleted >= 2
                    )
                    ModuleProgressLine(
                        if (isGerman) "Modul 3: Tauchphysiologie" else "Module 3: Diving Physiology",
                        if (student.videosWatched >= 2) (if (isGerman) "Abgeschlossen ✓" else "Completed ✓") else (if (isGerman) "In Bearbeitung" else "In Progress"),
                        student.videosWatched >= 2
                    )
                    ModuleProgressLine(
                        if (isGerman) "Modul 4: Problemlösung" else "Module 4: Problem Solving",
                        if (student.videosWatched >= 3) (if (isGerman) "Abgeschlossen ✓" else "Completed ✓") else (if (isGerman) "Ausstehend" else "Pending"),
                        student.videosWatched >= 3
                    )
                    ModuleProgressLine(
                        if (isGerman) "Modul 5: Tauchgangsplanung & Probeprüfung" else "Module 5: Dive Planning & Mock Exam",
                        if (student.finalMockExamScore != null) "${student.finalMockExamScore}% ${if (isGerman) "Bestanden ✓" else "Passed ✓"}" else (if (isGerman) "Ausstehend" else "Pending Review"),
                        student.finalMockExamScore != null
                    )
                }

                // Instructor Notes field
                Text(strings.instructorPrivateNotes, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    placeholder = { Text(strings.instructorNotesPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveNotes(notesText.trim())
                    onDismiss()
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
            ) {
                Text(strings.saveNotesAndClose)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(strings.cancel)
            }
        }
    )
}

@Composable
fun ModuleProgressLine(
    moduleTitle: String,
    statusText: String,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(moduleTitle, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (isCompleted) SeaGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
