package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AquaDatabase
import com.example.data.model.CourseModuleEntity
import com.example.data.model.DiverDocumentEntity
import com.example.data.model.DiveLogEntity
import com.example.data.model.DivePushNotification
import com.example.data.model.Flashcard
import com.example.data.model.LearningVideoEntity
import com.example.data.model.PresentationDeckEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestion
import com.example.data.model.SlideItem
import com.example.data.model.StudentRosterEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProgressEntity
import com.example.data.model.VerificationEmailMessage
import com.example.data.repository.DivingRepository
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed interface AuthState {
    object LoggedOut : AuthState
    data class PendingVerification(
        val email: String,
        val fullName: String,
        val lastEmailMessage: VerificationEmailMessage? = null
    ) : AuthState
    data class Authenticated(val user: UserAccountEntity) : AuthState
}

class AquaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DivingRepository

    init {
        val database = AquaDatabase.getDatabase(application)
        repository = DivingRepository(database.divingDao())
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            val existing = repository.getInitialAccount()
            if (existing != null) {
                _currentUser.value = existing
                _authState.value = AuthState.Authenticated(existing)
            }
        }
    }

    // --- Authentication State & Management ---
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow<String?>(null)
    val authSuccess: StateFlow<String?> = _authSuccess.asStateFlow()

    private val _latestVerificationEmail = MutableStateFlow<VerificationEmailMessage?>(null)
    val latestVerificationEmail: StateFlow<VerificationEmailMessage?> = _latestVerificationEmail.asStateFlow()

    // --- Language Preference & Management ---
    private val prefs = application.getSharedPreferences("aqua_settings", Context.MODE_PRIVATE)
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("app_language", "en"))
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("app_language", language.code).apply()
    }

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == AppLanguage.EN) AppLanguage.DE else AppLanguage.EN
        setLanguage(next)
    }

    // --- Unit System & Theme Preferences ---
    private val _isImperial = MutableStateFlow(prefs.getBoolean("is_imperial", false))
    val isImperial: StateFlow<Boolean> = _isImperial.asStateFlow()

    fun setImperialEnabled(enabled: Boolean) {
        _isImperial.value = enabled
        prefs.edit().putBoolean("is_imperial", enabled).apply()
    }

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun setDarkModeEnabled(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }

    // --- Offline Module Downloads & State ---
    private val _downloadedModules = MutableStateFlow<Set<String>>(
        prefs.getStringSet("downloaded_modules", emptySet()) ?: emptySet()
    )
    val downloadedModules: StateFlow<Set<String>> = _downloadedModules.asStateFlow()

    private val _downloadingModules = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadingModules: StateFlow<Map<String, Float>> = _downloadingModules.asStateFlow()

    fun downloadModule(moduleId: String) {
        viewModelScope.launch {
            if (_downloadedModules.value.contains(moduleId) || _downloadingModules.value.containsKey(moduleId)) {
                return@launch
            }
            for (step in 1..10) {
                kotlinx.coroutines.delay(120)
                val progress = step / 10f
                _downloadingModules.value = _downloadingModules.value.toMutableMap().apply {
                    put(moduleId, progress)
                }
            }
            _downloadingModules.value = _downloadingModules.value.toMutableMap().apply {
                remove(moduleId)
            }
            val newSet = _downloadedModules.value + moduleId
            _downloadedModules.value = newSet
            prefs.edit().putStringSet("downloaded_modules", newSet).apply()
        }
    }

    fun removeDownloadedModule(moduleId: String) {
        val newSet = _downloadedModules.value - moduleId
        _downloadedModules.value = newSet
        prefs.edit().putStringSet("downloaded_modules", newSet).apply()
    }

    fun registerUser(email: String, passwordPlain: String, fullName: String, role: String = "STUDENT") {
        _authError.value = null
        _authSuccess.value = null
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _authError.value = "Please enter a valid email address."
            return
        }
        if (passwordPlain.length < 6) {
            _authError.value = "Password must be at least 6 characters long."
            return
        }

        viewModelScope.launch {
            val result = repository.registerUser(email, passwordPlain, fullName, role)
            result.fold(
                onSuccess = { (account, emailMessage) ->
                    _latestVerificationEmail.value = emailMessage
                    _authState.value = AuthState.PendingVerification(
                        email = account.email,
                        fullName = account.fullName,
                        lastEmailMessage = emailMessage
                    )
                    _authSuccess.value = "Verification email automatically sent by Adrian Scheid to ${account.email}!"
                },
                onFailure = { err ->
                    _authError.value = err.message ?: "Registration failed."
                }
            )
        }
    }

    fun quickLoginStudent() {
        loginUser("leo.chen@diver.com", "diver123")
    }

    fun quickLoginInstructor() {
        loginUser("adrian.scheid01@gmail.com", "instructor123")
    }

    fun verifyUserEmail(email: String, code: String) {
        _authError.value = null
        _authSuccess.value = null
        if (code.isBlank() || code.trim().length != 6) {
            _authError.value = "Please enter the 6-digit verification code sent to your email."
            return
        }

        viewModelScope.launch {
            val result = repository.verifyUserEmail(email, code)
            result.fold(
                onSuccess = { verifiedAccount ->
                    _currentUser.value = verifiedAccount
                    _authState.value = AuthState.Authenticated(verifiedAccount)
                    _authSuccess.value = "Registration finalised! Welcome to AquaLearn, ${verifiedAccount.fullName}."
                },
                onFailure = { err ->
                    _authError.value = err.message ?: "Verification failed."
                }
            )
        }
    }

    fun resendVerificationEmail(email: String) {
        _authError.value = null
        _authSuccess.value = null
        viewModelScope.launch {
            val result = repository.resendVerificationCode(email)
            result.fold(
                onSuccess = { newMsg ->
                    _latestVerificationEmail.value = newMsg
                    _authSuccess.value = "A new verification code was sent to ${newMsg.recipientEmail}."
                },
                onFailure = { err ->
                    _authError.value = err.message ?: "Failed to resend code."
                }
            )
        }
    }

    fun loginUser(email: String, passwordPlain: String) {
        _authError.value = null
        _authSuccess.value = null
        if (email.isBlank() || passwordPlain.isBlank()) {
            _authError.value = "Please enter your email and password."
            return
        }

        viewModelScope.launch {
            val result = repository.loginUser(email, passwordPlain)
            result.fold(
                onSuccess = { account ->
                    _currentUser.value = account
                    _authState.value = AuthState.Authenticated(account)
                },
                onFailure = { err ->
                    if (err.message == "EMAIL_NOT_VERIFIED") {
                        // User needs to verify email first
                        val resendResult = repository.resendVerificationCode(email.trim().lowercase())
                        val emailMsg = resendResult.getOrNull()
                        _latestVerificationEmail.value = emailMsg
                        _authState.value = AuthState.PendingVerification(
                            email = email.trim().lowercase(),
                            fullName = "Candidate",
                            lastEmailMessage = emailMsg
                        )
                        _authError.value = "Your email is not verified yet. We have re-sent a 6-digit verification code to your email."
                    } else {
                        _authError.value = err.message ?: "Invalid email or password."
                    }
                }
            )
        }
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.LoggedOut
        _latestVerificationEmail.value = null
        _authError.value = null
        _authSuccess.value = null
    }

    fun clearAuthMessages() {
        _authError.value = null
        _authSuccess.value = null
    }

    fun setPendingVerification(email: String, name: String) {
        _authState.value = AuthState.PendingVerification(email, name, _latestVerificationEmail.value)
    }

    fun returnToLogin() {
        _authState.value = AuthState.LoggedOut
        _authError.value = null
        _authSuccess.value = null
    }

    val allModules: StateFlow<List<CourseModuleEntity>> = repository.allModules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresentations: StateFlow<List<PresentationDeckEntity>> = repository.allPresentations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deployedPresentations: StateFlow<List<PresentationDeckEntity>> = repository.deployedPresentations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideos: StateFlow<List<LearningVideoEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deployedVideos: StateFlow<List<LearningVideoEntity>> = repository.deployedVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuizzes: StateFlow<List<QuizEntity>> = repository.allQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deployedQuizzes: StateFlow<List<QuizEntity>> = repository.deployedQuizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<StudentRosterEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeStudents: StateFlow<List<StudentRosterEntity>> = repository.activeStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pastStudents: StateFlow<List<StudentRosterEntity>> = repository.pastStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProgress: StateFlow<UserProgressEntity?> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Dive Logbook State Flows
    val allDiveLogs: StateFlow<List<DiveLogEntity>> = repository.allDiveLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPendingDives: StateFlow<List<DiveLogEntity>> = repository.allPendingDives
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Diver Documents Vault State Flows & Operations ---
    val allDocuments: StateFlow<List<DiverDocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingDocumentApprovals: StateFlow<List<DiverDocumentEntity>> = repository.pendingDocumentApprovals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDocCategoryFilter = MutableStateFlow("ALL")
    val selectedDocCategoryFilter: StateFlow<String> = _selectedDocCategoryFilter.asStateFlow()

    private val _scubaCourses = MutableStateFlow(
        listOf(
            com.example.data.model.ScubaCourse(
                id = "course_owd",
                title = "Open Water Diver",
                subtitle = "Recreational Entry-Level Certification",
                description = "Learn the fundamentals of scuba diving, including diving physics, safety procedures, confined water skill mastery, and 4 qualifying open water ocean dives.",
                depthLimit = "18m / 60ft",
                duration = "3-4 Days",
                prerequisites = "None / Swim 200m",
                isEnrolled = true,
                iconEmoji = "🤿",
                lessonsCount = 5,
                difficulty = "Beginner"
            ),
            com.example.data.model.ScubaCourse(
                id = "course_nitrox",
                title = "Enriched Air Nitrox Specialty",
                subtitle = "Most Popular Specialty Course",
                description = "Extend your bottom time and dive safer by learning how to analyze oxygen percentages, calculate oxygen toxicity, and program dive computers for nitrox gas up to 40% O2.",
                depthLimit = "Prerequisite Rating Limit",
                duration = "1 Day",
                prerequisites = "Open Water Diver",
                isEnrolled = false,
                iconEmoji = "🧪",
                lessonsCount = 3,
                difficulty = "Easy"
            ),
            com.example.data.model.ScubaCourse(
                id = "course_deep",
                title = "Deep Diver Specialty",
                subtitle = "Dive down to the recreational limit",
                description = "Learn the safety protocols, physical adjustments, narcosis checks, and dual tank redundant gas supply management needed to safely explore deeper waters down to 40 meters / 130 feet.",
                depthLimit = "40m / 130ft",
                duration = "2 Days",
                prerequisites = "Advanced Open Water",
                isEnrolled = false,
                iconEmoji = "⚓",
                lessonsCount = 4,
                difficulty = "Advanced"
            ),
            com.example.data.model.ScubaCourse(
                id = "course_wreck",
                title = "Wreck Diver Specialty",
                subtitle = "Explore historic artificial reefs",
                description = "Acquire the skills to chart sunken vessels, lay guide penetration lines, avoid kicking up silt, and safely explore wreck interiors without getting trapped.",
                depthLimit = "Prerequisite Rating Limit",
                duration = "2 Days",
                prerequisites = "Advanced Open Water",
                isEnrolled = false,
                iconEmoji = "🚢",
                lessonsCount = 4,
                difficulty = "Challenging"
            ),
            com.example.data.model.ScubaCourse(
                id = "course_night",
                title = "Night Diver Specialty",
                subtitle = "Experience dive sites under a new light",
                description = "Navigate in complete darkness, master light communications, observe nocturnal marine life patterns, and experience the thrill of pitch-black diving.",
                depthLimit = "Prerequisite Rating Limit",
                duration = "3 Nights",
                prerequisites = "Open Water Diver",
                isEnrolled = false,
                iconEmoji = "🔦",
                lessonsCount = 3,
                difficulty = "Medium"
            )
        )
    )
    val scubaCourses: StateFlow<List<com.example.data.model.ScubaCourse>> = _scubaCourses.asStateFlow()

    fun enrollInCourse(courseId: String) {
        _scubaCourses.value = _scubaCourses.value.map {
            if (it.id == courseId) it.copy(isEnrolled = true) else it
        }
    }

    private val _selectedDocStatusFilter = MutableStateFlow("ALL")
    val selectedDocStatusFilter: StateFlow<String> = _selectedDocStatusFilter.asStateFlow()

    private val _documentSearchQuery = MutableStateFlow("")
    val documentSearchQuery: StateFlow<String> = _documentSearchQuery.asStateFlow()

    private val _selectedDocumentForDetails = MutableStateFlow<DiverDocumentEntity?>(null)
    val selectedDocumentForDetails: StateFlow<DiverDocumentEntity?> = _selectedDocumentForDetails.asStateFlow()

    private val _selectedDocumentForApproval = MutableStateFlow<DiverDocumentEntity?>(null)
    val selectedDocumentForApproval: StateFlow<DiverDocumentEntity?> = _selectedDocumentForApproval.asStateFlow()

    fun selectDocumentForDetails(doc: DiverDocumentEntity?) {
        _selectedDocumentForDetails.value = doc
    }

    fun selectDocumentForApproval(doc: DiverDocumentEntity?) {
        _selectedDocumentForApproval.value = doc
    }

    fun setDocumentCategoryFilter(category: String) {
        _selectedDocCategoryFilter.value = category
    }

    fun setDocumentStatusFilter(status: String) {
        _selectedDocStatusFilter.value = status
    }

    fun setDocumentSearchQuery(query: String) {
        _documentSearchQuery.value = query
    }

    fun saveDocument(
        title: String,
        category: String,
        documentNumber: String,
        issuingOrganization: String,
        issueDate: String,
        expiryDate: String,
        notes: String,
        cardColorHex: String,
        requiredApproverType: String,
        documentUri: String? = null,
        existingId: String? = null
    ) {
        val user = _currentUser.value
        val diverId = user?.id ?: "usr_student"
        val diverName = user?.fullName ?: "Diver Candidate"
        val diverEmail = user?.email ?: "diver@example.com"
        val docId = existingId ?: "doc_${UUID.randomUUID().toString().take(8)}"

        val doc = DiverDocumentEntity(
            id = docId,
            diverId = diverId,
            diverName = diverName,
            diverEmail = diverEmail,
            title = title.trim(),
            category = category,
            documentNumber = documentNumber.trim(),
            issuingOrganization = issuingOrganization.trim(),
            issueDate = issueDate.trim(),
            expiryDate = expiryDate.trim().ifBlank { "Lifetime / No Expiration" },
            notes = notes.trim(),
            cardColorHex = cardColorHex,
            documentUri = documentUri ?: if (existingId != null) _selectedDocumentForDetails.value?.documentUri else null,
            approvalStatus = if (existingId != null) (_selectedDocumentForDetails.value?.approvalStatus ?: "PENDING") else "PENDING",
            requiredApproverType = requiredApproverType,
            approverName = _selectedDocumentForDetails.value?.approverName ?: "",
            approverTitleOrLicense = _selectedDocumentForDetails.value?.approverTitleOrLicense ?: "",
            approverOrganization = _selectedDocumentForDetails.value?.approverOrganization ?: "",
            approvalDate = _selectedDocumentForDetails.value?.approvalDate ?: "",
            approvalStampCode = _selectedDocumentForDetails.value?.approvalStampCode ?: "",
            approverSignatureJson = _selectedDocumentForDetails.value?.approverSignatureJson ?: "",
            approverNotes = _selectedDocumentForDetails.value?.approverNotes ?: ""
        )

        viewModelScope.launch {
            repository.insertDocument(doc)
            if (_selectedDocumentForDetails.value?.id == docId) {
                _selectedDocumentForDetails.value = doc
            }
        }
    }

    fun deleteDocument(id: String) {
        viewModelScope.launch {
            repository.deleteDocument(id)
            if (_selectedDocumentForDetails.value?.id == id) {
                _selectedDocumentForDetails.value = null
            }
            if (_selectedDocumentForApproval.value?.id == id) {
                _selectedDocumentForApproval.value = null
            }
        }
    }

    fun approveDocument(
        documentId: String,
        approverName: String,
        license: String,
        org: String,
        approvalDate: String,
        stampCode: String,
        signatureJson: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.approveDocument(
                id = documentId,
                approverName = approverName.trim(),
                license = license.trim(),
                org = org.trim(),
                approvalDate = approvalDate.trim(),
                stampCode = stampCode.trim(),
                signatureJson = signatureJson,
                notes = notes.trim()
            )
            val updated = repository.getDocumentByIdDirect(documentId)
            if (updated != null && _selectedDocumentForDetails.value?.id == documentId) {
                _selectedDocumentForDetails.value = updated
            }
            _selectedDocumentForApproval.value = null
        }
    }

    fun rejectDocument(documentId: String, notes: String) {
        viewModelScope.launch {
            repository.rejectDocument(documentId, notes)
            val updated = repository.getDocumentByIdDirect(documentId)
            if (updated != null && _selectedDocumentForDetails.value?.id == documentId) {
                _selectedDocumentForDetails.value = updated
            }
            _selectedDocumentForApproval.value = null
        }
    }

    private val _activePushNotification = MutableStateFlow<DivePushNotification?>(null)
    val activePushNotification: StateFlow<DivePushNotification?> = _activePushNotification.asStateFlow()

    private val _selectedDiveForDetails = MutableStateFlow<DiveLogEntity?>(null)
    val selectedDiveForDetails: StateFlow<DiveLogEntity?> = _selectedDiveForDetails.asStateFlow()

    fun selectDiveForDetails(dive: DiveLogEntity?) {
        _selectedDiveForDetails.value = dive
    }

    fun dismissPushNotification() {
        _activePushNotification.value = null
    }

    val handSignals: List<Flashcard>
        get() = repository.getHandSignals(isGerman = (_currentLanguage.value == com.example.ui.i18n.AppLanguage.DE))

    fun getHandSignals(language: com.example.ui.i18n.AppLanguage): List<Flashcard> {
        return repository.getHandSignals(isGerman = (language == com.example.ui.i18n.AppLanguage.DE))
    }

    // Mode: Student vs Instructor Studio
    private val _isInstructorMode = MutableStateFlow(false)
    val isInstructorMode: StateFlow<Boolean> = _isInstructorMode.asStateFlow()

    fun toggleInstructorMode() {
        _isInstructorMode.value = !_isInstructorMode.value
    }

    // Active Presentation Viewer
    private val _activePresentation = MutableStateFlow<PresentationDeckEntity?>(null)
    val activePresentation: StateFlow<PresentationDeckEntity?> = _activePresentation.asStateFlow()

    private val _currentSlideIndex = MutableStateFlow(0)
    val currentSlideIndex: StateFlow<Int> = _currentSlideIndex.asStateFlow()

    fun openPresentation(presentation: PresentationDeckEntity) {
        _activePresentation.value = presentation
        _currentSlideIndex.value = presentation.lastSlideRead.coerceIn(0, (presentation.totalSlides - 1).coerceAtLeast(0))
    }

    fun closePresentation() {
        val deck = _activePresentation.value
        val slide = _currentSlideIndex.value
        if (deck != null) {
            val completed = slide >= deck.totalSlides - 1
            viewModelScope.launch {
                repository.savePresentationProgress(deck.id, slide, completed || deck.isCompleted)
            }
        }
        _activePresentation.value = null
        _currentSlideIndex.value = 0
    }

    fun nextSlide() {
        val deck = _activePresentation.value ?: return
        if (_currentSlideIndex.value < deck.totalSlides - 1) {
            _currentSlideIndex.value += 1
            viewModelScope.launch {
                repository.savePresentationProgress(deck.id, _currentSlideIndex.value, _currentSlideIndex.value == deck.totalSlides - 1)
            }
        }
    }

    fun prevSlide() {
        if (_currentSlideIndex.value > 0) {
            _currentSlideIndex.value -= 1
        }
    }

    fun setSlide(index: Int) {
        val deck = _activePresentation.value ?: return
        if (index in 0 until deck.totalSlides) {
            _currentSlideIndex.value = index
        }
    }

    fun markPresentationComplete() {
        val deck = _activePresentation.value ?: return
        viewModelScope.launch {
            repository.savePresentationProgress(deck.id, deck.totalSlides - 1, true)
        }
    }

    // Active Video Player
    private val _activeVideo = MutableStateFlow<LearningVideoEntity?>(null)
    val activeVideo: StateFlow<LearningVideoEntity?> = _activeVideo.asStateFlow()

    private val _isVideoPlaying = MutableStateFlow(false)
    val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

    private val _videoCurrentSeconds = MutableStateFlow(0)
    val videoCurrentSeconds: StateFlow<Int> = _videoCurrentSeconds.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun openVideo(video: LearningVideoEntity) {
        _activeVideo.value = video
        _videoCurrentSeconds.value = video.watchedSeconds
        _isVideoPlaying.value = true
    }

    fun toggleVideoPlay() {
        _isVideoPlaying.value = !_isVideoPlaying.value
    }

    fun seekVideo(sec: Int) {
        val vid = _activeVideo.value ?: return
        _videoCurrentSeconds.value = sec.coerceIn(0, vid.durationSeconds)
    }

    fun skipVideo(deltaSec: Int) {
        val vid = _activeVideo.value ?: return
        val target = (_videoCurrentSeconds.value + deltaSec).coerceIn(0, vid.durationSeconds)
        _videoCurrentSeconds.value = target
    }

    fun cyclePlaybackSpeed() {
        _playbackSpeed.value = when (_playbackSpeed.value) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            else -> 1.0f
        }
    }

    fun closeVideo() {
        val vid = _activeVideo.value
        val cur = _videoCurrentSeconds.value
        if (vid != null) {
            val completed = cur >= vid.durationSeconds - 5
            viewModelScope.launch {
                repository.saveVideoProgress(vid.id, cur, completed || vid.isCompleted)
            }
        }
        _activeVideo.value = null
        _isVideoPlaying.value = false
        _videoCurrentSeconds.value = 0
    }

    // Active Quiz / Exam Runner
    private val _activeQuiz = MutableStateFlow<QuizEntity?>(null)
    val activeQuiz: StateFlow<QuizEntity?> = _activeQuiz.asStateFlow()

    private val _quizQuestionIndex = MutableStateFlow(0)
    val quizQuestionIndex: StateFlow<Int> = _quizQuestionIndex.asStateFlow()

    val quizUserAnswers = mutableStateMapOf<Int, Int>() // questionIndex -> optionIndex

    private val _isOptionSubmitted = MutableStateFlow(false)
    val isOptionSubmitted: StateFlow<Boolean> = _isOptionSubmitted.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    private val _finalScorePercentage = MutableStateFlow(0)
    val finalScorePercentage: StateFlow<Int> = _finalScorePercentage.asStateFlow()

    private val _isPassedQuiz = MutableStateFlow(false)
    val isPassedQuiz: StateFlow<Boolean> = _isPassedQuiz.asStateFlow()

    fun openQuiz(quiz: QuizEntity) {
        _activeQuiz.value = quiz
        _quizQuestionIndex.value = 0
        quizUserAnswers.clear()
        _isOptionSubmitted.value = false
        _quizCompleted.value = false
        _finalScorePercentage.value = 0
        _isPassedQuiz.value = false
    }

    fun selectQuizAnswer(optionIndex: Int) {
        if (!_isOptionSubmitted.value) {
            quizUserAnswers[_quizQuestionIndex.value] = optionIndex
        }
    }

    fun submitCurrentAnswer() {
        if (quizUserAnswers.containsKey(_quizQuestionIndex.value)) {
            _isOptionSubmitted.value = true
        }
    }

    fun nextQuizQuestion() {
        val quiz = _activeQuiz.value ?: return
        val questions = quiz.parseQuestions()
        if (_quizQuestionIndex.value < questions.size - 1) {
            _quizQuestionIndex.value += 1
            _isOptionSubmitted.value = quizUserAnswers.containsKey(_quizQuestionIndex.value)
        } else {
            finishQuiz(quiz, questions)
        }
    }

    private fun finishQuiz(quiz: QuizEntity, questions: List<QuizQuestion>) {
        var correctCount = 0
        questions.forEachIndexed { index, q ->
            val userPick = quizUserAnswers[index]
            if (userPick == q.correctIndex) {
                correctCount++
            }
        }
        val percentage = if (questions.isNotEmpty()) (correctCount * 100) / questions.size else 0
        val passed = percentage >= quiz.passScorePercentage

        _finalScorePercentage.value = percentage
        _isPassedQuiz.value = passed
        _quizCompleted.value = true

        viewModelScope.launch {
            repository.recordQuizAttempt(quiz.id, percentage, passed)
        }
    }

    fun recordQuizAttempt(quizId: String, scorePercentage: Int, isPassed: Boolean) {
        viewModelScope.launch {
            repository.recordQuizAttempt(quizId, scorePercentage, isPassed)
        }
    }

    fun retakeCurrentQuiz() {
        val quiz = _activeQuiz.value ?: return
        openQuiz(quiz)
    }

    fun closeQuiz() {
        _activeQuiz.value = null
        quizUserAnswers.clear()
        _quizCompleted.value = false
    }

    // Exam Readiness Score (0 - 100%)
    val examReadiness: StateFlow<Int> = combine(
        allPresentations,
        allVideos,
        allQuizzes
    ) { presList, vidList, quizList ->
        val presScore = if (presList.isNotEmpty()) {
            (presList.count { it.isCompleted }.toFloat() / presList.size) * 30f
        } else 30f

        val vidScore = if (vidList.isNotEmpty()) {
            (vidList.count { it.isCompleted }.toFloat() / vidList.size) * 30f
        } else 30f

        val quizScore = if (quizList.isNotEmpty()) {
            (quizList.count { it.isPassed }.toFloat() / quizList.size) * 40f
        } else 0f

        (presScore + vidScore + quizScore).toInt().coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    // Instructor Creation Methods
    fun createPresentation(
        moduleId: String,
        title: String,
        description: String,
        slides: List<SlideItem>,
        documentUri: String? = null
    ) {
        viewModelScope.launch {
            val jsonArray = JSONArray()
            slides.forEach { s ->
                val obj = JSONObject().apply {
                    put("slideNumber", s.slideNumber)
                    put("title", s.title)
                    put("subtitle", s.subtitle)
                    put("bulletPoints", JSONArray().apply {
                        s.bulletPoints.forEach { put(it) }
                    })
                    put("diagramType", s.diagramType)
                    put("instructorNotes", s.instructorNotes)
                    put("keyTakeaway", s.keyTakeaway)
                }
                jsonArray.put(obj)
            }

            val deck = PresentationDeckEntity(
                id = "pres_custom_${UUID.randomUUID().toString().take(8)}",
                moduleId = moduleId,
                title = title.ifBlank { "Instructor Presentation" },
                description = description.ifBlank { "Custom uploaded slide deck." },
                slidesJson = jsonArray.toString(),
                documentUri = documentUri,
                totalSlides = slides.size.coerceAtLeast(1),
                lastSlideRead = 0,
                isCompleted = false,
                isInstructorUploaded = true
            )
            repository.insertCustomPresentation(deck)
        }
    }

    fun uploadVideoLesson(
        moduleId: String,
        title: String,
        description: String,
        durationMinutes: Int,
        videoUriOrUrl: String,
        chapters: List<Pair<Int, String>>,
        takeaways: List<String>
    ) {
        viewModelScope.launch {
            val chaptersJson = JSONArray().apply {
                chapters.forEach { (sec, chTitle) ->
                    put(JSONObject().apply {
                        put("timeSec", sec)
                        put("title", chTitle)
                        put("summary", "Key demonstration point")
                    })
                }
            }

            val takeawaysJson = JSONArray().apply {
                takeaways.forEach { put(it) }
            }

            val video = LearningVideoEntity(
                id = "vid_custom_${UUID.randomUUID().toString().take(8)}",
                moduleId = moduleId,
                title = title.ifBlank { "Instructor Training Video" },
                description = description.ifBlank { "Custom dive skill video." },
                durationSeconds = (durationMinutes * 60).coerceAtLeast(60),
                videoUrlOrUri = videoUriOrUrl.ifBlank { "scuba://video/custom_${System.currentTimeMillis()}" },
                chaptersJson = chaptersJson.toString(),
                takeawaysJson = takeawaysJson.toString(),
                watchedSeconds = 0,
                isCompleted = false,
                isInstructorUploaded = true
            )
            repository.insertCustomVideo(video)
        }
    }

    fun createQuiz(
        moduleId: String,
        title: String,
        description: String,
        passPercentage: Int,
        questions: List<QuizQuestion>,
        isMockExam: Boolean = false
    ) {
        viewModelScope.launch {
            val questionsArray = JSONArray().apply {
                questions.forEach { q ->
                    put(JSONObject().apply {
                        put("id", q.id)
                        put("question", q.question)
                        put("options", JSONArray().apply {
                            q.options.forEach { put(it) }
                        })
                        put("correctIndex", q.correctIndex)
                        put("explanation", q.explanation)
                        put("safetyRule", q.safetyRule)
                        put("depthOrContext", q.depthOrContext)
                    })
                }
            }

            val quiz = QuizEntity(
                id = "quiz_custom_${UUID.randomUUID().toString().take(8)}",
                moduleId = moduleId,
                title = title.ifBlank { "Custom Dive Quiz" },
                description = description.ifBlank { "Instructor-prepared quiz." },
                passScorePercentage = passPercentage.coerceIn(50, 100),
                questionsJson = questionsArray.toString(),
                isMockExam = isMockExam,
                isInstructorUploaded = true
            )
            repository.insertCustomQuiz(quiz)
        }
    }

    fun deleteUploadedItem(id: String, type: String) {
        viewModelScope.launch {
            when (type) {
                "presentation" -> repository.deletePresentation(id)
                "video" -> repository.deleteVideo(id)
                "quiz" -> repository.deleteQuiz(id)
            }
        }
    }

    // Material Deployment Controls for Instructor
    fun togglePresentationDeployment(id: String, isDeployed: Boolean) {
        viewModelScope.launch {
            repository.setPresentationDeployment(id, isDeployed)
        }
    }

    fun toggleVideoDeployment(id: String, isDeployed: Boolean) {
        viewModelScope.launch {
            repository.setVideoDeployment(id, isDeployed)
        }
    }

    fun toggleQuizDeployment(id: String, isDeployed: Boolean) {
        viewModelScope.launch {
            repository.setQuizDeployment(id, isDeployed)
        }
    }

    // Student Roster Management for Instructor
    fun updateStudentStatus(id: String, status: String, certifiedDate: String? = null) {
        viewModelScope.launch {
            repository.updateStudentStatus(id, status, certifiedDate)
        }
    }

    fun updateStudentNotes(id: String, notes: String) {
        viewModelScope.launch {
            repository.updateStudentNotes(id, notes)
        }
    }

    fun updateStudentName(name: String) {
        viewModelScope.launch {
            repository.updateStudentName(name)
            val current = _currentUser.value
            if (current != null) {
                _currentUser.value = current.copy(fullName = name)
            }
        }
    }

    fun updateProfilePicture(uri: String?) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                repository.updateProfilePicture(user.id, uri)
                _currentUser.value = user.copy(profilePictureUri = uri)
            } else {
                repository.updateProfilePicture("usr_student", uri)
            }
        }
    }

    // --- Dive Log Operations ---

    fun createDiveLog(
        diveNumber: Int,
        date: String,
        timeIn: String,
        timeOut: String,
        durationMinutes: Int,
        siteName: String,
        location: String,
        bodyOfWater: String,
        entryType: String,
        maxDepthMeters: Float,
        avgDepthMeters: Float,
        waterTempCelsius: Float,
        airTempCelsius: Float,
        visibilityMeters: Float,
        gasType: String,
        tankType: String,
        pressureInBar: Int,
        pressureOutBar: Int,
        suitType: String,
        weightKg: Float,
        safetyStopCompleted: Boolean,
        sightings: String,
        notes: String,
        buddyName: String,
        buddyEmail: String,
        diveCenterName: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val newDive = DiveLogEntity(
                id = "dive_${UUID.randomUUID().toString().take(8)}",
                diveNumber = diveNumber,
                date = date,
                timeIn = timeIn,
                timeOut = timeOut,
                durationMinutes = durationMinutes,
                siteName = siteName,
                location = location,
                bodyOfWater = bodyOfWater,
                entryType = entryType,
                maxDepthMeters = maxDepthMeters,
                avgDepthMeters = avgDepthMeters,
                waterTempCelsius = waterTempCelsius,
                airTempCelsius = airTempCelsius,
                visibilityMeters = visibilityMeters,
                gasType = gasType,
                tankType = tankType,
                pressureInBar = pressureInBar,
                pressureOutBar = pressureOutBar,
                suitType = suitType,
                weightKg = weightKg,
                safetyStopCompleted = safetyStopCompleted,
                sightings = sightings,
                notes = notes,
                diverId = user?.id ?: "diver_default",
                diverEmail = user?.email ?: "leo.chen@diver.com",
                diverName = user?.fullName ?: "Diver",
                buddyName = buddyName,
                buddyEmail = buddyEmail,
                diveCenterName = diveCenterName,
                approvalStatus = "PENDING",
                approvalMethod = "NONE"
            )
            repository.insertDiveLog(newDive)
            _selectedDiveForDetails.value = newDive
        }
    }

    fun approveDiveWithInstructorQr(
        diveId: String,
        qrStampCode: String,
        centerName: String,
        instructorName: String,
        license: String
    ) {
        viewModelScope.launch {
            repository.approveWithInstructorStamp(
                id = diveId,
                approvedBy = if (instructorName.isNotBlank()) "$instructorName ($centerName)" else centerName,
                stampCode = qrStampCode,
                instructorName = instructorName,
                license = license
            )
            val updated = repository.getDiveLogByIdDirect(diveId)
            if (updated != null) {
                _selectedDiveForDetails.value = updated
            }
        }
    }

    fun approveDiveWithBuddySignature(
        diveId: String,
        buddyName: String,
        signatureJson: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.approveWithBuddySignature(
                id = diveId,
                buddyName = buddyName,
                signatureJson = signatureJson,
                note = note
            )
            val updated = repository.getDiveLogByIdDirect(diveId)
            if (updated != null) {
                _selectedDiveForDetails.value = updated
            }
        }
    }

    fun sendDiveToBuddyForApproval(
        diveId: String,
        buddyEmail: String,
        buddyName: String
    ) {
        viewModelScope.launch {
            repository.markPendingBuddyPushApproval(
                id = diveId,
                buddyEmail = buddyEmail,
                buddyName = buddyName
            )
            val dive = repository.getDiveLogByIdDirect(diveId)
            if (dive != null) {
                _selectedDiveForDetails.value = dive
                // Dispatch simulated push notification to the buddy
                _activePushNotification.value = DivePushNotification(
                    notificationId = UUID.randomUUID().toString(),
                    diveId = dive.id,
                    diveNumber = dive.diveNumber,
                    siteName = dive.siteName,
                    maxDepthMeters = dive.maxDepthMeters,
                    durationMinutes = dive.durationMinutes,
                    requesterName = dive.diverName.ifBlank { "Leo Chen" },
                    requesterEmail = dive.diverEmail.ifBlank { "leo.chen@diver.com" },
                    targetBuddyEmail = buddyEmail,
                    messageText = "${dive.diverName.ifBlank { "Your buddy" }} requested verification for Dive #${dive.diveNumber} at ${dive.siteName} (${dive.maxDepthMeters}m, ${dive.durationMinutes} min)."
                )
            }
        }
    }

    fun approveBuddyDiveRequest(
        diveId: String,
        buddyName: String,
        note: String
    ) {
        viewModelScope.launch {
            repository.approveWithBuddyPush(
                id = diveId,
                buddyName = buddyName,
                note = note
            )
            val updated = repository.getDiveLogByIdDirect(diveId)
            if (updated != null) {
                _selectedDiveForDetails.value = updated
            }
            if (_activePushNotification.value?.diveId == diveId) {
                _activePushNotification.value = null
            }
        }
    }

    fun deleteDiveLog(id: String) {
        viewModelScope.launch {
            repository.deleteDiveLog(id)
            if (_selectedDiveForDetails.value?.id == id) {
                _selectedDiveForDetails.value = null
            }
        }
    }

    fun triggerSimulatedPushForDemo(dive: DiveLogEntity) {
        _activePushNotification.value = DivePushNotification(
            notificationId = UUID.randomUUID().toString(),
            diveId = dive.id,
            diveNumber = dive.diveNumber,
            siteName = dive.siteName,
            maxDepthMeters = dive.maxDepthMeters,
            durationMinutes = dive.durationMinutes,
            requesterName = dive.diverName.ifBlank { "Leo Chen" },
            requesterEmail = dive.diverEmail.ifBlank { "leo.chen@diver.com" },
            targetBuddyEmail = dive.buddyEmail.ifBlank { "buddy@diver.com" },
            messageText = "🔔 Push from ${dive.diverName.ifBlank { "Your dive buddy" }}: Please review & approve Dive #${dive.diveNumber} (${dive.siteName}, ${dive.maxDepthMeters}m)."
        )
    }
}
