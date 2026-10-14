package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LanguageSwitchButton
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.LocalCurrentLanguage
import com.example.ui.theme.AquaCyan
import com.example.ui.theme.CoralAlert
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanSecondary
import com.example.ui.theme.SandGold
import com.example.ui.theme.SeaGreen
import com.example.ui.viewmodel.AquaViewModel
import com.example.ui.viewmodel.AuthState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AquaViewModel,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()
    val latestEmail by viewModel.latestVerificationEmail.collectAsState()
    val strings = LocalAppStrings.current
    val currentLanguage = LocalCurrentLanguage.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Login, 1: Register

    // If in PendingVerification mode, display the Email Verification UI
    if (authState is AuthState.PendingVerification) {
        val pending = authState as AuthState.PendingVerification
        EmailVerificationScreen(
            email = pending.email,
            candidateName = pending.fullName,
            latestEmailMessage = latestEmail ?: pending.lastEmailMessage,
            authError = authError,
            authSuccess = authSuccess,
            onVerify = { code -> viewModel.verifyUserEmail(pending.email, code) },
            onResend = { viewModel.resendVerificationEmail(pending.email) },
            onBackToLogin = { viewModel.returnToLogin() }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top Language Switcher Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageSwitchButton(
                currentLanguage = currentLanguage,
                onLanguageChange = { viewModel.setLanguage(it) }
            )
        }

        // App Logo & Branding
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = OceanPrimary.copy(alpha = 0.15f),
            border = BorderStroke(2.dp, AquaCyan)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = "🤿", fontSize = 38.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "AquaLearn Diver Academy",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Self-Paced Training & Certification Portal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tab Row (Log In vs Register)
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    viewModel.clearAuthMessages()
                },
                text = { Text(strings.signIn, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_login")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    viewModel.clearAuthMessages()
                },
                text = { Text(strings.register, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_register")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error message banner
        if (authError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = CoralAlert.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = "Error",
                        tint = CoralAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authError!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralAlert,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Success message banner
        if (authSuccess != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SeaGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SeaGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authSuccess!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = SeaGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedTab == 0) {
            // LOGIN FORM
            LoginForm(
                onLogin = { email, password ->
                    viewModel.loginUser(email, password)
                },
                onQuickLoginStudent = { viewModel.quickLoginStudent() },
                onQuickLoginInstructor = { viewModel.quickLoginInstructor() },
                onSwitchToRegister = { selectedTab = 1 }
            )
        } else {
            // REGISTRATION FORM
            RegisterForm(
                onRegister = { email, password, name, role ->
                    viewModel.registerUser(email, password, name, role)
                },
                onSwitchToLogin = { selectedTab = 0 }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun LoginForm(
    onLogin: (email: String, passwordPlain: String) -> Unit,
    onQuickLoginStudent: () -> Unit,
    onQuickLoginInstructor: () -> Unit,
    onSwitchToRegister: () -> Unit
) {
    val strings = LocalAppStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = strings.welcomeBackAquaLearn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Quick Demo Switchers
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OceanPrimary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = strings.quickDemoLoginSubtitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onQuickLoginStudent,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_login_student_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, OceanPrimary)
                        ) {
                            Text("🎓 ${strings.studentDemoButton}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onQuickLoginInstructor,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_login_instructor_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
                        ) {
                            Text("🤿 ${strings.instructorDemoButton}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Text(
                    text = "  ${strings.orSignInWithCredentials}  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(strings.emailAddressLabel) },
                placeholder = { Text("e.g. diver@gmail.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = strings.email) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(strings.passwordLabel) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = strings.password) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            Button(
                onClick = { onLogin(email.trim(), password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_button")
            ) {
                Text(strings.signIn, fontWeight = FontWeight.Bold)
            }

            // Switch to Register link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.dontHaveAccountRegister,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onSwitchToRegister() }
                )
            }
        }
    }
}

@Composable
fun RegisterForm(
    onRegister: (email: String, passwordPlain: String, fullName: String, role: String) -> Unit,
    onSwitchToLogin: () -> Unit
) {
    val strings = LocalAppStrings.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("STUDENT") } // "STUDENT" or "INSTRUCTOR"
    var agreeMedicalDeclaration by remember { mutableStateOf(false) }

    val passwordsMatch = password.isNotBlank() && password == confirmPassword
    val passwordLengthOk = password.length >= 6

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = strings.createAccountTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                    "Erstelle dein persönliches Konto mit deiner E-Mail-Adresse (Sporttaucher oder Tauchlehrer/Leitung)."
                else
                    "Create your personal account with your email address (Diver or Leadership).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(strings.fullName) },
                placeholder = { Text("e.g. Alex Morgan") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = strings.fullName) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_name_input")
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(strings.emailAddressLabel) },
                placeholder = { Text("e.g. alex.diver@gmail.com") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = strings.email) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("${strings.passwordLabel} (Min 6)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = strings.password) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_password_input")
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(strings.confirmPasswordLabel) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = strings.confirmPasswordLabel) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                isError = confirmPassword.isNotBlank() && !passwordsMatch,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register_confirm_password_input")
            )

            // Role / Environment Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.chooseYourRole,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Student Role Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "STUDENT" }
                            .testTag("role_student_select"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedRole == "STUDENT") OceanPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = if (selectedRole == "STUDENT") 2.dp else 1.dp,
                            color = if (selectedRole == "STUDENT") OceanPrimary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedRole == "STUDENT",
                                    onClick = { selectedRole = "STUDENT" },
                                    colors = RadioButtonDefaults.colors(selectedColor = OceanPrimary)
                                )
                                Text(
                                    text = "🎓 ${strings.candidateStudentRole}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selectedRole == "STUDENT") OceanPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = strings.candidateStudentRoleDesc,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    // Instructor Role Card
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "INSTRUCTOR" }
                            .testTag("role_instructor_select"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedRole == "INSTRUCTOR") AquaCyan.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = if (selectedRole == "INSTRUCTOR") 2.dp else 1.dp,
                            color = if (selectedRole == "INSTRUCTOR") OceanPrimary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedRole == "INSTRUCTOR",
                                    onClick = { selectedRole = "INSTRUCTOR" },
                                    colors = RadioButtonDefaults.colors(selectedColor = OceanPrimary)
                                )
                                Text(
                                    text = "🤿 ${strings.certifiedInstructorRole}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selectedRole == "INSTRUCTOR") OceanPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = strings.certifiedInstructorRoleDesc,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            // Scuba safety agreement
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { agreeMedicalDeclaration = !agreeMedicalDeclaration }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = agreeMedicalDeclaration,
                    onCheckedChange = { agreeMedicalDeclaration = it },
                    colors = CheckboxDefaults.colors(checkedColor = OceanPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedRole == "STUDENT") {
                        "Open Water Safety Protocol Agreement"
                    } else {
                        "Certified Scuba Instructor Authorization"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            Button(
                onClick = { onRegister(email.trim(), password, fullName, selectedRole) },
                enabled = email.isNotBlank() && passwordLengthOk && passwordsMatch && agreeMedicalDeclaration,
                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("register_submit_button")
            ) {
                Icon(Icons.Default.Email, contentDescription = strings.register)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.register, fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.alreadyHaveAccount,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onSwitchToLogin() }
                )
            }
        }
    }
}

