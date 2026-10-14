package com.example.data.repository

import com.example.data.dao.DivingDao
import com.example.data.model.CourseModuleEntity
import com.example.data.model.DiverDocumentEntity
import com.example.data.model.DiveLogEntity
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
import com.example.data.model.StudentRosterEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProgressEntity
import com.example.data.model.VerificationEmailMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONArray
import org.json.JSONObject

class DivingRepository(private val divingDao: DivingDao) {

    val allModules: Flow<List<CourseModuleEntity>> = divingDao.getAllModules()
    val allPresentations: Flow<List<PresentationDeckEntity>> = divingDao.getAllPresentations()
    val deployedPresentations: Flow<List<PresentationDeckEntity>> = divingDao.getDeployedPresentations()

    val allVideos: Flow<List<LearningVideoEntity>> = divingDao.getAllVideos()
    val deployedVideos: Flow<List<LearningVideoEntity>> = divingDao.getDeployedVideos()

    val allQuizzes: Flow<List<QuizEntity>> = divingDao.getAllQuizzes()
    val deployedQuizzes: Flow<List<QuizEntity>> = divingDao.getDeployedQuizzes()

    val userProgress: Flow<UserProgressEntity?> = divingDao.getUserProgress()

    // Student Roster Tracking for Instructor
    val allStudents: Flow<List<StudentRosterEntity>> = divingDao.getAllStudents()
    val activeStudents: Flow<List<StudentRosterEntity>> = divingDao.getActiveStudents()
    val pastStudents: Flow<List<StudentRosterEntity>> = divingDao.getPastStudents()

    // Multi-tenant Organizations & DiveOps Flows
    val allOrganizations: Flow<List<OrganizationEntity>> = divingDao.getAllOrganizations()

    fun getOrganizationById(id: String): Flow<OrganizationEntity?> =
        divingDao.getOrganizationById(id)

    suspend fun getOrganizationByIdDirect(id: String): OrganizationEntity? =
        divingDao.getOrganizationByIdDirect(id)

    suspend fun insertOrganization(organization: OrganizationEntity) {
        divingDao.insertOrganization(organization)
    }

    suspend fun updateOrganization(organization: OrganizationEntity) {
        divingDao.updateOrganization(organization)
    }

    suspend fun deleteOrganization(id: String) {
        divingDao.deleteOrganization(id)
    }

    fun getMembersForOrganization(orgId: String): Flow<List<OrganizationMemberEntity>> =
        divingDao.getMembersForOrganization(orgId)

    fun getActiveMembersForOrganization(orgId: String): Flow<List<OrganizationMemberEntity>> =
        divingDao.getActiveMembersForOrganization(orgId)

    fun getMemberById(id: String): Flow<OrganizationMemberEntity?> =
        divingDao.getMemberById(id)

    suspend fun getMemberByIdDirect(id: String): OrganizationMemberEntity? =
        divingDao.getMemberByIdDirect(id)

    suspend fun getMemberByOrgAndUserDirect(orgId: String, userId: String): OrganizationMemberEntity? =
        divingDao.getMemberByOrgAndUserDirect(orgId, userId)

    fun getMembershipsForUser(userId: String): Flow<List<OrganizationMemberEntity>> =
        divingDao.getMembershipsForUser(userId)

    suspend fun insertMember(member: OrganizationMemberEntity) {
        divingDao.insertMember(member)
    }

    suspend fun insertMembers(members: List<OrganizationMemberEntity>) {
        divingDao.insertMembers(members)
    }

    suspend fun updateMember(member: OrganizationMemberEntity) {
        divingDao.updateMember(member)
    }

    suspend fun updateMemberRole(id: String, role: String) {
        divingDao.updateMemberRole(id, role)
    }

    suspend fun updateMemberStatus(id: String, status: String) {
        divingDao.updateMemberStatus(id, status)
    }

    suspend fun updateMemberTtu(id: String, docId: String?, expiryDate: String?, doctorName: String?) {
        divingDao.updateMemberTtu(id, docId, expiryDate, doctorName)
    }

    suspend fun deleteMember(id: String) {
        divingDao.deleteMember(id)
    }

    // --- DiveOps Events & Trips Flows ---
    fun getAllEventsForOrganization(orgId: String): Flow<List<DiveEventEntity>> =
        divingDao.getAllEventsForOrganization(orgId)

    fun getUpcomingEventsForOrganization(orgId: String): Flow<List<DiveEventEntity>> =
        divingDao.getUpcomingEventsForOrganization(orgId)

    fun getEventById(id: String): Flow<DiveEventEntity?> =
        divingDao.getEventById(id)

    suspend fun getEventByIdDirect(id: String): DiveEventEntity? =
        divingDao.getEventByIdDirect(id)

    suspend fun insertEvent(event: DiveEventEntity) {
        divingDao.insertEvent(event)
    }

    suspend fun updateEvent(event: DiveEventEntity) {
        divingDao.updateEvent(event)
    }

    suspend fun updateEventStatus(id: String, status: String) {
        divingDao.updateEventStatus(id, status)
    }

    suspend fun updateEventRegisteredCount(id: String, count: Int) {
        divingDao.updateEventRegisteredCount(id, count)
    }

    suspend fun deleteEvent(id: String) {
        divingDao.deleteParticipantsForEvent(id)
        divingDao.deleteEvent(id)
    }

    // --- DiveOps Participants & Manifest Flows ---
    fun getParticipantsForEvent(eventId: String): Flow<List<DiveEventParticipantEntity>> =
        divingDao.getParticipantsForEvent(eventId)

    fun getParticipantById(id: String): Flow<DiveEventParticipantEntity?> =
        divingDao.getParticipantById(id)

    suspend fun getParticipantByIdDirect(id: String): DiveEventParticipantEntity? =
        divingDao.getParticipantByIdDirect(id)

    suspend fun insertParticipant(participant: DiveEventParticipantEntity) {
        divingDao.insertParticipant(participant)
    }

    suspend fun updateParticipant(participant: DiveEventParticipantEntity) {
        divingDao.updateParticipant(participant)
    }

    suspend fun updateParticipantCheckInStatus(id: String, status: String) {
        divingDao.updateParticipantCheckInStatus(id, status)
    }

    suspend fun updateBuddyTeamAssignment(id: String, buddyTeamId: String?, groupGuideName: String?) {
        divingDao.updateBuddyTeamAssignment(id, buddyTeamId, groupGuideName)
    }

    suspend fun recordWaterEntryExit(id: String, timeIn: String?, timeOut: String?, pressureOut: Int, status: String) {
        divingDao.recordWaterEntryExit(id, timeIn, timeOut, pressureOut, status)
    }

    suspend fun deleteParticipant(id: String) {
        divingDao.deleteParticipant(id)
    }

    // --- Asset & Tech Inventory Flows & Operations ---
    fun getAllInventoryForOrganization(orgId: String): Flow<List<InventoryItemEntity>> =
        divingDao.getAllInventoryForOrganization(orgId)

    fun getInventoryByType(orgId: String, itemType: String): Flow<List<InventoryItemEntity>> =
        divingDao.getInventoryByType(orgId, itemType)

    fun getInventoryItemById(id: String): Flow<InventoryItemEntity?> =
        divingDao.getInventoryItemById(id)

    suspend fun getInventoryItemByIdDirect(id: String): InventoryItemEntity? =
        divingDao.getInventoryItemByIdDirect(id)

    suspend fun insertInventoryItem(item: InventoryItemEntity) {
        divingDao.insertInventoryItem(item)
    }

    suspend fun insertInventoryItems(items: List<InventoryItemEntity>) {
        divingDao.insertInventoryItems(items)
    }

    suspend fun updateInventoryItem(item: InventoryItemEntity) {
        divingDao.updateInventoryItem(item)
    }

    suspend fun updateInventoryStatus(id: String, status: String) {
        divingDao.updateInventoryStatus(id, status)
    }

    suspend fun updateInventoryService(id: String, nextDate: String, lastDate: String, notes: String) {
        divingDao.updateInventoryService(id, nextDate, lastDate, notes)
    }

    suspend fun updateCompressorTelemetry(id: String, hours: Float, filterHours: Float, airAnalysisDate: String) {
        divingDao.updateCompressorTelemetry(id, hours, filterHours, airAnalysisDate)
    }

    suspend fun updateInventoryRentalAssignment(id: String, status: String, renterId: String?, renterName: String?, dueDate: String?) {
        divingDao.updateInventoryRentalAssignment(id, status, renterId, renterName, dueDate)
    }

    suspend fun deleteInventoryItem(id: String) {
        divingDao.deleteInventoryItem(id)
    }

    // --- Asset & Tech Rental Transactions ---
    fun getActiveRentalsForOrganization(orgId: String): Flow<List<RentalTransactionEntity>> =
        divingDao.getActiveRentalsForOrganization(orgId)

    fun getAllRentalsForOrganization(orgId: String): Flow<List<RentalTransactionEntity>> =
        divingDao.getAllRentalsForOrganization(orgId)

    fun getRentalsForItem(itemId: String): Flow<List<RentalTransactionEntity>> =
        divingDao.getRentalsForItem(itemId)

    fun getRentalsForMember(memberId: String): Flow<List<RentalTransactionEntity>> =
        divingDao.getRentalsForMember(memberId)

    suspend fun insertRentalTransaction(rental: RentalTransactionEntity) {
        divingDao.insertRentalTransaction(rental)
    }

    suspend fun updateRentalTransaction(rental: RentalTransactionEntity) {
        divingDao.updateRentalTransaction(rental)
    }

    suspend fun completeRentalReturn(id: String, returnDate: String, status: String, condition: String, damageNotes: String) {
        divingDao.completeRentalReturn(id, returnDate, status, condition, damageNotes)
    }

    // --- Modul 4: Organization Documents & Signatures Repository Methods ---
    fun getAllDocumentsForOrganization(orgId: String): Flow<List<OrganizationDocumentEntity>> =
        divingDao.getAllDocumentsForOrganization(orgId)

    fun getDocumentsByVisibility(orgId: String, visibility: String): Flow<List<OrganizationDocumentEntity>> =
        divingDao.getDocumentsByVisibility(orgId, visibility)

    fun getMandatoryDocumentsForOrganization(orgId: String): Flow<List<OrganizationDocumentEntity>> =
        divingDao.getMandatoryDocumentsForOrganization(orgId)

    suspend fun getMandatoryDocumentsDirect(orgId: String): List<OrganizationDocumentEntity> =
        divingDao.getMandatoryDocumentsDirect(orgId)

    fun getOrganizationDocumentById(id: String): Flow<OrganizationDocumentEntity?> =
        divingDao.getOrganizationDocumentById(id)

    suspend fun getOrganizationDocumentByIdDirect(id: String): OrganizationDocumentEntity? =
        divingDao.getOrganizationDocumentByIdDirect(id)

    suspend fun insertOrganizationDocument(doc: OrganizationDocumentEntity) {
        divingDao.insertOrganizationDocument(doc)
    }

    suspend fun updateOrganizationDocument(doc: OrganizationDocumentEntity) {
        divingDao.updateOrganizationDocument(doc)
    }

    suspend fun updateDocumentAcknowledgmentCount(id: String, count: Int) {
        divingDao.updateDocumentAcknowledgmentCount(id, count)
    }

    suspend fun deleteOrganizationDocument(id: String) {
        divingDao.deleteSignaturesForDocument(id)
        divingDao.deleteOrganizationDocument(id)
    }

    fun getAllSignaturesForOrganization(orgId: String): Flow<List<DocumentSignatureEntity>> =
        divingDao.getAllSignaturesForOrganization(orgId)

    fun getSignaturesForDocument(documentId: String): Flow<List<DocumentSignatureEntity>> =
        divingDao.getSignaturesForDocument(documentId)

    fun getSignaturesForMember(orgId: String, memberId: String): Flow<List<DocumentSignatureEntity>> =
        divingDao.getSignaturesForMember(orgId, memberId)

    suspend fun getSignatureForMemberAndDocument(documentId: String, memberId: String): DocumentSignatureEntity? =
        divingDao.getSignatureForMemberAndDocument(documentId, memberId)

    suspend fun insertDocumentSignature(signature: DocumentSignatureEntity) {
        divingDao.insertDocumentSignature(signature)
    }

    // Dive Logbook Flows & Operations
    val allDiveLogs: Flow<List<DiveLogEntity>> = divingDao.getAllDiveLogs()
    val allPendingDives: Flow<List<DiveLogEntity>> = divingDao.getAllPendingDives()

    // Diver Documents & Certifications Vault
    val allDocuments: Flow<List<DiverDocumentEntity>> = divingDao.getAllDocuments()
    val pendingDocumentApprovals: Flow<List<DiverDocumentEntity>> = divingDao.getPendingApprovalDocuments()

    fun getDocumentsForDiver(diverId: String, diverEmail: String): Flow<List<DiverDocumentEntity>> =
        divingDao.getDocumentsForDiver(diverId, diverEmail)

    fun getDocumentById(id: String): Flow<DiverDocumentEntity?> =
        divingDao.getDocumentById(id)

    suspend fun getDocumentByIdDirect(id: String): DiverDocumentEntity? =
        divingDao.getDocumentByIdDirect(id)

    suspend fun insertDocument(document: DiverDocumentEntity) {
        divingDao.insertDocument(document)
    }

    suspend fun updateDocument(document: DiverDocumentEntity) {
        divingDao.updateDocument(document)
    }

    suspend fun deleteDocument(id: String) {
        divingDao.deleteDocument(id)
    }

    suspend fun approveDocument(
        id: String,
        approverName: String,
        license: String,
        org: String,
        approvalDate: String,
        stampCode: String,
        signatureJson: String,
        notes: String
    ) {
        divingDao.approveDocument(
            id = id,
            approverName = approverName,
            license = license,
            org = org,
            approvalDate = approvalDate,
            stampCode = stampCode,
            signatureJson = signatureJson,
            notes = notes
        )
    }

    suspend fun rejectDocument(id: String, notes: String) {
        divingDao.rejectDocument(id, notes)
    }

    fun getDiveLogsForDiver(diverId: String): Flow<List<DiveLogEntity>> =
        divingDao.getDiveLogsForDiver(diverId)

    fun getPendingBuddyApprovals(buddyEmail: String): Flow<List<DiveLogEntity>> =
        divingDao.getPendingBuddyApprovals(buddyEmail)

    fun getDiveLogById(id: String): Flow<DiveLogEntity?> =
        divingDao.getDiveLogById(id)

    suspend fun getDiveLogByIdDirect(id: String): DiveLogEntity? =
        divingDao.getDiveLogByIdDirect(id)

    suspend fun insertDiveLog(dive: DiveLogEntity) {
        divingDao.insertDiveLog(dive)
    }

    suspend fun updateDiveLog(dive: DiveLogEntity) {
        divingDao.updateDiveLog(dive)
    }

    suspend fun deleteDiveLog(id: String) {
        divingDao.deleteDiveLog(id)
    }

    suspend fun approveWithInstructorStamp(
        id: String,
        approvedBy: String,
        stampCode: String,
        instructorName: String,
        license: String
    ) {
        divingDao.approveWithInstructorStamp(
            id = id,
            status = "APPROVED_BY_INSTRUCTOR",
            method = "QR_CODE",
            approvedBy = approvedBy,
            timestamp = System.currentTimeMillis(),
            stampCode = stampCode,
            instructorName = instructorName,
            license = license
        )
    }

    suspend fun approveWithBuddySignature(
        id: String,
        buddyName: String,
        signatureJson: String,
        note: String
    ) {
        divingDao.approveWithBuddySignature(
            id = id,
            buddyName = buddyName,
            timestamp = System.currentTimeMillis(),
            signatureJson = signatureJson,
            note = note
        )
    }

    suspend fun approveWithBuddyPush(
        id: String,
        buddyName: String,
        note: String
    ) {
        divingDao.approveWithBuddyPush(
            id = id,
            buddyName = buddyName,
            timestamp = System.currentTimeMillis(),
            note = note
        )
    }

    suspend fun markPendingBuddyPushApproval(
        id: String,
        buddyEmail: String,
        buddyName: String
    ) {
        divingDao.markPendingBuddyPushApproval(
            id = id,
            buddyEmail = buddyEmail,
            buddyName = buddyName
        )
    }

    fun getPresentationsForModule(moduleId: String): Flow<List<PresentationDeckEntity>> =
        divingDao.getPresentationsForModule(moduleId)

    fun getVideosForModule(moduleId: String): Flow<List<LearningVideoEntity>> =
        divingDao.getVideosForModule(moduleId)

    fun getQuizzesForModule(moduleId: String): Flow<List<QuizEntity>> =
        divingDao.getQuizzesForModule(moduleId)

    fun getPresentationById(id: String): Flow<PresentationDeckEntity?> =
        divingDao.getPresentationById(id)

    fun getVideoById(id: String): Flow<LearningVideoEntity?> =
        divingDao.getVideoById(id)

    fun getQuizById(id: String): Flow<QuizEntity?> =
        divingDao.getQuizById(id)

    suspend fun savePresentationProgress(id: String, lastSlide: Int, isCompleted: Boolean) {
        divingDao.updatePresentationProgress(id, lastSlide, isCompleted)
    }

    suspend fun saveVideoProgress(id: String, watchedSec: Int, isCompleted: Boolean) {
        divingDao.updateVideoProgress(id, watchedSec, isCompleted)
    }

