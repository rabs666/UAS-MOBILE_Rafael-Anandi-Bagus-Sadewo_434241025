package com.example.e_ticketinghelpdeskuts.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.e_ticketinghelpdeskuts.ui.components.MessageBanner
import com.example.e_ticketinghelpdeskuts.ui.screens.ticket.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(navController: NavController, viewModel: TicketViewModel) {
    var email by remember { mutableStateOf("") }
    val authMessage by viewModel.authMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Lupa Password?",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Masukkan email Anda untuk menerima instruksi reset password.",
                style = MaterialTheme.typography.bodyMedium
            )
            
            authMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                MessageBanner(text = message.text, isError = message.isError)
            }

            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    if (viewModel.resetPassword(email)) {
                        // Keep on screen to show success message or navigate back
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank()
            ) {
                Text("Kirim Instruksi")
            }
        }
    }
}
