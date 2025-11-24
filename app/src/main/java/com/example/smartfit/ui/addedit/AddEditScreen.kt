package com.example.smartfit.ui.addedit

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
                title = { Text(if (uiState.isEditing) "Edit Log" else "New Entry", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isEditing) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.deleteLog()
                                    onNavigateUp()
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (viewModel.saveLog()) {
                        onNavigateUp()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Check, contentDescription = "Save")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LogEntryForm(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun LogEntryForm(
    uiState: AddEditUiState,
    viewModel: AddEditViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(24.dp)) {

        Spacer(modifier = Modifier.height(8.dp))

        // --- CATEGORY ---
        Text("What are you tracking?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectionCard("Workout", Icons.Default.FitnessCenter, uiState.category == "Workout", { viewModel.onCategoryChange("Workout") }, Modifier.weight(1f))
            SelectionCard("Food", Icons.Default.Restaurant, uiState.category != "Workout", { viewModel.onCategoryChange("Food & Drinks") }, Modifier.weight(1f))
        }

        // --- WORKOUT TYPE ---
        AnimatedVisibility(visible = uiState.category == "Workout") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Workout Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SelectionCard("Cardio", Icons.Default.DirectionsRun, uiState.workoutType == "Cardio", { viewModel.onWorkoutTypeChange("Cardio") }, Modifier.weight(1f))
                    SelectionCard("Strength", Icons.Default.MonitorWeight, uiState.workoutType == "Strength", { viewModel.onWorkoutTypeChange("Strength") }, Modifier.weight(1f))
                }
            }
        }

        HorizontalDivider()

        // --- DETAILS ---
        Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Name Input
        StyledTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = if (uiState.category == "Workout") "Exercise Name" else "Item Name",
            icon = Icons.Default.Edit,
            imeAction = ImeAction.Next
        )

        val (valueLabel, valueIcon) = when {
            uiState.category != "Workout" -> Pair("Calories (kcal)", Icons.Default.LocalFireDepartment)
            uiState.workoutType == "Strength" -> Pair("Weight (kg)", Icons.Default.FitnessCenter)
            else -> Pair("Duration (Minutes)", Icons.Default.Timer)
        }
        val placeholderText = if (uiState.workoutType == "Strength") "Leave empty for Bodyweight" else ""

        // Value Input
        StyledTextField(
            value = uiState.value,
            onValueChange = { viewModel.onValueChange(it) },
            label = valueLabel,
            icon = valueIcon,
            placeholder = placeholderText,
            isNumber = true
        )

        // --- STRENGTH VOLUME ---
        if (uiState.category == "Workout" && uiState.workoutType == "Strength") {
            Text("Volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sets
                StyledTextField(
                    value = uiState.sets,
                    onValueChange = { viewModel.onSetsChange(it) },
                    label = "Sets",
                    icon = null,
                    isNumber = true,
                    imeAction = ImeAction.Next,
                    modifier = Modifier.weight(1f)
                )

                Text("X", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)

                // Reps
                StyledTextField(
                    value = uiState.reps,
                    onValueChange = { viewModel.onRepsChange(it) },
                    label = "Reps",
                    icon = null,
                    isNumber = true,
                    imeAction = ImeAction.Done,
                    modifier = Modifier.weight(1f)
                )
            }

            if (uiState.showError) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Please input sets and reps.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        } else if (uiState.showError) {
            Text(
                text = "Please fill in all required fields.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun SelectionCard(text: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    Surface(modifier = modifier.height(80.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, borderColor, RoundedCornerShape(16.dp)).clickable { onClick() }, color = bgColor) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector?,
    // FIXED: Added default values here so you don't have to pass them every time
    placeholder: String = "",
    isNumber: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, placeholder = { if(placeholder.isNotEmpty()) Text(placeholder) },
        leadingIcon = if (icon != null) { { Icon(icon, null) } } else null,
        trailingIcon = if (value.isNotEmpty()) { { IconButton({ onValueChange("") }) { Icon(Icons.Default.Close, "Clear") } } } else null,
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text, imeAction = imeAction),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline)
    )
}