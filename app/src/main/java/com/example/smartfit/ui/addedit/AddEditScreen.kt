package com.example.smartfit.ui.addedit

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                    coroutineScope.launch {
                        viewModel.saveLog()
                        onNavigateUp()
                    }
                },
                containerColor = if (uiState.isEntryValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (uiState.isEntryValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    .verticalScroll(rememberScrollState()) // Added scrolling
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

        // --- 1. CATEGORY SELECTION (Better than Radio Buttons) ---
        Text("What are you tracking?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectionCard(
                text = "Workout",
                icon = Icons.Default.FitnessCenter,
                isSelected = uiState.category == "Workout",
                onClick = { viewModel.onCategoryChange("Workout") },
                modifier = Modifier.weight(1f)
            )
            SelectionCard(
                text = "Food",
                icon = Icons.Default.Restaurant,
                isSelected = uiState.category != "Workout",
                onClick = { viewModel.onCategoryChange("Food & Drinks") },
                modifier = Modifier.weight(1f)
            )
        }

        // --- 2. WORKOUT TYPE (Animated Visibility) ---
        AnimatedVisibility(visible = uiState.category == "Workout") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Workout Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SelectionCard(
                        text = "Cardio",
                        icon = Icons.Default.DirectionsRun,
                        isSelected = uiState.workoutType == "Cardio",
                        onClick = { viewModel.onWorkoutTypeChange("Cardio") },
                        modifier = Modifier.weight(1f)
                    )
                    SelectionCard(
                        text = "Strength",
                        icon = Icons.Default.MonitorWeight, // Changed icon
                        isSelected = uiState.workoutType == "Strength",
                        onClick = { viewModel.onWorkoutTypeChange("Strength") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        HorizontalDivider()

        // --- 3. DETAILS INPUTS ---
        Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Name Input
        StyledTextField(
            value = uiState.name,
            onValueChange = { viewModel.onNameChange(it) },
            label = if (uiState.category == "Workout") "Exercise Name (e.g. Bench Press)" else "Item Name (e.g. Banana)",
            icon = Icons.Default.Edit,
            imeAction = ImeAction.Next
        )

        // Value Input (Dynamic Label & Icon)
        val (valueLabel, valueIcon) = when {
            uiState.category != "Workout" -> Pair("Calories (kcal)", Icons.Default.LocalFireDepartment)
            uiState.workoutType == "Strength" -> Pair("Weight (kg)", Icons.Default.FitnessCenter)
            else -> Pair("Duration (Minutes)", Icons.Default.Timer)
        }
        val placeholderText = if (uiState.workoutType == "Strength") "Leave empty for Bodyweight" else ""

        StyledTextField(
            value = uiState.value,
            onValueChange = { viewModel.onValueChange(it) },
            label = valueLabel,
            icon = valueIcon,
            placeholder = placeholderText,
            isNumber = true
        )

        // --- 4. STRENGTH SPECIFIC (Sets x Reps) ---
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
                    icon = null, // No icon to save space
                    isNumber = true,
                    modifier = Modifier.weight(1f)
                )

                // Visual "X"
                Text("X", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.outline)

                // Reps
                StyledTextField(
                    value = uiState.reps,
                    onValueChange = { viewModel.onRepsChange(it) },
                    label = "Reps",
                    icon = null,
                    isNumber = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp)) // Extra space at bottom for FAB
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun SelectionCard(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Surface(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = bgColor
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if(isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if(isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector?,
    placeholder: String = "",
    isNumber: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if(placeholder.isNotEmpty()) Text(placeholder) },
        leadingIcon = if (icon != null) { { Icon(icon, contentDescription = null) } } else null,
        trailingIcon = if (value.isNotEmpty()) {
            { IconButton(onClick = { onValueChange("") }) { Icon(Icons.Default.Close, "Clear") } }
        } else null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isNumber) KeyboardType.Number else KeyboardType.Text,
            imeAction = imeAction
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
}