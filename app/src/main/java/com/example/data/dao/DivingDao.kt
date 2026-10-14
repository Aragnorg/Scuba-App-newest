package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CourseModuleEntity
import com.example.data.model.DiverDocumentEntity
import com.example.data.model.DiveLogEntity
import com.example.data.model.LearningVideoEntity
import com.example.data.model.PresentationDeckEntity
import com.example.data.model.QuizEntity
import com.example.data.model.StudentRosterEntity
import com.example.data.model.OrganizationEntity
import com.example.data.model.OrganizationMemberEntity
import com.example.data.model.DiveEventEntity
import com.example.data.model.DiveEventParticipantEntity
import com.example.data.model.InventoryItemEntity
import com.example.data.model.RentalTransactionEntity
import com.example.data.model.UserAccountEntity
import com.example.data.model.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DivingDao {

    // Modules
    @Query("SELECT * FROM course_modules ORDER BY orderIndex ASC")
    fun getAllModules(): Flow<List<CourseModuleEntity>>

    @Query("SELECT * FROM course_modules WHERE id = :id LIMIT 1")
    fun getModuleById(id: String): Flow<CourseModuleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: CourseModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<CourseModuleEntity>)

    // Presentations
    @Query("SELECT * FROM presentations ORDER BY createdAt DESC")
    fun getAllPresentations(): Flow<List<PresentationDeckEntity>>

    @Query("SELECT * FROM presentations WHERE isDeployed = 1 ORDER BY createdAt DESC")
    fun getDeployedPresentations(): Flow<List<PresentationDeckEntity>>

    @Query("SELECT * FROM presentations WHERE moduleId = :moduleId AND isDeployed = 1 ORDER BY createdAt DESC")
    fun getDeployedPresentationsForModule(moduleId: String): Flow<List<PresentationDeckEntity>>

    @Query("SELECT * FROM presentations WHERE moduleId = :moduleId ORDER BY createdAt DESC")
    fun getPresentationsForModule(moduleId: String): Flow<List<PresentationDeckEntity>>

    @Query("UPDATE presentations SET isDeployed = :isDeployed WHERE id = :id")
    suspend fun updatePresentationDeployment(id: String, isDeployed: Boolean)

    @Query("SELECT * FROM presentations WHERE id = :id LIMIT 1")
    fun getPresentationById(id: String): Flow<PresentationDeckEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresentation(presentation: PresentationDeckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPresentations(presentations: List<PresentationDeckEntity>)

    @Query("UPDATE presentations SET lastSlideRead = :lastSlide, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updatePresentationProgress(id: String, lastSlide: Int, isCompleted: Boolean)

    @Query("DELETE FROM presentations WHERE id = :id")
    suspend fun deletePresentation(id: String)

    // Videos
    @Query("SELECT * FROM learning_videos ORDER BY createdAt DESC")
    fun getAllVideos(): Flow<List<LearningVideoEntity>>

    @Query("SELECT * FROM learning_videos WHERE isDeployed = 1 ORDER BY createdAt DESC")
    fun getDeployedVideos(): Flow<List<LearningVideoEntity>>

    @Query("SELECT * FROM learning_videos WHERE moduleId = :moduleId AND isDeployed = 1 ORDER BY createdAt DESC")
    fun getDeployedVideosForModule(moduleId: String): Flow<List<LearningVideoEntity>>

    @Query("UPDATE learning_videos SET isDeployed = :isDeployed WHERE id = :id")
    suspend fun updateVideoDeployment(id: String, isDeployed: Boolean)

    @Query("SELECT * FROM learning_videos WHERE moduleId = :moduleId ORDER BY createdAt DESC")
    fun getVideosForModule(moduleId: String): Flow<List<LearningVideoEntity>>

    @Query("SELECT * FROM learning_videos WHERE id = :id LIMIT 1")
    fun getVideoById(id: String): Flow<LearningVideoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: LearningVideoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<LearningVideoEntity>)

    @Query("UPDATE learning_videos SET watchedSeconds = :watchedSec, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateVideoProgress(id: String, watchedSec: Int, isCompleted: Boolean)

    @Query("DELETE FROM learning_videos WHERE id = :id")
    suspend fun deleteVideo(id: String)

    // Quizzes
    @Query("SELECT * FROM quizzes ORDER BY isMockExam DESC, createdAt DESC")
    fun getAllQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE isDeployed = 1 ORDER BY isMockExam DESC, createdAt DESC")
    fun getDeployedQuizzes(): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE moduleId = :moduleId AND isDeployed = 1 ORDER BY createdAt DESC")
    fun getDeployedQuizzesForModule(moduleId: String): Flow<List<QuizEntity>>

    @Query("UPDATE quizzes SET isDeployed = :isDeployed WHERE id = :id")
    suspend fun updateQuizDeployment(id: String, isDeployed: Boolean)

    @Query("SELECT * FROM quizzes WHERE moduleId = :moduleId ORDER BY createdAt DESC")
    fun getQuizzesForModule(moduleId: String): Flow<List<QuizEntity>>

    @Query("SELECT * FROM quizzes WHERE id = :id LIMIT 1")
    fun getQuizById(id: String): Flow<QuizEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuiz(quiz: QuizEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<QuizEntity>)

    @Query("UPDATE quizzes SET highestScore = :score, isPassed = :isPassed, timesAttempted = :attempts, lastAttemptTimestamp = :timestamp WHERE id = :id")
    suspend fun updateQuizResult(id: String, score: Int, isPassed: Boolean, attempts: Int, timestamp: Long)

    @Query("DELETE FROM quizzes WHERE id = :id")
    suspend fun deleteQuiz(id: String)

    // User Progress
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgressEntity)

    // User Accounts & Authentication
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    fun getAccountByEmail(email: String): Flow<UserAccountEntity?>

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmailDirect(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    fun getAccountById(id: String): Flow<UserAccountEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccountEntity)

    @Update
    suspend fun updateUserAccount(account: UserAccountEntity)

    @Query("UPDATE user_accounts SET isEmailVerified = 1 WHERE email = :email")
    suspend fun markAccountVerified(email: String)

    @Query("UPDATE user_accounts SET profilePictureUri = :uri WHERE id = :id")
    suspend fun updateUserProfilePicture(id: String, uri: String?)

    @Query("UPDATE user_progress SET profilePictureUri = :uri WHERE id = 1")
    suspend fun updateProgressProfilePicture(uri: String?)

    @Query("UPDATE user_accounts SET verificationCode = :code, verificationSentAt = :timestamp WHERE email = :email")
    suspend fun updateVerificationCode(email: String, code: String, timestamp: Long)

    @Query("SELECT * FROM user_accounts WHERE isEmailVerified = 1 ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestVerifiedAccount(): UserAccountEntity?

    // Student Roster & Tracking (for Instructor)
    @Query("SELECT * FROM student_roster ORDER BY status ASC, enrolledDate DESC")
    fun getAllStudents(): Flow<List<StudentRosterEntity>>

    @Query("SELECT * FROM student_roster WHERE status = 'ACTIVE' ORDER BY enrolledDate DESC")
    fun getActiveStudents(): Flow<List<StudentRosterEntity>>

    @Query("SELECT * FROM student_roster WHERE status = 'PAST_STUDENT' ORDER BY enrolledDate DESC")
    fun getPastStudents(): Flow<List<StudentRosterEntity>>

    @Query("SELECT * FROM student_roster WHERE id = :id LIMIT 1")
    fun getStudentById(id: String): Flow<StudentRosterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentRosterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentRosterEntity>)

    @Query("UPDATE student_roster SET status = :status, certifiedDate = :certifiedDate WHERE id = :id")
    suspend fun updateStudentStatus(id: String, status: String, certifiedDate: String?)

    @Query("UPDATE student_roster SET instructorNotes = :notes WHERE id = :id")
    suspend fun updateStudentNotes(id: String, notes: String)

    // --- Dive Logbook ---
    @Query("SELECT * FROM dive_logs ORDER BY diveNumber DESC, createdAt DESC")
    fun getAllDiveLogs(): Flow<List<DiveLogEntity>>

    @Query("SELECT * FROM dive_logs WHERE diverId = :diverId OR diverEmail = :diverId OR diverName = :diverId ORDER BY diveNumber DESC, createdAt DESC")
    fun getDiveLogsForDiver(diverId: String): Flow<List<DiveLogEntity>>

    @Query("SELECT * FROM dive_logs WHERE id = :id LIMIT 1")
    fun getDiveLogById(id: String): Flow<DiveLogEntity?>

    @Query("SELECT * FROM dive_logs WHERE id = :id LIMIT 1")
    suspend fun getDiveLogByIdDirect(id: String): DiveLogEntity?

    @Query("SELECT * FROM dive_logs WHERE approvalStatus = 'PENDING' AND (buddyEmail = :buddyEmail OR buddyName = :buddyEmail) ORDER BY createdAt DESC")
    fun getPendingBuddyApprovals(buddyEmail: String): Flow<List<DiveLogEntity>>

    @Query("SELECT * FROM dive_logs WHERE approvalStatus = 'PENDING' ORDER BY createdAt DESC")
    fun getAllPendingDives(): Flow<List<DiveLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiveLog(dive: DiveLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiveLogs(dives: List<DiveLogEntity>)

    @Update
    suspend fun updateDiveLog(dive: DiveLogEntity)

    @Query("DELETE FROM dive_logs WHERE id = :id")
    suspend fun deleteDiveLog(id: String)

    @Query("UPDATE dive_logs SET approvalStatus = :status, approvalMethod = :method, approvedBy = :approvedBy, approvalTimestamp = :timestamp, qrStampCode = :stampCode, instructorName = :instructorName, instructorLicense = :license WHERE id = :id")
    suspend fun approveWithInstructorStamp(
        id: String,
        status: String,
        method: String,
        approvedBy: String,
        timestamp: Long,
        stampCode: String,
        instructorName: String,
        license: String
    )

    @Query("UPDATE dive_logs SET approvalStatus = 'APPROVED_BY_BUDDY_SIGNATURE', approvalMethod = 'BUDDY_SIGNATURE', approvedBy = :buddyName, approvalTimestamp = :timestamp, buddySignaturePathJson = :signatureJson, buddyApprovalNote = :note WHERE id = :id")
    suspend fun approveWithBuddySignature(
        id: String,
        buddyName: String,
        timestamp: Long,
        signatureJson: String,
        note: String
    )

    @Query("UPDATE dive_logs SET approvalStatus = 'APPROVED_BY_BUDDY_PUSH', approvalMethod = 'BUDDY_PUSH', approvedBy = :buddyName, approvalTimestamp = :timestamp, buddyApprovalNote = :note WHERE id = :id")
    suspend fun approveWithBuddyPush(
        id: String,
        buddyName: String,
        timestamp: Long,
        note: String
    )

    @Query("UPDATE dive_logs SET approvalStatus = 'PENDING', buddyEmail = :buddyEmail, buddyName = :buddyName WHERE id = :id")
    suspend fun markPendingBuddyPushApproval(
        id: String,
        buddyEmail: String,
        buddyName: String
    )

    // --- Diver Documents & Certifications Vault ---
    @Query("SELECT * FROM diver_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DiverDocumentEntity>>

    @Query("SELECT * FROM diver_documents WHERE diverId = :diverId OR diverEmail = :diverEmail ORDER BY createdAt DESC")
    fun getDocumentsForDiver(diverId: String, diverEmail: String): Flow<List<DiverDocumentEntity>>

    @Query("SELECT * FROM diver_documents WHERE approvalStatus = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingApprovalDocuments(): Flow<List<DiverDocumentEntity>>

    @Query("SELECT * FROM diver_documents WHERE id = :id LIMIT 1")
    fun getDocumentById(id: String): Flow<DiverDocumentEntity?>

    @Query("SELECT * FROM diver_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentByIdDirect(id: String): DiverDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DiverDocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DiverDocumentEntity>)

    @Update
    suspend fun updateDocument(document: DiverDocumentEntity)

    @Query("DELETE FROM diver_documents WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Query("UPDATE diver_documents SET approvalStatus = 'APPROVED', approverName = :approverName, approverTitleOrLicense = :license, approverOrganization = :org, approvalDate = :approvalDate, approvalStampCode = :stampCode, approverSignatureJson = :signatureJson, approverNotes = :notes WHERE id = :id")
    suspend fun approveDocument(
        id: String,
        approverName: String,
        license: String,
        org: String,
        approvalDate: String,
        stampCode: String,
        signatureJson: String,
        notes: String
    )

    @Query("UPDATE diver_documents SET approvalStatus = 'REJECTED', approverNotes = :notes WHERE id = :id")
    suspend fun rejectDocument(id: String, notes: String)

    // --- B2B2C Multi-Tenant Organizations (DiveOps) ---
    @Query("SELECT * FROM organizations ORDER BY name ASC")
    fun getAllOrganizations(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    fun getOrganizationById(id: String): Flow<OrganizationEntity?>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    suspend fun getOrganizationByIdDirect(id: String): OrganizationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(organization: OrganizationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganizations(organizations: List<OrganizationEntity>)

    @Update
    suspend fun updateOrganization(organization: OrganizationEntity)

    @Query("DELETE FROM organizations WHERE id = :id")
    suspend fun deleteOrganization(id: String)

    // --- Organization Members ---
    @Query("SELECT * FROM organization_members WHERE orgId = :orgId ORDER BY role ASC, fullName ASC")
    fun getMembersForOrganization(orgId: String): Flow<List<OrganizationMemberEntity>>

    @Query("SELECT * FROM organization_members WHERE orgId = :orgId AND status = 'ACTIVE' ORDER BY role ASC, fullName ASC")
    fun getActiveMembersForOrganization(orgId: String): Flow<List<OrganizationMemberEntity>>

    @Query("SELECT * FROM organization_members WHERE id = :id LIMIT 1")
    fun getMemberById(id: String): Flow<OrganizationMemberEntity?>

    @Query("SELECT * FROM organization_members WHERE id = :id LIMIT 1")
    suspend fun getMemberByIdDirect(id: String): OrganizationMemberEntity?

    @Query("SELECT * FROM organization_members WHERE orgId = :orgId AND userId = :userId LIMIT 1")
    fun getMemberByOrgAndUser(orgId: String, userId: String): Flow<OrganizationMemberEntity?>

    @Query("SELECT * FROM organization_members WHERE orgId = :orgId AND userId = :userId LIMIT 1")
    suspend fun getMemberByOrgAndUserDirect(orgId: String, userId: String): OrganizationMemberEntity?

    @Query("SELECT * FROM organization_members WHERE userId = :userId")
    fun getMembershipsForUser(userId: String): Flow<List<OrganizationMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: OrganizationMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<OrganizationMemberEntity>)

    @Update
    suspend fun updateMember(member: OrganizationMemberEntity)

    @Query("UPDATE organization_members SET role = :role WHERE id = :id")
    suspend fun updateMemberRole(id: String, role: String)

    @Query("UPDATE organization_members SET status = :status WHERE id = :id")
    suspend fun updateMemberStatus(id: String, status: String)

    @Query("UPDATE organization_members SET ttuDocumentId = :docId, ttuExpiryDate = :expiryDate, ttuDoctorName = :doctorName WHERE id = :id")
    suspend fun updateMemberTtu(id: String, docId: String?, expiryDate: String?, doctorName: String?)

    @Query("DELETE FROM organization_members WHERE id = :id")
    suspend fun deleteMember(id: String)

    // --- DiveOps Engine: Events, Boat Trips, Shore Excursions ---
    @Query("SELECT * FROM dive_events WHERE orgId = :orgId ORDER BY eventDate DESC, departureTime ASC")
    fun getAllEventsForOrganization(orgId: String): Flow<List<DiveEventEntity>>

    @Query("SELECT * FROM dive_events WHERE orgId = :orgId AND status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY eventDate ASC, departureTime ASC")
    fun getUpcomingEventsForOrganization(orgId: String): Flow<List<DiveEventEntity>>

    @Query("SELECT * FROM dive_events WHERE id = :id LIMIT 1")
    fun getEventById(id: String): Flow<DiveEventEntity?>

    @Query("SELECT * FROM dive_events WHERE id = :id LIMIT 1")
    suspend fun getEventByIdDirect(id: String): DiveEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: DiveEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<DiveEventEntity>)

    @Update
    suspend fun updateEvent(event: DiveEventEntity)

    @Query("UPDATE dive_events SET status = :status WHERE id = :id")
    suspend fun updateEventStatus(id: String, status: String)

    @Query("UPDATE dive_events SET registeredCount = :count WHERE id = :id")
    suspend fun updateEventRegisteredCount(id: String, count: Int)

    @Query("DELETE FROM dive_events WHERE id = :id")
    suspend fun deleteEvent(id: String)

    // --- DiveOps Check-in, Manifest & Participants ---
    @Query("SELECT * FROM dive_event_participants WHERE eventId = :eventId ORDER BY checkInTimestamp ASC")
    fun getParticipantsForEvent(eventId: String): Flow<List<DiveEventParticipantEntity>>

    @Query("SELECT * FROM dive_event_participants WHERE id = :id LIMIT 1")
    fun getParticipantById(id: String): Flow<DiveEventParticipantEntity?>

    @Query("SELECT * FROM dive_event_participants WHERE id = :id LIMIT 1")
    suspend fun getParticipantByIdDirect(id: String): DiveEventParticipantEntity?

    @Query("SELECT * FROM dive_event_participants WHERE eventId = :eventId AND memberId = :memberId LIMIT 1")
    suspend fun getParticipantByEventAndMember(eventId: String, memberId: String): DiveEventParticipantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: DiveEventParticipantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<DiveEventParticipantEntity>)

    @Update
    suspend fun updateParticipant(participant: DiveEventParticipantEntity)

    @Query("UPDATE dive_event_participants SET checkInStatus = :status WHERE id = :id")
    suspend fun updateParticipantCheckInStatus(id: String, status: String)

    @Query("UPDATE dive_event_participants SET buddyTeamId = :buddyTeamId, groupGuideName = :groupGuideName WHERE id = :id")
    suspend fun updateBuddyTeamAssignment(id: String, buddyTeamId: String?, groupGuideName: String?)

    @Query("UPDATE dive_event_participants SET timeIn = :timeIn, timeOut = :timeOut, tankPressureEndBar = :pressureOut, checkInStatus = :status WHERE id = :id")
    suspend fun recordWaterEntryExit(id: String, timeIn: String?, timeOut: String?, pressureOut: Int, status: String)

    @Query("DELETE FROM dive_event_participants WHERE id = :id")
    suspend fun deleteParticipant(id: String)

    @Query("DELETE FROM dive_event_participants WHERE eventId = :eventId")
    suspend fun deleteParticipantsForEvent(eventId: String)

    // --- Asset & Tech: Equipment Inventory & Compliance ---
    @Query("SELECT * FROM inventory_items WHERE orgId = :orgId ORDER BY itemType ASC, internalCode ASC")
    fun getAllInventoryForOrganization(orgId: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE orgId = :orgId AND itemType = :itemType ORDER BY internalCode ASC")
    fun getInventoryByType(orgId: String, itemType: String): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    fun getInventoryItemById(id: String): Flow<InventoryItemEntity?>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemByIdDirect(id: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET status = :status WHERE id = :id")
    suspend fun updateInventoryStatus(id: String, status: String)

    @Query("UPDATE inventory_items SET nextServiceDate = :nextDate, lastServiceDate = :lastDate, serviceNotes = :notes, status = 'AVAILABLE' WHERE id = :id")
    suspend fun updateInventoryService(id: String, nextDate: String, lastDate: String, notes: String)

    @Query("UPDATE inventory_items SET compressorHours = :hours, compressorFilterCurrentHours = :filterHours, airAnalysisDate = :airAnalysisDate WHERE id = :id")
    suspend fun updateCompressorTelemetry(id: String, hours: Float, filterHours: Float, airAnalysisDate: String)

    @Query("UPDATE inventory_items SET status = :status, currentRenterMemberId = :renterId, currentRenterName = :renterName, currentRentalDueDate = :dueDate WHERE id = :id")
    suspend fun updateInventoryRentalAssignment(id: String, status: String, renterId: String?, renterName: String?, dueDate: String?)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItem(id: String)

    // --- Asset & Tech: Rental Transactions ---
    @Query("SELECT * FROM rental_transactions WHERE orgId = :orgId AND status = 'ACTIVE' ORDER BY dueDate ASC")
    fun getActiveRentalsForOrganization(orgId: String): Flow<List<RentalTransactionEntity>>

    @Query("SELECT * FROM rental_transactions WHERE orgId = :orgId ORDER BY createdAt DESC")
    fun getAllRentalsForOrganization(orgId: String): Flow<List<RentalTransactionEntity>>

    @Query("SELECT * FROM rental_transactions WHERE itemId = :itemId ORDER BY createdAt DESC")
    fun getRentalsForItem(itemId: String): Flow<List<RentalTransactionEntity>>

    @Query("SELECT * FROM rental_transactions WHERE memberId = :memberId ORDER BY createdAt DESC")
    fun getRentalsForMember(memberId: String): Flow<List<RentalTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRentalTransaction(rental: RentalTransactionEntity)

    @Update
    suspend fun updateRentalTransaction(rental: RentalTransactionEntity)

    @Query("UPDATE rental_transactions SET returnDate = :returnDate, status = :status, returnCondition = :condition, damageReportNotes = :damageNotes WHERE id = :id")
    suspend fun completeRentalReturn(id: String, returnDate: String, status: String, condition: String, damageNotes: String)
}

