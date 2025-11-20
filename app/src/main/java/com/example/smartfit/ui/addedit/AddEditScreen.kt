package com.example.smartfit.ui.addedit

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.ui.theme.ViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddEditScreen(
    onNavigateUp: () -> Unit, // Navigation: Called to go back
) {
    // --- ViewModel Setup ---
    val application = LocalContext.current.applicationContext as SmartFitApplication

    val viewModel: AddEditViewModel = viewModel(
        factory = ViewModelFactory(
            application.repository,                // 1. Activity Repository
            application.userPreferencesRepository, // 2. User Preferences (The one you were missing)
            application.userRepository             // 3. User Auth Repository
        )
    )

    // --- State Collection ---
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                // Title changes based on whether we are adding or editing
                title = { Text(if (uiState.isEditing) "Edit Log" else "Add Log") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // --- Save Button ---
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.saveLog()
                                onNavigateUp() // Go back after saving
                            }
                        },
                        enabled = uiState.isEntryValid
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save Log"
                        )
                    }

                    // --- Delete Button (only show if editing) ---
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.deleteLog()
                                    onNavigateUp() // Go back after deleting
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete Log"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // --- UI Body (The Form) ---
        LogEntryForm(
            uiState = uiState,
            onTypeChange = viewModel::onTypeChange,
            onNameChange = viewModel::onNameChange,
            onValueChange = viewModel::onValueChange,
            onUnitChange = viewModel::onUnitChange,
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        )
    }
}

@Composable
private fun LogEntryForm(
    uiState: AddEditUiState,
    onTypeChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Activity Type Field ---
        OutlinedTextField(
            value = uiState.type,
            onValueChange = onTypeChange,
            label = { Text("Activity Type (e.g., Workout, Food)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = !uiState.isEntryValid && uiState.type.isBlank()
        )

        // --- Activity Name Field ---
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("Name (e.g., Morning Run, Apple)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = !uiState.isEntryValid && uiState.name.isBlank()
        )

        // --- Activity Value Field ---
        OutlinedTextField(
            value = uiState.value,
            onValueChange = onValueChange,
            label = { Text("Value (e.g., 30, 500, 150)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = !uiState.isEntryValid && (uiState.value.isBlank() || uiState.value.toDoubleOrNull() == null)
        )

        // --- Activity Unit Field ---
        OutlinedTextField(
            value = uiState.unit,
            onValueChange = onUnitChange,
            label = { Text("Unit (e.g., minutes, kcal, steps)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}