package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.QuizEntity
import com.example.ui.components.QuizFeedbackMode
import com.example.ui.components.ReusableQuizEngine
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.viewmodel.AquaViewModel

/**
 * Quiz & Exam Screen powered by the ReusableQuizEngine.
 * Handles self-paced module quizzes and final mock certification exams,
 * displaying questions, handling student answers, and providing comprehensive
 * immediate feedback after final submission of all questions.
 */
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
    val currentLang = com.example.ui.i18n.LocalCurrentLanguage.current
    val isGerman = currentLang == com.example.ui.i18n.AppLanguage.DE
    val localizedTitle = com.example.ui.i18n.AppLocalizer.localizeQuizTitle(quiz.title, isGerman)
    val questions = quiz.parseQuestions().map { com.example.ui.i18n.AppLocalizer.localizeQuizQuestion(it, isGerman) }

    ReusableQuizEngine(
        title = localizedTitle,
        subtitle = if (quiz.isMockExam) strings.mockExamSubtitle else strings.moduleQuizSubtitle,
        questions = questions,
        passingScorePercentage = quiz.passScorePercentage,
        feedbackMode = QuizFeedbackMode.AFTER_FINAL_SUBMISSION,
        onQuizCompleted = { scorePercentage, isPassed, _ ->
            viewModel.recordQuizAttempt(quiz.id, scorePercentage, isPassed)
        },
        onExit = {
            viewModel.closeQuiz()
        },
        modifier = modifier
    )
}
