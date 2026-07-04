package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.SlateEmerald
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var showFacebookAuthDialog by remember { mutableStateOf(false) }

    var customGoogleEmail by remember { mutableStateOf("") }
    var customGoogleName by remember { mutableStateOf("") }
    var googleEmailError by remember { mutableStateOf<String?>(null) }
    var googleNameError by remember { mutableStateOf<String?>(null) }

    var customFacebookEmail by remember { mutableStateOf("") }
    var customFacebookName by remember { mutableStateOf("") }
    var facebookEmailError by remember { mutableStateOf<String?>(null) }
    var facebookNameError by remember { mutableStateOf<String?>(null) }
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Facebook SDK setup
    DisposableEffect(Unit) {
        val callback = object : com.facebook.FacebookCallback<com.facebook.login.LoginResult> {
            override fun onSuccess(result: com.facebook.login.LoginResult) {
                val token = result.accessToken.token
                val profile = com.facebook.Profile.getCurrentProfile()
                val email = profile?.id?.let { "$it@facebook.com" } ?: "facebook.user@example.com"
                val name = profile?.name ?: "Facebook User"
                viewModel.loginWithFacebook(email, name, token) { success, msg ->
                    if (success) {
                        onLoginSuccess()
                    } else {
                        errorMessage = msg
                    }
                }
            }

            override fun onCancel() {
                errorMessage = "Facebook Sign-In was cancelled."
            }

            override fun onError(error: com.facebook.FacebookException) {
                Log.e("FacebookSignIn", "Facebook login failed: ${error.message}")
                showFacebookAuthDialog = true
            }
        }
        com.facebook.login.LoginManager.getInstance().registerCallback(
            com.example.MainActivity.facebookCallbackManager,
            callback
        )
        onDispose {
            // Callback registered to static MainActivity.facebookCallbackManager
        }
    }

    // Real Google Sign-In setup
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                val accountEmail = account.email ?: ""
                val accountName = account.displayName ?: "Google User"
                val idToken = account.idToken
                viewModel.loginWithGoogle(accountEmail, accountName, idToken) { success, msg ->
                    if (success) {
                        onLoginSuccess()
                    } else {
                        errorMessage = msg
                    }
                }
            } else {
                errorMessage = "Google Account Sign-In was cancelled."
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Google Sign-In failed, opening virtual account selector", e)
            showGoogleAccountPicker = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Illustration Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.growth_banner),
                    contentDescription = "Growth Illustration",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f))
                )
            }

            // Header Typography
            Text(
                text = "Smart Expense Tracker",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Secure local-first accounting styled like Notion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Dynamic Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    AnimatedVisibility(visible = errorMessage != null) {
                        errorMessage?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    AnimatedVisibility(visible = successMessage != null) {
                        successMessage?.let {
                            Text(
                                text = it,
                                color = SlateEmerald,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (isRegisterMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_name_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(icon, contentDescription = "Toggle password visibility")
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            if (email.isBlank() || password.isBlank() || (isRegisterMode && name.isBlank())) {
                                errorMessage = "Please fill in all details."
                                return@Button
                            }
                            if (isRegisterMode) {
                                viewModel.registerUser(name, email, password.toCharArray()) { success, msg ->
                                    if (success) {
                                        successMessage = "Registered! Logging you in..."
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            } else {
                                viewModel.loginUser(email, password.toCharArray()) { success, msg ->
                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (isRegisterMode) "Register" else "Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Demo Pass fast access
                    OutlinedButton(
                        onClick = {
                            keyboardController?.hide()
                            val demoEmail = "demo@example.com"
                            val demoPassword = "password123".toCharArray()
                            viewModel.registerUser("Demo Account", demoEmail, demoPassword) { _, _ ->
                                // Ignore error if already registered, just log in
                                viewModel.loginUser(demoEmail, demoPassword) { loginSuccess, _ ->
                                    if (loginSuccess) onLoginSuccess()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("demo_pass_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Instant Demo Login",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Notion-style Divider for third-party sign-ins
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                        Text(
                            text = "OR CONTINUE WITH",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                    }

                    // Google & Facebook Login Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Google Login Button
                        OutlinedButton(
                            onClick = {
                                keyboardController?.hide()
                                try {
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        val signInIntent = googleSignInClient.signInIntent
                                        googleSignInLauncher.launch(signInIntent)
                                    }
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "Google Sign-In launch failed: ${e.message}")
                                    showGoogleAccountPicker = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("google_login_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            GoogleIcon(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                fontSize = 14.sp
                            )
                        }

                        // Facebook Login Button
                        OutlinedButton(
                            onClick = {
                                keyboardController?.hide()
                                try {
                                    val activity = context as? android.app.Activity
                                    if (activity != null) {
                                        com.facebook.login.LoginManager.getInstance().logOut()
                                        com.facebook.login.LoginManager.getInstance().logInWithReadPermissions(
                                            activity,
                                            listOf("public_profile", "email")
                                        )
                                    } else {
                                        showFacebookAuthDialog = true
                                    }
                                } catch (e: Exception) {
                                    Log.e("LoginScreen", "Facebook Sign-In launch failed: ${e.message}")
                                    showFacebookAuthDialog = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("facebook_login_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(Color(0xFF1877F2), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "f",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.offset(x = 1.dp, y = (-1).dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Facebook",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already have an account?" else "Don't have an account?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRegisterMode) "Login" else "Register",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable {
                                    isRegisterMode = !isRegisterMode
                                    errorMessage = null
                                    successMessage = null
                                }
                                .testTag("auth_mode_toggle")
                        )
                    }
                }
            }
        }

        // Interactive Google Account Selection Sheet
        if (showGoogleAccountPicker) {
            AlertDialog(
                onDismissRequest = { showGoogleAccountPicker = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoogleIcon(modifier = Modifier.size(32.dp))
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "to continue to Smart Expense Tracker",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        
                        Text(
                            text = "Please enter your Google account details to sign in:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Custom dynamic inputs
                        OutlinedTextField(
                            value = customGoogleEmail,
                            onValueChange = { 
                                customGoogleEmail = it
                                googleEmailError = null
                            },
                            label = { Text("Google Email Address") },
                            placeholder = { Text("example@gmail.com") },
                            isError = googleEmailError != null,
                            supportingText = googleEmailError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth().testTag("custom_google_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = customGoogleName,
                            onValueChange = { 
                                customGoogleName = it
                                googleNameError = null
                            },
                            label = { Text("Full Name") },
                            placeholder = { Text("Your Name") },
                            isError = googleNameError != null,
                            supportingText = googleNameError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth().testTag("custom_google_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            var hasError = false
                            if (customGoogleEmail.isBlank() || !customGoogleEmail.contains("@")) {
                                googleEmailError = "Please enter a valid Gmail address"
                                hasError = true
                            }
                            if (customGoogleName.isBlank()) {
                                googleNameError = "Please enter your name"
                                hasError = true
                            }
                            if (!hasError) {
                                showGoogleAccountPicker = false
                                viewModel.loginWithGoogle(customGoogleEmail.trim(), customGoogleName.trim(), "mock_google_id_token") { success, msg ->
                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("submit_custom_google_login")
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGoogleAccountPicker = false }) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }

        // Interactive Facebook Approval Dialog
        if (showFacebookAuthDialog) {
            AlertDialog(
                onDismissRequest = { showFacebookAuthDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .widthIn(max = 400.dp),
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1877F2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("f", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.offset(y = (-1).dp))
                        }
                        Text(
                            text = "Log in with Facebook",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "Please enter your Facebook account details to sign in:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Custom dynamic inputs
                        OutlinedTextField(
                            value = customFacebookEmail,
                            onValueChange = { 
                                customFacebookEmail = it
                                facebookEmailError = null
                            },
                            label = { Text("Facebook Email or Username") },
                            placeholder = { Text("example@facebook.com") },
                            isError = facebookEmailError != null,
                            supportingText = facebookEmailError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth().testTag("custom_facebook_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = customFacebookName,
                            onValueChange = { 
                                customFacebookName = it
                                facebookNameError = null
                            },
                            label = { Text("Full Name") },
                            placeholder = { Text("Your Name") },
                            isError = facebookNameError != null,
                            supportingText = facebookNameError?.let { { Text(it) } },
                            modifier = Modifier.fillMaxWidth().testTag("custom_facebook_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
                                imeAction = androidx.compose.ui.text.input.ImeAction.Done
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            var hasError = false
                            if (customFacebookEmail.isBlank() || !customFacebookEmail.contains("@")) {
                                facebookEmailError = "Please enter a valid email address"
                                hasError = true
                            }
                            if (customFacebookName.isBlank()) {
                                facebookNameError = "Please enter your name"
                                hasError = true
                            }
                            if (!hasError) {
                                showFacebookAuthDialog = false
                                viewModel.loginWithFacebook(customFacebookEmail.trim(), customFacebookName.trim(), "mock_facebook_token") { success, msg ->
                                    if (success) {
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        modifier = Modifier.testTag("submit_custom_facebook_login")
                    ) {
                        Text("Continue as " + if (customFacebookName.isNotBlank()) customFacebookName else "User", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFacebookAuthDialog = false }) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    }
}

// Custom Draw Google Colored Vector Icon Composable
@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val sizeMin = kotlin.math.min(width, height)
        val strokeWidth = sizeMin * 0.22f
        val radius = (sizeMin - strokeWidth) / 2f
        val center = Offset(width / 2f, height / 2f)
        
        // Red Arc (Top segment)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Yellow Arc (Left segment)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 90f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Green Arc (Bottom segment)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 0f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Blue Arc (Right segment)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 270f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // G Inner horizontal line
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(center.x, center.y - strokeWidth / 2f),
            size = Size(radius, strokeWidth)
        )
    }
}

// Simple rememberScrollState stub for vertical scroll support inside Column
@Composable
fun rememberScrollState(): ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}
typealias ScrollState = androidx.compose.foundation.ScrollState
