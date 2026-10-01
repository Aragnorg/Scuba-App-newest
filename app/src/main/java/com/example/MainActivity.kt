package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.i18n.getAppStrings
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.InstructorEnvironment
import com.example.ui.screens.PresentationViewerScreen
import com.example.ui.screens.QuizExamScreen
import com.example.ui.screens.StudentEnvironment
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AquaViewModel
import com.example.ui.viewmodel.AuthState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AquaViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDarkMode) {
                AquaApp(viewModel = viewModel)
            }
        }
    }
}

/**
 * Root Application Router:
 * 1. Unauthenticated -> AuthScreen (Login & Register with personal email verification)
 * 2. Active in-course viewer (Presentation, Video, Quiz) -> full-screen focused learning
 * 3. Two distinct environments:
 *    - Instructor Environment: Deploy materials to students, monitor student progression & filter active vs past students
 *    - Student Environment: Self-paced learning roadmap, hand signals drill & individual progression only
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AquaApp(viewModel: AquaViewModel = viewModel()) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val appStrings = getAppStrings(currentLanguage)

    CompositionLocalProvider(
        LocalAppStrings provides appStrings,
        LocalCurrentLanguage provides currentLanguage
    ) {
        val authState by viewModel.authState.collectAsState()
        val currentUser by viewModel.currentUser.collectAsState()

        val activePres by viewModel.activePresentation.collectAsState()
        val activeVid by viewModel.activeVideo.collectAsState()
        val activeQz by viewModel.activeQuiz.collectAsState()

        // 1. Auth Flow: If user is not authenticated or pending verification, display AuthScreen
        if (authState !is AuthState.Authenticated) {
            AuthScreen(viewModel = viewModel)
            return@CompositionLocalProvider
        }

        // 2. Active In-Course Learning Experiences (Full Screen with BackHandler)
        when {
            activePres != null -> {
                PresentationViewerScreen(
                    viewModel = viewModel,
                    presentation = activePres!!
                )
            }
            activeVid != null -> {
                VideoPlayerScreen(
                    viewModel = viewModel,
                    video = activeVid!!
                )
            }
            activeQz != null -> {
                QuizExamScreen(
                    viewModel = viewModel,
                    quiz = activeQz!!
                )
            }
            // 3. Environment Segregation based on Account Role
            currentUser?.role == "INSTRUCTOR" -> {
                InstructorEnvironment(viewModel = viewModel)
            }
            else -> {
                StudentEnvironment(viewModel = viewModel)
            }
        }
    }
}
