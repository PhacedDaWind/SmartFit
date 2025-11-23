package com.example.smartfit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.ui.theme.ViewModelFactory
import java.text.DecimalFormat

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
    val username by viewModel.username.collectAsState()
    val scrollState = rememberScrollState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var showGoalDialog by remember { mutableStateOf(false) }
    var customGoalInput by remember { mutableStateOf("") }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateDate(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showGoalDialog) {
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Custom Goal") },
            text = {
                OutlinedTextField(
                    value = customGoalInput,
                    onValueChange = { customGoalInput = it },
                    label = { Text("Enter Steps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val goal = customGoalInput.toIntOrNull()
                    if (goal != null && goal > 0) {
                        viewModel.updateStepGoal(goal)
                    }
                    showGoalDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") } }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 600.dp).verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Hello, $username", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                    Text(text = "Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                }
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.height(40.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                        TimeFilterChip(text = "Daily", isSelected = filter == "Daily") { viewModel.updateFilter("Daily") }
                        Spacer(modifier = Modifier.width(4.dp))
                        TimeFilterChip(text = "Monthly", isSelected = filter == "Monthly") { viewModel.updateFilter("Monthly") }
                    }
                }
            }

            // Date Navigator
            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.previousPeriod() }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = MaterialTheme.colorScheme.primary) }
                    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { showDatePicker = true }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { viewModel.nextPeriod() }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary) }
                }
            }

            // Hero Steps Card
            StepsHeroCard(
                stats = stats,
                viewModel = viewModel,
                onCustomGoalClick = {
                    customGoalInput = "" // Clear input on open
                    showGoalDialog = true
                }
            )

            // Calories
            Text("Calories Today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GaugeCard("Burned", "Active Energy", stats.totalBurned, 600.0, Color(0xFFFF9800), Modifier.weight(1f))
                GaugeCard("Intake", "Food & Drink", stats.foodCalories, 2500.0, Color(0xFF4CAF50), Modifier.weight(1f))
            }

            // Activity Breakdown
            Text("Activity Breakdown", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ActivityStatCard("Cardio", stats.cardioCount.toString(), "Sessions", Icons.Default.DirectionsRun, Color(0xFF2196F3), { onWorkoutTypeClick("Cardio") }, Modifier.weight(1f))
                ActivityStatCard("Strength", stats.strengthCount.toString(), "Sessions", Icons.Default.FitnessCenter, Color(0xFF9C27B0), { onWorkoutTypeClick("Strength") }, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StepsHeroCard(stats: HomeStats, viewModel: HomeViewModel, onCustomGoalClick: () -> Unit) {
    // Safe calculation to prevent division by zero
    val progress = if (stats.stepGoal > 0) {
        (stats.steps.toFloat() / stats.stepGoal.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(0.dp)) {
        Box(modifier = Modifier.background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))).padding(24.dp)) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsRun, null, tint = Color.White.copy(0.8f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Step Count", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(0.8f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("${stats.steps}", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 64.sp, lineHeight = 64.sp)

                val goalText = if (stats.stepGoal > 0) "/ ${stats.stepGoal} steps" else "/ No Goal Set"
                Text(goalText, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(0.7f))

                Spacer(modifier = Modifier.height(24.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(50)), color = Color.White, trackColor = Color.White.copy(0.3f))
                Spacer(modifier = Modifier.height(24.dp))

                Text("Quick Goal Change:", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.8f), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    listOf(2500, 5000, 8000).forEach { goal ->
                        val isSelected = stats.stepGoal == goal
                        val bg = if (isSelected) Color.White else Color.White.copy(0.2f)
                        val txt = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                        Box(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(bg).clickable { viewModel.updateStepGoal(goal) }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text("$goal", style = MaterialTheme.typography.labelMedium, color = txt, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(modifier = Modifier.clip(CircleShape).background(Color.White.copy(0.2f)).clickable { onCustomGoalClick() }.padding(8.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Custom", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ... (Keep TimeFilterChip, GaugeCard, ActivityStatCard, formatDecimal exactly as they are) ...
@Composable
fun TimeFilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(bgColor).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = textColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GaugeCard(title: String, subtitle: String, current: Double, visualMax: Double, color: Color, modifier: Modifier = Modifier) {
    val progress = (current / visualMax).toFloat().coerceIn(0f, 1f)
    Card(modifier = modifier, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = color.copy(0.2f), strokeWidth = 10.dp, strokeCap = StrokeCap.Round)
                CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(), color = color, strokeWidth = 10.dp, strokeCap = StrokeCap.Round)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatDecimal(current), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ActivityStatCard(title: String, value: String, unit: String, icon: ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.clickable { onClick() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = color.copy(0.1f), modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("$title ($unit)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, lineHeight = 12.sp)
            }
        }
    }
}

fun formatDecimal(value: Double): String = DecimalFormat("#").format(value)