/**
 * Verification Screen:
 * Automatically displayed upon registration.
 * Candidate verifies the code sent automatically by the app owner (Adrian Scheid).
 */
@Composable
fun EmailVerificationScreen(
    email: String,
    candidateName: String,
    latestEmailMessage: com.example.data.model.VerificationEmailMessage?,
    authError: String?,
    authSuccess: String?,
    onVerify: (code: String) -> Unit,
    onResend: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    var verificationCode by remember { mutableStateOf("") }
    var showFullEmailPreview by remember { mutableStateOf(false) }

    // Resend countdown timer
    var resendCountdown by remember { mutableIntStateOf(30) }
    LaunchedEffect(Unit) {
        while (resendCountdown > 0) {
            delay(1000L)
            resendCountdown--
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back to Login icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBackToLogin,
                modifier = Modifier.testTag("back_to_login_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToLoginButton)
            }
        }

        // Email Icon Hero
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = AquaCyan.copy(alpha = 0.2f),
            border = BorderStroke(2.dp, AquaCyan)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.MarkEmailRead,
                    contentDescription = strings.verifyEmailTitle,
                    tint = OceanPrimary,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Text(
            text = strings.verifyEmailTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "${strings.verifyEmailSubtitle} $email",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = email,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = OceanPrimary
            )
        }

        // Error banner
        if (authError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = CoralAlert.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CoralAlert)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authError,
                        style = MaterialTheme.typography.bodySmall,
                        color = CoralAlert,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Success banner
        if (authSuccess != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SeaGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeaGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = authSuccess,
                        style = MaterialTheme.typography.bodySmall,
                        color = SeaGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Automatic Email Inbox Simulator / Inspection Card
        if (latestEmailMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("incoming_email_preview_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, OceanPrimary.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape = CircleShape,
                                color = SeaGreen.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = null,
                                        tint = SeaGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                                    "Aktivierungs-E-Mail (Posteingang)"
                                else
                                    "Verification Email (Inbox)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SeaGreen
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AquaCyan.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "INBOX",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Von: ${latestEmailMessage.senderName} <${latestEmailMessage.senderEmail}>",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Betreff: ${latestEmailMessage.subject}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Security Code Highlight
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${strings.verificationCodeLabel}:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = latestEmailMessage.verificationCode,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 4.sp,
                                        color = OceanPrimary
                                    )
                                }
                                Button(
                                    onClick = {
                                        verificationCode = latestEmailMessage.verificationCode
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanSecondary),
                                    modifier = Modifier.testTag("autofill_verification_code_button")
                                ) {
                                    Text(strings.autofillButton)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 1-Click Instant Activation
                            Button(
                                onClick = {
                                    verificationCode = latestEmailMessage.verificationCode
                                    onVerify(latestEmailMessage.verificationCode)
                                },
                                modifier = Modifier.fillMaxWidth().testTag("instant_activate_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                                        "⚡ 1-Klick Aktivieren & Einloggen"
                                    else
                                        "⚡ 1-Click Activate & Sign In",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Toggle full email body
                    TextButton(
                        onClick = { showFullEmailPreview = !showFullEmailPreview }
                    ) {
                        Text(if (showFullEmailPreview) strings.hideDetails else strings.showDetails)
                    }

                    AnimatedVisibility(visible = showFullEmailPreview) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = latestEmailMessage.emailHtmlBody,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Code Entry Field
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = strings.verificationCodeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            verificationCode = it
                        }
                    },
                    placeholder = { Text("123456") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 6.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (verificationCode.length == 6) {
                                onVerify(verificationCode)
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verification_code_input")
                )

                Button(
                    onClick = { onVerify(verificationCode) },
                    enabled = verificationCode.length == 6,
                    colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("verify_code_submit_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = strings.verifyButton)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.verifyButton, fontWeight = FontWeight.Bold)
                }

                // Resend Code Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            onResend()
                            resendCountdown = 30
                        },
                        enabled = resendCountdown == 0,
                        modifier = Modifier.testTag("resend_code_button")
                    ) {
                        Text(
                            text = if (resendCountdown > 0) "${strings.resendCodeButton} (${resendCountdown}s)" else strings.resendCodeButton,
                            fontWeight = FontWeight.Bold,
                            color = if (resendCountdown > 0) MaterialTheme.colorScheme.outline else OceanPrimary
                        )
                    }
                }
            }
        }
    }
}
