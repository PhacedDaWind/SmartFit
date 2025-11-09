package com.example.smartfit.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.di.AppViewModelFactory

@Composable
fun ProfileScreen() {
    // Initialize the ViewModel using our new Factory
    val viewModel: ProfileViewModel = viewModel(factory = AppViewModelFactory)

    // Collect the "isDarkTheme" value as state.
    // The UI will recompose whenever this changes.
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { isChecked ->
                    // Call the ViewModel function to save the setting
                    viewModel.setDarkTheme(isChecked)
                }
            )
        }
    }
}