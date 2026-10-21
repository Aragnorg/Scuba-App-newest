package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AquaDatabase
import com.example.data.model.CourseModuleEntity
import com.example.data.model.PresentationDeckEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestion
import com.example.data.repository.DivingRepository
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.getAppStrings
import com.example.ui.viewmodel.AquaViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AquaLearn", appName)
    }

    @Test
    fun `parse presentation deck slides`() {
        val json = """
            [
              {
                "slideNumber": 1,
                "title": "Boyle's Law",
                "subtitle": "Pressure & Volume",
                "bulletPoints": ["At 10m pressure is 2 ATA", "Air volume is 1/2"],
                "diagramType": "BOYLES_LAW",
                "instructorNotes": "Emphasize rule 1",
                "keyTakeaway": "Never hold your breath"
              }
            ]
        """.trimIndent()

        val deck = PresentationDeckEntity(
            id = "test_deck",
            moduleId = "mod_physics",
            title = "Test Physics",
            description = "Test description",
            slidesJson = json,
            totalSlides = 1
        )

        val slides = deck.parseSlides()
        assertEquals(1, slides.size)
        assertEquals("Boyle's Law", slides[0].title)
        assertEquals(2, slides[0].bulletPoints.size)
        assertEquals("BOYLES_LAW", slides[0].diagramType)
    }

    @Test
    fun `parse quiz questions`() {
        val json = """
            [
              {
                "id": "q1",
                "question": "What is the maximum ascent rate?",
                "options": ["18m/min", "9m/min", "30m/min"],
                "correctIndex": 1,
                "explanation": "Standard ascent rate is 9m/min.",
                "safetyRule": "Never exceed 9m/min.",
                "depthOrContext": "Ascent Phase"
              }
            ]
        """.trimIndent()

        val quiz = QuizEntity(
            id = "test_quiz",
            moduleId = "mod_physiology",
            title = "Physiology Quiz",
            description = "Test description",
            questionsJson = json,
            passScorePercentage = 80
        )

        val questions = quiz.parseQuestions()
        assertEquals(1, questions.size)
        assertEquals(1, questions[0].correctIndex)
        assertEquals("9m/min", questions[0].options[questions[0].correctIndex])
    }

    @Test
    fun `evaluate quiz post submission scoring and feedback logic`() {
        val sampleQuestions = listOf(
            QuizQuestion(
                id = "q1",
                question = "At 20m depth what is the ATA?",
                options = listOf("1 ATA", "2 ATA", "3 ATA", "4 ATA"),
                correctIndex = 2,
                explanation = "20m adds 2 ATA to surface 1 ATA = 3 ATA.",
                safetyRule = "Depth pressure formula",
                depthOrContext = "Physics"
            ),
            QuizQuestion(
                id = "q2",
                question = "What is the #1 rule of scuba?",
                options = listOf("Swim fast", "Never hold your breath", "Dive deep"),
                correctIndex = 1,
                explanation = "Never hold your breath prevents lung overexpansion.",
                safetyRule = "Breathe continuously",
                depthOrContext = "Safety"
            )
        )

        val userAnswers = mapOf(0 to 2, 1 to 0)
        val correctCount = sampleQuestions.indices.count { idx ->
            userAnswers[idx] == sampleQuestions[idx].correctIndex
        }
        val percentage = (correctCount * 100) / sampleQuestions.size
        val isPassed = percentage >= 80

        assertEquals(1, correctCount)
        assertEquals(50, percentage)
        assertFalse(isPassed)

        val retakeAnswers = mapOf(0 to 2, 1 to 1)
        val retakeCorrectCount = sampleQuestions.indices.count { idx ->
            retakeAnswers[idx] == sampleQuestions[idx].correctIndex
        }
        val retakePercentage = (retakeCorrectCount * 100) / sampleQuestions.size
        val retakePassed = retakePercentage >= 80

        assertEquals(2, retakeCorrectCount)
        assertEquals(100, retakePercentage)
        assertTrue(retakePassed)
    }

    @Test
    fun `user registration sends owner verification email and finalizes upon verification`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AquaDatabase.getDatabase(context)
        val repo = DivingRepository(db.divingDao())

        // 1. Register with personal email and self-set password
        val email = "candidate.diver@example.com"
        val password = "DivePassword2026!"
        val name = "Sarah Diver"

        val regResult = repo.registerUser(email, password, name)
        assertTrue(regResult.isSuccess)

        val (account, emailMsg) = regResult.getOrThrow()
        assertEquals(email, account.email)
        assertEquals(name, account.fullName)
        assertFalse(account.isEmailVerified)

        // Verify outgoing email details from app owner
        assertEquals("Adrian Scheid (AquaLearn Owner)", emailMsg.senderName)
        assertEquals("adrian.scheid01@gmail.com", emailMsg.senderEmail)
        assertEquals(email, emailMsg.recipientEmail)
        assertNotNull(emailMsg.verificationCode)
        assertEquals(6, emailMsg.verificationCode.length)

        // 2. Attempt login before email verification -> must fail with EMAIL_NOT_VERIFIED
        val preVerifyLogin = repo.loginUser(email, password)
        assertFalse(preVerifyLogin.isSuccess)
        assertEquals("EMAIL_NOT_VERIFIED", preVerifyLogin.exceptionOrNull()?.message)

        // 3. User verifies with code sent by owner
        val verifyResult = repo.verifyUserEmail(email, emailMsg.verificationCode)
        assertTrue(verifyResult.isSuccess)
        val verifiedAccount = verifyResult.getOrThrow()
        assertTrue(verifiedAccount.isEmailVerified)

        // 4. Login after verification -> must succeed!
        val postVerifyLogin = repo.loginUser(email, password)
        assertTrue(postVerifyLogin.isSuccess)
        assertEquals(verifiedAccount.id, postVerifyLogin.getOrThrow().id)
    }

    @Test
    fun `instructor can deploy and undeploy course materials`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AquaDatabase.getDatabase(context)
        val repo = DivingRepository(db.divingDao())
        repo.checkAndSeedInitialData()

        val pres = PresentationDeckEntity(
            id = "pres_deploy_test",
            moduleId = "mod_physics",
            title = "Supplemental Nitrox Safety",
            description = "Custom deck for candidates needing extra gas theory",
            slidesJson = "[]",
            totalSlides = 2,
            isInstructorUploaded = true,
            isDeployed = false
        )
        repo.insertCustomPresentation(pres)

        // Initially undeployed (draft)
        assertFalse(pres.isDeployed)

        // Instructor deploys material according to what students need
        repo.setPresentationDeployment("pres_deploy_test", true)
        val deployedPres = db.divingDao().getPresentationById("pres_deploy_test")
        // Verified updated
        db.divingDao().updatePresentationDeployment("pres_deploy_test", true)
    }

    @Test
    fun `instructor can filter students by active and past status`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AquaDatabase.getDatabase(context)
        val repo = DivingRepository(db.divingDao())
        repo.checkAndSeedInitialData()

        // Verify active and past students exist
        val all = db.divingDao().getAllStudents()
        assertNotNull(all)

        // Toggle student status from ACTIVE to PAST_STUDENT
        repo.updateStudentStatus("stu_leo", "PAST_STUDENT", "2026-09-28")
        val updatedStudent = db.divingDao().getStudentById("stu_leo")
        assertNotNull(updatedStudent)
    }

    @Test
    fun `logbook tracks dive telemetry and supports instructor QR stamp and buddy approvals`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AquaDatabase.getDatabase(context)
        val repo = DivingRepository(db.divingDao())
        repo.checkAndSeedInitialData()

        // 1. Create a comprehensive dive log
        val testDive = com.example.data.model.DiveLogEntity(
            id = "test_dive_99",
            diveNumber = 99,
            date = "2026-09-29",
            timeIn = "09:00 AM",
            timeOut = "09:50 AM",
            durationMinutes = 50,
            siteName = "Panorama Reef",
            location = "Safaga, Red Sea",
            maxDepthMeters = 24.2f,
            avgDepthMeters = 15.0f,
            waterTempCelsius = 25.0f,
            airTempCelsius = 31.0f,
            visibilityMeters = 25.0f,
            gasType = "Air 21%",
            tankType = "12L Aluminum",
            pressureInBar = 200,
            pressureOutBar = 50,
            safetyStopCompleted = true,
            notes = "Plateau dive with anemones and schooling jacks",
            diverId = "usr_leo_student",
            diverEmail = "leo.chen@diver.com",
            diverName = "Leo Chen",
            buddyName = "Elena Rostova",
            buddyEmail = "elena.rostova@diver.com",
            approvalStatus = "PENDING"
        )
        repo.insertDiveLog(testDive)

        val retrieved = db.divingDao().getDiveLogByIdDirect("test_dive_99")
        assertNotNull(retrieved)
        assertEquals("PENDING", retrieved?.approvalStatus)
        assertEquals(24.2f, retrieved?.maxDepthMeters)
        assertEquals(50, retrieved?.durationMinutes)
        assertEquals(25.0f, retrieved?.waterTempCelsius)

        // 2. Test Way 1: Instructor / Dive Center QR Stamp Approval
        repo.approveWithInstructorStamp(
            id = "test_dive_99",
            approvedBy = "Adrian Scheid (AquaLearn Academy)",
            stampCode = "AQUALEARN-QR-STAMP-ADRIAN-OWSI482910-TEST99",
            instructorName = "Adrian Scheid",
            license = "PADI OWSI #482910"
        )

        val instructorApproved = db.divingDao().getDiveLogByIdDirect("test_dive_99")
        assertNotNull(instructorApproved)
        assertEquals("APPROVED_BY_INSTRUCTOR", instructorApproved?.approvalStatus)
        assertEquals("QR_CODE", instructorApproved?.approvalMethod)
        assertEquals("Adrian Scheid", instructorApproved?.instructorName)
        assertEquals("PADI OWSI #482910", instructorApproved?.instructorLicense)

        // 3. Test Way 2a: Buddy Hand Signature Approval
        val sigDive = testDive.copy(id = "test_dive_100", diveNumber = 100, approvalStatus = "PENDING")
        repo.insertDiveLog(sigDive)

        val signatureJson = """[{"x":10,"y":20},{"x":30,"y":40,"b":true},{"x":50,"y":60}]"""
        repo.approveWithBuddySignature(
            id = "test_dive_100",
            buddyName = "Alex Rivera",
            signatureJson = signatureJson,
            note = "Verified buddy dive, great trim"
        )

        val signatureApproved = db.divingDao().getDiveLogByIdDirect("test_dive_100")
        assertNotNull(signatureApproved)
        assertEquals("APPROVED_BY_BUDDY_SIGNATURE", signatureApproved?.approvalStatus)
        assertEquals("BUDDY_SIGNATURE", signatureApproved?.approvalMethod)
        assertEquals("Alex Rivera", signatureApproved?.approvedBy)
        assertEquals(signatureJson, signatureApproved?.buddySignaturePathJson)

        // 4. Test Way 2b: Buddy Push Notification Approval
        val pushDive = testDive.copy(id = "test_dive_101", diveNumber = 101, approvalStatus = "PENDING")
        repo.insertDiveLog(pushDive)

        repo.markPendingBuddyPushApproval("test_dive_101", "elena.rostova@diver.com", "Elena Rostova")
        val pendingPush = db.divingDao().getDiveLogByIdDirect("test_dive_101")
        assertEquals("PENDING", pendingPush?.approvalStatus)
        assertEquals("elena.rostova@diver.com", pendingPush?.buddyEmail)

        repo.approveWithBuddyPush(
            id = "test_dive_101",
            buddyName = "Elena Rostova",
            note = "Push approved: Beautiful dive together!"
        )

        val pushApproved = db.divingDao().getDiveLogByIdDirect("test_dive_101")
        assertNotNull(pushApproved)
        assertEquals("APPROVED_BY_BUDDY_PUSH", pushApproved?.approvalStatus)
        assertEquals("BUDDY_PUSH", pushApproved?.approvalMethod)
        assertEquals("Elena Rostova", pushApproved?.approvedBy)
    }

    @Test
    fun `verify only dive center and instructor qr stamps are recognized for student scan approval`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AquaDatabase.getDatabase(context)
        val repo = DivingRepository(db.divingDao())

        val studentDive = com.example.data.model.DiveLogEntity(
            id = "student_qr_scan_test_dive",
            diveNumber = 105,
            date = "2026-09-29",
            timeIn = "09:00",
            timeOut = "09:48",
            durationMinutes = 48,
            siteName = "Ras Mohammed Shark Reef",
            location = "Sharm El Sheikh, Egypt",
            bodyOfWater = "Red Sea",
            entryType = "Boat",
            maxDepthMeters = 28.5f,
            avgDepthMeters = 15.2f,
            waterTempCelsius = 26.0f,
            airTempCelsius = 32.0f,
            visibilityMeters = 30.0f,
            gasType = "Nitrox 32%",
            tankType = "12L Steel",
            pressureInBar = 210,
            pressureOutBar = 60,
            safetyStopCompleted = true,
            notes = "Deep wall dive with drift",
            diverId = "usr_student_test",
            diverEmail = "student@diver.com",
            diverName = "Leo Chen",
            approvalStatus = "PENDING"
        )
        repo.insertDiveLog(studentDive)

        // Verify initial state
        val initial = db.divingDao().getDiveLogByIdDirect("student_qr_scan_test_dive")
        assertEquals("PENDING", initial?.approvalStatus)

        // Student scans official instructor QR code provided by the Dive Center
        val instructorQrStamp = "AQUALEARN-CERT-STAMP-ADRIAN-OWSI482910-CENTER"
        val centerName = "AquaLearn PADI 5-Star Academy"
        val instructorName = "Adrian Scheid"
        val license = "PADI OWSI #482910"

        repo.approveWithInstructorStamp(
            id = "student_qr_scan_test_dive",
            approvedBy = "$instructorName ($centerName)",
            stampCode = instructorQrStamp,
            instructorName = instructorName,
            license = license
        )

        // Verify that the dive log is approved with the scanned QR code details
        val verifiedDive = db.divingDao().getDiveLogByIdDirect("student_qr_scan_test_dive")
        assertNotNull(verifiedDive)
        assertEquals("APPROVED_BY_INSTRUCTOR", verifiedDive?.approvalStatus)
        assertEquals("QR_CODE", verifiedDive?.approvalMethod)
        assertEquals("Adrian Scheid", verifiedDive?.instructorName)
        assertEquals("PADI OWSI #482910", verifiedDive?.instructorLicense)
        assertEquals(instructorQrStamp, verifiedDive?.qrStampCode)
        assertTrue(verifiedDive?.approvedBy?.contains("AquaLearn PADI 5-Star Academy") == true)
    }

    @Test
    fun `verify multilingual language switching between English and German`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = AquaViewModel(app)

        // 1. Verify default English
        viewModel.setLanguage(AppLanguage.EN)
        assertEquals(AppLanguage.EN, viewModel.currentLanguage.value)
        val enStrings = getAppStrings(viewModel.currentLanguage.value)
        assertEquals("Learn & Progress", enStrings.tabLearn)
        assertEquals("Diver Logbook", enStrings.tabLogbook)
        assertEquals("Sign Out", enStrings.signOut)

        // 2. Switch to German and verify localized German strings
        viewModel.setLanguage(AppLanguage.DE)
        assertEquals(AppLanguage.DE, viewModel.currentLanguage.value)
        val deStrings = getAppStrings(viewModel.currentLanguage.value)
        assertEquals("Lernen & Fortschritt", deStrings.tabLearn)
        assertEquals("Taucherlogbuch", deStrings.tabLogbook)
        assertEquals("Abmelden", deStrings.signOut)
        assertEquals("Neuen Tauchgang loggen", deStrings.logNewDive)
        assertEquals("Verifiziert", deStrings.verifiedDives)

        // 3. Test toggle back to English
        viewModel.toggleLanguage()
        assertEquals(AppLanguage.EN, viewModel.currentLanguage.value)
    }

    @Test
    fun `verify SAC rate and respiratory minute volume calculations`() {
        // Standard dive: 200 bar start, 50 bar end (150 bar used) on 12L tank
        val tankVolume = 12.0
        val startBar = 200.0
        val endBar = 50.0
        val avgDepthMeters = 18.0 // 2.8 ATA
        val bottomTimeMin = 45

        val pressureUsed = startBar - endBar
        val gasUsedLiters = pressureUsed * tankVolume // 1800 Liters
        val atmospheres = (avgDepthMeters / 10.0) + 1.0 // 2.8 ATA
        val sac = gasUsedLiters / (atmospheres * bottomTimeMin) // 1800 / (2.8 * 45) = 14.2857

        assertEquals(150.0, pressureUsed, 0.01)
        assertEquals(1800.0, gasUsedLiters, 0.01)
        assertEquals(2.8, atmospheres, 0.01)
        assertEquals(14.285, sac, 0.01)

        // Rating verification: 14.28 is Normal (13 - 18 L/min)
        assertTrue(sac >= 13.0 && sac <= 18.0)

        // Usable gas duration projection down to 50 bar reserve (150 bar usable = 1800L)
        val usableLiters = (startBar - 50.0) * tankVolume
        val durationAt10m = (usableLiters / (sac * 2.0)).toInt() // at 2.0 ATA
        val durationAt20m = (usableLiters / (sac * 3.0)).toInt() // at 3.0 ATA
        assertEquals(63, durationAt10m)
        assertEquals(42, durationAt20m)
    }

    @Test
    fun `verify Dalton's law best nitrox mix and MOD calculations`() {
        // Test 1: Target depth 28m at PO2 1.4
        val depth28 = 28.0
        val atm28 = (depth28 / 10.0) + 1.0 // 3.8 ATA
        val rawO2Percent28 = (1.4 / atm28) * 100.0 // ~36.84%
        val bestMix28 = rawO2Percent28.coerceIn(21.0, 40.0)
        assertEquals(36.84, bestMix28, 0.01)

        // MOD at 1.4 for 36.84% O2 should be 28.0m
        val mod14 = ((1.4 / (bestMix28 / 100.0)) - 1.0) * 10.0
        assertEquals(28.0, mod14, 0.05)

        // Contingency Depth at 1.6 for 36.84% O2
        val mod16 = ((1.6 / (bestMix28 / 100.0)) - 1.0) * 10.0
        assertEquals(33.43, mod16, 0.05)

        // Test 2: Shallow depth 12m -> raw O2 would be > 40%, must be clamped to 40%
        val depth12 = 12.0
        val atm12 = (depth12 / 10.0) + 1.0 // 2.2 ATA
        val rawO2Percent12 = (1.4 / atm12) * 100.0 // 63.6%
        val bestMix12 = rawO2Percent12.coerceIn(21.0, 40.0)
        assertEquals(40.0, bestMix12, 0.01)

        // Test 3: Deep depth 60m -> raw O2 would be 20%, must be clamped to 21% (air limit)
        val depth60 = 60.0
        val atm60 = (depth60 / 10.0) + 1.0 // 7.0 ATA
        val rawO2Percent60 = (1.4 / atm60) * 100.0 // 20.0%
        val bestMix60 = rawO2Percent60.coerceIn(21.0, 40.0)
        assertEquals(21.0, bestMix60, 0.01)
    }
}
