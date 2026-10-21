package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.LanguageSwitchButton
import com.example.ui.i18n.AppLanguage
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
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

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

    // Oceanic Atmospheric Backdrop with glowing ambient depth
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020713), // Deep Abyssal Void
                        Color(0xFF07172C), // Subsurface Navy Glow
                        Color(0xFF04101F), // Oceanic Trench
                        Color(0xFF01040A)  // Deep Bottom
                    )
                )
            )
    ) {
        // Subtle ambient caustic / bioluminescent backlight behind seal
        Box(
            modifier = Modifier
                .size(340.dp)
                .align(Alignment.TopCenter)
                .padding(top = 20.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AquaCyan.copy(alpha = 0.15f),
                            OceanPrimary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top Language Switcher Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                LanguageSwitchButton(
                    currentLanguage = currentLanguage,
                    onLanguageChange = { viewModel.setLanguage(it) }
                )
            }

            // Glowing Academy Seal & Insignia
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                // Outer Luminous Glow Aura
                Surface(
                    modifier = Modifier.size(92.dp),
                    shape = CircleShape,
                    color = AquaCyan.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.35f))
                ) {}

                // Middle Tactical Bezel
                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    color = Color(0xFF0A1B2E).copy(alpha = 0.90f),
                    border = BorderStroke(2.dp, Brush.sweepGradient(listOf(AquaCyan, OceanPrimary, AquaCyan)))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🤿", fontSize = 38.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "AquaLearn Diver Academy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                color = Color.White
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AquaCyan.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.25f)),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "EST. 2024 • TACTICAL TRAINING & CERTIFICATION",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = AquaCyan,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Floating Glass Tab Switcher (Log In vs Register)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0B1728).copy(alpha = 0.75f),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(AquaCyan.copy(alpha = 0.3f), Color(0x15FFFFFF)))),
                modifier = Modifier.fillMaxWidth()
            ) {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    indicator = {},
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    val loginSelected = selectedTab == 0
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                selectedTab = 0
                                viewModel.clearAuthMessages()
                            }
                            .testTag("tab_login"),
                        shape = RoundedCornerShape(18.dp),
                        color = if (loginSelected) OceanPrimary else Color.Transparent,
                        border = if (loginSelected) BorderStroke(1.dp, AquaCyan.copy(alpha = 0.6f)) else null
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        ) {
                            Text(
                                text = strings.signIn,
                                fontWeight = if (loginSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (loginSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }

                    val registerSelected = selectedTab == 1
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .clickable {
                                selectedTab = 1
                                viewModel.clearAuthMessages()
                            }
                            .testTag("tab_register"),
                        shape = RoundedCornerShape(18.dp),
                        color = if (registerSelected) OceanPrimary else Color.Transparent,
                        border = if (registerSelected) BorderStroke(1.dp, AquaCyan.copy(alpha = 0.6f)) else null
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 11.dp)
                        ) {
                            Text(
                                text = strings.register,
                                fontWeight = if (registerSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (registerSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Error message banner
            if (authError != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CoralAlert.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = CoralAlert,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = authError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = CoralAlert,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Success message banner
            if (authSuccess != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = SeaGreen.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = SeaGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = authSuccess!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = SeaGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (selectedTab == 0) {
                // LOGIN FORM
                LoginForm(
                    onLogin = { email, password ->
                        viewModel.loginUser(email, password)
                    },
                    onQuickLoginStudent = { viewModel.quickLoginStudent() },
                    onQuickLoginInstructor = { viewModel.quickLoginInstructor() },
                    onSwitchToRegister = { selectedTab = 1 },
                    onForgotPassword = { showForgotPasswordDialog = true }
                )
            } else {
                // REGISTRATION FORM
                RegisterForm(
                    onRegister = { email, password, name, role ->
                        viewModel.registerUser(email, password, name, role)
                    },
                    onSwitchToLogin = { selectedTab = 0 },
                    onForgotPassword = { showForgotPasswordDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                viewModel = viewModel,
                onDismiss = { showForgotPasswordDialog = false }
            )
        }
    }
}

@Composable
fun LoginForm(
    onLogin: (email: String, passwordPlain: String) -> Unit,
    onQuickLoginStudent: () -> Unit,
    onQuickLoginInstructor: () -> Unit,
    onSwitchToRegister: () -> Unit,
    onForgotPassword: (() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val glassTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AquaCyan,
        unfocusedBorderColor = Color(0xFF243B55),
        focusedLabelColor = AquaCyan,
        unfocusedLabelColor = Color(0xFF94A3B8),
        focusedLeadingIconColor = AquaCyan,
        unfocusedLeadingIconColor = Color(0xFF64748B),
        focusedTrailingIconColor = AquaCyan,
        unfocusedTrailingIconColor = Color(0xFF64748B),
        cursorColor = AquaCyan,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color(0xFF060E1B).copy(alpha = 0.75f),
        unfocusedContainerColor = Color(0xFF060E1B).copy(alpha = 0.45f)
    )

    // Floating Glass Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B172A).copy(alpha = 0.88f)),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(AquaCyan.copy(alpha = 0.45f), OceanPrimary.copy(alpha = 0.2f), Color(0x15FFFFFF)))),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.welcomeBackAquaLearn,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AquaCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "PORTAL v2.4",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Quick Demo Switchers
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF081424).copy(alpha = 0.80f),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(AquaCyan.copy(alpha = 0.35f), OceanPrimary.copy(alpha = 0.15f)))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = OceanPrimary.copy(alpha = 0.25f),
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚡", fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.quickDemoLoginSubtitle,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AquaCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onQuickLoginStudent,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("quick_login_student_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF0A1E35).copy(alpha = 0.6f),
                                contentColor = Color.White
                            )
                        ) {
                            Text("🎓 ${strings.studentDemoButton}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = onQuickLoginInstructor,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("quick_login_instructor_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                            border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.5f))
                        ) {
                            Text("🤿 ${strings.instructorDemoButton}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF20364F)))
                Text(
                    text = "  ${strings.orSignInWithCredentials}  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF889BB4)
                )
                Spacer(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFF20364F)))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(strings.emailAddressLabel) },
                placeholder = { Text("e.g. diver@gmail.com", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = strings.email) },
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(strings.passwordLabel) },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = strings.password) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_password_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onForgotPassword?.invoke() },
                    modifier = Modifier.testTag("forgot_password_button")
                ) {
                    Text(
                        text = if (LocalCurrentLanguage.current == AppLanguage.DE) "Passwort vergessen?" else "Forgot password?",
                        style = MaterialTheme.typography.labelSmall,
                        color = AquaCyan,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onLogin(email.trim(), password) },
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OceanPrimary,
                        disabledContainerColor = OceanPrimary.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 40.dp, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("login_submit_button")
                ) {
                    Text(strings.signIn, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
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
                    color = AquaCyan,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSwitchToRegister() }
                )
            }
        }
    }
}

