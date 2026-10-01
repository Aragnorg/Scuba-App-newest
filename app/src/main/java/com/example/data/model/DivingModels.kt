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




