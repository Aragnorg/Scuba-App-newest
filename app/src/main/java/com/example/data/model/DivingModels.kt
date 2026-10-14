package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "course_modules")
data class CourseModuleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val iconName: String,
    val orderIndex: Int,
    val estimatedMin: Int,
    val isInstructorCustom: Boolean = false
)

@Entity(tableName = "presentations")
data class PresentationDeckEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val slidesJson: String,
    val documentUri: String? = null,
    val totalSlides: Int = 1,
    val lastSlideRead: Int = 0,
    val isCompleted: Boolean = false,
    val isInstructorUploaded: Boolean = false,
    val isDeployed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun parseSlides(): List<SlideItem> {
        val list = mutableListOf<SlideItem>()
        try {
            val jsonArray = JSONArray(slidesJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val bullets = mutableListOf<String>()
                val bArr = obj.optJSONArray("bulletPoints")
                if (bArr != null) {
                    for (j in 0 until bArr.length()) {
                        bullets.add(bArr.getString(j))
                    }
                }
                list.add(
                    SlideItem(
                        slideNumber = obj.optInt("slideNumber", i + 1),
                        title = obj.optString("title", "Slide ${i + 1}"),
                        subtitle = obj.optString("subtitle", ""),
                        bulletPoints = bullets,
                        diagramType = obj.optString("diagramType", "GENERAL_CHECKLIST"),
                        instructorNotes = obj.optString("instructorNotes", ""),
                        keyTakeaway = obj.optString("keyTakeaway", "")
                    )
                )
            }
        } catch (e: Exception) {
            // Fallback slide
            list.add(
                SlideItem(
                    slideNumber = 1,
                    title = title,
                    subtitle = description,
                    bulletPoints = listOf("Course slide notes ready for review."),
                    diagramType = "GENERAL_CHECKLIST",
                    instructorNotes = "Review this section thoroughly.",
                    keyTakeaway = "Safety first underwater."
                )
            )
        }
        return list
    }
}

data class SlideItem(
    val slideNumber: Int,
    val title: String,
    val subtitle: String,
    val bulletPoints: List<String>,
    val diagramType: String,
    val instructorNotes: String,
    val keyTakeaway: String
)