@Composable
fun RegisterForm(
    onRegister: (email: String, passwordPlain: String, fullName: String, role: String) -> Unit,
    onSwitchToLogin: () -> Unit,
    onForgotPassword: (() -> Unit)? = null
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

    val glassTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AquaCyan,
        unfocusedBorderColor = Color(0xFF243B55),
        focusedLabelColor = AquaCyan,
        unfocusedLabelColor = Color(0xFF94A3B8),
        focusedLeadingIconColor = AquaCyan,
        unfocusedLeadingIconColor = Color(0xFF64748B),
        focusedTrailingIconColor = AquaCyan,
        unfocusedTrailingIconColor = Color(0xFF64748B),
        cursorColor = AquaCyan,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color(0xFF060E1B).copy(alpha = 0.75f),
        unfocusedContainerColor = Color(0xFF060E1B).copy(alpha = 0.45f)
    )

    // Floating Glass Card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B172A).copy(alpha = 0.88f)),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(AquaCyan.copy(alpha = 0.45f), OceanPrimary.copy(alpha = 0.2f), Color(0x15FFFFFF)))),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = strings.createAccountTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            val isGerman = com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE

            Text(
                text = if (isGerman)
                    "Erstelle dein persönliches Konto mit deiner E-Mail-Adresse (Sporttaucher oder Tauchlehrer/Leitung)."
                else
                    "Create your personal account with your email address (Diver or Leadership).",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(strings.fullName) },
                placeholder = { Text(if (isGerman) "z. B. Alex Morgan" else "e.g. Alex Morgan", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = strings.fullName) },
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_name_input")
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(strings.emailAddressLabel) },
                placeholder = { Text(if (isGerman) "z. B. alex.diver@gmail.com" else "e.g. alex.diver@gmail.com", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = strings.email) },
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("${strings.passwordLabel} (Min 6)") },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = strings.password) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_password_input")
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(strings.confirmPasswordLabel) },
                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = strings.confirmPasswordLabel) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(14.dp),
                colors = glassTextFieldColors,
                singleLine = true,
                isError = confirmPassword.isNotBlank() && !passwordsMatch,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("register_confirm_password_input")
            )

            // Role / Environment Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = strings.chooseYourRole,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
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
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedRole == "STUDENT") OceanPrimary.copy(alpha = 0.22f) else Color(0xFF071220).copy(alpha = 0.6f),
                        border = BorderStroke(
                            width = if (selectedRole == "STUDENT") 2.dp else 1.dp,
                            color = if (selectedRole == "STUDENT") AquaCyan else Color(0xFF243B55)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedRole == "STUDENT",
                                    onClick = { selectedRole = "STUDENT" },
                                    colors = RadioButtonDefaults.colors(selectedColor = AquaCyan)
                                )
                                Text(
                                    text = "🎓 ${strings.candidateStudentRole}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selectedRole == "STUDENT") Color.White else Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = strings.candidateStudentRoleDesc,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
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
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedRole == "INSTRUCTOR") AquaCyan.copy(alpha = 0.22f) else Color(0xFF071220).copy(alpha = 0.6f),
                        border = BorderStroke(
                            width = if (selectedRole == "INSTRUCTOR") 2.dp else 1.dp,
                            color = if (selectedRole == "INSTRUCTOR") AquaCyan else Color(0xFF243B55)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedRole == "INSTRUCTOR",
                                    onClick = { selectedRole = "INSTRUCTOR" },
                                    colors = RadioButtonDefaults.colors(selectedColor = AquaCyan)
                                )
                                Text(
                                    text = "🤿 ${strings.certifiedInstructorRole}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selectedRole == "INSTRUCTOR") Color.White else Color(0xFF94A3B8)
                                )
                            }
                            Text(
                                text = strings.certifiedInstructorRoleDesc,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }

            // Scuba safety agreement
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { agreeMedicalDeclaration = !agreeMedicalDeclaration },
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF071220).copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color(0xFF243B55))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreeMedicalDeclaration,
                        onCheckedChange = { agreeMedicalDeclaration = it },
                        colors = CheckboxDefaults.colors(checkedColor = AquaCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGerman) {
                            if (selectedRole == "STUDENT") {
                                "Zustimmung zum Open Water Sicherheitsprotokoll"
                            } else {
                                "Autorisierung als zertifizierter Tauchlehrer"
                            }
                        } else {
                            if (selectedRole == "STUDENT") {
                                "Open Water Safety Protocol Agreement"
                            } else {
                                "Certified Scuba Instructor Authorization"
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { onRegister(email.trim(), password, fullName, selectedRole) },
                    enabled = email.isNotBlank() && passwordLengthOk && passwordsMatch && agreeMedicalDeclaration,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SeaGreen,
                        disabledContainerColor = SeaGreen.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 36.dp, vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("register_submit_button")
                ) {
                    Icon(Icons.Rounded.Email, contentDescription = strings.register, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.register, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.alreadyHaveAccount,
                    style = MaterialTheme.typography.bodySmall,
                    color = AquaCyan,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onSwitchToLogin() }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { onForgotPassword?.invoke() },
                    modifier = Modifier.testTag("register_forgot_password_button")
                ) {
                    Text(
                        text = if (LocalCurrentLanguage.current == AppLanguage.DE) "Passwort vergessen?" else "Forgot password?",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium
                    )
                }
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF020713),
                        Color(0xFF07172C),
                        Color(0xFF04101F),
                        Color(0xFF01040A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = strings.backToLoginButton)
            }
        }

        // Email Icon Hero
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = AquaCyan.copy(alpha = 0.15f),
            border = BorderStroke(2.dp, AquaCyan)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Rounded.MarkEmailRead,
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
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Text(
                text = email,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = OceanPrimary
            )
        }

        // Official Sender Badge (Adrian Scheid)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sender_badge_adrian_scheid"),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF09172A).copy(alpha = 0.85f),
            border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = AquaCyan.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = AquaCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                            "Offizieller Zertifizierungs- & Code-Dienst"
                        else
                            "Official Certification & Code Dispatch",
                        style = MaterialTheme.typography.labelSmall,
                        color = AquaCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Absender: Adrian Scheid <adrian.scheid01@gmail.com>",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // 1-Tap Action Buttons Strip
        val context = LocalContext.current
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1-tap "E-Mail über Mail-App öffnen / senden" button
            OutlinedButton(
                onClick = {
                    try {
                        val currentCode = latestEmailMessage?.verificationCode ?: verificationCode
                        val codeNotice = if (currentCode.isNotBlank()) "Code: $currentCode" else ""
                        val subject = "AquaLearn Aktivierungscode: $currentCode (Adrian Scheid)".trim()
                        val body = "Hallo Adrian,\n\nhier ist die Bestätigung für meine Registrierung in AquaLearn Pro für das Konto: $email.\n$codeNotice\n\nViele Grüße,\n$candidateName"
                        val mailUri = Uri.parse(
                            "mailto:${Uri.encode(email)}?cc=${Uri.encode("adrian.scheid01@gmail.com")}" +
                            "&subject=${Uri.encode(subject)}" +
                            "&body=${Uri.encode(body)}"
                        )
                        val intent = Intent(Intent.ACTION_SENDTO, mailUri)
                        context.startActivity(Intent.createChooser(intent, "E-Mail über Mail-App öffnen"))
                    } catch (e: Exception) {
                        try {
                            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "message/rfc822"
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                                putExtra(Intent.EXTRA_CC, arrayOf("adrian.scheid01@gmail.com"))
                                putExtra(Intent.EXTRA_SUBJECT, "AquaLearn Aktivierungscode (Adrian Scheid)")
                            }
                            context.startActivity(Intent.createChooser(fallbackIntent, "Mail-App wählen"))
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("open_mail_app_button"),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = AquaCyan.copy(alpha = 0.08f))
            ) {
                Icon(Icons.Rounded.Send, contentDescription = null, tint = AquaCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                        "E-Mail über Mail-App öffnen / senden"
                    else
                        "Open / Send via Mail App",
                    fontWeight = FontWeight.Bold,
                    color = AquaCyan
                )
            }

            // 1-tap "Code automatisch einfügen & Verifizieren" button
            Button(
                onClick = {
                    val codeToVerify = latestEmailMessage?.verificationCode ?: if (verificationCode.isNotBlank()) verificationCode else "123456"
                    verificationCode = codeToVerify
                    onVerify(codeToVerify)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("autofill_and_verify_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
            ) {
                Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                        "Code automatisch einfügen & Verifizieren"
                    else
                        "Auto-Fill Code & Verify",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }

        // Error banner
        if (authError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CoralAlert.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = CoralAlert)
                    Spacer(modifier = Modifier.width(10.dp))
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
                shape = RoundedCornerShape(20.dp),
                color = SeaGreen.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SeaGreen)
                    Spacer(modifier = Modifier.width(10.dp))
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(1.dp, OceanPrimary.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        Icons.Rounded.Email,
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
                            shape = RoundedCornerShape(8.dp),
                            color = AquaCyan.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "INBOX",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanSecondary),
                                    modifier = Modifier
                                        .height(46.dp)
                                        .testTag("autofill_verification_code_button")
                                ) {
                                    Text(strings.autofillButton, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = {
                                        verificationCode = latestEmailMessage.verificationCode
                                        onVerify(latestEmailMessage.verificationCode)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("instant_activate_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp)
                                ) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE)
                                            "⚡ 1-Klick Aktivieren & Einloggen"
                                        else
                                            "⚡ 1-Click Activate & Sign In",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
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
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = latestEmailMessage.emailHtmlBody,
                                modifier = Modifier.padding(12.dp),
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
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = strings.verificationCodeLabel,
                    style = MaterialTheme.typography.titleMedium,
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
                    shape = RoundedCornerShape(14.dp),
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
                        .height(52.dp)
                        .testTag("verification_code_input")
                )

                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { onVerify(verificationCode) },
                        enabled = verificationCode.length == 6,
                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 36.dp, vertical = 12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("verify_code_submit_button")
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = strings.verifyButton)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.verifyButton, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
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
}

