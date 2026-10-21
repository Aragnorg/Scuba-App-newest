package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestion
import com.example.ui.components.ReviewFilter
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.AppLocalizer
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel

/**
 * Quiz & Exam Screen for AquaLearn Pro.
 * Provides self-paced module quizzes and final mock certification exams.
 * Calculates score percentage, determines pass/fail (>= 75%), records attempts in Room DB,
 * and features an animated score gauge, pass/fail status, answer review, and navigation back to classroom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizExamScreen(
    viewModel: AquaViewModel,
    quiz: QuizEntity,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeQuiz()
    }

    val strings = LocalAppStrings.current
    val currentLang = LocalCurrentLanguage.current
    val isGerman = currentLang == AppLanguage.DE
    val localizedTitle = AppLocalizer.localizeQuizTitle(quiz.title, isGerman)
    val questions = remember(quiz, isGerman) {
        quiz.parseQuestions().map { AppLocalizer.localizeQuizQuestion(it, isGerman) }
    }

    if (questions.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(strings.noQuestionsAvailable)
        }
        return
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> optionIndex
    var isSubmitted by remember { mutableStateOf(false) }
    var showConfirmSubmitDialog by remember { mutableStateOf(false) }
    var reviewFilter by remember { mutableStateOf(ReviewFilter.ALL) }

    val totalQuestions = questions.size
    val safeIndex = currentIndex.coerceIn(0, totalQuestions - 1)
    val currentQuestion = questions[safeIndex]
    val answeredCount = userAnswers.size

    // Calculate real score and pass/fail (>= 75% passes)
    val correctCount = questions.indices.count { idx ->
        userAnswers[idx] == questions[idx].correctIndex
    }
    val scorePercentage = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0
    val isPassed = scorePercentage >= 75

    val progressFraction = (answeredCount.toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)

    if (showConfirmSubmitDialog) {
        val unanswered = totalQuestions - answeredCount
        AlertDialog(
            onDismissRequest = { showConfirmSubmitDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            icon = {
                Icon(
                    imageVector = Icons.Rounded.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = strings.confirmSubmitTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (unanswered > 0)
                        "${strings.confirmSubmitDesc} ($unanswered ${if (isGerman) "unbeantwortet" else "unanswered"})"
                    else
                        strings.confirmSubmitDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSubmitDialog = false
                        isSubmitted = true
                        // Record the attempt in Room DB
                        viewModel.recordQuizAttempt(quiz.id, scorePercentage, isPassed)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("confirm_submit_quiz_button")
                ) {
                    Text(strings.submitAll, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmSubmitDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.keepReviewing)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = localizedTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = if (isSubmitted) {
                                    if (isGerman) "Prüfungsauswertung & Feedback" else "Exam Results & Performance"
                                } else {
                                    if (quiz.isMockExam) strings.mockExamSubtitle else strings.moduleQuizSubtitle
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { viewModel.closeQuiz() },
                            modifier = Modifier.testTag("quiz_back_button")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = strings.exitQuizButton)
                        }
                    },
                    actions = {
                        if (!isSubmitted) {
                            TextButton(
                                onClick = { showConfirmSubmitDialog = true },
                                modifier = Modifier.testTag("top_submit_quiz_button")
                            ) {
                                Text(
                                    text = "${strings.submitQuiz} ($answeredCount/$totalQuestions)",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )

                if (!isSubmitted) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
            }
        },
        bottomBar = {
            if (!isSubmitted) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp
                ) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { if (currentIndex > 0) currentIndex-- },
                                enabled = currentIndex > 0,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("quiz_prev_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = strings.previousQuestion,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.previousQuestion)
                            }

                            if (safeIndex == totalQuestions - 1) {
                                Button(
                                    onClick = { showConfirmSubmitDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("quiz_final_submit_button")
                                ) {
                                    Icon(Icons.Rounded.Check, contentDescription = strings.submitFinalExam, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.submitFinalExam, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { if (currentIndex < totalQuestions - 1) currentIndex++ },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                                    modifier = Modifier.testTag("quiz_next_button")
                                ) {
                                    Text(strings.nextQuestion, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = strings.nextQuestion,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isSubmitted) {
            // ANIMATED RESULT SCREEN WITH SCORE GAUGE & REVIEW
            QuizResultAnimatedScreen(
                quizTitle = localizedTitle,
                questions = questions,
                userAnswers = userAnswers,
                scorePercentage = scorePercentage,
                isPassed = isPassed,
                correctCount = correctCount,
                reviewFilter = reviewFilter,
                onFilterChanged = { reviewFilter = it },
                onRetake = {
                    userAnswers.clear()
                    currentIndex = 0
                    isSubmitted = false
                },
                onBackToClassroom = {
                    viewModel.closeQuiz()
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            // QUESTION RUNNER VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Quick-Jump Strip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(totalQuestions) { index ->
                        val isCurrent = index == safeIndex
                        val isAnswered = userAnswers.containsKey(index)

                        val bgColor = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isAnswered -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        }

                        val textColor = when {
                            isCurrent -> MaterialTheme.colorScheme.onPrimary
                            isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { currentIndex = index }
                                .testTag("jump_pill_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isCurrent || isAnswered) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )
                        }
                    }
                }

                // Context Tag
                if (currentQuestion.depthOrContext.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = currentQuestion.depthOrContext,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Question Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quiz_question_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "${strings.questionPrefix} ${safeIndex + 1} ${strings.questionOf} $totalQuestions",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQuestion.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    }
                }

                // Multiple Choice Options
                Text(
                    text = strings.selectOneAnswer,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentQuestion.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = userAnswers[safeIndex] == optIndex
                        val cardBg by animateColorAsState(
                            targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceContainerLow,
                            label = "option_bg"
                        )
                        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    userAnswers[safeIndex] = optIndex
                                }
                                .testTag("quiz_option_${safeIndex}_$optIndex"),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { userAnswers[safeIndex] = optIndex },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Animated Result Screen with Circular Score Gauge, Pass/Fail Badge,
 * Telemetry Cards, Answer Review, and "Zurück zum Klassenzimmer" Action Button.
 */
