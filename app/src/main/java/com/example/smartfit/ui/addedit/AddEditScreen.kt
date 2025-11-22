package com.example.smartfit.ui.addedit

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
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
    onNavigateUp: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as SmartFitApplication
    val viewModel: AddEditViewModel = viewModel(
        factory = ViewModelFactory(
            application.repository,
            application.userPreferencesRepository,
            application.userRepository,
            application.stepSensorRepository
        )
    )

    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditing) "Edit Activity" else "Add Activity") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.saveLog()
                                onNavigateUp()
                            }
                        },
                        enabled = uiState.isEntryValid
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.deleteLog()
                                    onNavigateUp()
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LogEntryForm(
            uiState = uiState,
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()
        )
    }
}

@Composable
private fun LogEntryForm(
    uiState: AddEditUiState,
    viewModel: AddEditViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 1. Category Selection
        Text("Category", style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth()) {
            RadioButtonGroup(
                options = listOf("Workout", "Food & Drinks"),
                selectedOption = uiState.category,
                onOptionSelected = { viewModel.onCategoryChange(it) }
            )
        }

        // 2. Workout Type Selection
        if (uiState.category == "Workout") {
            Text("Workout Type", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth()) {
                RadioButtonGroup(
                    options = listOf("Cardio", "Strength"),
                    selectedOption = uiState.workoutType,
                    onOptionSelected = { viewModel.onWorkoutTypeChange(it) }
                )
            }
        }

        Divider()

        // 3. Name Input
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = { Text(if (uiState.category == "Food & Drinks") "Item Name" else "Exercise Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // 4. Value Input (Calories or Duration)
        val valueLabel = if (uiState.category == "Food & Drinks") "Calories (kcal)" else "Duration (Minutes)"
        OutlinedTextField(
            value = uiState.value,
            onValueChange = { viewModel.onValueChange(it) },
            label = { Text(valueLabel) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        // 5. Sets Input (Strength Only)
        if (uiState.category == "Workout" && uiState.workoutType == "Strength") {
            OutlinedTextField(
                value = uiState.sets,
                onValueChange = { viewModel.onSetsChange(it) },
                label = { Text("Number of Sets") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

@Composable
fun RadioButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        options.forEach { text ->
            Row(
                Modifier
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}