/**
 * Modal Dialog for "Passwort vergessen?":
 * - Allows requesting a 6-digit security code dispatched from Adrian Scheid (adrian.scheid01@gmail.com)
 * - Allows entering the code and setting a new password, persisted securely in Room DB
 */
@Composable
fun ForgotPasswordDialog(
    viewModel: AquaViewModel,
    initialEmail: String = "",
    onDismiss: () -> Unit
) {
    val isDe = com.example.ui.i18n.LocalCurrentLanguage.current == com.example.ui.i18n.AppLanguage.DE
    val resetSuccess by viewModel.passwordResetSuccess.collectAsState()
    val resetError by viewModel.passwordResetError.collectAsState()
    val latestResetEmail by viewModel.latestPasswordResetEmail.collectAsState()

    var email by remember { mutableStateOf(initialEmail) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(1) } // 1: Request Code, 2: Enter Code & New Password
    val context = LocalContext.current

    // Automatically advance to step 2 when reset email is dispatched
    LaunchedEffect(latestResetEmail) {
        if (latestResetEmail != null) {
            code = latestResetEmail?.verificationCode.orEmpty()
            currentStep = 2
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.clearPasswordResetState()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .verticalScroll(rememberScrollState())
                .testTag("forgot_password_dialog"),
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF091424),
            border = BorderStroke(1.dp, Brush.linearGradient(listOf(AquaCyan.copy(alpha = 0.5f), OceanPrimary.copy(alpha = 0.3f))))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            color = AquaCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Rounded.LockReset,
                                    contentDescription = null,
                                    tint = AquaCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isDe) "Passwort vergessen?" else "Forgot Password?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isDe) "Sicherheits-Wiederherstellung" else "Security Recovery",
                                style = MaterialTheme.typography.labelSmall,
                                color = AquaCyan
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.clearPasswordResetState()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("forgot_password_close_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                // Official Dispatcher Info Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF050E1B),
                    border = BorderStroke(1.dp, Color(0xFF1E3550))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = AquaCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDe)
                                "Code-Versand durch Adrian Scheid <adrian.scheid01@gmail.com>"
                            else
                                "Code dispatched by Adrian Scheid <adrian.scheid01@gmail.com>",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }

                // Step Switcher (Tab-like pills)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentStep = 1 },
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentStep == 1) OceanPrimary else Color(0xFF132238),
                        border = if (currentStep == 1) BorderStroke(1.dp, AquaCyan) else null
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = if (isDe) "1. Code anfordern" else "1. Request Code",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (currentStep == 1) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { currentStep = 2 },
                        shape = RoundedCornerShape(10.dp),
                        color = if (currentStep == 2) OceanPrimary else Color(0xFF132238),
                        border = if (currentStep == 2) BorderStroke(1.dp, AquaCyan) else null
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = if (isDe) "2. Neues Passwort" else "2. New Password",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (currentStep == 2) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Error Banner
                if (resetError != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralAlert.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CoralAlert.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = CoralAlert, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = resetError!!, style = MaterialTheme.typography.bodySmall, color = CoralAlert)
                        }
                    }
                }

                // Success Banner
                if (resetSuccess != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SeaGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = SeaGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = resetSuccess!!, style = MaterialTheme.typography.bodySmall, color = SeaGreen)
                        }
                    }
                }

                val dialogTextFieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AquaCyan,
                    unfocusedBorderColor = Color(0xFF243B55),
                    focusedLabelColor = AquaCyan,
                    unfocusedLabelColor = Color(0xFF94A3B8),
                    cursorColor = AquaCyan,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF060E1B).copy(alpha = 0.85f),
                    unfocusedContainerColor = Color(0xFF060E1B).copy(alpha = 0.65f)
                )

                if (currentStep == 1) {
                    // STEP 1: Enter email and trigger reset code
                    Text(
                        text = if (isDe)
                            "Gib die registrierte E-Mail-Adresse ein. Adrian Scheid sendet dir umgehend einen 6-stelligen Verifizierungscode."
                        else
                            "Enter your registered email address. Adrian Scheid will dispatch a 6-digit verification code immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(if (isDe) "E-Mail-Adresse" else "Email Address") },
                        placeholder = { Text("z.B. diver@gmail.com", color = Color(0xFF64748B)) },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = AquaCyan) },
                        shape = RoundedCornerShape(14.dp),
                        colors = dialogTextFieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("forgot_password_email_input")
                    )

                    Button(
                        onClick = { viewModel.requestPasswordReset(email.trim()) },
                        enabled = email.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("request_reset_code_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
                    ) {
                        Icon(Icons.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDe) "Sicherheitscode anfordern" else "Request Security Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Display Preview if code was dispatched
                    if (latestResetEmail != null) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0B1B2F),
                            border = BorderStroke(1.dp, SeaGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().testTag("reset_email_preview_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "E-Mail empfangen",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = SeaGreen
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AquaCyan.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = latestResetEmail!!.verificationCode,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 2.sp,
                                            color = AquaCyan
                                        )
                                    }
                                }
                                Text(
                                    text = "Von: ${latestResetEmail!!.senderName} <${latestResetEmail!!.senderEmail}>",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                                Button(
                                    onClick = {
                                        code = latestResetEmail!!.verificationCode
                                        currentStep = 2
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
                                ) {
                                    Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isDe) "Code übernehmen & zu Schritt 2" else "Use Code & Go to Step 2", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // STEP 2: Enter code & set new password
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                code = it
                            }
                        },
                        label = { Text(if (isDe) "6-stelliger Sicherheitscode" else "6-digit Security Code") },
                        placeholder = { Text("123456", color = Color(0xFF64748B)) },
                        leadingIcon = { Icon(Icons.Rounded.Shield, contentDescription = null, tint = AquaCyan) },
                        trailingIcon = {
                            if (latestResetEmail != null && code != latestResetEmail!!.verificationCode) {
                                TextButton(onClick = { code = latestResetEmail!!.verificationCode }) {
                                    Text("Einfügen", color = AquaCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = dialogTextFieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("forgot_password_code_input")
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(if (isDe) "Neues Passwort" else "New Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = AquaCyan) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        colors = dialogTextFieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("forgot_password_new_password_input")
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(if (isDe) "Passwort bestätigen" else "Confirm Password") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = AquaCyan) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        colors = dialogTextFieldColors,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("forgot_password_confirm_password_input")
                    )

                    val passwordsMatch = newPassword.isNotEmpty() && newPassword == confirmPassword
                    val canSubmit = email.isNotBlank() && code.length == 6 && newPassword.length >= 6 && passwordsMatch

                    Button(
                        onClick = {
                            viewModel.confirmPasswordReset(
                                email = email.trim(),
                                code = code.trim(),
                                newPasswordPlain = newPassword
                            )
                        },
                        enabled = canSubmit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_password_reset_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SeaGreen)
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDe) "Passwort speichern & aktivieren" else "Save Password & Activate",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Native Mail App Button
                    OutlinedButton(
                        onClick = {
                            try {
                                val subject = "AquaLearn Passwort-Reset: $code (Adrian Scheid)"
                                val body = "Hallo Adrian,\n\nich setze mein Passwort für $email mit dem Code $code zurück.\n\nViele Grüße"
                                val mailUri = Uri.parse(
                                    "mailto:${Uri.encode(email)}?cc=${Uri.encode("adrian.scheid01@gmail.com")}" +
                                    "&subject=${Uri.encode(subject)}" +
                                    "&body=${Uri.encode(body)}"
                                )
                                val intent = Intent(Intent.ACTION_SENDTO, mailUri)
                                context.startActivity(Intent.createChooser(intent, "Mail-App öffnen"))
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("reset_open_mail_app_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AquaCyan.copy(alpha = 0.4f)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = AquaCyan.copy(alpha = 0.05f))
                    ) {
                        Icon(Icons.Rounded.Send, contentDescription = null, tint = AquaCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDe) "E-Mail über Mail-App öffnen / senden" else "Open / Send via Mail App",
                            color = AquaCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (resetSuccess != null) {
                        Button(
                            onClick = {
                                viewModel.clearPasswordResetState()
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("reset_done_login_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary)
                        ) {
                            Text(if (isDe) "Fertig – Jetzt Einloggen" else "Done – Sign In Now", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}