@Entity(tableName = "learning_videos")
data class LearningVideoEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val durationSeconds: Int,
    val videoUrlOrUri: String,
    val thumbnailUrl: String? = null,
    val chaptersJson: String = "[]",
    val takeawaysJson: String = "[]",
    val watchedSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val isInstructorUploaded: Boolean = false,
    val isDeployed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun parseChapters(): List<VideoChapter> {
        val list = mutableListOf<VideoChapter>()
        try {
            val array = JSONArray(chaptersJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    VideoChapter(
                        timeSec = obj.optInt("timeSec", 0),
                        title = obj.optString("title", "Chapter ${i + 1}"),
                        summary = obj.optString("summary", "")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun parseTakeaways(): List<String> {
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(takeawaysJson)
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }
}

data class VideoChapter(
    val timeSec: Int,
    val title: String,
    val summary: String
)

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val title: String,
    val description: String,
    val passScorePercentage: Int = 80,
    val questionsJson: String,
    val isMockExam: Boolean = false,
    val highestScore: Int = 0,
    val timesAttempted: Int = 0,
    val lastAttemptTimestamp: Long = 0L,
    val isPassed: Boolean = false,
    val isInstructorUploaded: Boolean = false,
    val isDeployed: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun parseQuestions(): List<QuizQuestion> {
        val list = mutableListOf<QuizQuestion>()
        try {
            val array = JSONArray(questionsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val opts = mutableListOf<String>()
                val optsArr = obj.optJSONArray("options")
                if (optsArr != null) {
                    for (j in 0 until optsArr.length()) {
                        opts.add(optsArr.getString(j))
                    }
                }
                list.add(
                    QuizQuestion(
                        id = obj.optString("id", "q_$i"),
                        question = obj.optString("question", ""),
                        options = opts,
                        correctIndex = obj.optInt("correctIndex", 0),
                        explanation = obj.optString("explanation", ""),
                        safetyRule = obj.optString("safetyRule", "Always dive with a certified buddy."),
                        depthOrContext = obj.optString("depthOrContext", "Open Water Standard")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }
}

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val safetyRule: String,
    val depthOrContext: String
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val studentName: String = "Scuba Diver Candidate",
    val profilePictureUri: String? = null,
    val studyStreakDays: Int = 3,
    val totalStudyMinutes: Int = 45,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

data class Flashcard(
    val id: String,
    val signalName: String,
    val instruction: String,
    val meaning: String,
    val actionRequired: String,
    val symbolEmoji: String
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val isEmailVerified: Boolean = false,
    val verificationCode: String = "",
    val verificationSentAt: Long = System.currentTimeMillis(),
    val role: String = "STUDENT",
    val profilePictureUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class VerificationEmailMessage(
    val senderName: String = "Adrian Scheid (AquaLearn Owner)",
    val senderEmail: String = "adrian.scheid01@gmail.com",
    val recipientEmail: String,
    val recipientName: String,
    val subject: String = "Verify Your AquaLearn Diver Academy Registration",
    val verificationCode: String,
    val sentTimestamp: Long = System.currentTimeMillis(),
    val emailHtmlBody: String
)

@Entity(tableName = "student_roster")
data class StudentRosterEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val status: String = "ACTIVE", // "ACTIVE" or "PAST_STUDENT"
    val enrolledDate: String,
    val certifiedDate: String? = null,
    val targetCourse: String = "Open Water Diver (18m)",
    val presentationsCompleted: Int = 0,
    val totalPresentations: Int = 3,
    val videosWatched: Int = 0,
    val totalVideos: Int = 3,
    val quizzesPassed: Int = 0,
    val totalQuizzes: Int = 3,
    val examReadinessScore: Int = 0,
    val finalMockExamScore: Int? = null,
    val instructorNotes: String = ""
)

@Entity(tableName = "dive_logs")
data class DiveLogEntity(
    @PrimaryKey val id: String,
    val diveNumber: Int,
    val date: String, // e.g. "2026-09-28"
    val timeIn: String, // e.g. "09:45 AM"
    val timeOut: String, // e.g. "10:37 AM"
    val durationMinutes: Int, // e.g. 52 min
    val siteName: String,
    val location: String, // e.g. "Ras Mohammed, Sharm El Sheikh, Egypt"
    val bodyOfWater: String = "Ocean", // Ocean, Sea, Lake, Quarry, Cave
    val entryType: String = "Boat", // Boat, Shore, Drift, Wreck, Night
    val maxDepthMeters: Float, // e.g. 18.2m
    val avgDepthMeters: Float, // e.g. 12.4m
    val waterTempCelsius: Float, // e.g. 26.0°C
    val airTempCelsius: Float, // e.g. 31.0°C
    val visibilityMeters: Float = 25.0f, // e.g. 25m
    val gasType: String = "Air 21%", // Air 21%, EANx 32%, EANx 36%
    val tankType: String = "12L Aluminum", // 12L Aluminum, 12L Steel, 15L Steel
    val pressureInBar: Int = 200, // Bar
    val pressureOutBar: Int = 50, // Bar
    val suitType: String = "5mm Full Suit", // 3mm Shorty, 5mm Full Suit, Semi-Dry, Drysuit
    val weightKg: Float = 6.0f,
    val safetyStopMinutes: Int = 3,
    val safetyStopDepthMeters: Float = 5.0f,
    val safetyStopCompleted: Boolean = true,
    val sightings: String = "", // e.g. "Green sea turtle, spotted eagle ray, lionfish"
    val notes: String = "",
    val diverId: String = "", // Diver's user id
    val diverEmail: String = "", // Diver's email
    val diverName: String = "",
    val buddyName: String = "",
    val buddyEmail: String = "",
    val diveCenterName: String = "",
    val instructorName: String = "",
    val instructorLicense: String = "",

    // Approval status: "PENDING", "APPROVED_BY_INSTRUCTOR", "APPROVED_BY_BUDDY_SIGNATURE", "APPROVED_BY_BUDDY_PUSH"
    val approvalStatus: String = "PENDING",
    // Approval method: "NONE", "QR_CODE", "INSTRUCTOR_DIRECT", "BUDDY_SIGNATURE", "BUDDY_PUSH"
    val approvalMethod: String = "NONE",
    val approvedBy: String = "",
    val approvalTimestamp: Long = 0L,
    val qrStampCode: String = "",
    val buddySignaturePathJson: String = "", // Coordinates or SVG-like path for hand signature
    val buddyApprovalNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class DivePushNotification(
    val notificationId: String,
    val diveId: String,
    val diveNumber: Int,
    val siteName: String,
    val maxDepthMeters: Float,
    val durationMinutes: Int,
    val requesterName: String,
    val requesterEmail: String,
    val targetBuddyEmail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messageText: String
)

@Entity(tableName = "diver_documents")
data class DiverDocumentEntity(
    @PrimaryKey val id: String,
    val diverId: String,
    val diverName: String,
    val diverEmail: String,
    val title: String,
    val category: String, // "CERTIFICATION", "MEDICAL_FORM", "LIABILITY_WAIVER", "INSURANCE", "EQUIPMENT_RECORD", "CUSTOM"
    val documentNumber: String, // e.g. "OW-892401", "MED-2026-99"
    val issuingOrganization: String, // e.g. "PADI", "SSI", "Dr. med. Weber (Diving Physician)", "DAN Europe"
    val issueDate: String, // e.g. "2026-05-15"
    val expiryDate: String, // e.g. "2027-05-15" or "Lifetime / No Expiration"
    val notes: String = "",
    val cardColorHex: String = "#006699", // Hex gradient base color
    val documentUri: String? = null,

    // Approval details
    val approvalStatus: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val requiredApproverType: String = "INSTRUCTOR", // "INSTRUCTOR", "DOCTOR", "DIVE_CENTER", "AGENCY", "ANY"
    val approverName: String = "",
    val approverTitleOrLicense: String = "", // e.g. "MD - Diving Medicine Specialist #DE-8921" or "PADI MSDT #391024"
    val approverOrganization: String = "", // e.g. "Hyperbaric Medical Center" or "AquaLearn Dive Academy"
    val approvalDate: String = "",
    val approvalStampCode: String = "",
    val approverSignatureJson: String = "",
    val approverNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ScubaCourse(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val depthLimit: String,
    val duration: String,
    val prerequisites: String,
    val isEnrolled: Boolean = false,
    val iconEmoji: String,
    val lessonsCount: Int = 4,
    val difficulty: String = "Beginner"
)

// ==========================================
// B2B2C DiveOps Multi-Tenant Architecture
// ==========================================

@Entity(tableName = "organizations")
data class OrganizationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val logoUri: String? = null,
    val orgType: String = "CLUB", // "CLUB" (Verein), "DIVE_CENTER" (Tauchbasis), "DIVE_SCHOOL" (Tauchschule)
    val affiliations: String = "VDST / CMAS", // e.g. "VDST / CMAS", "PADI 5-Star CDC", "SSI Diamond Resort", "i.a.c."
    val location: String,
    val waterBody: String,
    val contactEmail: String,
    val contactPhone: String,
    val website: String = "",
    val emergencyPlanJson: String = "",
    val activeMembersCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isClub: Boolean get() = orgType == "CLUB"
    val isDiveCenter: Boolean get() = orgType == "DIVE_CENTER"
    val isDiveSchool: Boolean get() = orgType == "DIVE_SCHOOL"
}

@Entity(tableName = "organization_members")
data class OrganizationMemberEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val userId: String = "",
    val fullName: String,
    val email: String,
    val phoneNumber: String = "",
    val role: String = "MEMBER", // "OWNER", "ADMIN", "INSTRUCTOR", "DIVE_LEADER", "TECH_MANAGER", "MEMBER", "GUEST"
    val membershipNumber: String = "", // e.g. "DELPHIN-042" or "GUEST-2026-99"
    val joinedDate: String, // e.g. "2024-03-01"
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE", "SUSPENDED"
    val certLevel: String = "Open Water Diver",
    val totalLoggedDives: Int = 12,
    val specialtiesJson: String = "[]",
    val ttuDocumentId: String? = null,
    val ttuExpiryDate: String = "", // e.g. "2027-05-20"
    val ttuDoctorName: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val insuranceProvider: String = "",
    val insuranceNumber: String = "",
    val signedWaiverDate: String? = null,
    val rentalPreferences: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun parseSpecialties(): List<String> {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(specialtiesJson)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    val isLeadershipRole: Boolean
        get() = role in listOf("OWNER", "ADMIN", "INSTRUCTOR", "DIVE_LEADER")

    val isTechRole: Boolean
        get() = role in listOf("OWNER", "ADMIN", "TECH_MANAGER")
}

@Entity(tableName = "dive_events")
data class DiveEventEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val title: String,
    val eventType: String = "BOAT_TRIP", // "BOAT_TRIP", "SHORE_DIVE", "COURSE_TRAINING", "CLUB_OUTING"
    val siteName: String,
    val location: String,
    val eventDate: String, // "YYYY-MM-DD"
    val meetingTime: String = "08:30",
    val departureTime: String = "09:00",
    val maxCapacity: Int = 12,
    val registeredCount: Int = 0,
    val leadGuideName: String = "",
    val leadGuideUserId: String = "",
    val maxDepthMeters: Float = 20.0f,
    val requiredMinCert: String = "Open Water Diver",
    val emergencyBriefing: String = "",
    val status: String = "PLANNING", // "PLANNING", "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
) {
    val isBoatTrip: Boolean get() = eventType == "BOAT_TRIP"
    val isShoreDive: Boolean get() = eventType == "SHORE_DIVE"
    val isCourse: Boolean get() = eventType == "COURSE_TRAINING"
    val isClubOuting: Boolean get() = eventType == "CLUB_OUTING"
}

@Entity(tableName = "dive_event_participants")
data class DiveEventParticipantEntity(
    @PrimaryKey val id: String,
    val eventId: String,
    val memberId: String,
    val diverName: String,
    val diverEmail: String,
    val diverCertLevel: String = "Open Water Diver",
    val diverTotalDives: Int = 0,
    val checkInStatus: String = "REGISTERED", // "REGISTERED", "SAFETY_CHECKED", "ON_BOARD", "IN_WATER", "SURFACED_OK", "ABSENT"
    val safetyGatePassed: Boolean = true,
    val safetyGateIssuesJson: String = "[]",
    val tankSelection: String = "12L Aluminum",
    val gasType: String = "Air 21%",
    val modMeters: Float = 0f,
    val rentalGearJson: String = "[]",
    val transferCarPoolNote: String = "",
    val buddyTeamId: String? = null,
    val groupGuideName: String? = null,
    val tankPressureStartBar: Int = 200,
    val tankPressureEndBar: Int = 50,
    val timeIn: String? = null,
    val timeOut: String? = null,
    val checkInTimestamp: Long = System.currentTimeMillis()
) {
    fun parseSafetyIssues(): List<String> {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(safetyGateIssuesJson)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }

    fun parseRentalGear(): List<String> {
        val list = mutableListOf<String>()
        try {
            val arr = JSONArray(rentalGearJson)
            for (i in 0 until arr.length()) {
                list.add(arr.getString(i))
            }
        } catch (_: Exception) {}
        return list
    }
}

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val itemType: String, // "TANK", "REGULATOR", "BCD", "SUIT", "COMPUTER", "O2_KIT", "COMPRESSOR", "ACCESSORY"
    val internalCode: String, // e.g. "T-12-01", "REG-APX-01", "COMP-01"
    val brand: String, // e.g. "Apeks", "Aqualung", "Mares", "Bauer Kompressoren", "Luxfer"
    val model: String, // e.g. "XTX 50 DIN", "Mariner 250", "Pro HD", "12L Long 232 bar"
    val serialNumber: String = "",
    val sizeOrSpec: String = "", // e.g. "12L Stahl lang, 200 bar", "Größe L / 5mm", "DIN 300 bar"
    val status: String = "AVAILABLE", // "AVAILABLE", "RENTED", "MAINTENANCE", "LOCKED_EXPIRED", "DECOMMISSIONED"
    val nextServiceDate: String = "", // "YYYY-MM-DD" e.g. TÜV date, Regulator revision date, O2 expiry date
    val lastServiceDate: String = "",
    val serviceNotes: String = "", // e.g. "TÜV Süd Prüfprotokoll #8841-A"
    val certificateDocId: String? = null,
    // Compressor specific fields
    val compressorHours: Float = 0f,
    val compressorFilterMaxHours: Float = 50f,
    val compressorFilterCurrentHours: Float = 0f,
    val airAnalysisDate: String = "", // DIN EN 12021
    // Current rental assignment (if rented out)
    val currentRenterMemberId: String? = null,
    val currentRenterName: String? = null,
    val currentRentalDueDate: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isTank: Boolean get() = itemType == "TANK"
    val isRegulator: Boolean get() = itemType == "REGULATOR"
    val isBcd: Boolean get() = itemType == "BCD"
    val isSuit: Boolean get() = itemType == "SUIT"
    val isComputer: Boolean get() = itemType == "COMPUTER"
    val isO2Kit: Boolean get() = itemType == "O2_KIT"
    val isCompressor: Boolean get() = itemType == "COMPRESSOR"
    val isRented: Boolean get() = status == "RENTED"
    val isAvailable: Boolean get() = status == "AVAILABLE"
    val isLocked: Boolean get() = status == "LOCKED_EXPIRED" || status == "MAINTENANCE"
}

@Entity(tableName = "rental_transactions")
data class RentalTransactionEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val itemId: String,
    val itemCode: String,
    val itemDescription: String,
    val memberId: String,
    val renterName: String,
    val checkOutDate: String, // "YYYY-MM-DD"
    val dueDate: String, // "YYYY-MM-DD"
    val returnDate: String? = null,
    val status: String = "ACTIVE", // "ACTIVE", "RETURNED", "OVERDUE", "DAMAGED"
    val returnCondition: String = "INTACT", // "INTACT", "CLEANING_NEEDED", "DAMAGED"
    val damageReportNotes: String = "",
    val staffUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "organization_documents")
data class OrganizationDocumentEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val title: String,
    val category: String, // "WAIVER", "RULES", "CONSTITUTION", "PROTOCOL", "EMERGENCY_PLAN", "PRICE_LIST", "MAP", "OTHER"
    val fileUri: String = "",
    val fileName: String = "",
    val fileSizeBytes: Long = 0,
    val mimeType: String = "application/pdf",
    val visibility: String = "ALL_MEMBERS", // "PUBLIC", "ALL_MEMBERS", "DIVE_LEADERS_ONLY", "ADMINS_ONLY"
    val requiresAcknowledgment: Boolean = false, // Check-in Mandatory Safety Document
    val acknowledgmentCount: Int = 0,
    val version: String = "1.0",
    val documentContentSummary: String = "",
    val uploadedByName: String = "",
    val uploadedByUserId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val contentSummary: String get() = documentContentSummary
    val uploadedByMemberId: String get() = uploadedByUserId
    val isMandatory: Boolean get() = requiresAcknowledgment
    val isWaiver: Boolean get() = category == "WAIVER"
    val isRules: Boolean get() = category == "RULES"
    val isConstitution: Boolean get() = category == "CONSTITUTION"
    val isProtocol: Boolean get() = category == "PROTOCOL"
    val isEmergencyPlan: Boolean get() = category == "EMERGENCY_PLAN"
    val isMap: Boolean get() = category == "MAP"
}

@Entity(tableName = "document_signatures")
data class DocumentSignatureEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val documentId: String,
    val documentTitle: String = "",
    val memberId: String,
    val signerName: String,
    val signerEmail: String,
    val signatureType: String = "CHECKBOX_CONFIRMED", // "CHECKBOX_CONFIRMED", "DIGITAL_SIGNATURE"
    val signatureDataJson: String = "",
    val signedTimestamp: Long = System.currentTimeMillis(),
    val ipOrDeviceNote: String = "AquaLearn Verified App Signature"
) {
    val touchPathPointsJson: String? get() = signatureDataJson.ifBlank { null }
    val deviceVerificationNote: String get() = ipOrDeviceNote
}

