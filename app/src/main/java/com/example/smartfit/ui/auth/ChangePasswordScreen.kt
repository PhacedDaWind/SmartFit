package com.example.smartfit.ui.auth

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
import com.example.smartfit.ui.theme.ViewModelFactory

@Composable
fun ChangePasswordScreen(onSuccess: () -> Unit) {
    val app = LocalContext.current.applicationContext as SmartFitApplication
    val viewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(app.repository, app.userPreferencesRepository, app.userRepository)
    )

    var username by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState) {
        when(authState) {
            is AuthState.Success -> {
                snackbarHostState.showSnackbar("Password Updated!")
                onSuccess()
            }
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
            Text("Change Password", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.changePassword(username, currentPassword, newPassword) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Password")
            }
        }
    }
}