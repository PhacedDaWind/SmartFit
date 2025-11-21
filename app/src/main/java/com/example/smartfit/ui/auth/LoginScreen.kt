package com.example.smartfit.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.example.smartfit.ui.theme.ViewModelFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.SmartFitApplication


@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit
) {
    val app = LocalContext.current.applicationContext as SmartFitApplication
    val viewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(
            app.repository,
            app.userPreferencesRepository,
            app.userRepository,
            app.stepSensorRepository
        )
    )

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle State Changes
    LaunchedEffect(authState) {
        when(authState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
                snackbarHostState.showSnackbar((authState as AuthState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { p ->
        Column(
            modifier = Modifier.padding(p).fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SmartFit Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(24.dp))
            Button(onClick = { viewModel.login(username, password) }, modifier = Modifier.fillMaxWidth()) {
                Text("Login")
            }

            TextButton(onClick = onNavigateToRegister) { Text("Don't have an account? Register") }
            TextButton(onClick = onNavigateToForgot) { Text("Forgot/Change Password?") }
        }
    }
}