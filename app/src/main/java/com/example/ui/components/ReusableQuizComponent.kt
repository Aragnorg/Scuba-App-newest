package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizQuestion
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen

enum class QuizFeedbackMode {
    AFTER_FINAL_SUBMISSION, // Candidate answers all questions first; full immediate feedback on submission
    INSTANT_PER_QUESTION    // Immediate feedback revealed right after answering each question
}

enum class ReviewFilter {
    ALL,
    INCORRECT_ONLY,
    CORRECT_ONLY
}

/**
 * Reusable Quiz Component in Jetpack Compose:
 * - Displays questions with clear navigation & options
 * - Handles user input and question state
 * - Provides immediate feedback after final submission of all questions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReusableQuizEngine(
    title: String,
    questions: List<QuizQuestion>,
    modifier: Modifier = Modifier,
    subtitle: String = "Self-Paced Knowledge Evaluation",
    passingScorePercentage: Int = 80,
    feedbackMode: QuizFeedbackMode = QuizFeedbackMode.AFTER_FINAL_SUBMISSION,
    onQuizCompleted: (scorePercentage: Int, isPassed: Boolean, answers: Map<Int, Int>) -> Unit = { _, _, _ -> },
    onExit: () -> Unit = {}
) {
    val strings = LocalAppStrings.current
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

    // Calculate score
    val correctCount = questions.indices.count { idx ->
        userAnswers[idx] == questions[idx].correctIndex
    }
    val scorePercentage = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0
    val isPassed = scorePercentage >= passingScorePercentage

    val progressFraction = (answeredCount.toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)

    if (showConfirmSubmitDialog) {
        val unanswered = totalQuestions - answeredCount
        AlertDialog(
            onDismissRequest = { showConfirmSubmitDialog = false },
            title = { Text(strings.confirmSubmitTitle) },
            text = {
                Text(
                    if (unanswered > 0)
                        strings.confirmSubmitDesc + " ($unanswered)"
                    else
                        strings.confirmSubmitDesc
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSubmitDialog = false
                        isSubmitted = true
                        onQuizCompleted(scorePercentage, isPassed, userAnswers.toMap())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                    modifier = Modifier.testTag("confirm_submit_quiz_button")
                ) {
                    Text(strings.submitAll)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSubmitDialog = false }) {
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
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (isSubmitted) strings.quizResultsTitle else subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onExit,
                            modifier = Modifier.testTag("quiz_back_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = strings.exitQuizButton)
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
                                    color = OceanPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                if (!isSubmitted) {
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier.fillMaxWidth(),
                        color = AquaCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
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
                            modifier = Modifier.testTag("quiz_prev_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.previousQuestion)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.previousQuestion)
                        }

                        if (safeIndex == totalQuestions - 1) {
                            Button(
                                onClick = { showConfirmSubmitDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                                modifier = Modifier.testTag("quiz_final_submit_button")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = strings.submitFinalExam)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.submitFinalExam)
                            }
                        } else {
                            Button(
                                onClick = { if (currentIndex < totalQuestions - 1) currentIndex++ },
                                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                                modifier = Modifier.testTag("quiz_next_button")
                            ) {
                                Text(strings.nextQuestion)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = strings.nextQuestion)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isSubmitted) {
            // POST-SUBMISSION IMMEDIATE FEEDBACK VIEW
            PostSubmissionFeedbackView(
                title = title,
                questions = questions,
                userAnswers = userAnswers,
                scorePercentage = scorePercentage,
                isPassed = isPassed,
                passingScorePercentage = passingScorePercentage,
                reviewFilter = reviewFilter,
                onFilterChanged = { reviewFilter = it },
                onRetake = {
                    userAnswers.clear()
                    currentIndex = 0
                    isSubmitted = false
                },
                onExit = onExit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            // QUESTION RUNNER VIEW (Answer & Navigate questions)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Question Quick-Jump Strip
                QuestionQuickJumpStrip(
                    totalQuestions = totalQuestions,
                    currentIndex = safeIndex,
                    userAnswers = userAnswers,
                    onSelectQuestion = { currentIndex = it }
                )

                // Context tag
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
                        .testTag("reusable_question_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentQuestion.options.forEachIndexed { optIndex, optionText ->
                        val isSelected = userAnswers[safeIndex] == optIndex
                        val cardBg by animateColorAsState(
                            targetValue = if (isSelected) AquaCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            label = "option_bg"
                        )
                        val borderColor = if (isSelected) OceanPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    userAnswers[safeIndex] = optIndex
                                }
                                .testTag("quiz_option_${safeIndex}_$optIndex"),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
                            shape = RoundedCornerShape(12.dp)
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
                                        selectedColor = OceanPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyMedium,
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

@Composable
fun QuestionQuickJumpStrip(
    totalQuestions: Int,
    currentIndex: Int,
    userAnswers: Map<Int, Int>,
    onSelectQuestion: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(totalQuestions) { index ->
            val isCurrent = index == currentIndex
            val isAnswered = userAnswers.containsKey(index)

            val bgColor = when {
                isCurrent -> OceanPrimary
                isAnswered -> AquaCyan.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val textColor = when {
                isCurrent -> Color.White
                isAnswered -> OceanPrimary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) AquaCyan else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelectQuestion(index) }
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
}

@Composable
fun PostSubmissionFeedbackView(
    title: String,
    questions: List<QuizQuestion>,
    userAnswers: Map<Int, Int>,
    scorePercentage: Int,
    isPassed: Boolean,
    passingScorePercentage: Int,
    reviewFilter: ReviewFilter,
    onFilterChanged: (ReviewFilter) -> Unit,
    onRetake: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val total = questions.size
    val correctCount = questions.indices.count { idx -> userAnswers[idx] == questions[idx].correctIndex }
    val incorrectCount = total - correctCount

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
        // Hero Score Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quiz_feedback_hero_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPassed) SeaGreen.copy(alpha = 0.12f) else CoralAlert.copy(alpha = 0.12f)
                ),
                border = BorderStroke(2.dp, if (isPassed) SeaGreen else CoralAlert)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = if (isPassed) SeaGreen else CoralAlert
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPassed) Icons.Default.EmojiEvents else Icons.Default.Cancel,
                                contentDescription = if (isPassed) strings.examPassedTitle else strings.needsFurtherReviewTitle,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isPassed) strings.examPassedTitle else strings.needsFurtherReviewTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isPassed) SeaGreen else CoralAlert
                    )

                    Text(
                        text = "$scorePercentage%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "$correctCount ${strings.questionOf} $total ${strings.questionsAnsweredCorrectly} (${strings.passCriteria}: $passingScorePercentage%)",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRetake,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("feedback_retake_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = strings.retakeQuizButton)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.retakeQuizButton)
                        }

                        Button(
                            onClick = onExit,
                            colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("feedback_done_button")
                        ) {
                            Text(strings.continueButton)
                        }
                    }
                }
            }
        }

        // Filter Chips for Review
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.detailedFeedbackTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = reviewFilter == ReviewFilter.ALL,
                        onClick = { onFilterChanged(ReviewFilter.ALL) },
                        label = { Text("${strings.filterAllQuestions} ($total)") }
                    )
                    FilterChip(
                        selected = reviewFilter == ReviewFilter.INCORRECT_ONLY,
                        onClick = { onFilterChanged(ReviewFilter.INCORRECT_ONLY) },
                        label = { Text("${strings.filterIncorrectOnly} ($incorrectCount)") }
                    )
                    FilterChip(
                        selected = reviewFilter == ReviewFilter.CORRECT_ONLY,
                        onClick = { onFilterChanged(ReviewFilter.CORRECT_ONLY) },
                        label = { Text("${strings.filterCorrectOnly} ($correctCount)") }
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, if (isCorrect) SeaGreen.copy(alpha = 0.6f) else CoralAlert.copy(alpha = 0.6f))
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
                            text = "${strings.questionPrefix} ${qIndex + 1}",
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect) SeaGreen else CoralAlert,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCorrect) strings.correctLabel else strings.incorrectLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) SeaGreen else CoralAlert
                                )
                            }
                        }
                    }

                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Options breakdown with candidate's choice highlighted
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        question.options.forEachIndexed { optIdx, optText ->
                            val isPickedByUser = userPickIndex == optIdx
                            val isTheRightAnswer = optIdx == question.correctIndex

                            val optBg = when {
                                isTheRightAnswer -> SeaGreen.copy(alpha = 0.15f)
                                isPickedByUser && !isTheRightAnswer -> CoralAlert.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }

                            val optBorder = when {
                                isTheRightAnswer -> SeaGreen
                                isPickedByUser && !isTheRightAnswer -> CoralAlert
                                else -> Color.Transparent
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(optBg)
                                    .border(1.dp, optBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${('A' + optIdx)}. ",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isTheRightAnswer) {
                                    Text(
                                        text = "✓ ${strings.correctLabel}",
                                        color = SeaGreen,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                } else if (isPickedByUser) {
                                    Text(
                                        text = strings.yourAnswerLabel,
                                        color = CoralAlert,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    // In-depth Explanation
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = strings.explanationRationaleLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = question.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Scuba Safety Protocol Callout
                    if (question.safetyRule.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(OceanPrimary.copy(alpha = 0.08f))
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = strings.safetyRule,
                                tint = OceanPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${strings.safetyRule}: ${question.safetyRule}",
                                style = MaterialTheme.typography.labelSmall,
                                color = OceanPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
