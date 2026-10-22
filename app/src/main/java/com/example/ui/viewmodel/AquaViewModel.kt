package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.DiveApprovalEmailMessage
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
import com.example.data.model.OrganizationEntity
import com.example.data.model.OrganizationMemberEntity
import com.example.data.model.DiveEventEntity
import com.example.data.model.DiveEventParticipantEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.RentalTransactionEntity
import com.example.data.model.OrganizationDocumentEntity
import com.example.data.model.DocumentSignatureEntity
import com.example.data.model.PresentationDeckEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestion
import com.example.data.model.SlideItem
import com.example.data.model.StudentRosterEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProgressEntity
import com.example.data.model.VerificationEmailMessage
import com.example.data.model.DiveSpot
import com.example.data.model.EmergencyInfo
import com.example.data.model.PlannedDiveDraft
import com.example.data.repository.DiveSpotsData
import com.example.data.repository.DivingRepository
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

    private val prefs = application.getSharedPreferences("aqua_settings", Context.MODE_PRIVATE)

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

    private val _passwordResetSuccess = MutableStateFlow<String?>(null)
    val passwordResetSuccess: StateFlow<String?> = _passwordResetSuccess.asStateFlow()

    private val _passwordResetError = MutableStateFlow<String?>(null)
    val passwordResetError: StateFlow<String?> = _passwordResetError.asStateFlow()

    private val _latestPasswordResetEmail = MutableStateFlow<VerificationEmailMessage?>(null)
    val latestPasswordResetEmail: StateFlow<VerificationEmailMessage?> = _latestPasswordResetEmail.asStateFlow()

    private val repository: DivingRepository

    init {
        val database = AquaDatabase.getDatabase(application)
        repository = DivingRepository(database.divingDao())
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            val savedUserId = prefs.getString("logged_in_user_id", null)
            if (!savedUserId.isNullOrBlank()) {
                val user = repository.getUserAccountById(savedUserId)
                if (user != null && user.isEmailVerified) {
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                } else {
                    prefs.edit().remove("logged_in_user_id").apply()
                    _currentUser.value = null
                    _authState.value = AuthState.LoggedOut
                }
            } else {
                // Showcase Edition v4.3: Auto-login to showcase candidate account on fresh launch
                val showcaseUser = repository.getUserAccountById("usr_leo_student")
                    ?: repository.getAccountByEmailDirect("leo.chen@diver.com")
                    ?: repository.getInitialAccount()
                if (showcaseUser != null) {
                    prefs.edit().putString("logged_in_user_id", showcaseUser.id).apply()
                    _currentUser.value = showcaseUser
                    _authState.value = AuthState.Authenticated(showcaseUser)
                } else {
                    _currentUser.value = null
                    _authState.value = AuthState.LoggedOut
                }
            }
        }
    }

    // --- Language Preference & Management ---
    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("app_language", "de"))
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
        val isDe = _currentLanguage.value == AppLanguage.DE
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _authError.value = if (isDe) "Bitte gib eine gültige E-Mail-Adresse ein." else "Please enter a valid email address."
            return
        }
        if (passwordPlain.length < 6) {
            _authError.value = if (isDe) "Das Passwort muss mindestens 6 Zeichen lang sein." else "Password must be at least 6 characters long."
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
                    _authSuccess.value = if (isDe) {
                        "Bestätigungs-E-Mail wurde automatisch an ${account.email} gesendet!"
                    } else {
                        "Verification email automatically sent by Adrian Scheid to ${account.email}!"
                    }
                },
                onFailure = { err ->
                    _authError.value = err.message ?: if (isDe) "Registrierung fehlgeschlagen." else "Registration failed."
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

    fun requestPasswordReset(email: String, onCodeDispatched: (VerificationEmailMessage) -> Unit = {}) {
        _passwordResetError.value = null
        _passwordResetSuccess.value = null
        val isDe = _currentLanguage.value == AppLanguage.DE
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _passwordResetError.value = if (isDe) "Bitte gib eine gültige E-Mail-Adresse ein." else "Please enter a valid email address."
            return
        }

        viewModelScope.launch {
            val result = repository.requestPasswordReset(cleanEmail)
            result.fold(
                onSuccess = { emailMessage ->
                    _latestPasswordResetEmail.value = emailMessage
                    _passwordResetSuccess.value = if (isDe) {
                        "Sicherheitscode wurde offiziell von Adrian Scheid an $cleanEmail gesendet!"
                    } else {
                        "Security reset code dispatched by Adrian Scheid to $cleanEmail!"
                    }
                    onCodeDispatched(emailMessage)
                },
                onFailure = { err ->
                    _passwordResetError.value = err.message ?: if (isDe) "Passwort-Zurücksetzen fehlgeschlagen." else "Password reset request failed."
                }
            )
        }
    }

    fun confirmPasswordReset(email: String, code: String, newPasswordPlain: String, onSuccess: () -> Unit = {}) {
        _passwordResetError.value = null
        _passwordResetSuccess.value = null
        val isDe = _currentLanguage.value == AppLanguage.DE
        val cleanCode = code.trim()
        if (cleanCode.length != 6 || !cleanCode.all { it.isDigit() }) {
            _passwordResetError.value = if (isDe) "Bitte gib den 6-stelligen Code ein." else "Please enter the 6-digit code."
            return
        }
        if (newPasswordPlain.length < 6) {
            _passwordResetError.value = if (isDe) "Das neue Passwort muss mindestens 6 Zeichen lang sein." else "New password must be at least 6 characters."
            return
        }

        viewModelScope.launch {
            val result = repository.resetPasswordWithCode(email.trim(), cleanCode, newPasswordPlain)
            result.fold(
                onSuccess = { account ->
                    _passwordResetSuccess.value = if (isDe) {
                        "Passwort erfolgreich geändert! Bitte logge dich ein."
                    } else {
                        "Password reset successfully! Please sign in."
                    }
                    _authSuccess.value = if (isDe) {
                        "Passwort für ${account.email} aktualisiert. Du kannst dich jetzt einloggen."
                    } else {
                        "Password for ${account.email} updated. You can now sign in."
                    }
                    onSuccess()
                },
                onFailure = { err ->
                    _passwordResetError.value = err.message ?: if (isDe) "Code oder E-Mail ungültig." else "Invalid reset code or email."
                }
            )
        }
    }

    fun clearPasswordResetState() {
        _passwordResetError.value = null
        _passwordResetSuccess.value = null
        _latestPasswordResetEmail.value = null
    }

    fun verifyUserEmail(email: String, code: String) {
        _authError.value = null
        _authSuccess.value = null
        val isDe = _currentLanguage.value == AppLanguage.DE
        if (code.isBlank() || code.trim().length != 6) {
            _authError.value = if (isDe) {
                "Bitte gib den 6-stelligen Bestätigungscode ein, der an deine E-Mail gesendet wurde."
            } else {
                "Please enter the 6-digit verification code sent to your email."
            }
            return
        }

        viewModelScope.launch {
            val result = repository.verifyUserEmail(email, code)
            result.fold(
                onSuccess = { verifiedAccount ->
                    prefs.edit().putString("logged_in_user_id", verifiedAccount.id).apply()
                    _currentUser.value = verifiedAccount
                    _authState.value = AuthState.Authenticated(verifiedAccount)
                    _authSuccess.value = if (isDe) {
                        "Registrierung abgeschlossen! Willkommen bei AquaLearn, ${verifiedAccount.fullName}."
                    } else {
                        "Registration finalised! Welcome to AquaLearn, ${verifiedAccount.fullName}."
                    }
                },
                onFailure = { err ->
                    _authError.value = err.message ?: if (isDe) "Verifizierung fehlgeschlagen." else "Verification failed."
                }
            )
        }
    }

    fun resendVerificationEmail(email: String) {
        _authError.value = null
        _authSuccess.value = null
        val isDe = _currentLanguage.value == AppLanguage.DE
        viewModelScope.launch {
            val result = repository.resendVerificationCode(email)
            result.fold(
                onSuccess = { newMsg ->
                    _latestVerificationEmail.value = newMsg
                    _authSuccess.value = if (isDe) {
                        "Ein neuer Bestätigungscode wurde an ${newMsg.recipientEmail} gesendet."
                    } else {
                        "A new verification code was sent to ${newMsg.recipientEmail}."
                    }
                },
                onFailure = { err ->
                    _authError.value = err.message ?: if (isDe) "Code konnte nicht erneut gesendet werden." else "Failed to resend code."
                }
            )
        }
    }

    fun loginUser(email: String, passwordPlain: String) {
        _authError.value = null
        _authSuccess.value = null
        val isDe = _currentLanguage.value == AppLanguage.DE
        if (email.isBlank() || passwordPlain.isBlank()) {
            _authError.value = if (isDe) "Bitte E-Mail-Adresse und Passwort eingeben." else "Please enter your email and password."
            return
        }

        viewModelScope.launch {
            val result = repository.loginUser(email, passwordPlain)
            result.fold(
                onSuccess = { account ->
                    prefs.edit().putString("logged_in_user_id", account.id).apply()
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
                        _authError.value = if (isDe) {
                            "Deine E-Mail-Adresse ist noch nicht verifiziert. Wir haben einen neuen 6-stelligen Bestätigungscode gesendet."
                        } else {
                            "Your email is not verified yet. We have re-sent a 6-digit verification code to your email."
                        }
                    } else {
                        _authError.value = err.message ?: if (isDe) "Ungültige E-Mail-Adresse oder falsches Passwort." else "Invalid email or password."
                    }
                }
            )
        }
    }

    fun logout() {
        prefs.edit().remove("logged_in_user_id").apply()
        _currentUser.value = null
        _authState.value = AuthState.LoggedOut
        _latestVerificationEmail.value = null
        _authError.value = null
        _authSuccess.value = null
        selectOrganization(null)
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

    fun switchUserRole(newRole: String) {
        viewModelScope.launch {
            val current = _currentUser.value
            val isDemoAccount = current == null || current.id == "usr_adrian_instructor" || current.id == "usr_leo_student"
            val targetUser = if (isDemoAccount) {
                val targetEmail = if (newRole == "INSTRUCTOR") "adrian.scheid01@gmail.com" else "leo.chen@diver.com"
                repository.getAccountByEmailDirect(targetEmail) ?: current?.copy(
                    role = newRole,
                    fullName = if (newRole == "INSTRUCTOR") "Adrian Scheid" else "Leo Chen",
                    email = targetEmail
                ) ?: UserAccountEntity(
                    id = if (newRole == "INSTRUCTOR") "usr_adrian_instructor" else "usr_leo_student",
                    email = targetEmail,
                    fullName = if (newRole == "INSTRUCTOR") "Adrian Scheid" else "Leo Chen",
                    role = newRole
                )
            } else {
                current.copy(role = newRole)
            }

            prefs.edit().putString("logged_in_user_id", targetUser.id).apply()
            _currentUser.value = targetUser
            _authState.value = AuthState.Authenticated(targetUser)
            if (newRole != "INSTRUCTOR") {
                selectOrganization(null)
            }
            repository.updateUserAccount(targetUser)
            repository.updateStudentName(targetUser.fullName)
        }
    }

    fun updateFullUserProfile(updated: UserAccountEntity) {
        _currentUser.value = updated
        _authState.value = AuthState.Authenticated(updated)
        prefs.edit().putString("logged_in_user_id", updated.id).apply()
        viewModelScope.launch {
            repository.updateUserAccount(updated)
            repository.updateStudentName(updated.fullName)
        }
    }

    val allModules: StateFlow<List<CourseModuleEntity>> = repository.allModules
        .map { list ->
            list.filter { it.id in listOf("mod_physics", "mod_equipment", "mod_physiology", "mod_emergencies", "mod_planning") }
                .sortedBy { it.orderIndex }
        }
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

    // --- Custom Dive Spots & Mission Planning State Flows ---
    private val _customDiveSpots = MutableStateFlow<List<DiveSpot>>(emptyList())
    val customDiveSpots: StateFlow<List<DiveSpot>> = _customDiveSpots.asStateFlow()

    val allDiveSpots: StateFlow<List<DiveSpot>> = _customDiveSpots.map { custom ->
        custom + DiveSpotsData.allDiveSpots
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DiveSpotsData.allDiveSpots)

    private val _plannedDiveDraft = MutableStateFlow<PlannedDiveDraft?>(null)
    val plannedDiveDraft: StateFlow<PlannedDiveDraft?> = _plannedDiveDraft.asStateFlow()

    // --- Multi-Tenant Organization & DiveOps State Flows ---
    val allOrganizations: StateFlow<List<OrganizationEntity>> = repository.allOrganizations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUpcomingEvents: StateFlow<List<DiveEventEntity>> = repository.getAllUpcomingEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val myDiverRegistrations: StateFlow<List<DiveEventParticipantEntity>> =
        _currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getParticipantsForDiver(user.email, user.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val myMemberships: StateFlow<List<OrganizationMemberEntity>> =
        _currentUser.flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getMembershipsForUser(user.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentOrganization = MutableStateFlow<OrganizationEntity?>(null)
    val currentOrganization: StateFlow<OrganizationEntity?> = _currentOrganization.asStateFlow()

    private val _currentOrgMembers = MutableStateFlow<List<OrganizationMemberEntity>>(emptyList())
    val currentOrgMembers: StateFlow<List<OrganizationMemberEntity>> = _currentOrgMembers.asStateFlow()

    private val _currentOrgEvents = MutableStateFlow<List<DiveEventEntity>>(emptyList())
    val currentOrgEvents: StateFlow<List<DiveEventEntity>> = _currentOrgEvents.asStateFlow()

    private val _currentOrgInventory = MutableStateFlow<List<InventoryItemEntity>>(emptyList())
    val currentOrgInventory: StateFlow<List<InventoryItemEntity>> = _currentOrgInventory.asStateFlow()

    private val _currentOrgActiveRentals = MutableStateFlow<List<RentalTransactionEntity>>(emptyList())
    val currentOrgActiveRentals: StateFlow<List<RentalTransactionEntity>> = _currentOrgActiveRentals.asStateFlow()

    private val _currentOrgDocuments = MutableStateFlow<List<OrganizationDocumentEntity>>(emptyList())
    val currentOrgDocuments: StateFlow<List<OrganizationDocumentEntity>> = _currentOrgDocuments.asStateFlow()

    private val _currentOrgSignatures = MutableStateFlow<List<DocumentSignatureEntity>>(emptyList())
    val currentOrgSignatures: StateFlow<List<DocumentSignatureEntity>> = _currentOrgSignatures.asStateFlow()

    private val _selectedEvent = MutableStateFlow<DiveEventEntity?>(null)
    val selectedEvent: StateFlow<DiveEventEntity?> = _selectedEvent.asStateFlow()

    private val _selectedEventParticipants = MutableStateFlow<List<DiveEventParticipantEntity>>(emptyList())
    val selectedEventParticipants: StateFlow<List<DiveEventParticipantEntity>> = _selectedEventParticipants.asStateFlow()

    private var orgMembersJob: kotlinx.coroutines.Job? = null
    private var orgEventsJob: kotlinx.coroutines.Job? = null
    private var orgInventoryJob: kotlinx.coroutines.Job? = null
    private var orgRentalsJob: kotlinx.coroutines.Job? = null
    private var orgDocumentsJob: kotlinx.coroutines.Job? = null
    private var orgSignaturesJob: kotlinx.coroutines.Job? = null
    private var eventParticipantsJob: kotlinx.coroutines.Job? = null

    fun selectOrganization(org: OrganizationEntity?) {
        _currentOrganization.value = org
        orgMembersJob?.cancel()
        orgEventsJob?.cancel()
        orgInventoryJob?.cancel()
        orgRentalsJob?.cancel()
        orgDocumentsJob?.cancel()
        orgSignaturesJob?.cancel()
        selectDiveEvent(null)
        if (org != null) {
            orgMembersJob = viewModelScope.launch {
                repository.getMembersForOrganization(org.id).collect { members ->
                    _currentOrgMembers.value = members
                }
            }
            orgEventsJob = viewModelScope.launch {
                repository.getAllEventsForOrganization(org.id).collect { events ->
                    _currentOrgEvents.value = events
                }
            }
            orgInventoryJob = viewModelScope.launch {
                repository.getAllInventoryForOrganization(org.id).collect { inventory ->
                    _currentOrgInventory.value = inventory
                }
            }
            orgRentalsJob = viewModelScope.launch {
                repository.getActiveRentalsForOrganization(org.id).collect { rentals ->
                    _currentOrgActiveRentals.value = rentals
                }
            }
            orgDocumentsJob = viewModelScope.launch {
                repository.getAllDocumentsForOrganization(org.id).collect { docs ->
                    _currentOrgDocuments.value = docs
                }
            }
            orgSignaturesJob = viewModelScope.launch {
                repository.getAllSignaturesForOrganization(org.id).collect { sigs ->
                    _currentOrgSignatures.value = sigs
                }
            }
        } else {
            _currentOrgMembers.value = emptyList()
            _currentOrgEvents.value = emptyList()
            _currentOrgInventory.value = emptyList()
            _currentOrgActiveRentals.value = emptyList()
            _currentOrgDocuments.value = emptyList()
            _currentOrgSignatures.value = emptyList()
        }
    }

    fun selectDiveEvent(event: DiveEventEntity?) {
        _selectedEvent.value = event
        eventParticipantsJob?.cancel()
        if (event != null) {
            eventParticipantsJob = viewModelScope.launch {
                repository.getParticipantsForEvent(event.id).collect { participants ->
                    _selectedEventParticipants.value = participants
                }
            }
        } else {
            _selectedEventParticipants.value = emptyList()
        }
    }

    fun selectOrganizationById(orgId: String?) {
        if (orgId == null) {
            selectOrganization(null)
            return
        }
        val org = allOrganizations.value.find { it.id == orgId }
        selectOrganization(org)
    }

    fun createOrganization(
        name: String,
        orgType: String,
        affiliations: String = "",
        location: String = "",
        waterBody: String = "",
        contactEmail: String = "",
        contactPhone: String = "",
        website: String = "",
        emergencyPlanJson: String = ""
    ) {
        val newOrg = OrganizationEntity(
            id = "org_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            logoUri = "",
            orgType = orgType,
            affiliations = affiliations.trim(),
            location = location.trim(),
            waterBody = waterBody.trim(),
            contactEmail = contactEmail.trim(),
            contactPhone = contactPhone.trim(),
            website = website.trim(),
            emergencyPlanJson = emergencyPlanJson.ifBlank {
                JSONObject().apply {
                    put("nearestChamber", "Lokales Druckkammerzentrum")
                    put("emergencyPhone", "112 / DAN HotLine")
                    put("gpsEvacuationPoint", "Basis Bootssteg")
                    put("o2KitLocation", "Basis Tresen")
                }.toString()
            },
            activeMembersCount = 1,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertOrganization(newOrg)
            val user = _currentUser.value
            val ownerMember = OrganizationMemberEntity(
                id = "mem_${UUID.randomUUID().toString().take(8)}",
                orgId = newOrg.id,
                userId = user?.id ?: "usr_adrian_instructor",
                fullName = user?.fullName ?: "Adrian Scheid",
                email = user?.email ?: "adrian.scheid01@gmail.com",
                phoneNumber = "",
                role = "OWNER",
                membershipNumber = if (newOrg.isClub) "V-001" else "STAFF-01",
                joinedDate = java.time.LocalDate.now().toString(),
                status = "ACTIVE",
                certLevel = "Course Director / TL3",
                totalLoggedDives = 350,
                ttuExpiryDate = "2027-12-31"
            )
            repository.insertMember(ownerMember)
            selectOrganization(newOrg)
        }
    }

    fun updateOrganizationDetails(updated: OrganizationEntity) {
        viewModelScope.launch {
            repository.updateOrganization(updated)
            if (_currentOrganization.value?.id == updated.id) {
                _currentOrganization.value = updated
            }
        }
    }

    fun seedSampleMembersForCurrentOrg() {
        val currentOrg = _currentOrganization.value ?: return
        viewModelScope.launch {
            val samples = listOf(
                OrganizationMemberEntity(
                    id = "mem_${UUID.randomUUID().toString().take(8)}",
                    orgId = currentOrg.id,
                    fullName = "Julian Sommer",
                    email = "j.sommer@ocean.de",
                    phoneNumber = "+49 171 4455667",
                    role = if (currentOrg.isClub) "INSTRUCTOR" else "DIVE_LEADER",
                    membershipNumber = if (currentOrg.isClub) "V-002" else "GUIDE-02",
                    joinedDate = "2023-04-10",
                    status = "ACTIVE",
                    certLevel = "CMAS ** / Rescue Diver",
                    totalLoggedDives = 185,
                    specialtiesJson = "[\"Nitrox\", \"Trockenanzug\", \"Tieftauchen\"]",
                    ttuExpiryDate = "2027-09-15",
                    ttuDoctorName = "Dr. med. Weber",
                    emergencyContactName = "Katja Sommer",
                    emergencyContactPhone = "+49 171 9988776",
                    insuranceProvider = "DAN Europe",
                    insuranceNumber = "DAN-DE-9912",
                    rentalPreferences = "12L DIN, BCD L",
                    notes = "Erfahrener Guide für Kaltwasser & Steilwände."
                ),
                OrganizationMemberEntity(
                    id = "mem_${UUID.randomUUID().toString().take(8)}",
                    orgId = currentOrg.id,
                    fullName = "Elena Vogel",
                    email = "elena.vogel@travel.de",
                    phoneNumber = "+49 175 1122334",
                    role = if (currentOrg.isClub) "MEMBER" else "GUEST",
                    membershipNumber = if (currentOrg.isClub) "V-015" else "GAST-104",
                    joinedDate = "2024-02-01",
                    status = "ACTIVE",
                    certLevel = "Open Water Diver (18m)",
                    totalLoggedDives = 22,
                    specialtiesJson = "[\"Nitrox\"]",
                    ttuExpiryDate = java.time.LocalDate.now().plusDays(20).toString(),
                    ttuDoctorName = "Dr. med. H. Becker",
                    emergencyContactName = "Peter Vogel",
                    emergencyContactPhone = "+49 175 5544332",
                    insuranceProvider = "AquaMed",
                    insuranceNumber = "AQM-2231",
                    rentalPreferences = "Neopren Gr. S, BCD S, 10L Flasche",
                    notes = "TTU läuft bald ab; Auffrischung vereinbart."
                ),
                OrganizationMemberEntity(
                    id = "mem_${UUID.randomUUID().toString().take(8)}",
                    orgId = currentOrg.id,
                    fullName = "Maximilian Wolf",
                    email = "max.wolf@alpen.at",
                    phoneNumber = "+43 664 123456",
                    role = if (currentOrg.isClub) "MEMBER" else "GUEST",
                    membershipNumber = if (currentOrg.isClub) "V-021" else "GAST-105",
                    joinedDate = "2024-06-15",
                    status = "ACTIVE",
                    certLevel = "Advanced Open Water Diver (AOWD)",
                    totalLoggedDives = 58,
                    specialtiesJson = "[\"Nitrox\", \"Wracktauchen\"]",
                    ttuExpiryDate = "2025-11-10",
                    ttuDoctorName = "Dr. K. Huber",
                    emergencyContactName = "Claudia Wolf",
                    emergencyContactPhone = "+43 664 654321",
                    insuranceProvider = "DiveAssure",
                    insuranceNumber = "DA-AT-5541",
                    rentalPreferences = "12L DIN, BCD XL",
                    notes = "Achtung: TTU ist abgelaufen - vor Event neu vorlegen!"
                ),
                OrganizationMemberEntity(
                    id = "mem_${UUID.randomUUID().toString().take(8)}",
                    orgId = currentOrg.id,
                    fullName = "Stefan 'Tech' Bauer",
                    email = "stefan.bauer@diveops.de",
                    phoneNumber = "+49 172 8899001",
                    role = "TECH_MANAGER",
                    membershipNumber = if (currentOrg.isClub) "V-003" else "TECH-01",
                    joinedDate = "2022-08-01",
                    status = "ACTIVE",
                    certLevel = "Trimix & Rebreather Diver",
                    totalLoggedDives = 520,
                    specialtiesJson = "[\"Gas Blender\", \"Equipment Specialist\", \"Trockenanzug\"]",
                    ttuExpiryDate = "2027-11-30",
                    ttuDoctorName = "Dr. med. T. Weber",
                    emergencyContactName = "Sabine Bauer",
                    emergencyContactPhone = "+49 172 1122998",
                    insuranceProvider = "DAN Europe",
                    insuranceNumber = "DAN-DE-1102",
                    rentalPreferences = "Doppel-12 DIN, Wing",
                    notes = "Verantwortlich für Kompressor & Flaschen-TÜV."
                )
            )
            repository.insertMembers(samples)
            val updatedOrg = currentOrg.copy(activeMembersCount = currentOrg.activeMembersCount + samples.size)
            repository.updateOrganization(updatedOrg)
            _currentOrganization.value = updatedOrg
        }
    }

    fun addOrganizationMember(
        fullName: String,
        email: String,
        role: String,
        certLevel: String = "Open Water Diver",
        phoneNumber: String = "",
        membershipNumber: String = "",
        ttuExpiryDate: String? = null,
        notes: String = ""
    ) {
        val currentOrg = _currentOrganization.value ?: return
        val newMember = OrganizationMemberEntity(
            id = "mem_${UUID.randomUUID().toString().take(8)}",
            orgId = currentOrg.id,
            userId = "",
            fullName = fullName.trim(),
            email = email.trim(),
            phoneNumber = phoneNumber.trim(),
            role = role,
            membershipNumber = membershipNumber.ifBlank { "M-${(10..99).random()}" },
            joinedDate = java.time.LocalDate.now().toString(),
            status = "ACTIVE",
            certLevel = certLevel.trim(),
            totalLoggedDives = 0,
            ttuExpiryDate = ttuExpiryDate ?: "",
            notes = notes.trim()
        )
        viewModelScope.launch {
            repository.insertMember(newMember)
            repository.updateOrganization(
                currentOrg.copy(activeMembersCount = currentOrg.activeMembersCount + 1)
            )
        }
    }

    fun updateMemberRole(memberId: String, newRole: String) {
        viewModelScope.launch {
            repository.updateMemberRole(memberId, newRole)
        }
    }

    fun updateMemberStatus(memberId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateMemberStatus(memberId, newStatus)
        }
    }

    fun updateMemberTtu(memberId: String, docId: String?, expiryDate: String?, doctorName: String?) {
        viewModelScope.launch {
            repository.updateMemberTtu(memberId, docId, expiryDate, doctorName)
        }
    }

    fun removeOrganizationMember(memberId: String) {
        val currentOrg = _currentOrganization.value ?: return
        viewModelScope.launch {
            repository.deleteMember(memberId)
            repository.updateOrganization(
                currentOrg.copy(activeMembersCount = maxOf(0, currentOrg.activeMembersCount - 1))
            )
        }
    }

    // --- DiveOps Events & Trip Management ---
    fun createDiveEvent(
        title: String,
        eventType: String,
        siteName: String,
        location: String,
        eventDate: String,
        meetingTime: String = "08:30",
        departureTime: String = "09:00",
        maxCapacity: Int = 12,
        leadGuideName: String = "",
        maxDepthMeters: Float = 20.0f,
        requiredMinCert: String = "Open Water Diver",
        emergencyBriefing: String = "",
        chamberName: String = "",
        chamberPhone: String = "",
        chamberDistanceTime: String = "",
        coastGuardVHF: String = "",
        emergencyNotes: String = "",
        entryExitInfo: String = "",
        waterConditions: String = "",
        turnPressureBar: Int = 120,
        equipmentRequirements: String = "",
        hazardsAndRules: String = "",
        briefingDocumentUri: String = ""
    ) {
        val currentOrg = _currentOrganization.value ?: return
        val newEvent = DiveEventEntity(
            id = "evt_${UUID.randomUUID().toString().take(8)}",
            orgId = currentOrg.id,
            title = title.trim(),
            eventType = eventType,
            siteName = siteName.trim(),
            location = location.trim(),
            eventDate = eventDate.trim(),
            meetingTime = meetingTime.trim(),
            departureTime = departureTime.trim(),
            maxCapacity = maxCapacity,
            registeredCount = 0,
            leadGuideName = leadGuideName.trim(),
            leadGuideUserId = _currentUser.value?.id ?: "",
            maxDepthMeters = maxDepthMeters,
            requiredMinCert = requiredMinCert.trim(),
            emergencyBriefing = emergencyBriefing.trim(),
            chamberName = chamberName.trim(),
            chamberPhone = chamberPhone.trim(),
            chamberDistanceTime = chamberDistanceTime.trim(),
            coastGuardVHF = coastGuardVHF.trim(),
            emergencyNotes = emergencyNotes.trim(),
            entryExitInfo = entryExitInfo.trim(),
            waterConditions = waterConditions.trim(),
            turnPressureBar = turnPressureBar,
            equipmentRequirements = equipmentRequirements.trim(),
            hazardsAndRules = hazardsAndRules.trim(),
            briefingDocumentUri = briefingDocumentUri.trim(),
            status = "CONFIRMED",
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertEvent(newEvent)
            selectDiveEvent(newEvent)
        }
    }

    fun updateDiveEvent(event: DiveEventEntity) {
        viewModelScope.launch {
            repository.updateEvent(event)
            if (_selectedEvent.value?.id == event.id) {
                _selectedEvent.value = event
            }
        }
    }

    fun updateEventStatus(eventId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateEventStatus(eventId, newStatus)
            if (_selectedEvent.value?.id == eventId) {
                _selectedEvent.value = _selectedEvent.value?.copy(status = newStatus)
            }
        }
    }

    fun deleteDiveEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            if (_selectedEvent.value?.id == eventId) {
                selectDiveEvent(null)
            }
        }
    }

    // --- 1-Click Check-In with Auto-Safety-Gate ---
    fun checkInDiverForEvent(
        event: DiveEventEntity,
        member: OrganizationMemberEntity,
        tankSelection: String = "12L Aluminum",
        gasType: String = "Air 21%",
        rentalGear: List<String> = emptyList(),
        transferCarPoolNote: String = ""
    ) {
        viewModelScope.launch {
            // Auto-Safety-Gate Verification:
            val issues = mutableListOf<String>()

            // 1. TTU Expiry Check on Event Day
            val rawTtu = member.ttuExpiryDate.trim()
            if (rawTtu.isBlank()) {
                issues.add("Keine TTU / ärztliches Tauchtauglichkeitsattest hinterlegt!")
            } else {
                val ttuInfo = com.example.util.DocumentExpirationHelper.computeDateExpiration(rawTtu, isGerman = true)
                if (ttuInfo.level == com.example.util.ExpirationAlertLevel.EXPIRED) {
                    issues.add("TTU ist abgelaufen ($rawTtu)!")
                } else if (rawTtu < event.eventDate) {
                    issues.add("TTU läuft vor dem Event-Tag ab ($rawTtu < ${event.eventDate})!")
                }
            }

            // 2. Brevet vs Max Depth Check
            val certLower = member.certLevel.lowercase()
            val maxAllowedDepth = when {
                certLower.contains("scuba diver") -> 12.0f
                certLower.contains("open water") || certLower.contains("cmas *") || certLower.contains("owd") -> 18.0f
                certLower.contains("advanced") || certLower.contains("cmas **") || certLower.contains("aowd") -> 30.0f
                else -> 40.0f
            }
            if (event.maxDepthMeters > maxAllowedDepth) {
                issues.add("Brevet '${member.certLevel}' (max ${maxAllowedDepth.toInt()}m) reicht nicht für geplante Maximaltiefe (${event.maxDepthMeters.toInt()}m)!")
            }

            // 3. Nitrox Gas MOD Calculation (PPO2 max = 1.4 bar)
            val calculatedMod = when (gasType) {
                "Nitrox 32%" -> 33.7f
                "Nitrox 36%" -> 28.8f
                "Nitrox 40%" -> 25.0f
                else -> 56.6f
            }
            if (event.maxDepthMeters > calculatedMod) {
                issues.add("Geplante Tiefe (${event.maxDepthMeters.toInt()}m) überschreitet MOD von $gasType (${calculatedMod.toInt()}m)!")
            }

            // 4. Mandatory Documents & Waivers Check
            val mandatoryDocs = repository.getMandatoryDocumentsDirect(event.orgId)
            for (doc in mandatoryDocs) {
                val sig = repository.getSignatureForMemberAndDocument(doc.id, member.id)
                if (sig == null) {
                    issues.add("Pflichtdokument '${doc.title}' (${doc.category}) noch nicht bestätigt/unterzeichnet!")
                }
            }

            val passed = issues.isEmpty()
            val issuesJson = JSONArray(issues).toString()

            val newParticipant = DiveEventParticipantEntity(
                id = "part_${UUID.randomUUID().toString().take(8)}",
                eventId = event.id,
                memberId = member.id,
                diverName = member.fullName,
                diverEmail = member.email,
                diverCertLevel = member.certLevel,
                diverTotalDives = member.totalLoggedDives,
                checkInStatus = if (passed) "SAFETY_CHECKED" else "REGISTERED",
                safetyGatePassed = passed,
                safetyGateIssuesJson = issuesJson,
                tankSelection = tankSelection,
                gasType = gasType,
                modMeters = calculatedMod,
                rentalGearJson = JSONArray(rentalGear).toString(),
                transferCarPoolNote = transferCarPoolNote.trim(),
                buddyTeamId = null,
                groupGuideName = null,
                tankPressureStartBar = 200,
                tankPressureEndBar = 50,
                checkInTimestamp = System.currentTimeMillis()
            )

            repository.insertParticipant(newParticipant)
            val updatedCount = event.registeredCount + 1
            repository.updateEventRegisteredCount(event.id, updatedCount)
            if (_selectedEvent.value?.id == event.id) {
                _selectedEvent.value = _selectedEvent.value?.copy(registeredCount = updatedCount)
            }
        }
    }

    fun updateParticipantCheckInStatus(participantId: String, newStatus: String) {
        viewModelScope.launch {
            repository.updateParticipantCheckInStatus(participantId, newStatus)
        }
    }

    fun assignBuddyTeam(participantId: String, buddyTeamId: String?, groupGuideName: String? = null) {
        viewModelScope.launch {
            repository.updateBuddyTeamAssignment(participantId, buddyTeamId, groupGuideName)
        }
    }

    fun recordDiverSurfaced(participantId: String, pressureOut: Int, timeOut: String) {
        viewModelScope.launch {
            repository.recordWaterEntryExit(
                id = participantId,
                timeIn = null,
                timeOut = timeOut,
                pressureOut = pressureOut,
                status = "SURFACED_OK"
            )
        }
    }

    fun recordDiverSplash(participantId: String, timeIn: String) {
        viewModelScope.launch {
            repository.recordWaterEntryExit(
                id = participantId,
                timeIn = timeIn,
                timeOut = null,
                pressureOut = 200,
                status = "IN_WATER"
            )
        }
    }

    fun removeParticipantFromEvent(participantId: String, eventId: String) {
        viewModelScope.launch {
            repository.deleteParticipant(participantId)
            val event = _selectedEvent.value
            if (event != null && event.id == eventId) {
                val newCount = maxOf(0, event.registeredCount - 1)
                repository.updateEventRegisteredCount(eventId, newCount)
                _selectedEvent.value = event.copy(registeredCount = newCount)
            }
        }
    }

    fun updateEventSiteMapImage(eventId: String, imageUri: String) {
        viewModelScope.launch {
            repository.updateEventSiteMapImage(eventId, imageUri)
            if (_selectedEvent.value?.id == eventId) {
                _selectedEvent.value = _selectedEvent.value?.copy(siteMapImageUri = imageUri)
            }
        }
    }

    fun toggleDiverOrgConnection(org: OrganizationEntity) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val existing = repository.getMemberByOrgAndUserDirect(org.id, user.id)
            if (existing != null) {
                if (existing.status == "ACTIVE") {
                    repository.deleteMember(existing.id)
                } else {
                    repository.updateMemberStatus(existing.id, "ACTIVE")
                }
            } else {
                val newMember = OrganizationMemberEntity(
                    id = "mem_${UUID.randomUUID().toString().take(8)}",
                    orgId = org.id,
                    userId = user.id,
                    fullName = user.fullName,
                    email = user.email,
                    role = "MEMBER",
                    membershipNumber = "DIV-${(100..999).random()}",
                    joinedDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                    status = "ACTIVE",
                    certLevel = "Open Water Diver",
                    totalLoggedDives = 15,
                    specialtiesJson = "[]"
                )
                repository.insertMember(newMember)
            }
        }
    }

    fun registerDiverForEvent(
        event: DiveEventEntity,
        tankSelection: String,
        gasType: String,
        rentalGear: List<String>,
        carpoolNotes: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = _currentUser.value
        val diverName = user?.fullName ?: "Diver"
        val diverEmail = user?.email ?: "diver@example.com"
        val diverId = user?.id ?: "usr_${UUID.randomUUID().toString().take(6)}"

        viewModelScope.launch {
            // Check if user is already registered for this event
            val existingParticipants = repository.getParticipantsForDiver(diverEmail, diverId)
            // Or check directly in db
            val existingDirect = repository.getParticipantByIdDirect(diverId)
            val newParticipant = DiveEventParticipantEntity(
                id = "part_${UUID.randomUUID().toString().take(8)}",
                eventId = event.id,
                memberId = diverId,
                diverName = diverName,
                diverEmail = diverEmail,
                diverCertLevel = "Open Water Diver",
                diverTotalDives = 20,
                checkInStatus = "REGISTERED",
                safetyGatePassed = true,
                safetyGateIssuesJson = "[]",
                tankSelection = tankSelection,
                gasType = gasType,
                modMeters = if (gasType.contains("32")) 33.8f else 56.0f,
                rentalGearJson = JSONArray(rentalGear).toString(),
                transferCarPoolNote = carpoolNotes.trim(),
                buddyTeamId = null,
                groupGuideName = null,
                tankPressureStartBar = 200,
                tankPressureEndBar = 50,
                checkInTimestamp = System.currentTimeMillis()
            )
            repository.insertParticipant(newParticipant)
            val updatedCount = event.registeredCount + 1
            repository.updateEventRegisteredCount(event.id, updatedCount)
            if (_selectedEvent.value?.id == event.id) {
                _selectedEvent.value = _selectedEvent.value?.copy(registeredCount = updatedCount)
            }
            onSuccess()
        }
    }

    fun cancelDiverEventRegistration(eventId: String, participantId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteParticipant(participantId)
            val event = repository.getEventByIdDirect(eventId)
            if (event != null) {
                val newCount = maxOf(0, event.registeredCount - 1)
                repository.updateEventRegisteredCount(eventId, newCount)
                if (_selectedEvent.value?.id == eventId) {
                    _selectedEvent.value = _selectedEvent.value?.copy(registeredCount = newCount)
                }
            }
            onComplete()
        }
    }

    // --- Smart Buddy Matching Auto-Suggest Engine ---
    fun autoSuggestBuddyTeams(eventId: String) {
        val participants = _selectedEventParticipants.value
        val safeParticipants = participants.filter { it.safetyGatePassed }
        if (safeParticipants.isEmpty()) return

        val sorted = safeParticipants.sortedByDescending { it.diverTotalDives }
        val teamNames = listOf("Team Alpha", "Team Bravo", "Team Charlie", "Team Delta", "Team Echo", "Team Foxtrot")

        viewModelScope.launch {
            var teamIndex = 0
            var left = 0
            var right = sorted.size - 1

            while (left <= right && teamIndex < teamNames.size) {
                val currentTeam = teamNames[teamIndex]
                if (left == right) {
                    repository.updateBuddyTeamAssignment(sorted[left].id, currentTeam, null)
                    left++
                } else {
                    repository.updateBuddyTeamAssignment(sorted[left].id, currentTeam, null)
                    repository.updateBuddyTeamAssignment(sorted[right].id, currentTeam, null)
                    left++
                    right--
                }
                teamIndex++
            }
        }
    }

    // --- Asset & Tech: Equipment, TÜV, Compressor & Rentals ---
    fun addInventoryItem(
        itemType: String,
        internalCode: String,
        brand: String,
        model: String,
        sizeOrSpec: String,
        serialNumber: String = "",
        nextServiceDate: String = "",
        serviceNotes: String = ""
    ) {
        val org = _currentOrganization.value ?: return
        val isExpired = nextServiceDate.isNotBlank() && nextServiceDate < java.time.LocalDate.now().toString()
        val initialStatus = if (isExpired) "LOCKED_EXPIRED" else "AVAILABLE"

        val newItem = InventoryItemEntity(
            id = "inv_${UUID.randomUUID().toString().take(8)}",
            orgId = org.id,
            itemType = itemType,
            internalCode = internalCode.trim(),
            brand = brand.trim(),
            model = model.trim(),
            serialNumber = serialNumber.trim(),
            sizeOrSpec = sizeOrSpec.trim(),
            status = initialStatus,
            nextServiceDate = nextServiceDate.trim(),
            lastServiceDate = java.time.LocalDate.now().toString(),
            serviceNotes = serviceNotes.trim(),
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insertInventoryItem(newItem)
        }
    }

    fun updateInventoryItem(item: InventoryItemEntity) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
        }
    }

    fun deleteInventoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteInventoryItem(id)
        }
    }

    fun checkOutRentalItem(
        item: InventoryItemEntity,
        member: OrganizationMemberEntity,
        dueDate: String,
        notes: String = ""
    ) {
        val org = _currentOrganization.value ?: return
        val today = java.time.LocalDate.now().toString()

        val rental = RentalTransactionEntity(
            id = "rent_${UUID.randomUUID().toString().take(8)}",
            orgId = org.id,
            itemId = item.id,
            itemCode = item.internalCode,
            itemDescription = "${item.brand} ${item.model} (${item.sizeOrSpec})",
            memberId = member.id,
            renterName = member.fullName,
            checkOutDate = today,
            dueDate = dueDate.trim(),
            status = "ACTIVE",
            returnCondition = "INTACT",
            damageReportNotes = notes.trim(),
            staffUserId = _currentUser.value?.id ?: "usr_staff",
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insertRentalTransaction(rental)
            repository.updateInventoryRentalAssignment(
                id = item.id,
                status = "RENTED",
                renterId = member.id,
                renterName = member.fullName,
                dueDate = dueDate.trim()
            )
        }
    }

    fun checkInRentalItem(
        item: InventoryItemEntity,
        rentalId: String?,
        returnCondition: String,
        damageNotes: String
    ) {
        val today = java.time.LocalDate.now().toString()
        val newStatus = if (returnCondition == "DAMAGED") "MAINTENANCE" else "AVAILABLE"

        viewModelScope.launch {
            if (rentalId != null) {
                repository.completeRentalReturn(
                    id = rentalId,
                    returnDate = today,
                    status = if (returnCondition == "DAMAGED") "DAMAGED" else "RETURNED",
                    condition = returnCondition,
                    damageNotes = damageNotes.trim()
                )
            }
            repository.updateInventoryRentalAssignment(
                id = item.id,
                status = newStatus,
                renterId = null,
                renterName = null,
                dueDate = null
            )
        }
    }

    fun updateInventoryServiceTuv(
        itemId: String,
        nextDate: String,
        lastDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.updateInventoryService(
                id = itemId,
                nextDate = nextDate.trim(),
                lastDate = lastDate.trim(),
                notes = notes.trim()
            )
        }
    }

    fun updateCompressorTelemetry(
        itemId: String,
        addedHours: Float,
        resetFilter: Boolean,
        airAnalysisDate: String
    ) {
        val item = _currentOrgInventory.value.find { it.id == itemId } ?: return
        val newHours = item.compressorHours + addedHours
        val newFilterHours = if (resetFilter) 0f else (item.compressorFilterCurrentHours + addedHours)
        val newAirDate = airAnalysisDate.ifBlank { item.airAnalysisDate }

        viewModelScope.launch {
            repository.updateCompressorTelemetry(
                id = itemId,
                hours = newHours,
                filterHours = newFilterHours,
                airAnalysisDate = newAirDate
            )
        }
    }

    // --- Modul 4: Organization Document Vault & Signatures ---
    fun uploadOrganizationDocument(
        title: String,
        category: String,
        visibility: String,
        requiresAcknowledgment: Boolean,
        contentSummary: String,
        fileUri: String = "",
        fileName: String = "",
        fileSizeBytes: Long = 0
    ) {
        val org = _currentOrganization.value ?: return
        val newDoc = OrganizationDocumentEntity(
            id = "doc_${UUID.randomUUID().toString().take(8)}",
            orgId = org.id,
            title = title.trim(),
            category = category,
            visibility = visibility,
            fileUri = fileUri,
            fileName = fileName.ifBlank { "${title.trim().lowercase().replace(' ', '_')}.pdf" },
            fileSizeBytes = if (fileSizeBytes > 0) fileSizeBytes else 245000L,
            mimeType = "application/pdf",
            requiresAcknowledgment = requiresAcknowledgment,
            uploadedByUserId = _currentOrgMembers.value.firstOrNull { it.role == "OWNER" || it.role == "ADMIN" }?.id ?: "admin",
            uploadedByName = _currentOrgMembers.value.firstOrNull { it.role == "OWNER" || it.role == "ADMIN" }?.fullName ?: "Administrator",
            documentContentSummary = contentSummary.trim(),
            createdAt = System.currentTimeMillis(),
            acknowledgmentCount = 0
        )
        viewModelScope.launch {
            repository.insertOrganizationDocument(newDoc)
        }
    }

    fun signDocument(
        documentId: String,
        memberId: String,
        signerName: String,
        signerEmail: String,
        signatureType: String = "CHECKBOX_CONFIRMED",
        touchPathPointsJson: String? = null,
        deviceVerificationNote: String = "Mobile Verified (Aqua App)"
    ) {
        val org = _currentOrganization.value ?: return
        viewModelScope.launch {
            val doc = repository.getOrganizationDocumentByIdDirect(documentId) ?: return@launch
            val existing = repository.getSignatureForMemberAndDocument(documentId, memberId)
            val newSig = DocumentSignatureEntity(
                id = existing?.id ?: "sig_${UUID.randomUUID().toString().take(8)}",
                orgId = org.id,
                documentId = documentId,
                documentTitle = doc.title,
                memberId = memberId,
                signerName = signerName,
                signerEmail = signerEmail,
                signedTimestamp = System.currentTimeMillis(),
                signatureType = signatureType,
                signatureDataJson = touchPathPointsJson ?: "",
                ipOrDeviceNote = deviceVerificationNote
            )
            repository.insertDocumentSignature(newSig)
            if (existing == null) {
                repository.updateDocumentAcknowledgmentCount(documentId, doc.acknowledgmentCount + 1)
            }
        }
    }

    fun deleteOrganizationDocument(documentId: String) {
        viewModelScope.launch {
            repository.deleteOrganizationDocument(documentId)
        }
    }

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
                subtitle = "Einstiegs-Zertifizierung für Sporttaucher",
                description = "Lerne die Grundlagen des Gerätetauchens: Tauchphysik, Notfallprozeduren, Fertigkeiten im Pool und 4 Freiwasser-Tauchgänge.",
                depthLimit = "18m",
                duration = "3-4 Tage",
                prerequisites = "Keine / 200m Schwimmen",
                isEnrolled = true,
                iconEmoji = "🤿",
                lessonsCount = 5,
                difficulty = "Einsteiger"
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

    fun markPresentationProgress(presentationId: String, isCompleted: Boolean = true) {
        viewModelScope.launch {
            repository.savePresentationProgress(presentationId, 0, isCompleted)
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
        _finalScorePercentage.value = scorePercentage
        _isPassedQuiz.value = isPassed
        _quizCompleted.value = true
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

    // Exam Readiness Score (0 - 100%) dynamically scaling 33%, 66%, 100%
    val examReadiness: StateFlow<Int> = combine(
        allPresentations,
        allVideos,
        allQuizzes
    ) { presList, vidList, quizList ->
        val owdQuizzes = quizList.filter { 
            it.moduleId in listOf("mod_physics", "mod_equipment", "mod_physiology", "mod_emergencies", "mod_planning") || it.isMockExam 
        }
        val targetQuizzes = if (owdQuizzes.isNotEmpty()) owdQuizzes else quizList
        if (targetQuizzes.isNotEmpty()) {
            val passedCount = targetQuizzes.count { it.isPassed }
            val compPres = presList.count { it.isCompleted }
            val compVids = vidList.count { it.isCompleted }
            // Comprehensive readiness: 50% Quizzes, 30% Presentations, 20% Videos
            val quizPct = (passedCount.toFloat() / targetQuizzes.size.coerceAtLeast(1)) * 50f
            val presPct = (compPres.toFloat() / presList.size.coerceAtLeast(1)) * 30f
            val vidPct = (compVids.toFloat() / vidList.size.coerceAtLeast(1)) * 20f
            (quizPct + presPct + vidPct).toInt().coerceIn(0, 100)
        } else {
            0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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
                title = title.ifBlank { if (_currentLanguage.value == AppLanguage.DE) "Tauchlehrer-Präsentation" else "Instructor Presentation" },
                description = description.ifBlank { if (_currentLanguage.value == AppLanguage.DE) "Hochgeladene Präsentationsfolien." else "Custom uploaded slide deck." },
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
                title = title.ifBlank { if (_currentLanguage.value == AppLanguage.DE) "Tauchlehrer-Lehrvideo" else "Instructor Training Video" },
                description = description.ifBlank { if (_currentLanguage.value == AppLanguage.DE) "Praxisnahes Tauchfertigkeiten-Video." else "Custom dive skill video." },
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
                title = title.ifBlank { if (_currentLanguage.value == AppLanguage.DE) "Tauchquiz" else "Custom Dive Quiz" },
                description = description.ifBlank { if (_currentLanguage.value == AppLanguage.DE) "Vom Tauchlehrer erstelltes Prüfungsquiz." else "Instructor-prepared quiz." },
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
                val updated = current.copy(fullName = name)
                _currentUser.value = updated
                _authState.value = AuthState.Authenticated(updated)
                repository.updateUserAccount(updated)
            }
        }
    }

    fun updateProfilePicture(uri: String?) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                val updated = user.copy(profilePictureUri = uri)
                _currentUser.value = updated
                _authState.value = AuthState.Authenticated(updated)
                repository.updateProfilePicture(user.id, uri)
                repository.updateUserAccount(updated)
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

    fun addCustomDiveSpot(
        name: String,
        region: String,
        country: String,
        minDepthMeters: Int,
        maxDepthMeters: Int,
        difficulty: String,
        entryType: String,
        waterType: String,
        notes: String,
        emergencyContact: String,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) {
        val newSpot = DiveSpot(
            id = "custom_spot_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            region = region.trim(),
            country = country.trim(),
            latitude = latitude,
            longitude = longitude,
            minDepthMeters = minDepthMeters,
            maxDepthMeters = maxDepthMeters,
            difficulty = difficulty.uppercase(),
            spotType = if (waterType.equals("FRESH", ignoreCase = true)) "LAKE" else "REEF",
            entryType = entryType.uppercase(),
            waterType = waterType.uppercase(),
            avgVisibilityMeters = 20,
            currentStrength = "MILD",
            highlights = if (notes.isNotBlank()) listOf(notes.take(40)) else listOf("Custom Diver Site", "Explorer Spot"),
            description = notes.ifBlank { "Custom dive site registered by diver." },
            routeAdvice = "Follow pre-dive briefing and buddy team safety plan.",
            bestSeason = "All Year Round",
            emergencyInfo = EmergencyInfo(
                nearestChamberName = "Local Hyperbaric Chamber / Hospital",
                chamberLocation = region.ifBlank { country },
                chamberDistanceKm = 15.0,
                chamberPhone = emergencyContact.ifBlank { "112 / 911" },
                coastGuardVHF = "VHF Ch 16",
                localEmergencyNumber = emergencyContact.ifBlank { "112" },
                danHotline = "+39-06-695-4000 (DAN Europe) / +1-919-684-9111",
                localHospitalName = "Nearest Regional Emergency Hospital",
                localHospitalPhone = emergencyContact.ifBlank { "112" },
                emergencyNotes = "Emergency contact: ${emergencyContact.ifBlank { "Local Dispatch" }}. 100% O2 rescue protocol.",
                oxygenOnSite = true
            ),
            isCustom = true
        )
        _customDiveSpots.value = listOf(newSpot) + _customDiveSpots.value
    }

    fun addCustomDiveSpot(spot: DiveSpot) {
        _customDiveSpots.value = listOf(spot.copy(isCustom = true)) + _customDiveSpots.value
    }

    fun deleteCustomDiveSpot(spotId: String) {
        _customDiveSpots.value = _customDiveSpots.value.filter { it.id != spotId }
    }

    fun setPlannedDiveDraft(draft: PlannedDiveDraft?) {
        _plannedDiveDraft.value = draft
    }

    fun savePlannedDiveAsLog(
        draft: PlannedDiveDraft,
        siteName: String,
        location: String,
        notes: String
    ) {
        val nextNumber = (allDiveLogs.value.maxOfOrNull { it.diveNumber } ?: 0) + 1
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        createDiveLog(
            diveNumber = nextNumber,
            date = todayDate,
            timeIn = "10:00",
            timeOut = "10:${String.format("%02d", draft.plannedBottomTimeMin.coerceIn(0, 59))}",
            durationMinutes = draft.plannedBottomTimeMin,
            siteName = siteName.ifBlank { draft.siteName },
            location = location.ifBlank { draft.location },
            bodyOfWater = "Ocean",
            entryType = "Boat",
            maxDepthMeters = draft.maxDepthMeters,
            avgDepthMeters = (draft.maxDepthMeters * 0.65f),
            waterTempCelsius = 24.0f,
            airTempCelsius = 28.0f,
            visibilityMeters = 20.0f,
            gasType = draft.gasType,
            tankType = "12L Aluminum",
            pressureInBar = draft.tankStartPressureBar,
            pressureOutBar = 50,
            suitType = "5mm Full Suit",
            weightKg = 6.0f,
            safetyStopCompleted = true,
            sightings = "",
            notes = if (notes.isNotBlank()) notes else "Planned Dive from Gas Calculator. Max Depth: ${draft.maxDepthMeters}m, Gas Mix: ${draft.gasType}, Turn Pressure: ${draft.turnPressureBar} bar.",
            buddyName = "",
            buddyEmail = "",
            diveCenterName = ""
        )
        _plannedDiveDraft.value = null
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

    // --- Dive Approval via Email (Target: adrian.scheid01@gmail.com) ---
    private val _activeEmailVerification = MutableStateFlow<DiveApprovalEmailMessage?>(null)
    val activeEmailVerification: StateFlow<DiveApprovalEmailMessage?> = _activeEmailVerification.asStateFlow()

    fun sendDiveForEmailApproval(
        context: Context?,
        diveId: String,
        recipientEmail: String = "adrian.scheid01@gmail.com",
        recipientName: String = "Adrian Scheid",
        userNote: String = ""
    ) {
        viewModelScope.launch {
            repository.markPendingEmailApproval(
                id = diveId,
                buddyEmail = recipientEmail,
                buddyName = recipientName
            )
            val dive = repository.getDiveLogByIdDirect(diveId) ?: return@launch
            _selectedDiveForDetails.value = dive

            val randomCode = (100000 + (Math.random() * 900000).toInt()).toString()
            val verifCode = "AQUA-EMAIL-OWD-$randomCode"
            val subject = "[AquaLearn Pro] Tauchgang #${dive.diveNumber} Bestätigung: ${dive.siteName}"

            val plainBody = buildString {
                appendLine("Hallo $recipientName,")
                appendLine()
                appendLine("Tauchschüler ${dive.diverName.ifBlank { "Leo Chen" }} (${dive.diverEmail.ifBlank { "leo.chen@diver.com" }}) bittet um deine offizielle Tauchgangsbestätigung:")
                appendLine()
                appendLine("══════════════════════════════════════════════════")
                appendLine("TAUCHGANG #${dive.diveNumber} - DETAILS & TELEMETRIE")
                appendLine("══════════════════════════════════════════════════")
                appendLine("• Datum: ${dive.date} • Uhrzeit: ${dive.timeIn}")
                appendLine("• Tauchplatz: ${dive.siteName}")
                appendLine("• Ort & Gewässer: ${dive.location} (${dive.bodyOfWater})")
                appendLine("• Einstieg: ${dive.entryType}")
                appendLine("• Maximaltiefe: ${dive.maxDepthMeters} m (Ø ${dive.avgDepthMeters} m)")
                appendLine("• Tauchzeit: ${dive.durationMinutes} min")
                appendLine("• Wassertemperatur: ${dive.waterTempCelsius} °C (Luft: ${dive.airTempCelsius} °C)")
                appendLine("• Sichtweite: ${dive.visibilityMeters} m")
                appendLine("• Gas & Flasche: ${dive.gasType} (${dive.tankType})")
                appendLine("• Druck: Start ${dive.pressureInBar} bar ➔ Ende ${dive.pressureOutBar} bar (Verbrauch: ${dive.pressureInBar - dive.pressureOutBar} bar)")
                appendLine("• Ausrüstung: ${dive.suitType} • Blei: ${dive.weightKg} kg")
                appendLine("• Sicherheitsstopp: ${dive.safetyStopMinutes} min auf ${dive.safetyStopDepthMeters} m (Erfolgreich absolviert ✓)")
                if (dive.sightings.isNotBlank()) appendLine("• Sichtungen: ${dive.sightings}")
                if (dive.notes.isNotBlank()) appendLine("• Notizen: ${dive.notes}")
                if (userNote.isNotBlank()) appendLine("• Nachricht des Tauchers: $userNote")
                appendLine()
                appendLine("══════════════════════════════════════════════════")
                appendLine("OFFIZIELLER FREIGABE-CODE: $verifCode")
                appendLine("══════════════════════════════════════════════════")
                appendLine("Bestätigung in der AquaLearn Pro App:")
                appendLine("Öffne das Logbuch und tippe auf 'Per E-Mail bestätigen' oder nutze diesen Code.")
                appendLine()
                appendLine("Mit sportlichen Grüßen,")
                appendLine("AquaLearn Pro Scuba Diving Academy")
            }

            val emailMsg = DiveApprovalEmailMessage(
                diveId = dive.id,
                diveNumber = dive.diveNumber,
                siteName = dive.siteName,
                diverName = dive.diverName.ifBlank { "Leo Chen" },
                diverEmail = dive.diverEmail.ifBlank { "leo.chen@diver.com" },
                recipientEmail = recipientEmail,
                recipientName = recipientName,
                subject = subject,
                verificationCode = verifCode,
                telemetrySummary = "Max ${dive.maxDepthMeters}m • ${dive.durationMinutes} min • ${dive.gasType}",
                emailBodyText = plainBody
            )

            _activeEmailVerification.value = emailMsg

            // Trigger real Android mail intent if context available
            context?.let { ctx ->
                try {
                    val mailUri = Uri.parse("mailto:$recipientEmail" +
                            "?subject=" + Uri.encode(subject) +
                            "&body=" + Uri.encode(plainBody))
                    val intent = Intent(Intent.ACTION_SENDTO, mailUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val chooser = Intent.createChooser(intent, "Bestätigungs-E-Mail senden an $recipientEmail...")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(chooser)
                } catch (_: Exception) {
                    // Fallback to in-app simulated mail if no email app installed
                }
            }
        }
    }

    fun approveDiveViaEmail(
        diveId: String,
        approverName: String = "Adrian Scheid",
        approverEmail: String = "adrian.scheid01@gmail.com",
        note: String = "Per E-Mail verifizierter Tauchgang. Profil, Tauchzeit und Sicherheitsstopp bestätigt."
    ) {
        viewModelScope.launch {
            val stampCode = "AQUALEARN-EMAIL-VERIF-" + UUID.randomUUID().toString().take(8).uppercase()
            repository.approveWithEmail(
                id = diveId,
                approvedBy = "$approverName ($approverEmail)",
                stampCode = stampCode,
                note = note
            )
            val updated = repository.getDiveLogByIdDirect(diveId)
            if (updated != null) {
                _selectedDiveForDetails.value = updated
            }
            if (_activeEmailVerification.value?.diveId == diveId) {
                _activeEmailVerification.value = null
            }
        }
    }

    fun dismissEmailVerification() {
        _activeEmailVerification.value = null
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
        val isDe = _currentLanguage.value == AppLanguage.DE
        val requester = dive.diverName.ifBlank { if (isDe) "Dein Tauchpartner" else "Your dive buddy" }
        val pushMsg = if (isDe) {
            "🔔 Benachrichtigung von $requester: Bitte prüfe und bestätige Tauchgang #${dive.diveNumber} (${dive.siteName}, ${dive.maxDepthMeters}m)."
        } else {
            "🔔 Push from $requester: Please review & approve Dive #${dive.diveNumber} (${dive.siteName}, ${dive.maxDepthMeters}m)."
        }

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
            messageText = pushMsg
        )
    }
}
