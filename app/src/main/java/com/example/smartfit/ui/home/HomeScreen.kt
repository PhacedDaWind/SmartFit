package com.example.smartfit.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@Composable
fun HomeScreen() {
    val application = LocalContext.current.applicationContext as SmartFitApplication
    val viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory(
            application.repository,
            application.userPreferencesRepository,
            application.userRepository,
            application.stepSensorRepository
        )
    )

    // Observe stats (which now includes the user's saved stepGoal)
    val stats by viewModel.stats.collectAsState()
    val filter by viewModel.timeFilter.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // --- HEADER (Title + Time Filters) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                FilterChip(
                    selected = filter == "Daily",
                    onClick = { viewModel.updateFilter("Daily") },
                    label = { Text("Daily") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                FilterChip(
                    selected = filter == "Monthly",
                    onClick = { viewModel.updateFilter("Monthly") },
                    label = { Text("Monthly") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    border = null
                )
            }
        }

        // --- 1. STEPS CARD (With Goal & Progress) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Steps", style = MaterialTheme.typography.labelLarge)

                // Current Step Count
                Text(
                    text = "${stats.steps}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )

                // Progress Bar
                // Calculate progress (0.0 to 1.0) based on current goal
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

                // Goal Text
                Text(
                    text = "Goal: ${stats.stepGoal} • ${formatDecimal(stats.totalBurned)} kcal",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- GOAL SELECTOR CHIPS ---
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
                            onClick = { viewModel.updateStepGoal(goal) }, // Saves to DB
                            label = { Text("$goal") },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                            )
                        )
                    }
                }
            }
        }

        // --- 2. CALORIE GAUGES ---
        Text("Calories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Burned (Steps Only)
            GaugeCard(
                title = "Burned",
                subtitle = "(Walking Only)",
                current = stats.totalBurned,
                visualMax = 500.0,
                color = Color(0xFFFFA726), // Orange
                modifier = Modifier.weight(1f)
            )

            // Intake (Food)
            GaugeCard(
                title = "Intake",
                subtitle = "(Food & Drink)",
                current = stats.foodCalories,
                visualMax = 2000.0,
                color = Color(0xFF66BB6A), // Green
                modifier = Modifier.weight(1f)
            )
        }

        // --- 3. WORKOUT STATS ---
        Text("Workouts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = "Cardio",
                value = formatDecimal(stats.cardioMins),
                unit = "Mins",
                icon = "🏃",
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Strength",
                value = "${stats.strengthSets}",
                unit = "Sets",
                icon = "💪",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun GaugeCard(
    title: String,
    subtitle: String,
    current: Double,
    visualMax: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = (current / visualMax).toFloat().coerceIn(0f, 1f)

    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center) {
                // Background Circle
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(80.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 8.dp
                )
                // Foreground Progress Circle
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(80.dp),
                    color = color,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = formatDecimal(current),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, unit: String, icon: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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