@Composable
fun QuizResultAnimatedScreen(
    quizTitle: String,
    questions: List<QuizQuestion>,
    userAnswers: Map<Int, Int>,
    scorePercentage: Int,
    isPassed: Boolean,
    correctCount: Int,
    reviewFilter: ReviewFilter,
    onFilterChanged: (ReviewFilter) -> Unit,
    onRetake: () -> Unit,
    onBackToClassroom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang = LocalCurrentLanguage.current
    val isGerman = currentLang == AppLanguage.DE
    val totalQuestions = questions.size
    val incorrectCount = totalQuestions - correctCount

    val filteredQuestions = questions.mapIndexed { idx, q ->
        Triple(idx, q, userAnswers[idx] == q.correctIndex)
    }.filter { (_, _, isCorrect) ->
        when (reviewFilter) {
            ReviewFilter.ALL -> true
            ReviewFilter.INCORRECT_ONLY -> !isCorrect
            ReviewFilter.CORRECT_ONLY -> isCorrect
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Score & Animated Gauge Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_result_hero_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPassed) SeaGreen.copy(alpha = 0.08f) else CoralAlert.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.5.dp, if (isPassed) SeaGreen.copy(alpha = 0.6f) else CoralAlert.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Animated Circular Score Gauge
                    AnimatedQuizScoreGauge(
                        scorePercentage = scorePercentage,
                        isPassed = isPassed
                    )

                    // Pass / Fail Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPassed) SeaGreen.copy(alpha = 0.18f) else CoralAlert.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, if (isPassed) SeaGreen else CoralAlert)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPassed) Icons.Rounded.EmojiEvents else Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = if (isPassed) SeaGreen else CoralAlert,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPassed) {
                                    if (isGerman) "HERZLICHEN GLÜCKWUNSCH! BESTANDEN" else "CONGRATULATIONS! EXAM PASSED"
                                } else {
                                    if (isGerman) "LEIDER NICHT BESTANDEN (MIN. 75%)" else "NEEDS REVIEW (MIN. 75% REQUIRED)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isPassed) SeaGreen else CoralAlert
                            )
                        }
                    }

                    Text(
                        text = if (isPassed) {
                            if (isGerman)
                                "Hervorragende Leistung! Du hast das Prüfungsziel von mindestens 75% erfolgreich gemeistert."
                            else
                                "Outstanding achievement! You have exceeded the 75% passing threshold."
                        } else {
                            if (isGerman)
                                "Zum Bestehen sind mindestens 75% erforderlich. Wiederhole die Präsentationen und versuche es erneut."
                            else
                                "A minimum score of 75% is required to pass. Review the course material and try again."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Performance Metric Chips Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Correct
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = SeaGreen.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isGerman) "Richtig" else "Correct",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeaGreen
                                )
                                Text(
                                    text = "$correctCount / $totalQuestions",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SeaGreen
                                )
                            }
                        }

                        // Incorrect
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = CoralAlert.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isGerman) "Falsch" else "Wrong",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralAlert
                                )
                                Text(
                                    text = "$incorrectCount / $totalQuestions",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CoralAlert
                                )
                            }
                        }

                        // Criteria
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = OceanPrimary.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.35f))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isGerman) "Kriterium" else "Pass Mark",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanPrimary
                                )
                                Text(
                                    text = "75%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = OceanPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary Button: "Zurück zum Klassenzimmer"
                    Button(
                        onClick = onBackToClassroom,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OceanPrimary,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_back_to_classroom"),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Rounded.School, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGerman) "Zurück zum Klassenzimmer" else "Back to Classroom",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Secondary Button: "Quiz wiederholen"
                    OutlinedButton(
                        onClick = onRetake,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_retake_quiz"),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGerman) "Prüfung wiederholen" else "Retake Exam",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Filter Header for Question Review
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isGerman) "Detaillierte Antworten-Prüfung" else "Detailed Answer Review",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = reviewFilter == ReviewFilter.ALL,
                        onClick = { onFilterChanged(ReviewFilter.ALL) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        label = { Text("${if (isGerman) "Alle" else "All"} ($totalQuestions)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    FilterChip(
                        selected = reviewFilter == ReviewFilter.INCORRECT_ONLY,
                        onClick = { onFilterChanged(ReviewFilter.INCORRECT_ONLY) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        label = { Text("${if (isGerman) "Falsche" else "Incorrect"} ($incorrectCount)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    FilterChip(
                        selected = reviewFilter == ReviewFilter.CORRECT_ONLY,
                        onClick = { onFilterChanged(ReviewFilter.CORRECT_ONLY) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        label = { Text("${if (isGerman) "Richtig" else "Correct"} ($correctCount)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        // Detailed Feedback Cards
        itemsIndexed(filteredQuestions, key = { _, item -> item.first }) { _, (qIndex, question, isCorrect) ->
            val userPickIndex = userAnswers[qIndex]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("review_card_$qIndex"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                border = BorderStroke(
                    1.dp,
                    if (isCorrect) SeaGreen.copy(alpha = 0.45f) else CoralAlert.copy(alpha = 0.45f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${if (isGerman) "Frage" else "Question"} ${qIndex + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCorrect) SeaGreen.copy(alpha = 0.15f) else CoralAlert.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect) SeaGreen else CoralAlert,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isCorrect) {
                                        if (isGerman) "Richtig" else "Correct"
                                    } else {
                                        if (isGerman) "Falsch" else "Incorrect"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) SeaGreen else CoralAlert
                                )
                            }
                        }
                    }

                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Options list with status
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        question.options.forEachIndexed { optIndex, optText ->
                            val isUserPick = userPickIndex == optIndex
                            val isCorrectAnswer = question.correctIndex == optIndex

                            val (optBg, optBorder, optTextColor) = when {
                                isCorrectAnswer -> Triple(
                                    SeaGreen.copy(alpha = 0.12f),
                                    SeaGreen.copy(alpha = 0.5f),
                                    SeaGreen
                                )
                                isUserPick && !isCorrect -> Triple(
                                    CoralAlert.copy(alpha = 0.12f),
                                    CoralAlert.copy(alpha = 0.5f),
                                    CoralAlert
                                )
                                else -> Triple(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = optBg,
                                border = BorderStroke(1.dp, optBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCorrectAnswer) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            tint = SeaGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else if (isUserPick) {
                                        Icon(
                                            imageVector = Icons.Rounded.Cancel,
                                            contentDescription = null,
                                            tint = CoralAlert,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optText,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isCorrectAnswer || isUserPick) FontWeight.Bold else FontWeight.Normal,
                                        color = optTextColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isCorrectAnswer) {
                                        Text(
                                            text = if (isGerman) "✓ Richtig" else "✓ Correct",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SeaGreen
                                        )
                                    } else if (isUserPick) {
                                        Text(
                                            text = if (isGerman) "✗ Deine Wahl" else "✗ Your Choice",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CoralAlert
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Explanation & Safety Rule Box
                    if (question.explanation.isNotBlank() || question.safetyRule.isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (question.explanation.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Rounded.Lightbulb,
                                            contentDescription = null,
                                            tint = AquaCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = question.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                if (question.safetyRule.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.Top) {
                                        Icon(
                                            imageVector = Icons.Rounded.Security,
                                            contentDescription = null,
                                            tint = SandGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = question.safetyRule,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SandGold
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
}

/**
 * Tactical Animated Score Gauge dial (240 degrees) with smooth sweep animation.
 */
@Composable
fun AnimatedQuizScoreGauge(
    scorePercentage: Int,
    isPassed: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (scorePercentage.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "scoreGaugeProgress"
    )

    val gaugeColor = if (isPassed) SeaGreen else CoralAlert
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    Box(
        modifier = modifier.size(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Background arc (240 degrees, centered at bottom)
            drawArc(
                color = trackColor,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(
                            gaugeColor.copy(alpha = 0.7f),
                            gaugeColor,
                            gaugeColor
                        )
                    ),
                    startAngle = 150f,
                    sweepAngle = 240f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = gaugeColor
            )
            Text(
                text = if (isPassed) "BESTANDEN" else "NICHT BESTANDEN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 8.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
