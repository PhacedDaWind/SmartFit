package com.example.smartfit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.ui.theme.ViewModelFactory
import java.text.DecimalFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onWorkoutTypeClick: (String) -> Unit
) {
    val application = LocalContext.current.applicationContext as SmartFitApplication
    val viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(
            application.repository,
            application.userPreferencesRepository,
            application.userRepository,
            application.stepSensorRepository
        )
    )

    val stats by viewModel.stats.collectAsState()
    val filter by viewModel.timeFilter.collectAsState()
    val dateLabel by viewModel.dateLabel.collectAsState()
    val scrollState = rememberScrollState()

    // --- DATE PICKER STATE ---
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Handle Date Selection
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // Get selected date from picker and pass to ViewModel
                    datePickerState.selectedDateMillis?.let {
                        viewModel.updateDate(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 1. TABLET WRAPPER
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        // 2. CONTENT COLUMN
        Column(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 600.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                        .padding(4.dp)
                ) {
                    FilterChip(
                        selected = filter == "Daily",
                        onClick = { viewModel.updateFilter("Daily") },
                        label = { Text("Daily") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                        border = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    FilterChip(
                        selected = filter == "Monthly",
                        onClick = { viewModel.updateFilter("Monthly") },
                        label = { Text("Monthly") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                        border = null
                    )
                }
            }

            // --- DATE NAVIGATOR (With Date Picker Trigger) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousPeriod() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous")
                    }

                    // CLICKABLE DATE TEXT
                    Row(
                        modifier = Modifier
                            .clickable { showDatePicker = true } // <--- Opens Picker
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { viewModel.nextPeriod() }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next")
                    }
                }
            }

            // --- STEPS CARD ---
            // (Same as before)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Steps", style = MaterialTheme.typography.labelLarge)
                    Text("${stats.steps}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                    val progress = (stats.steps.toFloat() / stats.stepGoal.toFloat()).coerceIn(0f, 1f)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(0.7f).height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                        strokeCap = StrokeCap.Round,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Goal: ${stats.stepGoal} • ${formatDecimal(stats.totalBurned)} kcal", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Goal Chips
                    Text("Set Goal:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val goals = listOf(1500, 2500, 4000)
                        goals.forEach { goal ->
                            FilterChip(
                                selected = (stats.stepGoal == goal),
                                onClick = { viewModel.updateStepGoal(goal) },
                                label = { Text("$goal") },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.tertiary, selectedLabelColor = MaterialTheme.colorScheme.onTertiary)
                            )
                        }
                    }
                }
            }

            // --- CALORIES ---
            Text("Calories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GaugeCard(
                    title = "Burned",
                    subtitle = "(Walking Only)",
                    current = stats.totalBurned,
                    visualMax = 500.0,
                    color = Color(0xFFFFA726),
                    modifier = Modifier.weight(1f)
                )
                GaugeCard(
                    title = "Intake",
                    subtitle = "(Food & Drink)",
                    current = stats.foodCalories,
                    visualMax = 2000.0,
                    color = Color(0xFF66BB6A),
                    modifier = Modifier.weight(1f)
                )
            }

            // --- WORKOUTS ---
            Text("Workouts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Cardio",
                    value = "${stats.cardioCount}",
                    unit = "Sessions",
                    icon = "🏃",
                    modifier = Modifier.weight(1f),
                    onClick = { onWorkoutTypeClick("Cardio") }
                )
                StatCard(
                    title = "Strength",
                    value = "${stats.strengthCount}",
                    unit = "Sessions",
                    icon = "💪",
                    modifier = Modifier.weight(1f),
                    onClick = { onWorkoutTypeClick("Strength") }
                )
            }
        }
    }
}

@Composable
fun GaugeCard(title: String, subtitle: String, current: Double, visualMax: Double, color: Color, modifier: Modifier = Modifier) {
    val progress = (current / visualMax).toFloat().coerceIn(0f, 1f)
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { 1f }, modifier = Modifier.size(80.dp), color = MaterialTheme.colorScheme.surfaceVariant, strokeWidth = 8.dp)
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(80.dp), color = color, strokeWidth = 8.dp, strokeCap = StrokeCap.Round)
                Text(text = formatDecimal(current), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(modifier = modifier.clickable { onClick() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("$title ($unit)", style = MaterialTheme.typography.labelMedium)
        }
    }
}

fun formatDecimal(value: Double): String {
    return DecimalFormat("#.##").format(value)
}