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

@Composable
fun LoginScreen(navController: NavController, viewModel: TicketViewModel) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val authMessage by viewModel.authMessage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.clearAuthMessage()
    }

    LaunchedEffect(currentUser?.id) {
        if (currentUser != null) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "E-Ticketing Helpdesk", style = MaterialTheme.typography.headlineLarge)
        Text(text = "UTS Frontend", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        // Auth Message (Success or Error) — typed, colour driven by AuthMessage.isError
        authMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            MessageBanner(text = message.text, isError = message.isError)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot Password Link
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(
                onClick = { navController.navigate(Screen.ResetPassword.route) },
                enabled = !isLoading
            ) {
                Text("Lupa Password?")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    return@Button
                }
                isLoading = true
                val loginSuccess = viewModel.login(username, password)
                isLoading = false
                if (loginSuccess) {
                    // Navigation handled by LaunchedEffect
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = username.isNotBlank() && password.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Login")
        }

        TextButton(
            onClick = { navController.navigate(Screen.Register.route) },
            enabled = !isLoading
        ) {
            Text("Belum punya akun? Daftar sekarang")
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Demo Credentials Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Demo Akun:", style = MaterialTheme.typography.labelMedium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                Spacer(modifier = Modifier.height(4.dp))
                Text("User: ahmad / Password: 123456", style = MaterialTheme.typography.bodySmall)
                Text("Helpdesk: helpdesk / Password: 123456", style = MaterialTheme.typography.bodySmall)
                Text("Admin: admin / Password: 123456", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
