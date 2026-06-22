package com.example.e_ticketinghelpdeskuts.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_ticketinghelpdeskuts.ui.components.MessageBanner
import com.example.e_ticketinghelpdeskuts.ui.navigation.Screen
import com.example.e_ticketinghelpdeskuts.ui.screens.ticket.TicketViewModel
import com.example.e_ticketinghelpdeskuts.ui.utils.InputValidation

@Composable
fun RegisterScreen(navController: NavController, viewModel: TicketViewModel) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val authMessage by viewModel.authMessage.collectAsState()
    
    // Validation checks
    val nameError = if (name.isBlank()) "Nama lengkap harus diisi" else null
    val usernameError = if (username.isBlank()) "Username harus diisi" else if (username.length < 3) "Username minimal 3 karakter" else null
    val emailError = InputValidation.getEmailError(email)
    val passwordError = InputValidation.getPasswordError(password)
    val confirmPasswordError = if (confirmPassword.isBlank()) "Konfirmasi password harus diisi" else if (password != confirmPassword) "Password tidak cocok" else null
    
    val isFormValid = nameError == null && usernameError == null && emailError == null && passwordError == null && confirmPasswordError == null

    LaunchedEffect(Unit) {
        viewModel.clearAuthMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Buat Akun Baru", style = MaterialTheme.typography.headlineLarge)
        Text(text = "Daftar untuk menggunakan E-Ticketing Helpdesk", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(24.dp))
        
        // Full Name Field
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = { if (nameError != null) Text(nameError, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            isError = usernameError != null,
            supportingText = { if (usernameError != null) Text(usernameError, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = emailError != null,
            supportingText = { if (emailError != null) Text(emailError, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = passwordError != null,
            supportingText = { if (passwordError != null) Text(passwordError, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Konfirmasi Password") },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }, enabled = !isLoading) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = confirmPasswordError != null,
            supportingText = { if (confirmPasswordError != null) Text(confirmPasswordError, color = MaterialTheme.colorScheme.error) },
            singleLine = true,
            enabled = !isLoading
        )

        // Auth Message — typed, colour driven by AuthMessage.isError
        authMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            MessageBanner(text = message.text, isError = message.isError)
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                if (isFormValid) {
                    isLoading = true
                    if (viewModel.register(name, username, email, password)) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Daftar")
        }
        
        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isLoading
        ) {
            Text("Sudah punya akun? Login")
        }
    }
}