    suspend fun recordQuizAttempt(quizId: String, scorePercentage: Int, isPassed: Boolean) {
        val quiz = divingDao.getQuizById(quizId).firstOrNull() ?: return
        val newHighest = maxOf(quiz.highestScore, scorePercentage)
        val passedState = quiz.isPassed || isPassed
        val attempts = quiz.timesAttempted + 1
        divingDao.updateQuizResult(
            id = quizId,
            score = newHighest,
            isPassed = passedState,
            attempts = attempts,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun insertCustomPresentation(presentation: PresentationDeckEntity) {
        divingDao.insertPresentation(presentation)
    }

    suspend fun insertCustomVideo(video: LearningVideoEntity) {
        divingDao.insertVideo(video)
    }

    suspend fun insertCustomQuiz(quiz: QuizEntity) {
        divingDao.insertQuiz(quiz)
    }

    suspend fun deletePresentation(id: String) {
        divingDao.deletePresentation(id)
    }

    suspend fun deleteVideo(id: String) {
        divingDao.deleteVideo(id)
    }

    suspend fun deleteQuiz(id: String) {
        divingDao.deleteQuiz(id)
    }

    suspend fun updateStudentName(name: String) {
        val curr = divingDao.getUserProgress().firstOrNull() ?: UserProgressEntity()
        divingDao.insertOrUpdateProgress(curr.copy(studentName = name))
    }

    suspend fun updateProfilePicture(userId: String, uri: String?) {
        divingDao.updateUserProfilePicture(userId, uri)
        divingDao.updateProgressProfilePicture(uri)
    }

    // --- Deployment & Instructor Controls ---
    suspend fun setPresentationDeployment(id: String, isDeployed: Boolean) {
        divingDao.updatePresentationDeployment(id, isDeployed)
    }

    suspend fun setVideoDeployment(id: String, isDeployed: Boolean) {
        divingDao.updateVideoDeployment(id, isDeployed)
    }

    suspend fun setQuizDeployment(id: String, isDeployed: Boolean) {
        divingDao.updateQuizDeployment(id, isDeployed)
    }

    suspend fun updateStudentStatus(id: String, status: String, certifiedDate: String? = null) {
        divingDao.updateStudentStatus(id, status, certifiedDate)
    }

    suspend fun updateStudentNotes(id: String, notes: String) {
        divingDao.updateStudentNotes(id, notes)
    }

    // --- Authentication & Email Verification ---

    suspend fun getUserAccountById(id: String): UserAccountEntity? =
        divingDao.getAccountByIdDirect(id)

    suspend fun registerUser(
        email: String,
        passwordPlain: String,
        fullName: String,
        role: String = "STUDENT"
    ): Result<Pair<UserAccountEntity, VerificationEmailMessage>> {
        val cleanEmail = email.trim().lowercase()
        val existing = divingDao.getAccountByEmailDirect(cleanEmail)
        if (existing != null && existing.isEmailVerified) {
            val isSeedAccount = existing.id == "usr_adrian_instructor" || existing.id == "usr_leo_student"
            if (!isSeedAccount) {
                return Result.failure(Exception("An account with this email already exists. Please log in."))
            }
        }

        // Generate a 6-digit verification security code
        val verificationCode = (100000..999999).random().toString()
        val accountId = existing?.id ?: "usr_${java.util.UUID.randomUUID().toString().take(8)}"
        val passwordHash = hashPassword(passwordPlain)
        val determinedRole = role
        val isInstructor = determinedRole == "INSTRUCTOR"

        val newAccount = UserAccountEntity(
            id = accountId,
            email = cleanEmail,
            passwordHash = passwordHash,
            fullName = fullName.trim().ifBlank { if (isInstructor) "Tauchlehrer" else "Taucher" },
            isEmailVerified = false,
            verificationCode = verificationCode,
            verificationSentAt = System.currentTimeMillis(),
            role = determinedRole,
            phoneNumber = existing?.phoneNumber ?: "",
            certLevel = existing?.certLevel ?: (if (isInstructor) "CMAS ** / PADI OWSI" else "Open Water Diver (OWD)"),
            certAgency = existing?.certAgency ?: "VDST / CMAS",
            licenseNumber = existing?.licenseNumber ?: "",
            totalLoggedDives = existing?.totalLoggedDives ?: (if (isInstructor) 150 else 0),
            ttuExpiryDate = existing?.ttuExpiryDate ?: "2027-12-31",
            ttuDoctorName = existing?.ttuDoctorName ?: "",
            emergencyContactName = existing?.emergencyContactName ?: "",
            emergencyContactPhone = existing?.emergencyContactPhone ?: "",
            insuranceProvider = existing?.insuranceProvider ?: "DAN Europe",
            insuranceNumber = existing?.insuranceNumber ?: "",
            suitSize = existing?.suitSize ?: "L (52)",
            shoeSize = existing?.shoeSize ?: "43",
            bcdSize = existing?.bcdSize ?: "M",
            preferredTank = existing?.preferredTank ?: "12L Stahl DIN",
            instructorAgency = if (isInstructor) (existing?.instructorAgency ?: "VDST / CMAS") else "",
            instructorLicenseNumber = if (isInstructor) (existing?.instructorLicenseNumber ?: "") else "",
            instructorLevel = if (isInstructor) (existing?.instructorLevel ?: "Tauchlehrer ** (CMAS **)") else "",
            teachingSpecialtiesJson = if (isInstructor) (existing?.teachingSpecialtiesJson ?: "[\"Nitrox\", \"Tieftauchen\", \"Trockentauchen\"]") else "[]",
            instructorValidUntil = if (isInstructor) (existing?.instructorValidUntil ?: "2027-12-31") else "",
            homeOrganizationName = if (isInstructor) (existing?.homeOrganizationName ?: "") else ""
        )
        divingDao.insertUserAccount(newAccount)

        val emailMessage = createVerificationEmailMessage(
            recipientEmail = cleanEmail,
            recipientName = newAccount.fullName,
            code = verificationCode
        )

        return Result.success(Pair(newAccount, emailMessage))
    }

    suspend fun verifyUserEmail(email: String, enteredCode: String): Result<UserAccountEntity> {
        val cleanEmail = email.trim().lowercase()
        val account = divingDao.getAccountByEmailDirect(cleanEmail)
            ?: return Result.failure(Exception("Account not found for email $cleanEmail."))

        if (account.verificationCode != enteredCode.trim()) {
            return Result.failure(Exception("Incorrect verification code. Please check your email or request a new code."))
        }

        val verifiedAccount = account.copy(isEmailVerified = true)
        divingDao.insertUserAccount(verifiedAccount)

        if (verifiedAccount.role == "STUDENT") {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val studentEntry = StudentRosterEntity(
                id = verifiedAccount.id,
                fullName = verifiedAccount.fullName,
                email = verifiedAccount.email,
                status = "ACTIVE",
                enrolledDate = today,
                certifiedDate = null,
                targetCourse = "Open Water Diver (18m)",
                presentationsCompleted = 0,
                totalPresentations = 3,
                videosWatched = 0,
                totalVideos = 3,
                quizzesPassed = 0,
                totalQuizzes = 3,
                examReadinessScore = 0,
                finalMockExamScore = null,
                instructorNotes = "Newly registered student candidate."
            )
            divingDao.insertStudent(studentEntry)
        }

        // Sync with user progress profile
        val curr = divingDao.getUserProgress().firstOrNull() ?: UserProgressEntity()
        divingDao.insertOrUpdateProgress(curr.copy(studentName = account.fullName))

        return Result.success(verifiedAccount)
    }

    suspend fun resendVerificationCode(email: String): Result<VerificationEmailMessage> {
        val cleanEmail = email.trim().lowercase()
        val account = divingDao.getAccountByEmailDirect(cleanEmail)
            ?: return Result.failure(Exception("No account found for $cleanEmail."))

        val newCode = (100000..999999).random().toString()
        divingDao.updateVerificationCode(cleanEmail, newCode, System.currentTimeMillis())

        val message = createVerificationEmailMessage(
            recipientEmail = cleanEmail,
            recipientName = account.fullName,
            code = newCode
        )
        return Result.success(message)
    }

    suspend fun loginUser(email: String, passwordPlain: String): Result<UserAccountEntity> {
        val cleanEmail = email.trim().lowercase()
        val account = divingDao.getAccountByEmailDirect(cleanEmail)
            ?: return Result.failure(Exception("No account found with this email. Please register first."))

        if (account.passwordHash != hashPassword(passwordPlain)) {
            return Result.failure(Exception("Invalid password. Please try again."))
        }

        if (!account.isEmailVerified) {
            return Result.failure(Exception("EMAIL_NOT_VERIFIED"))
        }

        // Sync student name
        val curr = divingDao.getUserProgress().firstOrNull() ?: UserProgressEntity()
        divingDao.insertOrUpdateProgress(curr.copy(studentName = account.fullName))

        return Result.success(account)
    }

    suspend fun getInitialAccount(): UserAccountEntity? {
        return divingDao.getLatestVerifiedAccount()
    }

    suspend fun updateUserAccount(account: UserAccountEntity) {
        divingDao.updateUserAccount(account)
    }

    private fun hashPassword(password: String): String {
        return try {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(password.toByteArray())
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (_: Exception) {
            password.hashCode().toString()
        }
    }

    private fun createVerificationEmailMessage(
        recipientEmail: String,
        recipientName: String,
        code: String
    ): VerificationEmailMessage {
        val html = """
            Willkommen bei AquaLearn, $recipientName!
            
            Vielen Dank für Ihre Registrierung mit Ihrer E-Mail-Adresse ($recipientEmail).
            Um Ihr Taucher-Konto zu aktivieren und loszulegen, verwenden Sie bitte Ihren 6-stelligen Aktivierungscode:
            
            AKTIVIERUNGSCODE: $code
            
            Ihr Konto steht Ihnen anschließend sofort mit vollem Funktionsumfang zur Verfügung.
            
            Gut Luft & Allzeit sicheres Tauchen!
            Ihr AquaLearn Team
        """.trimIndent()

        return VerificationEmailMessage(
            senderName = "AquaLearn Security System",
            senderEmail = "noreply@aqualearn.app",
            recipientEmail = recipientEmail,
            recipientName = recipientName,
            subject = "AquaLearn Aktivierungscode: $code",
            verificationCode = code,
            sentTimestamp = System.currentTimeMillis(),
            emailHtmlBody = html
        )
    }

    fun getHandSignals(isGerman: Boolean = false): List<Flashcard> {
        if (isGerman) {
            return listOf(
                Flashcard(
                    id = "sig_ok",
                    signalName = "OK / Alles in Ordnung",
                    instruction = "Zeigefinger und Daumen zu einem Kreis ('O') formen, restliche Finger abspreizen.",
                    meaning = "Alle Systeme normal. Dient zur Frage ('Alles OK?') und Antwort ('Mir geht es gut').",
                    actionRequired = "Auf ein OK-Signal immer unverzüglich mit einem OK-Signal antworten.",
                    symbolEmoji = "👌"
                ),
                Flashcard(
                    id = "sig_up",
                    signalName = "Aufsteigen / Tauchgang beenden",
                    instruction = "Faust bilden und den Daumen gerade nach oben richten.",
                    meaning = "Zeit zum Aufstieg oder Beenden des Tauchgangs. Bedeutet NIEMALS 'super/toll'.",
                    actionRequired = "Buddy signalisieren, Tiefe/Luft prüfen, gemeinsam langsamen Aufstieg beginnen.",
                    symbolEmoji = "👍"
                ),
                Flashcard(
                    id = "sig_down",
                    signalName = "Abtauchen / Nach unten",
                    instruction = "Faust bilden und den Daumen gerade nach unten richten.",
                    meaning = "Signal zum Entlüften der Tarierweste, Druckausgleich und kontrollierten Abstieg.",
                    actionRequired = "Druckausgleich frühzeitig und regelmäßig alle 0,5–1 m beim Abstieg durchführen.",
                    symbolEmoji = "👎"
                ),
                Flashcard(
                    id = "sig_out_of_air",
                    signalName = "Keine Luft mehr!",
                    instruction = "Die flache Hand mit schneidender Bewegung horizontal über die Kehle ziehen.",
                    meaning = "Notfall: Flasche ist leer oder Luftzufuhr ist komplett ausgefallen.",
                    actionRequired = "Buddy bietet sofort Oktopus/alternative Luftquelle an. Nicht unkontrolliert hochschnellen!",
                    symbolEmoji = "🫱"
                ),
                Flashcard(
                    id = "sig_share_air",
                    signalName = "Luft mit mir teilen",
                    instruction = "Fingerspitzen wiederholt zum Mund führen oder Atemregler berühren.",
                    meaning = "Anforderung des alternativen zweiten Atemreglers (Oktopus) des Buddys.",
                    actionRequired = "Spender-Regler greifen, ausblasen, atmen und physischen Kontakt herstellen.",
                    symbolEmoji = "🤝"
                ),
                Flashcard(
                    id = "sig_low_air",
                    signalName = "Wenig Luft / Reservestand",
                    instruction = "Geballte Faust gegen die Brust halten oder gegen den Atemregler klopfen.",
                    meaning = "Erreichen des Reservedrucks (üblicherweise 50 bar).",
                    actionRequired = "Tauchgang umkehren und Aufstieg mit Sicherheitsstopp einleiten.",
                    symbolEmoji = "✊"
                ),
                Flashcard(
                    id = "sig_level_off",
                    signalName = "Tiefe halten / Auf dieser Tiefe bleiben",
                    instruction = "Flache Handfläche nach unten, langsam horizontal hin und her bewegen.",
                    meaning = "Aktuelle Tiefe beibehalten; Aufstieg oder Abstieg stoppen.",
                    actionRequired = "Kleine Menge Luft in Tarierweste geben oder ablassen, um neutrale Tarierung herzustellen.",
                    symbolEmoji = "🫳"
                ),
                Flashcard(
                    id = "sig_cramp",
                    signalName = "Wadenkrampf / Beinkrampf",
                    instruction = "Hand wiederholt wie eine Klaue öffnen und schließen, auf den betroffenen Muskel zeigen.",
                    meaning = "Waden- oder Oberschenkelkrampf behindert das Schwimmen.",
                    actionRequired = "Flossenspitze zum Knie ziehen, um Muskel zu dehnen; Buddy unterstützt.",
                    symbolEmoji = "🤏"
                ),
                Flashcard(
                    id = "sig_problem",
                    signalName = "Etwas stimmt nicht / Problem",
                    instruction = "Offene Handfläche nach unten auf Brusthöhe hin und her kippen.",
                    meaning = "Zeigt ein Problem an (Druckausgleich, Ausrüstung verhakt, Unwohlsein).",
                    actionRequired = "Anschließend auf das Problem zeigen (z.B. Ohr, Maske, Finimeter).",
                    symbolEmoji = "👋"
                )
            )
        }
        return listOf(
            Flashcard(
                id = "sig_ok",
                signalName = "OK / Everything Good",
                instruction = "Join index finger and thumb in an 'O' ring, extending other fingers.",
                meaning = "All systems normal. Used to question buddy ('Are you OK?') and respond ('I am OK').",
                actionRequired = "Always answer an OK signal with an OK signal immediately.",
                symbolEmoji = "👌"
            ),
            Flashcard(
                id = "sig_up",
                signalName = "Ascend / End Dive",
                instruction = "Make a fist and point thumb straight upward.",
                meaning = "Time to ascend or end the dive. NEVER means 'great/good'.",
                actionRequired = "Signal buddy, verify depth/air, begin slow ascent together.",
                symbolEmoji = "👍"
            ),
            Flashcard(
                id = "sig_down",
                signalName = "Descend / Go Down",
                instruction = "Make a fist and point thumb straight downward.",
                meaning = "Signal to deflate BCD, equalize ears, and begin controlled descent.",
                actionRequired = "Equalize ears early and often every 0.5-1m as you descend.",
                symbolEmoji = "👎"
            ),
            Flashcard(
                id = "sig_out_of_air",
                signalName = "Out of Air!",
                instruction = "Draw open flat hand horizontally across the throat in a slashing motion.",
                meaning = "Emergency: Cylinder is empty or air delivery has failed completely.",
                actionRequired = "Buddy immediately offers octopus/alternate air source. Do not bolt!",
                symbolEmoji = "🫱"
            ),
            Flashcard(
                id = "sig_share_air",
                signalName = "Share Air with Me",
                instruction = "Point fingertips toward mouth back and forth or touch regulator.",
                meaning = "Requesting buddy's alternate second stage regulator.",
                actionRequired = "Secure donor regulator, purge, breathe, and establish physical contact.",
                symbolEmoji = "🤝"
            ),
            Flashcard(
                id = "sig_low_air",
                signalName = "Low Air / Reserve Pressure",
                instruction = "Clenched fist held against the chest or tapped against regulator.",
                meaning = "Approaching reserve pressure (typically 50 bar / 700 psi).",
                actionRequired = "Begin turning the dive and planning safety stop ascent.",
                symbolEmoji = "✊"
            ),
            Flashcard(
                id = "sig_level_off",
                signalName = "Level Off / Stay at this Depth",
                instruction = "Flat palm face down, moving slowly side to side horizontally.",
                meaning = "Maintain current depth; stop ascending or descending.",
                actionRequired = "Add or vent tiny puff of air from BCD to establish neutral buoyancy.",
                symbolEmoji = "🫳"
            ),
            Flashcard(
                id = "sig_cramp",
                signalName = "Muscle Cramp in Leg/Fin",
                instruction = "Repeatedly open and clench hand like a claw pointing to affected muscle.",
                meaning = "Calf or hamstring cramp impeding swimming.",
                actionRequired = "Pull fin tip toward knee to stretch muscle; buddy assists.",
                symbolEmoji = "🤏"
            ),
            Flashcard(
                id = "sig_problem",
                signalName = "Something is Wrong / Problem",
                instruction = "Open hand palm down, rocking from side to side at chest height.",
                meaning = "Indicating an issue (equalization, gear snag, feeling unwell).",
                actionRequired = "Follow with pointing to problem (e.g. ear, mask, gauge).",
                symbolEmoji = "👋"
            )
        )
    }

    suspend fun checkAndSeedInitialData() {
        val existingModules = divingDao.getAllModules().firstOrNull()
        if (existingModules.isNullOrEmpty()) {
            seedDatabase()
        }
        ensureSpecialtyContentsSeeded()
        seedStudentsAndAccountsIfEmpty()
        seedDiveLogsIfEmpty()
        seedDocumentsIfEmpty()
        seedOrganizationsIfEmpty()
    }

    private suspend fun ensureSpecialtyContentsSeeded() {
        val existingModules = divingDao.getAllModules().firstOrNull()
        val hasNitrox = existingModules?.any { it.id == "mod_nitrox" } == true
        if (!hasNitrox) {
            val specModules = listOf(
                CourseModuleEntity(
                    id = "mod_nitrox",
                    title = "Oxygen Analysis & Gas Mixing",
                    subtitle = "Enriched Air Nitrox Specialty",
                    description = "Theory and safety procedures for diving with oxygen concentrations up to 40% Enriched Air Nitrox (EANx).",
                    iconName = "science",
                    orderIndex = 6,
                    estimatedMin = 60
                ),
                CourseModuleEntity(
                    id = "mod_deep",
                    title = "Deep Diving Limits & Narcosis Safety",
                    subtitle = "Deep Diver Specialty",
                    description = "Master safe descent procedures, redundant gas planning, and narcosis management for deep diving.",
                    iconName = "explore",
                    orderIndex = 7,
                    estimatedMin = 90
                ),
                CourseModuleEntity(
                    id = "mod_wreck",
                    title = "Wreck Surveying & Line Laying",
                    subtitle = "Wreck Diver Specialty",
                    description = "Procedures and protocols for mapping artificial reefs, deploying penetration guidelines, and avoiding silt-outs.",
                    iconName = "sailing",
                    orderIndex = 8,
                    estimatedMin = 80
                ),
                CourseModuleEntity(
                    id = "mod_night",
                    title = "Dark Navigation & Light Communication",
                    subtitle = "Night Diver Specialty",
                    description = "Master night dive navigation, specialized buddy communication signals, and explore nocturnal reef environments safely.",
                    iconName = "flashlight_on",
                    orderIndex = 9,
                    estimatedMin = 60
                )
            )
            divingDao.insertModules(specModules)

            // Seed Nitrox Presentation
            val nitroxSlides = JSONArray().apply {
                put(JSONObject().apply {
                    put("slideNumber", 1)
                    put("title", "What is Enriched Air Nitrox?")
                    put("subtitle", "Breathing air with higher oxygen and lower nitrogen.")
                    put("bulletPoints", JSONArray().apply {
                        put("Standard air is 21% Oxygen and 79% Nitrogen.")
                        put("Enriched Air Nitrox (EANx) typically has 32% or 36% Oxygen.")
                        put("Benefit: Lower nitrogen absorption means longer No-Decompression Limits (NDL).")
                    })
                    put("diagramType", "GENERAL_CHECKLIST")
                    put("instructorNotes", "Explain that Nitrox does not make you dive deeper, only longer at medium depths.")
                    put("keyTakeaway", "Nitrox reduces nitrogen uptake, extending bottom times.")
                })
                put(JSONObject().apply {
                    put("slideNumber", 2)
                    put("title", "Oxygen Toxicity and Partial Pressure")
                    put("subtitle", "Managing the depth limits of oxygen.")
                    put("bulletPoints", JSONArray().apply {
                        put("Oxygen becomes toxic under pressure. Max safe partial pressure (PPO2) is 1.4 ATA.")
                        put("Maximum Operating Depth (MOD) is calculated based on oxygen percentage.")
                        put("Diver must analyze and verify cylinder oxygen content prior to diving.")
                    })
                    put("diagramType", "BOYLES_LAW")
                    put("instructorNotes", "Show how MOD changes based on whether EANx32 or EANx36 is used.")
                    put("keyTakeaway", "Always check your cylinder percentage and calculate your MOD.")
                })
            }
            val presNitrox = PresentationDeckEntity(
                id = "pres_nitrox",
                moduleId = "mod_nitrox",
                title = "Nitrox Gas Analysis & Max Depth (MOD) calculations",
                description = "Learn how to analyze oxygen percentages, calculate oxygen toxicity, and program dive computers for nitrox gas up to 40% O2.",
                slidesJson = nitroxSlides.toString(),
                totalSlides = 2,
                lastSlideRead = 0,
                isCompleted = false,
                isInstructorUploaded = false
            )

            // Seed Deep Presentation
            val deepSlides = JSONArray().apply {
                put(JSONObject().apply {
                    put("slideNumber", 1)
                    put("title", "Safety Limits of Deep Diving")
                    put("subtitle", "Understanding depth limits and decompression theory.")
                    put("bulletPoints", JSONArray().apply {
                        put("Recreational deep limit is 40 meters / 130 feet.")
                        put("Nitrogen narcosis becomes prominent past 30 meters.")
                        put("Ascending slowly (9m/min) and performing a safety stop is mandatory.")
                    })
                    put("diagramType", "GENERAL_CHECKLIST")
                    put("instructorNotes", "Emphasize narcosis awareness and redundant air source necessity.")
                    put("keyTakeaway", "Respect deep diving depth limits and monitor gas consumption.")
                })
            }
            val presDeep = PresentationDeckEntity(
                id = "pres_deep",
                moduleId = "mod_deep",
                title = "Decompression Safety & Gas Planning",
                description = "Learn the safety protocols, physical adjustments, narcosis checks, and dual tank redundant gas supply management.",
                slidesJson = deepSlides.toString(),
                totalSlides = 1,
                lastSlideRead = 0,
                isCompleted = false,
                isInstructorUploaded = false
            )

            // Seed Wreck Presentation
            val wreckSlides = JSONArray().apply {
                put(JSONObject().apply {
                    put("slideNumber", 1)
                    put("title", "Wreck Hazards and Safety lines")
                    put("subtitle", "Navigation and safety guidelines inside and around wrecks.")
                    put("bulletPoints", JSONArray().apply {
                        put("Wrecks are artificial reefs but have sharp metals and silt hazards.")
                        put("Laying guidelines is essential for any inside penetration.")
                        put("Always use frog kicks to avoid kicking up silt and losing visibility.")
                    })
                    put("diagramType", "GENERAL_CHECKLIST")
                    put("instructorNotes", "Explain line-laying and tie-off procedures.")
                    put("keyTakeaway", "Never enter a wreck without proper training, lighting, and guideline.")
                })
            }
            val presWreck = PresentationDeckEntity(
                id = "pres_wreck",
                moduleId = "mod_wreck",
                title = "Wreck Hazards, Navigation & Penetration Lines",
                description = "Acquire the skills to chart sunken vessels, lay guide penetration lines, and safely explore wreck interiors.",
                slidesJson = wreckSlides.toString(),
                totalSlides = 1,
                lastSlideRead = 0,
                isCompleted = false,
                isInstructorUploaded = false
            )

            // Seed Night Presentation
            val nightSlides = JSONArray().apply {
                put(JSONObject().apply {
                    put("slideNumber", 1)
                    put("title", "Night Diving Communication & Lighting")
                    put("subtitle", "Communication under water when dark.")
                    put("bulletPoints", JSONArray().apply {
                        put("Use primary and backup dive torches.")
                        put("Signal buddy using torch patterns (circle for OK, waving side-to-side for attention).")
                        put("Never shine light directly into buddy's eyes.")
                    })
                    put("diagramType", "GENERAL_CHECKLIST")
                    put("instructorNotes", "Demonstrate hand and light communication signals.")
                    put("keyTakeaway", "Light is your lifeline at night. Communicate calmly and observe nocturnal life.")
                })
            }
            val presNight = PresentationDeckEntity(
                id = "pres_night",
                moduleId = "mod_night",
                title = "Nocturnal Wildlife & Dive Torch Management",
                description = "Navigate in complete darkness, master light communications, and observe nocturnal marine life.",
                slidesJson = nightSlides.toString(),
                totalSlides = 1,
                lastSlideRead = 0,
                isCompleted = false,
                isInstructorUploaded = false
            )

            divingDao.insertPresentations(listOf(presNitrox, presDeep, presWreck, presNight))

            // Seed Nitrox Quiz
            val nitroxQuestions = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "nq_1")
                    put("question", "What is the primary benefit of diving with Enriched Air Nitrox?")
                    put("options", JSONArray().apply {
                        put("It allows you to dive deeper.")
                        put("It reduces nitrogen absorption, extending no-decompression limits.")
                        put("It completely prevents nitrogen narcosis.")
                        put("It warms your body temperature.")
                    })
                    put("correctIndex", 1)
                    put("explanation", "By replacing some nitrogen with oxygen, your body absorbs less nitrogen, which extends your No-Decompression bottom times.")
                    put("safetyRule", "Nitrox allows longer dives but requires strict tracking of oxygen depth limits.")
                    put("depthOrContext", "Nitrox Physics")
                })
            }
            val quizNitrox = QuizEntity(
                id = "quiz_nitrox",
                moduleId = "mod_nitrox",
                title = "Nitrox Specialty Quiz",
                description = "Enriched Air Safety & Oxygen Toxicity Exams",
                passScorePercentage = 80,
                questionsJson = nitroxQuestions.toString(),
                isMockExam = false,
                isInstructorUploaded = false
            )

            // Seed Deep Quiz
            val deepQuestions = JSONArray().apply {
                put(JSONObject().apply {
                    put("id", "dq_1")
                    put("question", "What is the recreational deep diving limit?")
                    put("options", JSONArray().apply {
                        put("18 meters")
                        put("30 meters")
                        put("40 meters")
                        put("50 meters")
                    })
                    put("correctIndex", 2)
                    put("explanation", "40 meters (130 feet) is the absolute depth limit for recreational scuba diving.")
                    put("safetyRule", "Never exceed 40m without technical decompression training.")
                    put("depthOrContext", "Deep Limits")
                })
            }
            val quizDeep = QuizEntity(
                id = "quiz_deep",
                moduleId = "mod_deep",
                title = "Deep Diving Specialty Quiz",
                description = "Narcosis Symptoms & Safety Protocol Quiz",
                passScorePercentage = 80,
                questionsJson = deepQuestions.toString(),
                isMockExam = false,
                isInstructorUploaded = false
            )

            divingDao.insertQuizzes(listOf(quizNitrox, quizDeep))
        }
    }

    private suspend fun seedDocumentsIfEmpty() {
        val existingDocs = divingDao.getAllDocuments().firstOrNull()
        if (existingDocs.isNullOrEmpty()) {
            val initialDocs = listOf(
                DiverDocumentEntity(
                    id = "doc_cert_ow",
                    diverId = "usr_student",
                    diverName = "Alex Rivera",
                    diverEmail = "alex.rivera@example.com",
                    title = "Open Water Diver (18m / 60ft)",
                    category = "CERTIFICATION",
                    documentNumber = "OWD-884920-CH",
                    issuingOrganization = "PADI Worldwide",
                    issueDate = "2026-06-15",
                    expiryDate = "Lifetime / No Expiration",
                    notes = "Certified recreational open water scuba diver. Maximum recommended depth 18m with certified buddy.",
                    cardColorHex = "#006699",
                    approvalStatus = "APPROVED",
                    requiredApproverType = "INSTRUCTOR",
                    approverName = "Adrian Scheid",
                    approverTitleOrLicense = "PADI Master Scuba Diver Trainer #391024",
                    approverOrganization = "AquaLearn Diving Academy",
                    approvalDate = "2026-06-15",
                    approvalStampCode = "PADI-MSDT-391024-CERT-SEAL",
                    approverSignatureJson = "[{\"x\":25,\"y\":55,\"pressure\":1.0},{\"x\":65,\"y\":40,\"pressure\":1.0},{\"x\":120,\"y\":65,\"pressure\":1.0},{\"x\":175,\"y\":45,\"pressure\":1.0}]",
                    approverNotes = "Student successfully mastered all 24 confined water skills and 4 open water qualifying dives."
                ),
                DiverDocumentEntity(
                    id = "doc_med_fitness",
                    diverId = "usr_student",
                    diverName = "Alex Rivera",
                    diverEmail = "alex.rivera@example.com",
                    title = "RSTC Medical Statement & Diving Fitness Assessment",
                    category = "MEDICAL_FORM",
                    documentNumber = "MED-2026-DR-9941",
                    issuingOrganization = "Dr. med. Thomas Weber (Diving & Hyperbaric Medicine)",
                    issueDate = "2025-10-15",
                    expiryDate = "2026-10-15",
                    notes = "Annual diving medical examination. Evaluated cardiovascular fitness, pulmonary function, and ENT equalization.",
                    cardColorHex = "#00875A",
                    documentUri = "sample_rstc_medical_statement",
                    approvalStatus = "APPROVED",
                    requiredApproverType = "DOCTOR",
                    approverName = "Dr. med. Thomas Weber",
                    approverTitleOrLicense = "Diving Medical Officer (GTMÜ / EDTC) #DE-77492",
                    approverOrganization = "Hyperbaric & Sports Medicine Clinic Berlin",
                    approvalDate = "2025-10-15",
                    approvalStampCode = "MED-FIT-TO-DIVE-VALID-2026",
                    approverSignatureJson = "[{\"x\":30,\"y\":60,\"pressure\":1.0},{\"x\":80,\"y\":35,\"pressure\":1.0},{\"x\":140,\"y\":70,\"pressure\":1.0},{\"x\":190,\"y\":50,\"pressure\":1.0}]",
                    approverNotes = "Candidate examined thoroughly: Clear bilateral tympanic membranes, normal lung spirometry (FVC/FEV1 > 95%), ECG normal. Fit for unrestricted recreational scuba diving to 30m depth."
                ),
                DiverDocumentEntity(
                    id = "doc_dan_insurance",
                    diverId = "usr_student",
                    diverName = "Alex Rivera",
                    diverEmail = "alex.rivera@example.com",
                    title = "DAN Europe Diving Accident Insurance (Sport Silver)",
                    category = "INSURANCE",
                    documentNumber = "DAN-EUR-883921",
                    issuingOrganization = "Divers Alert Network (DAN) Europe",
                    issueDate = "2026-01-01",
                    expiryDate = "2027-01-01",
                    notes = "Full international emergency medical coverage: Hyperbaric chamber treatment, emergency air evacuation, search and rescue ($100,000 max), 24/7 dive emergency medical hotline.",
                    cardColorHex = "#C93B2B",
                    approvalStatus = "APPROVED",
                    requiredApproverType = "AGENCY",
                    approverName = "DAN Europe Verification Portal",
                    approverTitleOrLicense = "DAN Emergency Assist Network #DAN-EU-24H",
                    approverOrganization = "DAN Europe Foundation",
                    approvalDate = "2026-01-01",
                    approvalStampCode = "DAN-COVERAGE-ACTIVE-2026-2027",
                    approverSignatureJson = "[{\"x\":20,\"y\":50,\"pressure\":1.0},{\"x\":90,\"y\":50,\"pressure\":1.0},{\"x\":160,\"y\":50,\"pressure\":1.0}]",
                    approverNotes = "Policy verified active. Direct billing agreement in place with hyperbaric facilities worldwide."
                ),
                DiverDocumentEntity(
                    id = "doc_regulator_service",
                    diverId = "usr_student",
                    diverName = "Alex Rivera",
                    diverEmail = "alex.rivera@example.com",
                    title = "Scuba Regulator & BCD Annual Hydro / Service Certificate",
                    category = "EQUIPMENT_RECORD",
                    documentNumber = "SVC-APX-7739",
                    issuingOrganization = "AquaTech Authorized Service Center",
                    issueDate = "2025-08-15",
                    expiryDate = "2026-08-15",
                    notes = "Annual 1st & 2nd stage regulator rebuild and intermediate pressure calibration (9.5 bar).",
                    cardColorHex = "#D97706",
                    approvalStatus = "APPROVED",
                    requiredApproverType = "DIVE_CENTER",
                    approverName = "Marcus Vance",
                    approverTitleOrLicense = "Certified Scuba Technician #TECH-9912",
                    approverOrganization = "AquaTech Dive Services",
                    approvalDate = "2025-08-15",
                    approvalStampCode = "TECH-SVC-OVERHAUL-OK-2025",
                    approverSignatureJson = "[{\"x\":25,\"y\":45,\"pressure\":1.0},{\"x\":80,\"y\":55,\"pressure\":1.0}]",
                    approverNotes = "All O-rings, high-pressure seats, and diaphragms replaced. Annual overhaul expired."
                ),
                DiverDocumentEntity(
                    id = "doc_nitrox_specialty",
                    diverId = "usr_student",
                    diverName = "Alex Rivera",
                    diverEmail = "alex.rivera@example.com",
                    title = "Enriched Air Nitrox Diver (EANx 40%)",
                    category = "CERTIFICATION",
                    documentNumber = "NITROX-994012-APP",
                    issuingOrganization = "SSI / Scuba Schools International",
                    issueDate = "2026-09-18",
                    expiryDate = "Lifetime / No Expiration",
                    notes = "Theory exam passed 100%, gas blending analysis practical session completed.",
                    cardColorHex = "#D97706",
                    approvalStatus = "PENDING",
                    requiredApproverType = "INSTRUCTOR",
                    approverName = "",
                    approverTitleOrLicense = "",
                    approverOrganization = "",
                    approvalDate = "",
                    approvalStampCode = "",
                    approverSignatureJson = "",
                    approverNotes = "Awaiting final practical sign-off on oxygen analyzer calibration and tank labeling from certified instructor."
                ),
                DiverDocumentEntity(
                    id = "doc_liability_waiver",
                    diverId = "usr_student",
                    diverName = "Alex Rivera",
                    diverEmail = "alex.rivera@example.com",
                    title = "General Diving Liability Release & Safe Diving Practices",
                    category = "LIABILITY_WAIVER",
                    documentNumber = "WAIV-2026-AQUA-01",
                    issuingOrganization = "AquaLearn Academy & Dive Base",
                    issueDate = "2026-09-01",
                    expiryDate = "2027-09-01",
                    notes = "Standard liability waiver and risk assumption document signed prior to open water boat excursions.",
                    cardColorHex = "#4A5568",
                    approvalStatus = "APPROVED",
                    requiredApproverType = "DIVE_CENTER",
                    approverName = "Adrian Scheid",
                    approverTitleOrLicense = "Dive Base Operations Manager",
                    approverOrganization = "AquaLearn Base Sharm",
                    approvalDate = "2026-09-01",
                    approvalStampCode = "AQUA-BASE-WAIVER-FILED-2026",
                    approverSignatureJson = "[{\"x\":20,\"y\":45,\"pressure\":1.0},{\"x\":70,\"y\":65,\"pressure\":1.0},{\"x\":130,\"y\":40,\"pressure\":1.0}]",
                    approverNotes = "Waiver on file for current dive season."
                )
            )
            divingDao.insertDocuments(initialDocs)
        }
    }

    private suspend fun seedDiveLogsIfEmpty() {
        val existingDives = divingDao.getAllDiveLogs().firstOrNull()
        if (existingDives.isNullOrEmpty()) {
            val initialDives = listOf(
                DiveLogEntity(
                    id = "dive_001",
                    diveNumber = 1,
                    date = "2026-09-20",
                    timeIn = "09:30 AM",
                    timeOut = "10:18 AM",
                    durationMinutes = 48,
                    siteName = "Ras Mohammed - Shark Reef & Yolanda",
                    location = "Red Sea, Sharm El Sheikh, Egypt",
                    bodyOfWater = "Ocean",
                    entryType = "Boat",
                    maxDepthMeters = 18.2f,
                    avgDepthMeters = 12.8f,
                    waterTempCelsius = 26.5f,
                    airTempCelsius = 32.0f,
                    visibilityMeters = 30.0f,
                    gasType = "Air 21%",
                    tankType = "12L Aluminum",
                    pressureInBar = 205,
                    pressureOutBar = 55,
                    suitType = "5mm Full Suit",
                    weightKg = 6.0f,
                    safetyStopMinutes = 3,
                    safetyStopDepthMeters = 5.0f,
                    safetyStopCompleted = true,
                    sightings = "Green sea turtle, schooling barracudas, bluespotted ribbontail ray, clownfish",
                    notes = "Incredible wall drop-off dive! Perfect buoyancy around the anemone garden. Calm current. Completed mandatory 3-minute safety stop at 5 meters.",
                    diverId = "usr_leo_student",
                    diverEmail = "leo.chen@diver.com",
                    diverName = "Leo Chen",
                    buddyName = "Sarah Connor",
                    buddyEmail = "sarah.c@ocean.net",
                    diveCenterName = "AquaLearn PADI 5-Star Dive Academy",
                    instructorName = "Adrian Scheid",
                    instructorLicense = "PADI OWSI #482910",
                    approvalStatus = "APPROVED_BY_INSTRUCTOR",
                    approvalMethod = "QR_CODE",
                    approvedBy = "Adrian Scheid (Instructor)",
                    approvalTimestamp = System.currentTimeMillis() - (7 * 86400000L),
                    qrStampCode = "AQUALEARN-QR-STAMP-ADRIAN-OWSI482910-SHARKREEF",
                    buddySignaturePathJson = "",
                    buddyApprovalNote = "Verified student open water training dive. Excellent mask clearing and air consumption!"
                ),
                DiveLogEntity(
                    id = "dive_002",
                    diveNumber = 2,
                    date = "2026-09-24",
                    timeIn = "11:15 AM",
                    timeOut = "12:01 PM",
                    durationMinutes = 46,
                    siteName = "SS Thistlegorm Wreck (Upper Decks)",
                    location = "Straits of Gubal, Red Sea",
                    bodyOfWater = "Ocean",
                    entryType = "Boat",
                    maxDepthMeters = 17.8f,
                    avgDepthMeters = 12.0f,
                    waterTempCelsius = 25.0f,
                    airTempCelsius = 30.0f,
                    visibilityMeters = 22.0f,
                    gasType = "Air 21%",
                    tankType = "12L Steel",
                    pressureInBar = 200,
                    pressureOutBar = 60,
                    suitType = "5mm Full Suit",
                    weightKg = 5.5f,
                    safetyStopMinutes = 3,
                    safetyStopDepthMeters = 5.0f,
                    safetyStopCompleted = true,
                    sightings = "Crocodilefish, giant moray eel, batfish, lionfish",
                    notes = "Historic WWII cargo ship wreck! Visited the steam locomotive and truck beds on the deck. Mild current along the mooring line. Good air management.",
                    diverId = "usr_leo_student",
                    diverEmail = "leo.chen@diver.com",
                    diverName = "Leo Chen",
                    buddyName = "Alex Rivera (Rescue Diver #RD-9912)",
                    buddyEmail = "alex.rivera@divebuddy.org",
                    diveCenterName = "AquaLearn Red Sea Expedition",
                    instructorName = "",
                    instructorLicense = "",
                    approvalStatus = "APPROVED_BY_BUDDY_SIGNATURE",
                    approvalMethod = "BUDDY_SIGNATURE",
                    approvedBy = "Alex Rivera",
                    approvalTimestamp = System.currentTimeMillis() - (3 * 86400000L),
                    qrStampCode = "",
                    buddySignaturePathJson = """[{"x":25,"y":55},{"x":40,"y":35},{"x":70,"y":70},{"x":100,"y":30},{"x":130,"y":60},{"x":160,"y":40},{"x":190,"y":65}]""",
                    buddyApprovalNote = "Buddy approved: Great trim through the upper deck passage and respectful non-contact with the wreck."
                ),
                DiveLogEntity(
                    id = "dive_003",
                    diveNumber = 3,
                    date = "2026-09-28",
                    timeIn = "02:00 PM",
                    timeOut = "02:44 PM",
                    durationMinutes = 44,
                    siteName = "The Bells to Blue Hole",
                    location = "Dahab, South Sinai, Egypt",
                    bodyOfWater = "Ocean",
                    entryType = "Shore",
                    maxDepthMeters = 16.5f,
                    avgDepthMeters = 11.5f,
                    waterTempCelsius = 25.5f,
                    airTempCelsius = 29.0f,
                    visibilityMeters = 28.0f,
                    gasType = "Air 21%",
                    tankType = "12L Aluminum",
                    pressureInBar = 210,
                    pressureOutBar = 65,
                    suitType = "3mm Full Suit",
                    weightKg = 5.0f,
                    safetyStopMinutes = 3,
                    safetyStopDepthMeters = 5.0f,
                    safetyStopCompleted = true,
                    sightings = "Anthias clouds, red sea anemonefish, coral grouper, titan triggerfish",
                    notes = "Vertical chimney entry at The Bells down to 16.5m, then gentle drift along the vertical coral wall into the Blue Hole saddle. Gorgeous sunlight beams.",
                    diverId = "usr_leo_student",
                    diverEmail = "leo.chen@diver.com",
                    diverName = "Leo Chen",
                    buddyName = "Elena Rostova",
                    buddyEmail = "elena.rostova@diver.com",
                    diveCenterName = "Dahab Divers Lodge",
                    instructorName = "",
                    instructorLicense = "",
                    approvalStatus = "PENDING",
                    approvalMethod = "NONE",
                    approvedBy = "",
                    approvalTimestamp = 0L,
                    qrStampCode = "",
                    buddySignaturePathJson = "",
                    buddyApprovalNote = ""
                )
            )
            divingDao.insertDiveLogs(initialDives)
        }
    }

    private suspend fun seedStudentsAndAccountsIfEmpty() {
        val existingStudents = divingDao.getAllStudents().firstOrNull()
        if (existingStudents.isNullOrEmpty()) {
            val roster = listOf(
                StudentRosterEntity(
                    id = "stu_leo",
                    fullName = "Leo Chen",
                    email = "leo.chen@diver.com",
                    status = "ACTIVE",
                    enrolledDate = "2026-09-12",
                    certifiedDate = null,
                    targetCourse = "Open Water Diver (18m)",
                    presentationsCompleted = 2,
                    totalPresentations = 3,
                    videosWatched = 2,
                    totalVideos = 3,
                    quizzesPassed = 2,
                    totalQuizzes = 3,
                    examReadinessScore = 75,
                    finalMockExamScore = null,
                    instructorNotes = "Strong trim and finning; reviewing Boyle's law depth consumption."
                ),
                StudentRosterEntity(
                    id = "stu_maya",
                    fullName = "Maya Lin",
                    email = "maya.lin@ocean.io",
                    status = "ACTIVE",
                    enrolledDate = "2026-09-19",
                    certifiedDate = null,
                    targetCourse = "Open Water Diver (18m)",
                    presentationsCompleted = 1,
                    totalPresentations = 3,
                    videosWatched = 2,
                    totalVideos = 3,
                    quizzesPassed = 1,
                    totalQuizzes = 3,
                    examReadinessScore = 50,
                    finalMockExamScore = null,
                    instructorNotes = "Great mask clearing skills. Needs review on ascent safety stop protocols."
                ),
                StudentRosterEntity(
                    id = "stu_david",
                    fullName = "David Morales",
                    email = "d.morales@sea.org",
                    status = "ACTIVE",
                    enrolledDate = "2026-09-25",
                    certifiedDate = null,
                    targetCourse = "Scuba Diver (12m)",
                    presentationsCompleted = 1,
                    totalPresentations = 3,
                    videosWatched = 1,
                    totalVideos = 3,
                    quizzesPassed = 1,
                    totalQuizzes = 3,
                    examReadinessScore = 30,
                    finalMockExamScore = null,
                    instructorNotes = "Recently started. Reviewing physics and equipment modules."
                ),
                StudentRosterEntity(
                    id = "stu_elena",
                    fullName = "Elena Rostova",
                    email = "elena.r@deepblue.com",
                    status = "PAST_STUDENT",
                    enrolledDate = "2026-07-10",
                    certifiedDate = "2026-08-14",
                    targetCourse = "Open Water Diver (18m)",
                    presentationsCompleted = 3,
                    totalPresentations = 3,
                    videosWatched = 3,
                    totalVideos = 3,
                    quizzesPassed = 3,
                    totalQuizzes = 3,
                    examReadinessScore = 100,
                    finalMockExamScore = 96,
                    instructorNotes = "Certified with honors! Flawless out-of-air and buoyancy checks."
                ),
                StudentRosterEntity(
                    id = "stu_marcus",
                    fullName = "Marcus Vance",
                    email = "m.vance@diveclub.net",
                    status = "PAST_STUDENT",
                    enrolledDate = "2026-06-20",
                    certifiedDate = "2026-07-25",
                    targetCourse = "Open Water Diver (18m)",
                    presentationsCompleted = 3,
                    totalPresentations = 3,
                    videosWatched = 3,
                    totalVideos = 3,
                    quizzesPassed = 3,
                    totalQuizzes = 3,
                    examReadinessScore = 95,
                    finalMockExamScore = 90,
                    instructorNotes = "Certified OW diver. Completed all open water training dives with distinction."
                ),
                StudentRosterEntity(
                    id = "stu_sophia",
                    fullName = "Sophia Nguyen",
                    email = "sophia.n@bay.com",
                    status = "PAST_STUDENT",
                    enrolledDate = "2026-05-15",
                    certifiedDate = "2026-06-28",
                    targetCourse = "Open Water Diver (18m)",
                    presentationsCompleted = 3,
                    totalPresentations = 3,
                    videosWatched = 3,
                    totalVideos = 3,
                    quizzesPassed = 3,
                    totalQuizzes = 3,
                    examReadinessScore = 100,
                    finalMockExamScore = 94,
                    instructorNotes = "Successfully graduated and certified. Registered for Advanced Adventurer course."
                )
            )
            divingDao.insertStudents(roster)
        }

        // Seed default instructor & student accounts if not present
        if (divingDao.getAccountByEmailDirect("adrian.scheid01@gmail.com") == null) {
            val instructorAccount = UserAccountEntity(
                id = "usr_adrian_instructor",
                email = "adrian.scheid01@gmail.com",
                passwordHash = hashPassword("instructor123"),
                fullName = "Adrian Scheid",
                isEmailVerified = true,
                verificationCode = "999888",
                role = "INSTRUCTOR",
                phoneNumber = "+49 170 1234567",
                certLevel = "CMAS *** / PADI OWSI",
                certAgency = "VDST / CMAS",
                licenseNumber = "TL2-GER-9921",
                totalLoggedDives = 340,
                ttuExpiryDate = "2027-05-20",
                ttuDoctorName = "Dr. med. Thomas Weber",
                emergencyContactName = "Monika Scheid",
                emergencyContactPhone = "+49 171 9876543",
                insuranceProvider = "DAN Europe",
                insuranceNumber = "DAN-DE-449102",
                suitSize = "L (52)",
                shoeSize = "43",
                bcdSize = "M",
                preferredTank = "12L Stahl DIN",
                instructorAgency = "VDST / CMAS",
                instructorLicenseNumber = "TL2-GER-9921",
                instructorLevel = "Tauchlehrer ** (CMAS Instructor **)",
                teachingSpecialtiesJson = "[\"Nitrox EANx\", \"Tieftauchen\", \"Trockentauchen\", \"Wracktauchen\", \"HLW / O2-Provider\"]",
                instructorValidUntil = "2027-12-31",
                homeOrganizationName = "Tauchclub Delphin Bodensee e.V."
            )
            divingDao.insertUserAccount(instructorAccount)
        }

        if (divingDao.getAccountByEmailDirect("leo.chen@diver.com") == null) {
            val studentAccount = UserAccountEntity(
                id = "usr_leo_student",
                email = "leo.chen@diver.com",
                passwordHash = hashPassword("diver123"),
                fullName = "Leo Chen",
                isEmailVerified = true,
                verificationCode = "888777",
                role = "STUDENT",
                phoneNumber = "+49 176 9988776",
                certLevel = "Open Water Diver (18m)",
                certAgency = "SSI",
                licenseNumber = "SSI-DIV-5512",
                totalLoggedDives = 14,
                ttuExpiryDate = "2027-08-15",
                ttuDoctorName = "Dr. med. Frank Meyer",
                emergencyContactName = "Sarah Chen",
                emergencyContactPhone = "+49 176 1122334",
                insuranceProvider = "AquaMed",
                insuranceNumber = "AQM-78192-DE",
                suitSize = "M",
                shoeSize = "42",
                bcdSize = "M",
                preferredTank = "12L Alu DIN"
            )
            divingDao.insertUserAccount(studentAccount)
        }
    }

    private suspend fun seedOrganizationsIfEmpty() {
        val existingOrgs = divingDao.getAllOrganizations().firstOrNull()
        if (existingOrgs.isNullOrEmpty()) {
            // Organization 1: Tauchverein (CLUB)
            val emergencyClubJson = JSONObject().apply {
                put("nearestChamber", "Druckkammerzentrum Überlingen / Helios Spital (Tel: +49 7551 8370)")
                put("emergencyPhone", "+49 112 (DLRG / Wasserwacht)")
                put("gpsEvacuationPoint", "47.7667° N, 9.1702° E (Bootsrampe Ost)")
                put("o2KitLocation", "Vereinsheim Regatta & Einsatzanhänger")
            }.toString()

            val club = OrganizationEntity(
                id = "org_club_delphin",
                name = "Tauchclub Delphin Bodensee e.V.",
                logoUri = "",
                orgType = "CLUB",
                affiliations = "VDST, CMAS, BLTV",
                location = "Überlingen am Bodensee, Deutschland",
                waterBody = "Bodensee / Überlinger See",
                contactEmail = "vorstand@tc-delphin-bodensee.de",
                contactPhone = "+49 7551 987654",
                website = "www.tc-delphin-bodensee.de",
                emergencyPlanJson = emergencyClubJson,
                activeMembersCount = 4,
                createdAt = System.currentTimeMillis() - (180 * 86400000L)
            )

            // Organization 2: Kommerzielle Tauchbasis / Safari (DIVE_CENTER)
            val emergencyCenterJson = JSONObject().apply {
                put("nearestChamber", "Sharm Medical Hyperbaric Center, Dr. Adel Taher (VHF Ch 16 / Tel: +20 69 366 0922)")
                put("emergencyPhone", "+20 122 218 4800 (Red Sea SAR)")
                put("gpsEvacuationPoint", "27.8585° N, 34.3218° E (Sharm Marina Jetty 4)")
                put("o2KitLocation", "Speedboat 1, Safari-Yacht Horizon, Dive Center Counter")
            }.toString()

            val diveCenter = OrganizationEntity(
                id = "org_center_blue_horizon",
                name = "Blue Horizon Dive Resort & Liveaboard",
                logoUri = "",
                orgType = "DIVE_CENTER",
                affiliations = "PADI 5-Star CDC, SSI Diamond Center, DAN Partner",
                location = "Sharm El Sheikh, Süd-Sinai, Ägypten",
                waterBody = "Rotes Meer / Ras Mohammed / Straits of Tiran",
                contactEmail = "ops@bluehorizon-dive.com",
                contactPhone = "+20 69 360 4420",
                website = "www.bluehorizon-dive.com",
                emergencyPlanJson = emergencyCenterJson,
                activeMembersCount = 3,
                createdAt = System.currentTimeMillis() - (365 * 86400000L)
            )

            divingDao.insertOrganization(club)
            divingDao.insertOrganization(diveCenter)

            // Members for Club Delphin
            val clubMembers = listOf(
                OrganizationMemberEntity(
                    id = "mem_delphin_adrian",
                    orgId = "org_club_delphin",
                    userId = "usr_adrian_instructor",
                    fullName = "Adrian Scheid",
                    email = "adrian.scheid01@gmail.com",
                    phoneNumber = "+49 170 1234567",
                    role = "OWNER",
                    membershipNumber = "TCD-001",
                    joinedDate = "2022-01-15",
                    status = "ACTIVE",
                    certLevel = "CMAS *** / PADI OWSI",
                    totalLoggedDives = 340,
                    specialtiesJson = "[\"Nitrox\", \"Drysuit\", \"Deep Diver\", \"Wreck\"]",
                    ttuExpiryDate = "2027-05-20",
                    ttuDoctorName = "Dr. med. Thomas Weber",
                    emergencyContactName = "Monika Scheid",
                    emergencyContactPhone = "+49 171 998877",
                    insuranceProvider = "DAN Europe Sport Silver",
                    insuranceNumber = "DAN-EUR-883921",
                    signedWaiverDate = "2026-01-10",
                    notes = "1. Vorsitzender & TL2"
                ),
                OrganizationMemberEntity(
                    id = "mem_delphin_marcus",
                    orgId = "org_club_delphin",
                    userId = "usr_marcus",
                    fullName = "Marcus Vance",
                    email = "m.vance@diveclub.net",
                    phoneNumber = "+49 172 3456789",
                    role = "TECH_MANAGER",
                    membershipNumber = "TCD-014",
                    joinedDate = "2023-03-01",
                    status = "ACTIVE",
                    certLevel = "CMAS ** / Silver",
                    totalLoggedDives = 185,
                    specialtiesJson = "[\"Equipment Specialist\", \"Compressor Tech\", \"Nitrox\"]",
                    ttuExpiryDate = "2026-11-05",
                    ttuDoctorName = "Dr. med. Frank Bauer",
                    emergencyContactName = "Sarah Vance",
                    emergencyContactPhone = "+49 172 987654",
                    insuranceProvider = "AquaMed Dive Card",
                    insuranceNumber = "AQM-44912",
                    signedWaiverDate = "2026-02-14",
                    notes = "Gerätewart Flaschenfüllanlage & Leihmaterial"
                ),
                OrganizationMemberEntity(
                    id = "mem_delphin_leo",
                    orgId = "org_club_delphin",
                    userId = "usr_leo_student",
                    fullName = "Leo Chen",
                    email = "leo.chen@diver.com",
                    phoneNumber = "+49 176 8877665",
                    role = "MEMBER",
                    membershipNumber = "TCD-089",
                    joinedDate = "2025-06-01",
                    status = "ACTIVE",
                    certLevel = "Open Water Diver (18m)",
                    totalLoggedDives = 24,
                    specialtiesJson = "[\"Drysuit\"]",
                    ttuExpiryDate = "2025-08-10",
                    ttuDoctorName = "Dr. med. H. Becker",
                    emergencyContactName = "Mei Chen",
                    emergencyContactPhone = "+49 176 1122334",
                    insuranceProvider = "DAN Europe",
                    insuranceNumber = "DAN-DE-5561",
                    signedWaiverDate = "2025-06-01",
                    rentalPreferences = "Jacket M, 7mm suit, 12L Steel",
                    notes = "Aktives Mitglied, trainiert im Hallenbad"
                ),
                OrganizationMemberEntity(
                    id = "mem_delphin_elena",
                    orgId = "org_club_delphin",
                    userId = "usr_elena",
                    fullName = "Elena Rostova",
                    email = "elena.r@deepblue.com",
                    phoneNumber = "+49 173 5544332",
                    role = "INSTRUCTOR",
                    membershipNumber = "TCD-028",
                    joinedDate = "2023-09-15",
                    status = "ACTIVE",
                    certLevel = "CMAS *** / Divemaster",
                    totalLoggedDives = 210,
                    specialtiesJson = "[\"Deep Diver\", \"Group Leader\", \"First Aid O2\"]",
                    ttuExpiryDate = "2027-02-15",
                    ttuDoctorName = "Dr. med. Thomas Weber",
                    emergencyContactName = "Pavel Rostov",
                    emergencyContactPhone = "+49 173 119988",
                    insuranceProvider = "VDST Verbandsversicherung",
                    insuranceNumber = "VDST-88310",
                    signedWaiverDate = "2026-01-15",
                    notes = "Organisiert Mittwochs-Dämmerungstauchen"
                )
            )

            // Members for Blue Horizon Dive Resort
            val centerMembers = listOf(
                OrganizationMemberEntity(
                    id = "mem_bh_adrian",
                    orgId = "org_center_blue_horizon",
                    userId = "usr_adrian_instructor",
                    fullName = "Adrian Scheid",
                    email = "adrian.scheid01@gmail.com",
                    phoneNumber = "+20 100 876 5432",
                    role = "OWNER",
                    membershipNumber = "STAFF-01",
                    joinedDate = "2021-11-01",
                    status = "ACTIVE",
                    certLevel = "PADI Course Director / MSDT",
                    totalLoggedDives = 1450,
                    specialtiesJson = "[\"Nitrox Instructor\", \"Trimix\", \"Sidemount\", \"O2 Provider\"]",
                    ttuExpiryDate = "2027-08-30",
                    ttuDoctorName = "Dr. Adel Taher (Hyperbaric Doctor)",
                    emergencyContactName = "Monika Scheid",
                    emergencyContactPhone = "+49 171 998877",
                    insuranceProvider = "DAN Pro Worldwide",
                    insuranceNumber = "DAN-PRO-99120",
                    signedWaiverDate = "2026-01-01",
                    notes = "Head of Base Operations & Technical Director"
                ),
                OrganizationMemberEntity(
                    id = "mem_bh_sarah",
                    orgId = "org_center_blue_horizon",
                    userId = "usr_sarah",
                    fullName = "Sarah Connor",
                    email = "sarah.c@ocean.net",
                    phoneNumber = "+20 101 234 5678",
                    role = "INSTRUCTOR",
                    membershipNumber = "STAFF-04",
                    joinedDate = "2023-04-10",
                    status = "ACTIVE",
                    certLevel = "PADI MSDT #412093",
                    totalLoggedDives = 780,
                    specialtiesJson = "[\"Boat Guide\", \"Shark Behavior\", \"Nitrox\", \"Deep\"]",
                    ttuExpiryDate = "2027-03-10",
                    ttuDoctorName = "Dr. Adel Taher",
                    emergencyContactName = "John Connor",
                    emergencyContactPhone = "+44 7700 900123",
                    insuranceProvider = "DAN Europe Pro",
                    insuranceNumber = "DAN-PRO-4412",
                    signedWaiverDate = "2026-01-05",
                    notes = "Safari Boat Lead Guide"
                ),
                OrganizationMemberEntity(
                    id = "mem_bh_leo",
                    orgId = "org_center_blue_horizon",
                    userId = "usr_leo_student",
                    fullName = "Leo Chen",
                    email = "leo.chen@diver.com",
                    phoneNumber = "+49 176 8877665",
                    role = "GUEST",
                    membershipNumber = "GST-2026-88",
                    joinedDate = "2026-09-18",
                    status = "ACTIVE",
                    certLevel = "Advanced Open Water Diver",
                    totalLoggedDives = 28,
                    specialtiesJson = "[\"Nitrox\", \"Boat Diver\"]",
                    ttuExpiryDate = "2026-10-28",
                    ttuDoctorName = "Dr. med. Thomas Weber",
                    emergencyContactName = "Mei Chen",
                    emergencyContactPhone = "+49 176 1122334",
                    insuranceProvider = "DAN Europe Sport",
                    insuranceNumber = "DAN-EUR-883921",
                    signedWaiverDate = "2026-09-18",
                    rentalPreferences = "BCD Size M, Reg DIN 300, Fins 42-43, 12L Alu",
                    notes = "Gebucht für Thistlegorm & Ras Mohammed Ausfahrt"
                )
            )

            (clubMembers + centerMembers).forEach { member ->
                divingDao.insertMember(member)
            }

            // 1. Initial Club Outing Event (Delphin Bodensee e.V.)
            val clubEvent = DiveEventEntity(
                id = "evt_delphin_spring_lake",
                orgId = "org_club_delphin",
                title = "Bodensee Steilwand-Tauchen & Clubausfahrt",
                eventType = "CLUB_OUTING",
                siteName = "Überlingen - Parkhaus Post Steilwand",
                location = "Überlingen am Bodensee",
                eventDate = "2026-10-25",
                meetingTime = "09:30",
                departureTime = "10:15",
                maxCapacity = 12,
                registeredCount = 3,
                leadGuideName = "Adrian Scheid (TL2)",
                leadGuideUserId = "usr_adrian_instructor",
                maxDepthMeters = 30.0f,
                requiredMinCert = "CMAS ** / AOWD",
                emergencyBriefing = "Bodensee Kaltwasser-Tauchgang: Zwei getrennte Kaltwasser-Reglersysteme (DIN 300) zwingend erforderlich. Druckkammer Überlingen (Helios Spital) 5 Minuten entfernt. O2-Notfallkoffer am Vereins-Transporter.",
                status = "CONFIRMED",
                createdAt = System.currentTimeMillis() - (5 * 86400000L)
            )

            // Participants for Club Event
            val clubParticipants = listOf(
                DiveEventParticipantEntity(
                    id = "part_club_adrian",
                    eventId = "evt_delphin_spring_lake",
                    memberId = "mem_delphin_adrian",
                    diverName = "Adrian Scheid",
                    diverEmail = "adrian.scheid01@gmail.com",
                    diverCertLevel = "CMAS *** / PADI OWSI",
                    diverTotalDives = 340,
                    checkInStatus = "SAFETY_CHECKED",
                    safetyGatePassed = true,
                    safetyGateIssuesJson = "[]",
                    tankSelection = "12L Steel Doppelpack",
                    gasType = "Air 21%",
                    modMeters = 56.0f,
                    transferCarPoolNote = "Fährt Vereins-Transporter",
                    buddyTeamId = "Team Alpha (Kaltwasser)",
                    groupGuideName = "Adrian Scheid",
                    tankPressureStartBar = 210,
                    tankPressureEndBar = 60
                ),
                DiveEventParticipantEntity(
                    id = "part_club_marcus",
                    eventId = "evt_delphin_spring_lake",
                    memberId = "mem_delphin_marcus",
                    diverName = "Marcus Vance",
                    diverEmail = "m.vance@diveclub.net",
                    diverCertLevel = "CMAS ** / Silver",
                    diverTotalDives = 185,
                    checkInStatus = "SAFETY_CHECKED",
                    safetyGatePassed = true,
                    safetyGateIssuesJson = "[]",
                    tankSelection = "15L Steel",
                    gasType = "Air 21%",
                    modMeters = 56.0f,
                    transferCarPoolNote = "Fahrgemeinschaft mit Adrian",
                    buddyTeamId = "Team Alpha (Kaltwasser)",
                    groupGuideName = "Adrian Scheid",
                    tankPressureStartBar = 205,
                    tankPressureEndBar = 55
                ),
                DiveEventParticipantEntity(
                    id = "part_club_leo",
                    eventId = "evt_delphin_spring_lake",
                    memberId = "mem_delphin_leo",
                    diverName = "Leo Chen",
                    diverEmail = "leo.chen@diver.com",
                    diverCertLevel = "Open Water Diver (18m)",
                    diverTotalDives = 24,
                    checkInStatus = "REGISTERED",
                    safetyGatePassed = false,
                    safetyGateIssuesJson = "[\"TTU ist seit 10.08.2025 abgelaufen!\", \"Brevet Open Water Diver (18m) reicht nicht für geplante 30m Steilwand!\"]",
                    tankSelection = "12L Steel",
                    gasType = "Air 21%",
                    modMeters = 56.0f,
                    rentalGearJson = "[\"Kaltwasser-Reglerset DIN\", \"7mm Halbtrockenanzug M\", \"12L Stahlflasche\"]",
                    transferCarPoolNote = "Benötigt Mitfahrgelegenheit ab Bahnhof Überlingen",
                    buddyTeamId = null,
                    groupGuideName = null,
                    tankPressureStartBar = 200,
                    tankPressureEndBar = 50
                )
            )

            // 2. Initial Dive Center Boat Trip (Blue Horizon Resort)
            val centerEvent = DiveEventEntity(
                id = "evt_bh_ras_mohammed",
                orgId = "org_center_blue_horizon",
                title = "Ras Mohammed Shark & Yolanda Wall Boat Safari",
                eventType = "BOAT_TRIP",
                siteName = "Shark Reef & Yolanda Wreck",
                location = "Ras Mohammed National Park, Red Sea",
                eventDate = "2026-10-22",
                meetingTime = "07:30",
                departureTime = "08:00",
                maxCapacity = 16,
                registeredCount = 3,
                leadGuideName = "Sarah Connor",
                leadGuideUserId = "usr_sarah",
                maxDepthMeters = 26.0f,
                requiredMinCert = "Advanced Open Water Diver",
                emergencyBriefing = "Tages-Bootsausfahrt auf M/Y Blue Horizon. Strömungstauchgang (Drift) mit Schlauchboot-Abholung (Zodiac). Signalboje (SMB) pro Buddy-Team Pflicht. Dekompressionskammer Sharm El Sheikh VHF Kanal 16. O2-Notfallkoffer und DAN Sauerstoff-Set auf dem Hauptdeck.",
                status = "CONFIRMED",
                createdAt = System.currentTimeMillis() - (7 * 86400000L)
            )

            // Participants for Dive Center Event
            val centerParticipants = listOf(
                DiveEventParticipantEntity(
                    id = "part_bh_sarah",
                    eventId = "evt_bh_ras_mohammed",
                    memberId = "mem_bh_sarah",
                    diverName = "Sarah Connor",
                    diverEmail = "sarah.c@ocean.net",
                    diverCertLevel = "PADI MSDT #412093",
                    diverTotalDives = 780,
                    checkInStatus = "ON_BOARD",
                    safetyGatePassed = true,
                    safetyGateIssuesJson = "[]",
                    tankSelection = "12L Aluminum EANx32",
                    gasType = "Nitrox 32%",
                    modMeters = 33.7f,
                    buddyTeamId = "Guide Team",
                    groupGuideName = "Sarah Connor",
                    tankPressureStartBar = 215,
                    tankPressureEndBar = 55
                ),
                DiveEventParticipantEntity(
                    id = "part_bh_adrian",
                    eventId = "evt_bh_ras_mohammed",
                    memberId = "mem_bh_adrian",
                    diverName = "Adrian Scheid",
                    diverEmail = "adrian.scheid01@gmail.com",
                    diverCertLevel = "PADI Course Director / MSDT",
                    diverTotalDives = 1450,
                    checkInStatus = "ON_BOARD",
                    safetyGatePassed = true,
                    safetyGateIssuesJson = "[]",
                    tankSelection = "12L Aluminum EANx32",
                    gasType = "Nitrox 32%",
                    modMeters = 33.7f,
                    buddyTeamId = "Team Blue Horizon 1",
                    groupGuideName = "Sarah Connor",
                    tankPressureStartBar = 210,
                    tankPressureEndBar = 60
                ),
                DiveEventParticipantEntity(
                    id = "part_bh_leo",
                    eventId = "evt_bh_ras_mohammed",
                    memberId = "mem_bh_leo",
                    diverName = "Leo Chen",
                    diverEmail = "leo.chen@diver.com",
                    diverCertLevel = "Advanced Open Water Diver",
                    diverTotalDives = 28,
                    checkInStatus = "SAFETY_CHECKED",
                    safetyGatePassed = true,
                    safetyGateIssuesJson = "[]",
                    tankSelection = "12L Aluminum EANx32",
                    gasType = "Nitrox 32%",
                    modMeters = 33.7f,
                    rentalGearJson = "[\"Jacket Size M\", \"Regulator DIN 300\", \"Fins 42-43\"]",
                    transferCarPoolNote = "Hotel Shuttle ab Mövenpick Naama Bay",
                    buddyTeamId = "Team Blue Horizon 1",
                    groupGuideName = "Sarah Connor",
                    tankPressureStartBar = 205,
                    tankPressureEndBar = 50
                )
            )

            divingDao.insertEvent(clubEvent)
            divingDao.insertEvent(centerEvent)
            (clubParticipants + centerParticipants).forEach { participant ->
                divingDao.insertParticipant(participant)
            }

            // 3. Initial Inventory Items (Club Delphin & Blue Horizon Resort)
            val clubInventory = listOf(
                InventoryItemEntity(
                    id = "inv_club_comp_bauer",
                    orgId = "org_club_delphin",
                    itemType = "COMPRESSOR",
                    internalCode = "COMP-01",
                    brand = "Bauer Kompressoren",
                    model = "Mariner 250 Silent (200/300 bar)",
                    serialNumber = "BAU-M250-9941",
                    sizeOrSpec = "250 L/min • 3-stufig • P31 Filtersystem",
                    status = "AVAILABLE",
                    nextServiceDate = "2027-05-30",
                    lastServiceDate = "2026-05-20",
                    serviceNotes = "Jahreswartung durch Bauer Service München durchgeführt. Ölwechsel Synthetiköl.",
                    compressorHours = 142.5f,
                    compressorFilterMaxHours = 50f,
                    compressorFilterCurrentHours = 38.0f,
                    airAnalysisDate = "2026-09-15"
                ),
                InventoryItemEntity(
                    id = "inv_club_tank_01",
                    orgId = "org_club_delphin",
                    itemType = "TANK",
                    internalCode = "T-12-01",
                    brand = "Eurocylinder",
                    model = "12L Stahl lang",
                    serialNumber = "EC-88192-232",
                    sizeOrSpec = "12L Stahl • 232 bar • Mono-Ventil DIN",
                    status = "AVAILABLE",
                    nextServiceDate = "2028-05-15",
                    lastServiceDate = "2025-05-15",
                    serviceNotes = "TÜV Süd Prüfprotokoll #8841-A: Innendruckprüfung & optische Prüfung ohne Beanstandung bestanden."
                ),
                InventoryItemEntity(
                    id = "inv_club_tank_02",
                    orgId = "org_club_delphin",
                    itemType = "TANK",
                    internalCode = "T-15-01",
                    brand = "Eurocylinder",
                    model = "15L Stahl",
                    serialNumber = "EC-99301-232",
                    sizeOrSpec = "15L Stahl • 232 bar • Doppelabgang",
                    status = "AVAILABLE",
                    nextServiceDate = "2027-09-30",
                    lastServiceDate = "2024-09-30",
                    serviceNotes = "TÜV Rheinland Prüfbericht #7720-B."
                ),
                InventoryItemEntity(
                    id = "inv_club_tank_03_expired",
                    orgId = "org_club_delphin",
                    itemType = "TANK",
                    internalCode = "T-12-04 (TÜV FÄLLIG)",
                    brand = "Faber",
                    model = "12L Stahl",
                    serialNumber = "FAB-4421-200",
                    sizeOrSpec = "12L Stahl • 200 bar",
                    status = "LOCKED_EXPIRED",
                    nextServiceDate = "2025-06-01",
                    lastServiceDate = "2022-06-01",
                    serviceNotes = "TÜV seit 01.06.2025 ABGELAUFEN! Automatische Verleih-Sperre aktiv. Keine Füllung erlaubt!"
                ),
                InventoryItemEntity(
                    id = "inv_club_reg_01",
                    orgId = "org_club_delphin",
                    itemType = "REGULATOR",
                    internalCode = "REG-APX-01",
                    brand = "Apeks",
                    model = "XTX 50 + Oktopus",
                    serialNumber = "APX-55120-DIN",
                    sizeOrSpec = "Kaltwasser-zertifiziert DIN 300 bar",
                    status = "AVAILABLE",
                    nextServiceDate = "2027-03-20",
                    lastServiceDate = "2026-03-20",
                    serviceNotes = "Jährliche Revision bei Fachhändler TauchSport München. Mitteldruck stabil bei 9.5 bar."
                ),
                InventoryItemEntity(
                    id = "inv_club_reg_02_rented",
                    orgId = "org_club_delphin",
                    itemType = "REGULATOR",
                    internalCode = "REG-APX-02",
                    brand = "Apeks",
                    model = "XTX 200 DIN 300",
                    serialNumber = "APX-77402-DIN",
                    sizeOrSpec = "Kaltwasser-zertifiziert DIN 300 bar",
                    status = "RENTED",
                    nextServiceDate = "2027-06-15",
                    lastServiceDate = "2026-06-15",
                    serviceNotes = "Revision durchgeführt.",
                    currentRenterMemberId = "mem_delphin_marcus",
                    currentRenterName = "Marcus Vance",
                    currentRentalDueDate = "2026-10-28"
                ),
                InventoryItemEntity(
                    id = "inv_club_bcd_01",
                    orgId = "org_club_delphin",
                    itemType = "BCD",
                    internalCode = "BCD-01",
                    brand = "Mares",
                    model = "Rover Pro",
                    serialNumber = "MAR-2291",
                    sizeOrSpec = "Größe M • 150N Auftrieb",
                    status = "AVAILABLE",
                    nextServiceDate = "2027-08-10",
                    lastServiceDate = "2026-08-10",
                    serviceNotes = "Schnellentlüfter & Inflator desinfiziert und geprüft."
                ),
                InventoryItemEntity(
                    id = "inv_club_o2_01",
                    orgId = "org_club_delphin",
                    itemType = "O2_KIT",
                    internalCode = "O2-WENOLL",
                    brand = "Wenoll",
                    model = "100% Notfall-Sauerstoffkoffer",
                    serialNumber = "WEN-9921",
                    sizeOrSpec = "2L O2 Stahl 200 bar • Konstantfluss & Demand",
                    status = "AVAILABLE",
                    nextServiceDate = "2027-12-31",
                    lastServiceDate = "2026-01-10",
                    serviceNotes = "O2-Flasche voll (200 bar), Beatmungsmasken steril verpackt."
                )
            )

            val centerInventory = listOf(
                InventoryItemEntity(
                    id = "inv_bh_comp_poseidon",
                    orgId = "org_center_blue_horizon",
                    itemType = "COMPRESSOR",
                    internalCode = "COMP-01",
                    brand = "Bauer Kompressoren",
                    model = "Poseidon PE 300 Silent",
                    serialNumber = "POS-PE300-8812",
                    sizeOrSpec = "300 L/min • B-Timer Filterüberwachung",
                    status = "AVAILABLE",
                    nextServiceDate = "2027-08-01",
                    lastServiceDate = "2026-08-01",
                    serviceNotes = "Filterwechsel & B-Timer Reset durchgeführt.",
                    compressorHours = 320.0f,
                    compressorFilterMaxHours = 50f,
                    compressorFilterCurrentHours = 46.5f,
                    airAnalysisDate = "2026-08-01"
                ),
                InventoryItemEntity(
                    id = "inv_bh_tank_alu_01",
                    orgId = "org_center_blue_horizon",
                    itemType = "TANK",
                    internalCode = "ALU-S80-01",
                    brand = "Luxfer",
                    model = "Aluminum S80 (11.1L)",
                    serialNumber = "LUX-88201-207",
                    sizeOrSpec = "Alu 80 cuft • 207 bar • O2 Clean für Nitrox",
                    status = "AVAILABLE",
                    nextServiceDate = "2027-11-20",
                    lastServiceDate = "2025-11-20",
                    serviceNotes = "Hydrostatische Prüfung & O2-Reinigung gültig."
                ),
                InventoryItemEntity(
                    id = "inv_bh_reg_01_rented",
                    orgId = "org_center_blue_horizon",
                    itemType = "REGULATOR",
                    internalCode = "REG-AL-01",
                    brand = "Aqualung",
                    model = "Core Supreme DIN",
                    serialNumber = "AL-7741-DIN",
                    sizeOrSpec = "DIN 300 bar • Kaltwassersystem",
                    status = "RENTED",
                    nextServiceDate = "2027-05-10",
                    lastServiceDate = "2026-05-10",
                    serviceNotes = "Geprüft.",
                    currentRenterMemberId = "mem_bh_sarah",
                    currentRenterName = "Sarah Connor",
                    currentRentalDueDate = "2026-10-24"
                ),
                InventoryItemEntity(
                    id = "inv_bh_comp_suunto",
                    orgId = "org_center_blue_horizon",
                    itemType = "COMPUTER",
                    internalCode = "TC-01",
                    brand = "Suunto",
                    model = "Zoop Novo",
                    serialNumber = "SUU-229188",
                    sizeOrSpec = "Luft & Nitrox bis 50%",
                    status = "AVAILABLE",
                    nextServiceDate = "2028-01-01",
                    lastServiceDate = "2026-01-01",
                    serviceNotes = "Neue O-Ring Dichtung & Markenzelle."
                )
            )

            val initialRentals = listOf(
                RentalTransactionEntity(
                    id = "rent_club_marcus",
                    orgId = "org_club_delphin",
                    itemId = "inv_club_reg_02_rented",
                    itemCode = "REG-APX-02",
                    itemDescription = "Apeks XTX 200 DIN 300",
                    memberId = "mem_delphin_marcus",
                    renterName = "Marcus Vance",
                    checkOutDate = "2026-10-14",
                    dueDate = "2026-10-28",
                    status = "ACTIVE",
                    returnCondition = "INTACT",
                    damageReportNotes = "",
                    staffUserId = "usr_adrian_instructor"
                ),
                RentalTransactionEntity(
                    id = "rent_bh_sarah",
                    orgId = "org_center_blue_horizon",
                    itemId = "inv_bh_reg_01_rented",
                    itemCode = "REG-AL-01",
                    itemDescription = "Aqualung Core Supreme DIN",
                    memberId = "mem_bh_sarah",
                    renterName = "Sarah Connor",
                    checkOutDate = "2026-10-12",
                    dueDate = "2026-10-24",
                    status = "ACTIVE",
                    returnCondition = "INTACT",
                    damageReportNotes = "",
                    staffUserId = "usr_adrian_instructor"
                )
            )

            clubInventory.forEach { divingDao.insertInventoryItem(it) }
            centerInventory.forEach { divingDao.insertInventoryItem(it) }
            initialRentals.forEach { divingDao.insertRentalTransaction(it) }

            // 4. Initial Organization Documents & Digital Signatures (Phase 4)
            val clubDocuments = listOf(
                OrganizationDocumentEntity(
                    id = "doc_delphin_satzung",
                    orgId = "org_club_delphin",
                    title = "Vereinssatzung Tauchclub Delphin e.V.",
                    category = "CONSTITUTION",
                    fileName = "Satzung_TC_Delphin_e_V_2026.pdf",
                    fileSizeBytes = 245000L,
                    visibility = "ALL_MEMBERS",
                    requiresAcknowledgment = true,
                    acknowledgmentCount = 2,
                    version = "3.2",
                    documentContentSummary = "Offizielle Vereinssatzung: Rechte und Pflichten der Mitglieder, Gemeinnützigkeit, Beitragsordnung, Organe des Vereins und Beschlussfähigkeit der Jahreshauptversammlung.",
                    uploadedByName = "Adrian Scheid (Vorstand)"
                ),
                OrganizationDocumentEntity(
                    id = "doc_delphin_seeordnung",
                    orgId = "org_club_delphin",
                    title = "Gewässer- & Tauchplatzordnung Starnberger See",
                    category = "RULES",
                    fileName = "Gewässerordnung_Starnberger_See.pdf",
                    fileSizeBytes = 180000L,
                    visibility = "ALL_MEMBERS",
                    requiresAcknowledgment = true,
                    acknowledgmentCount = 2,
                    version = "2.0",
                    documentContentSummary = "Regelungen zum Tauchen am Vereinssteg: Schutz der Schilf- und Flachwasserzonen, Bojenpflicht bei Freiwasser-Querung und Sicherheitsregeln für Nachttauchgänge.",
                    uploadedByName = "Adrian Scheid (Vorstand)"
                ),
                OrganizationDocumentEntity(
                    id = "doc_delphin_jhv",
                    orgId = "org_club_delphin",
                    title = "Protokoll der Jahreshauptversammlung 2026",
                    category = "PROTOCOL",
                    fileName = "JHV_Protokoll_2026_Final.pdf",
                    fileSizeBytes = 310000L,
                    visibility = "ALL_MEMBERS",
                    requiresAcknowledgment = false,
                    acknowledgmentCount = 0,
                    version = "1.0",
                    documentContentSummary = "Bericht des Vorstands, Kassenprüfung, Entlastung des Vorstands, Wahl des neuen Gerätewarts (Marcus Vance) und Beschluss zur Anschaffung der Bauer Mariner Kompressoranlage.",
                    uploadedByName = "Adrian Scheid (Vorstand)"
                ),
                OrganizationDocumentEntity(
                    id = "doc_delphin_intern",
                    orgId = "org_club_delphin",
                    title = "Internes Vorstandsprotokoll & Haushaltsplan Q3/Q4",
                    category = "PROTOCOL",
                    fileName = "Vorstand_Intern_Q3_2026.pdf",
                    fileSizeBytes = 125000L,
                    visibility = "ADMINS_ONLY",
                    requiresAcknowledgment = false,
                    acknowledgmentCount = 0,
                    version = "1.0",
                    documentContentSummary = "Vertraulicher Finanzstatus, Beitragsrückstände und Investitionsbudget für neue Kaltwasser-Leihregler in 2027.",
                    uploadedByName = "Adrian Scheid (Vorstand)"
                )
            )

            val centerDocuments = listOf(
                OrganizationDocumentEntity(
                    id = "doc_bh_waiver",
                    orgId = "org_center_blue_horizon",
                    title = "Haftungsausschluss & Risikoaufklärung (Standard Waiver)",
                    category = "WAIVER",
                    fileName = "Blue_Horizon_Liability_Waiver_2026.pdf",
                    fileSizeBytes = 210000L,
                    visibility = "PUBLIC",
                    requiresAcknowledgment = true,
                    acknowledgmentCount = 2,
                    version = "4.1",
                    documentContentSummary = "Rechtlich geprüfter Haftungsausschluss gemäß WRSTC/RSTC Standards: Aufklärung über Risiken des Gerätetauchens, Eigenverantwortung, Ausschluss einfacher Fahrlässigkeit und Bestätigung der körperlichen Tauchtauglichkeit.",
                    uploadedByName = "Sarah Connor (Basisleiterin)"
                ),
                OrganizationDocumentEntity(
                    id = "doc_bh_rules",
                    orgId = "org_center_blue_horizon",
                    title = "Basis- & Bootsordnung M/Y Blue Horizon",
                    category = "RULES",
                    fileName = "Bootsordnung_Safaris.pdf",
                    fileSizeBytes = 195000L,
                    visibility = "PUBLIC",
                    requiresAcknowledgment = true,
                    acknowledgmentCount = 2,
                    version = "2.1",
                    documentContentSummary = "Sicherheits- und Verhaltensregeln an Bord: Rauchverbot im Salon, Flaschen-Sicherung am Tauchdeck, Zodiac-Einstiegsprotokoll und Notfall-Sammelplätze.",
                    uploadedByName = "Sarah Connor (Basisleiterin)"
                ),
                OrganizationDocumentEntity(
                    id = "doc_bh_map",
                    orgId = "org_center_blue_horizon",
                    title = "Tauchplatzkarte Shark & Yolanda Reef",
                    category = "MAP",
                    fileName = "Map_Ras_Mohammed_Shark_Yolanda.pdf",
                    fileSizeBytes = 890000L,
                    visibility = "PUBLIC",
                    requiresAcknowledgment = false,
                    acknowledgmentCount = 0,
                    version = "1.0",
                    documentContentSummary = "Detaillierte Tiefen- und Strömungskarte: Shark Reef Steilwand bis 750m, Anemonen City und die Wrackreste der Yolanda mit Toiletten-Fracht.",
                    uploadedByName = "Sarah Connor (Basisleiterin)"
                )
            )

            val initialSignatures = listOf(
                DocumentSignatureEntity(
                    id = "sig_delphin_adrian_satzung",
                    orgId = "org_club_delphin",
                    documentId = "doc_delphin_satzung",
                    documentTitle = "Vereinssatzung Tauchclub Delphin e.V.",
                    memberId = "mem_delphin_adrian",
                    signerName = "Adrian Scheid",
                    signerEmail = "adrian.scheid01@gmail.com",
                    signatureType = "CHECKBOX_CONFIRMED",
                    signedTimestamp = System.currentTimeMillis() - 86400000L * 30
                ),
                DocumentSignatureEntity(
                    id = "sig_delphin_adrian_see",
                    orgId = "org_club_delphin",
                    documentId = "doc_delphin_seeordnung",
                    documentTitle = "Gewässer- & Tauchplatzordnung Starnberger See",
                    memberId = "mem_delphin_adrian",
                    signerName = "Adrian Scheid",
                    signerEmail = "adrian.scheid01@gmail.com",
                    signatureType = "CHECKBOX_CONFIRMED",
                    signedTimestamp = System.currentTimeMillis() - 86400000L * 30
                ),
                DocumentSignatureEntity(
                    id = "sig_delphin_marcus_satzung",
                    orgId = "org_club_delphin",
                    documentId = "doc_delphin_satzung",
                    documentTitle = "Vereinssatzung Tauchclub Delphin e.V.",
                    memberId = "mem_delphin_marcus",
                    signerName = "Marcus Vance",
                    signerEmail = "m.vance@diveclub.net",
                    signatureType = "CHECKBOX_CONFIRMED",
                    signedTimestamp = System.currentTimeMillis() - 86400000L * 14
                ),
                DocumentSignatureEntity(
                    id = "sig_delphin_marcus_see",
                    orgId = "org_club_delphin",
                    documentId = "doc_delphin_seeordnung",
                    documentTitle = "Gewässer- & Tauchplatzordnung Starnberger See",
                    memberId = "mem_delphin_marcus",
                    signerName = "Marcus Vance",
                    signerEmail = "m.vance@diveclub.net",
                    signatureType = "CHECKBOX_CONFIRMED",
                    signedTimestamp = System.currentTimeMillis() - 86400000L * 14
                ),
                DocumentSignatureEntity(
                    id = "sig_bh_sarah_waiver",
                    orgId = "org_center_blue_horizon",
                    documentId = "doc_bh_waiver",
                    documentTitle = "Haftungsausschluss & Risikoaufklärung (Standard Waiver)",
                    memberId = "mem_bh_sarah",
                    signerName = "Sarah Connor",
                    signerEmail = "sarah.c@ocean.net",
                    signatureType = "CHECKBOX_CONFIRMED",
                    signedTimestamp = System.currentTimeMillis() - 86400000L * 20
                ),
                DocumentSignatureEntity(
                    id = "sig_bh_sarah_rules",
                    orgId = "org_center_blue_horizon",
                    documentId = "doc_bh_rules",
                    documentTitle = "Basis- & Bootsordnung M/Y Blue Horizon",
                    memberId = "mem_bh_sarah",
                    signerName = "Sarah Connor",
                    signerEmail = "sarah.c@ocean.net",
                    signatureType = "CHECKBOX_CONFIRMED",
                    signedTimestamp = System.currentTimeMillis() - 86400000L * 20
                ),
                DocumentSignatureEntity(
                    id = "sig_bh_adrian_waiver",
                    orgId = "org_center_blue_horizon",
                    documentId = "doc_bh_waiver",
                    documentTitle = "Haftungsausschluss & Risikoaufklärung (Standard Waiver)",
                    memberId = "mem_bh_adrian",
                    signerName = "Adrian Scheid",
                    signerEmail = "adrian.scheid01@gmail.com",
                    signatureType = "CHECKBOX_CONFIRMED",
                    signedTimestamp = System.currentTimeMillis() - 86400000L * 10
                ),
                DocumentSignatureEntity(
                    id = "sig_bh_adrian_rules",
                    orgId = "org_center_blue_horizon",
                    documentId = "doc_bh_rules",
                    documentTitle = "Basis- & Bootsordnung M/Y Blue Horizon",
                    memberId = "mem_bh_adrian",
                    signerName = "Adrian Scheid",
                    signerEmail = "adrian.scheid01@gmail.com",
                    signatureType = "CHECKBOX_CONFIRMED",
                    signedTimestamp = System.currentTimeMillis() - 86400000L * 10
                )
            )

            clubDocuments.forEach { divingDao.insertOrganizationDocument(it) }
            centerDocuments.forEach { divingDao.insertOrganizationDocument(it) }
            initialSignatures.forEach { divingDao.insertDocumentSignature(it) }
        }
    }

    private suspend fun seedDatabase() {
        // 1. Initial Modules
        val modules = listOf(
            CourseModuleEntity(
                id = "mod_physics",
                title = "Physics & Pressure",
                subtitle = "Boyle's Law, Depth & Air Consumption",
                description = "Master how water pressure changes with depth, how air volume compresses, and the golden rules of scuba diving.",
                iconName = "compress",
                orderIndex = 1,
                estimatedMin = 35
            ),
            CourseModuleEntity(
                id = "mod_equipment",
                title = "Scuba Equipment Essentials",
                subtitle = "Regulators, BCD, Cylinders & BWRAF",
                description = "Understand how life-support equipment functions and execute flawless pre-dive buddy safety checks.",
                iconName = "settings",
                orderIndex = 2,
                estimatedMin = 40
            ),
            CourseModuleEntity(
                id = "mod_physiology",
                title = "Diving Physiology & Safety",
                subtitle = "Nitrogen, Narcosis, DCS & Ascent Rates",
                description = "Understand how breathing compressed gas affects the body, how to prevent decompression sickness, and ascent protocols.",
                iconName = "health_and_safety",
                orderIndex = 3,
                estimatedMin = 45
            ),
            CourseModuleEntity(
                id = "mod_emergencies",
                title = "Underwater Problem Solving",
                subtitle = "Mask Clearing, Out-of-Air & Cramps",
                description = "Build self-reliance and buddy teamwork: regulator recovery, mask clearing, out-of-air ascents, and calm problem resolution.",
                iconName = "warning",
                orderIndex = 4,
                estimatedMin = 30
            ),
            CourseModuleEntity(
                id = "mod_planning",
                title = "Dive Planning & Exam Review",
                subtitle = "Dive Tables, NDL & Final Certification Exam",
                description = "Learn how to calculate No-Decompression Limits, execute surface intervals, and pass your Open Water certification exam.",
                iconName = "school",
                orderIndex = 5,
                estimatedMin = 50
            )
        )
        divingDao.insertModules(modules)

        // 2. Initial Presentations
        val presPhysicsSlides = JSONArray().apply {
            put(JSONObject().apply {
                put("slideNumber", 1)
                put("title", "Pressure Underwater & Atmospheres (ATA)")
                put("subtitle", "Water is 800x denser than air, creating rapid pressure increases.")
                put("bulletPoints", JSONArray().apply {
                    put("At surface (0m / 0ft): Pressure is 1.0 ATA (Atmosphere Absolute).")
                    put("Every 10 meters (33 feet) of seawater adds another 1.0 ATA of hydrostatic pressure.")
                    put("At 10m (33ft): Total ambient pressure is 2.0 ATA (Double surface pressure!).")
                    put("At 20m (66ft): Total ambient pressure is 3.0 ATA.")
                    put("At 30m (99ft): Total ambient pressure is 4.0 ATA.")
                })
                put("diagramType", "BOYLES_LAW")
                put("instructorNotes", "Emphasize that the most dramatic relative pressure change occurs in the first 10 meters (from 1 to 2 ATA, a 100% increase).")
                put("keyTakeaway", "Every 10m of saltwater adds 1 ATA. Pressure at depth = (Depth in meters / 10) + 1.")
            })
            put(JSONObject().apply {
                put("slideNumber", 2)
                put("title", "Boyle's Law: Pressure vs Gas Volume")
                put("subtitle", "As pressure increases, gas volume decreases proportionally.")
                put("bulletPoints", JSONArray().apply {
                    put("At Surface (1 ATA): Volume is 1.0 (100%), Gas Density is 1x.")
                    put("At 10m (2 ATA): Gas volume is halved to 1/2 (50%), Density is 2x.")
                    put("At 20m (3 ATA): Gas volume compresses to 1/3 (33%), Density is 3x.")
                    put("At 30m (4 ATA): Gas volume compresses to 1/4 (25%), Density is 4x.")
                    put("Air consumption: You breathe 2x faster at 10m, and 4x faster at 30m!")
                })
                put("diagramType", "BOYLES_LAW")
                put("instructorNotes", "Remind students that your tank air lasts a fraction of the time at depth because you breathe denser gas with every breath cycle.")
                put("keyTakeaway", "Pressure and gas volume are inversely related. Air consumption accelerates as you go deeper.")
            })
            put(JSONObject().apply {
                put("slideNumber", 3)
                put("title", "The #1 Golden Rule of Scuba Diving")
                put("subtitle", "Breathe continuously and never, ever hold your breath!")
                put("bulletPoints", JSONArray().apply {
                    put("If you hold your breath while ascending, expanding air will overexpand your lungs.")
                    put("Lung overexpansion injury (Arterial Gas Embolism, Pneumothorax) can occur from as shallow as 1.5 meters (4-5 feet)!")
                    put("Rule: Always breathe normally, deeply, and continuously.")
                    put("If your regulator falls out: Blow continuous tiny bubbles while recovering it.")
                })
                put("diagramType", "GENERAL_CHECKLIST")
                put("instructorNotes", "This is the single most tested rule in every certification agency exam. Zero tolerance for holding breath.")
                put("keyTakeaway", "NEVER hold your breath while scuba diving. Always maintain an open airway.")
            })
            put(JSONObject().apply {
                put("slideNumber", 4)
                put("title", "Equalization Techniques for Air Spaces")
                put("subtitle", "Preventing middle ear, sinus, and mask squeezes.")
                put("bulletPoints", JSONArray().apply {
                    put("Pinch nose and blow gently (Valsalva maneuver) every meter during descent.")
                    put("Swallow, wiggle jaw, or tilt head side to side.")
                    put("Equalize EARLY and OFTEN before feeling pain or pressure.")
                    put("Mask squeeze: Blow a gentle puff of air through your nose into the mask.")
                    put("If you cannot equalize: STOP descent, ascend slightly until clear, retry.")
                })
                put("diagramType", "GENERAL_CHECKLIST")
                put("instructorNotes", "Never force equalization violently. If pain persists, abort descent.")
                put("keyTakeaway", "Equalize before discomfort starts. Never dive with a head cold or decongestants.")
            })
        }

        val presEquipmentSlides = JSONArray().apply {
            put(JSONObject().apply {
                put("slideNumber", 1)
                put("title", "Scuba Regulator System Anatomy")
                put("subtitle", "Delivering breathing gas on demand at ambient water pressure.")
                put("bulletPoints", JSONArray().apply {
                    put("First Stage: Mounts to tank valve; reduces high cylinder pressure (~200 bar/3000 psi) to intermediate pressure (~9-10 bar).")
                    put("Primary Second Stage: Mouthpiece you breathe from; delivers air at exact surrounding ambient water pressure.")
                    put("Alternate Air Source (Octopus): Bright yellow backup regulator for your buddy in emergencies.")
                    put("Submersible Pressure Gauge (SPG) / Console: Displays remaining tank air in bar or psi.")
                    put("Low-Pressure Inflator Hose: Powers the BCD bladder.")
                })
                put("diagramType", "REGULATOR_SYSTEM")
                put("instructorNotes", "Demonstrate how the octopus must always be secured in the chest triangle area between chin and lower ribcage for instant access.")
                put("keyTakeaway", "The regulator reduces tank pressure in two stages so breathing is effortless at any depth.")
            })
            put(JSONObject().apply {
                put("slideNumber", 2)
                put("title", "The BWRAF Pre-Dive Buddy Check")
                put("subtitle", "Mnemonic: 'Begin With Review And Friend' or 'Burger With Relish And Fries'")
                put("bulletPoints", JSONArray().apply {
                    put("B - BCD: Test oral inflator, power inflator, and dump valves.")
                    put("W - Weights: Check right-hand release, free and clear for emergency ditching.")
                    put("R - Releases: Fasten all BCD straps, chest clip, and tank band.")
                    put("A - Air: Test breathe primary and octopus while watching SPG needle (needle must not dip).")
                    put("F - Final OK: Check mask, fins, snorkel, dive computer, and mental readiness.")
                })
                put("diagramType", "BWRAF_BUDDY")
                put("instructorNotes", "Have students recite the mnemonic and physically execute the check on their buddy.")
                put("keyTakeaway", "Every dive begins with BWRAF. Never enter the water without a buddy check.")
            })
            put(JSONObject().apply {
                put("slideNumber", 3)
                put("title", "Buoyancy Control Device (BCD) Operation")
                put("subtitle", "Achieving positive, negative, and neutral buoyancy effortlessly.")
                put("bulletPoints", JSONArray().apply {
                    put("Positive Buoyancy: Floating at surface. Inflate BCD fully on entry and upon surfacing.")
                    put("Negative Buoyancy: Sinking. Deflate BCD slowly with dump valve held high to descend.")
                    put("Neutral Buoyancy: Weightless like an astronaut underwater. Add tiny short bursts of air as you descend.")
                    put("Never use BCD as an elevator to ascend! Kick gently and vent expanding air.")
                })
                put("diagramType", "GENERAL_CHECKLIST")
                put("instructorNotes", "Teach fine-tuning with lung volume: breathing in raises you slightly, exhaling lowers you.")
                put("keyTakeaway", "Vent BCD air as you ascend because air in the bladder expands and increases ascent speed.")
            })
        }

        val presPhysiologySlides = JSONArray().apply {
            put(JSONObject().apply {
                put("slideNumber", 1)
                put("title", "Nitrogen Absorption & Decompression Sickness (DCS)")
                put("subtitle", "Henry's Law: Gas dissolves into body tissues under pressure.")
                put("bulletPoints", JSONArray().apply {
                    put("Air is 21% Oxygen and 78% Nitrogen. Nitrogen is inert and does not get metabolized.")
                    put("At depth, high ambient pressure forces Nitrogen to dissolve into bloodstream and tissues.")
                    put("Ascending too quickly: Nitrogen comes out of solution forming dangerous microbubbles ('the bends').")
                    put("Symptoms of DCS: Joint pain, rash, extreme fatigue, numbness, paralysis, shortness of breath.")
                    put("First Aid for DCS: 100% Pure Oxygen at surface, emergency medical evacuation to Hyperbaric Chamber.")
                })
                put("diagramType", "ASCENT_PROFILE")
                put("instructorNotes", "Dehydration, cold water, heavy exertion, and alcohol drastically increase DCS risk.")
                put("keyTakeaway", "Prevent DCS by ascending slowly, respecting dive computer limits, and staying hydrated.")
            })
            put(JSONObject().apply {
                put("slideNumber", 2)
                put("title", "Ascent Protocol & The 3-Minute Safety Stop")
                put("subtitle", "The ultimate buffer against decompression illness.")
                put("bulletPoints", JSONArray().apply {
                    put("Maximum Ascent Rate: 9 meters per minute (30 feet per minute) - slower than your smallest bubbles!")
                    put("Mandatory Safety Stop: 3 minutes at 5 meters (15 feet) on all dives deeper than 10 meters.")
                    put("Why Safety Stop? Allows accumulated Nitrogen to off-gas safely before reaching 1 ATA.")
                    put("Maintain visual contact with buddy and dive computer depth display.")
                })
                put("diagramType", "ASCENT_PROFILE")
                put("instructorNotes", "Always leave sufficient air for the safety stop and reserve (minimum 50 bar / 700 psi on boat).")
                put("keyTakeaway", "Ascend no faster than 9m/30ft per minute and pause for 3 minutes at 5 meters.")
            })
            put(JSONObject().apply {
                put("slideNumber", 3)
                put("title", "Flying After Diving Guidelines")
                put("subtitle", "Cabin pressure in commercial flights is lower than sea level pressure.")
                put("bulletPoints", JSONArray().apply {
                    put("Single no-decompression dive: Minimum 12 hours surface interval before flying.")
                    put("Multiple dives / Repetitive dives over multiple days: Minimum 18 to 24 hours surface interval.")
                    put("Any dive requiring decompression stops: Minimum 24+ hours.")
                    put("Applies also to driving up high mountain passes (>300m / 1000ft altitude).")
                })
                put("diagramType", "GENERAL_CHECKLIST")
                put("instructorNotes", "Emphasize this for holiday travelers booking return flights right after dive packages.")
                put("keyTakeaway", "Wait at least 18-24 hours after repetitive diving before boarding an aircraft.")
            })
        }

        val presentations = listOf(
            PresentationDeckEntity(
                id = "pres_physics_core",
                moduleId = "mod_physics",
                title = "Scuba Physics, Boyle's Law & Equalization",
                description = "Core physics deck covering pressure at depth, gas volume compression, and avoiding squeeze injuries.",
                slidesJson = presPhysicsSlides.toString(),
                totalSlides = 4,
                lastSlideRead = 0,
                isCompleted = false,
                isInstructorUploaded = false
            ),
            PresentationDeckEntity(
                id = "pres_equipment_core",
                moduleId = "mod_equipment",
                title = "Scuba Equipment System & BWRAF Check",
                description = "Complete breakdown of regulators, BCD inflator, weight systems, and the pre-dive buddy safety check.",
                slidesJson = presEquipmentSlides.toString(),
                totalSlides = 3,
                lastSlideRead = 0,
                isCompleted = false,
                isInstructorUploaded = false
            ),
            PresentationDeckEntity(
                id = "pres_physiology_core",
                moduleId = "mod_physiology",
                title = "Nitrogen Off-gassing, DCS & Ascent Rules",
                description = "Physiology presentation on inert gas absorption, decompression sickness prevention, and the 5m safety stop.",
                slidesJson = presPhysiologySlides.toString(),
                totalSlides = 3,
                lastSlideRead = 0,
                isCompleted = false,
                isInstructorUploaded = false
            )
        )
        divingDao.insertPresentations(presentations)

        // 3. Initial Learning Videos
        val vidEqualizationChapters = JSONArray().apply {
            put(JSONObject().apply {
                put("timeSec", 0)
                put("title", "Introduction: The Pressure Gradient")
                put("summary", "Why water pressure changes rapidly in the first 5 meters.")
            })
            put(JSONObject().apply {
                put("timeSec", 45)
                put("title", "Valsalva Technique & Nose Pinch")
                put("summary", "Demonstration of gentle pressure equalization on the eustachian tubes.")
            })
            put(JSONObject().apply {
                put("timeSec", 110)
                put("title", "Mask Squeeze Prevention")
                put("summary", "How a gentle exhale through the nose equalizes mask airspace.")
            })
            put(JSONObject().apply {
                put("timeSec", 160)
                put("title", "Troubleshooting Inability to Equalize")
                put("summary", "What to do if one ear blocks: Ascend 1 meter, tilt head, swallow.")
            })
        }
        val vidEqualizationTakeaways = JSONArray().apply {
            put("Equalize on the surface before starting descent, and continuously every 0.5m.")
            put("Never push through ear pain; doing so can rupture the eardrum.")
            put("Ascend slightly if an ear feels locked before re-attempting.")
        }

        val vidBwrafChapters = JSONArray().apply {
            put(JSONObject().apply {
                put("timeSec", 0)
                put("title", "Why Buddy Checks Save Lives")
                put("summary", "Statistical review of pre-dive gear inspection.")
            })
            put(JSONObject().apply {
                put("timeSec", 30)
                put("title", "B - BCD Inflator & Dump Valves")
                put("summary", "Testing power inflator, oral inflation tube, and dump toggles.")
            })
            put(JSONObject().apply {
                put("timeSec", 75)
                put("title", "W - Weight Belt & Quick Release")
                put("summary", "Verifying weight system is free of entanglements and ready to ditch.")
            })
            put(JSONObject().apply {
                put("timeSec", 130)
                put("title", "R - Releases & Harness Straps")
                put("summary", "Checking shoulder, chest, cumberbund, and tank band friction lock.")
            })
            put(JSONObject().apply {
                put("timeSec", 185)
                put("title", "A & F - Air Supply & Final OK")
                put("summary", "Breathing test while watching SPG needle; mask and fin confirmation.")
            })
        }
        val vidBwrafTakeaways = JSONArray().apply {
            put("If the SPG needle drops when breathing, the tank valve is partially closed.")
            put("Ensure buddy's alternate second stage is mounted in the golden triangle.")
            put("Weight belt right-hand release must be accessible without obstruction.")
        }

        val vidMaskClearChapters = JSONArray().apply {
            put(JSONObject().apply {
                put("timeSec", 0)
                put("title", "Overcoming Water in the Mask")
                put("summary", "Calm breathing from regulator while water enters mask.")
            })
            put(JSONObject().apply {
                put("timeSec", 40)
                put("title", "Partial Flood & Clear Technique")
                put("summary", "Pressing top frame, tilting head up, and exhaling steadily through nose.")
            })
            put(JSONObject().apply {
                put("timeSec", 115)
                put("title", "Full Mask Removal & Replacement")
                put("summary", "Replacing strap, sweeping hair out of skirt, and complete purge.")
            })
        }
        val vidMaskClearTakeaways = JSONArray().apply {
            put("Tilt head upward slightly so water pools at the bottom of the mask skirt.")
            put("Breathe in exclusively through the mouth; exhale through the nose to purge water.")
            put("Keep eyes open or gently closed; saltwater will not harm your eyes.")
        }

        val videos = listOf(
            LearningVideoEntity(
                id = "vid_equalization",
                moduleId = "mod_physics",
                title = "Equalization Techniques & Pressure Awareness",
                description = "Essential underwater demonstration of ear and sinus equalization, mask squeeze avoidance, and pacing your descent.",
                durationSeconds = 240,
                videoUrlOrUri = "scuba://video/equalization_mastery",
                chaptersJson = vidEqualizationChapters.toString(),
                takeawaysJson = vidEqualizationTakeaways.toString(),
                watchedSeconds = 0,
                isCompleted = false,
                isInstructorUploaded = false
            ),
            LearningVideoEntity(
                id = "vid_bwraf",
                moduleId = "mod_equipment",
                title = "Mastering the BWRAF Pre-Dive Buddy Check",
                description = "Step-by-step masterclass walking through every component of the buddy check before giant stride entry.",
                durationSeconds = 280,
                videoUrlOrUri = "scuba://video/bwraf_buddy_check",
                chaptersJson = vidBwrafChapters.toString(),
                takeawaysJson = vidBwrafTakeaways.toString(),
                watchedSeconds = 0,
                isCompleted = false,
                isInstructorUploaded = false
            ),
            LearningVideoEntity(
                id = "vid_mask_clear",
                moduleId = "mod_emergencies",
                title = "Mask Clearing & Regulator Recovery In-Depth",
                description = "Master the two foundational open water skills: clearing a flooded mask and the sweep & reach regulator recovery.",
                durationSeconds = 210,
                videoUrlOrUri = "scuba://video/mask_clearing_regulator_recovery",
                chaptersJson = vidMaskClearChapters.toString(),
                takeawaysJson = vidMaskClearTakeaways.toString(),
                watchedSeconds = 0,
                isCompleted = false,
                isInstructorUploaded = false
            )
        )
        divingDao.insertVideos(videos)

        // 4. Initial Quizzes (Module Quizzes + Full Certification Mock Exam)
        val quizPhysicsQuestions = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "qp_1")
                put("question", "What is the total ambient pressure exerted on a diver at a depth of 20 meters (66 feet) in saltwater?")
                put("options", JSONArray().apply {
                    put("1.0 ATA")
                    put("2.0 ATA")
                    put("3.0 ATA")
                    put("4.0 ATA")
                })
                put("correctIndex", 2)
                put("explanation", "Surface pressure is 1.0 ATA. Every 10 meters of saltwater adds 1.0 ATA. At 20 meters: 1.0 (surface) + 2.0 (water) = 3.0 ATA.")
                put("safetyRule", "Depth pressure formula: Pressure (ATA) = (Depth in meters / 10) + 1.")
                put("depthOrContext", "Depth: 20m / 66ft")
            })
            put(JSONObject().apply {
                put("id", "qp_2")
                put("question", "According to Boyle's Law, if an inverted flexible container holds 6 liters of air at the surface, what will its volume be at 10 meters (33 feet)?")
                put("options", JSONArray().apply {
                    put("6 Liters")
                    put("3 Liters")
                    put("2 Liters")
                    put("12 Liters")
                })
                put("correctIndex", 1)
                put("explanation", "At 10 meters, pressure doubles from 1 ATA to 2 ATA. Pressure and volume are inversely proportional, so gas volume compresses to 1/2 of surface volume: 6 L / 2 = 3 Liters.")
                put("safetyRule", "Boyle's Law states P1 * V1 = P2 * V2 at constant temperature.")
                put("depthOrContext", "Depth: 10m / 33ft")
            })
            put(JSONObject().apply {
                put("id", "qp_3")
                put("question", "What is the most important rule in all of scuba diving?")
                put("options", JSONArray().apply {
                    put("Never dive deeper than 18 meters alone")
                    put("Breathe continuously and never hold your breath")
                    put("Always carry two dive knives")
                    put("Never drink coffee before diving")
                })
                put("correctIndex", 1)
                put("explanation", "Holding your breath while ascending causes trapped air in the lungs to expand rapidly, resulting in arterial gas embolism or lung rupture.")
                put("safetyRule", "Rule #1: Continuous breathing. Blow small bubbles if regulator is removed.")
                put("depthOrContext", "Fundamental Safety Rule")
            })
            put(JSONObject().apply {
                put("id", "qp_4")
                put("question", "If you experience ear pain or fullness during descent and cannot equalize, what should you do immediately?")
                put("options", JSONArray().apply {
                    put("Blow as hard as possible through your nose")
                    put("Stop descent, ascend slightly until pain subsides, and retry equalizing gently")
                    put("Descend faster to reach neutral depth")
                    put("Remove your mask and flood your ears")
                })
                put("correctIndex", 1)
                put("explanation", "Never force equalization against pain. Ascending a meter reduces the pressure gradient on the eardrum, allowing the eustachian tube to clear.")
                put("safetyRule", "Never push through pain. If equalization fails after re-trying, end the dive.")
                put("depthOrContext", "Descent Phase")
            })
            put(JSONObject().apply {
                put("id", "qp_5")
                put("question", "How does breathing air at 30 meters (4 ATA) affect your air consumption compared to breathing at the surface?")
                put("options", JSONArray().apply {
                    put("You consume air at the same rate")
                    put("You consume air twice as fast")
                    put("You consume air four times as fast")
                    put("You consume air half as fast")
                })
                put("correctIndex", 2)
                put("explanation", "At 30 meters (4 ATA), air is four times denser. Each lung volume breath contains 4 times as many air molecules, so cylinder air is consumed 4 times faster.")
                put("safetyRule", "Always monitor your SPG more frequently the deeper you dive.")
                put("depthOrContext", "Depth: 30m / 99ft")
            })
        }

        val quizEquipmentQuestions = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "qe_1")
                put("question", "In the BWRAF pre-dive buddy check, what does the letter 'W' stand for?")
                put("options", JSONArray().apply {
                    put("Water conditions")
                    put("Weights (checked and clear for right-hand quick release)")
                    put("Wrist dive computer")
                    put("Wetsuit thickness")
                })
                put("correctIndex", 1)
                put("explanation", "'W' stands for Weights. Both divers confirm weights are properly secured and clear of BCD straps so they can be ditched instantly in an emergency.")
                put("safetyRule", "Weights must always be rigged for immediate one-hand release.")
                put("depthOrContext", "Pre-Dive Check")
            })
            put(JSONObject().apply {
                put("id", "qe_2")
                put("question", "What happens to the reading on your Submersible Pressure Gauge (SPG) if your tank valve is only barely cracked open rather than fully opened?")
                put("options", JSONArray().apply {
                    put("The needle reads zero")
                    put("The needle reads full, but fluctuates or drops dramatically when you breathe")
                    put("The gauge will explode")
                    put("The gauge reads 50 bar higher than actual")
                })
                put("correctIndex", 1)
                put("explanation", "A partially open valve allows static pressure to equalize, showing full on the gauge. When you breathe, air cannot flow fast enough, so the needle dips or drops.")
                put("safetyRule", "Always test breathe your regulator while watching the SPG gauge needle before entering water.")
                put("depthOrContext", "Equipment Safety")
            })
            put(JSONObject().apply {
                put("id", "qe_3")
                put("question", "Where must a diver's Alternate Air Source (Octopus) be secured during a dive?")
                put("options", JSONArray().apply {
                    put("Tucked inside the BCD pocket with zipper closed")
                    put("Dangling behind the diver's tank")
                    put("Secured in the triangle area formed by the chin and lower corners of the ribcage")
                    put("Wrapped around the diver's neck")
                })
                put("correctIndex", 2)
                put("explanation", "The golden chest triangle ensures the octopus is instantly visible and graspable by an out-of-air buddy without searching.")
                put("safetyRule", "Secure the octopus with a quick-release clip in the chest triangle.")
                put("depthOrContext", "Buddy Safety")
            })
        }

        val quizMockExamQuestions = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "qm_1")
                put("question", "What is the recommended maximum ascent rate for recreational scuba diving?")
                put("options", JSONArray().apply {
                    put("18 meters (60 feet) per minute")
                    put("9 meters (30 feet) per minute")
                    put("3 meters (10 feet) per minute")
                    put("As fast as you can kick")
                })
                put("correctIndex", 1)
                put("explanation", "The universal standard is 9 meters (30 feet) per minute or slower. Many dive computers display audio/visual alarms if you exceed this rate.")
                put("safetyRule", "Never exceed 9m/min ascent rate. A safe visual reference is slower than small bubbles.")
                put("depthOrContext", "Ascent Phase")
            })
            put(JSONObject().apply {
                put("id", "qm_2")
                put("question", "What is the primary purpose of a 3-minute safety stop at 5 meters (15 feet)?")
                put("options", JSONArray().apply {
                    put("To wait for the dive boat to arrive")
                    put("To allow excess dissolved Nitrogen to off-gas safely and reduce microbubble formation")
                    put("To wash out your mask in clean surface water")
                    put("To consume remaining tank air")
                })
                put("correctIndex", 1)
                put("explanation", "The safety stop provides critical off-gassing time for Nitrogen while ambient pressure is still high enough (1.5 ATA) to prevent bubble expansion.")
                put("safetyRule", "A 3-minute stop at 5m/15ft is recommended on every dive deeper than 10 meters.")
                put("depthOrContext", "Safety Protocol")
            })
            put(JSONObject().apply {
                put("id", "qm_3")
                put("question", "What should you do if your buddy gives you the horizontal throat-slashing hand signal underwater?")
                put("options", JSONArray().apply {
                    put("Ask them if they want to take a photo")
                    put("Immediately offer your alternate air source (octopus) and establish physical contact")
                    put("Swim to the surface as fast as possible alone")
                    put("Signal back with an 'OK' sign")
                })
                put("correctIndex", 1)
                put("explanation", "The slashing motion across the throat indicates 'Out of Air!'. You must immediately present your alternate second stage regulator and hold onto your buddy.")
                put("safetyRule", "Immediate air donation and buddy contact prevents panic and dangerous uncontrolled ascents.")
                put("depthOrContext", "Emergency Response")
            })
            put(JSONObject().apply {
                put("id", "qm_4")
                put("question", "After completing two dives in one day, what is the minimum recommended surface interval before boarding a commercial airplane?")
                put("options", JSONArray().apply {
                    put("2 hours")
                    put("6 hours")
                    put("Minimum 18 hours (up to 24 hours)")
                    put("48 hours always")
                })
                put("correctIndex", 2)
                put("explanation", "For repetitive dives over multiple days, divers should wait at least 18 hours (24 hours is ideal) before flying to avoid decompression sickness from lower cabin pressure.")
                put("safetyRule", "Wait 18-24 hours after repetitive diving before flying or ascending into high altitudes.")
                put("depthOrContext", "Post-Dive Safety")
            })
            put(JSONObject().apply {
                put("id", "qm_5")
                put("question", "If a diver experiences numbness, joint pain in the elbows or knees, and extreme fatigue within 6 hours of diving, what condition should be suspected?")
                put("options", JSONArray().apply {
                    put("Nitrogen Narcosis")
                    put("Decompression Sickness (DCS)")
                    put("Dehydration only")
                    put("Sunstroke")
                })
                put("correctIndex", 1)
                put("explanation", "Joint pain ('the bends'), numbness, and fatigue are hallmark signs of Nitrogen bubbles in tissue and circulation (DCS). Immediate treatment is 100% O2 and hyperbaric care.")
                put("safetyRule", "Administer 100% pure oxygen and activate emergency medical evacuation for hyperbaric chamber treatment.")
                put("depthOrContext", "Diving Medical Emergency")
            })
            put(JSONObject().apply {
                put("id", "qm_6")
                put("question", "What is Nitrogen Narcosis, and at approximately what depth does it commonly begin affecting divers breathing standard air?")
                put("options", JSONArray().apply {
                    put("An ear squeeze occurring at 3 meters")
                    put("An intoxicating anesthetic feeling causing impaired judgment, commonly beginning around 30 meters (100 feet)")
                    put("A muscle cramp in the fins at 10 meters")
                    put("Permanent brain damage from salt water")
                })
                put("correctIndex", 1)
                put("explanation", "Nitrogen behaves like an anesthetic at high partial pressures. Often called 'Martini's Law', symptoms of euphoria and poor judgment start noticeably around 30m. Cure: Ascend to shallower depth.")
                put("safetyRule", "If experiencing narcosis, signal your buddy and ascend together until symptoms disappear immediately.")
                put("depthOrContext", "Deep Diving Physiology")
            })
        }

        val quizzes = listOf(
            QuizEntity(
                id = "quiz_physics",
                moduleId = "mod_physics",
                title = "Physics & Pressure Mastery Quiz",
                description = "5 challenging questions on Boyle's Law, depth atmospheres, air density, and equalizing air spaces.",
                passScorePercentage = 80,
                questionsJson = quizPhysicsQuestions.toString(),
                isMockExam = false,
                isInstructorUploaded = false
            ),
            QuizEntity(
                id = "quiz_equipment",
                moduleId = "mod_equipment",
                title = "Gear & Pre-Dive Safety Quiz",
                description = "Test your knowledge on regulator anatomy, the BWRAF check, and buoyancy control.",
                passScorePercentage = 80,
                questionsJson = quizEquipmentQuestions.toString(),
                isMockExam = false,
                isInstructorUploaded = false
            ),
            QuizEntity(
                id = "quiz_final_mock",
                moduleId = "mod_planning",
                title = "Open Water Final Certification Mock Exam",
                description = "Authentic full mock exam testing emergency procedures, decompression theory, ascent rates, and flying guidelines.",
                passScorePercentage = 80,
                questionsJson = quizMockExamQuestions.toString(),
                isMockExam = true,
                isInstructorUploaded = false
            )
        )
        divingDao.insertQuizzes(quizzes)

        // 5. Initial User Progress
        divingDao.insertOrUpdateProgress(
            UserProgressEntity(
                id = 1,
                studentName = "Scuba Diver Candidate",
                studyStreakDays = 1,
                totalStudyMinutes = 20,
                lastActiveTimestamp = System.currentTimeMillis()
            )
        )
    }
}
