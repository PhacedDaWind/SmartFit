package com.example.smartfit.ui.logs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.data.local.ActivityLog
import com.example.smartfit.data.local.DailySummary
import com.example.smartfit.ui.theme.ViewModelFactory
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivityLogScreen(
    onLogClick: (Int) -> Unit,
    onAddLogClick: () -> Unit,
) {
    // --- ViewModel Setup ---
    val application = LocalContext.current.applicationContext as SmartFitApplication
    val viewModel: ActivityLogViewModel = viewModel(
        factory = ViewModelFactory(
            application.repository,
            application.userPreferencesRepository,
            application.userRepository,
            application.stepSensorRepository
        )
    )

    // --- State Collection ---
    val logs by viewModel.allLogs.collectAsState()
    val summary by viewModel.dailySummary.collectAsState()
    val summaryUnit by viewModel.summaryUnit.collectAsState()

    // This is the filter state ("All", "Steps", "Food & Drinks", etc.)
    val selectedFilter by viewModel.filterType.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Logs") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLogClick,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new log")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- 1. FILTER TABS ---
            // We use ScrollableTabRow because "Food & Drinks" is long
            val filters = listOf("All", "Steps", "Workout", "Food & Drinks")

            ScrollableTabRow(
                selectedTabIndex = filters.indexOf(selectedFilter),
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = {}, // Optional: Remove indicator line for cleaner look if desired
                divider = {}
            ) {
                filters.forEach { title ->
                    Tab(
                        selected = selectedFilter == title,
                        onClick = { viewModel.updateFilter(title) },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedFilter == title) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // --- 2. SUMMARY HEADER (Daily Totals) ---
            SummaryHeader(
                summary = summary,
                selectedUnit = summaryUnit,
                onUnitChange = viewModel::updateSummaryUnit
            )

            // --- 3. LOG LIST ---
            Box(modifier = Modifier.weight(1f)) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No activities found.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LogList(
                        logs = logs,
                        onLogClick = onLogClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// --- COMPOSABLES ---

@Composable
fun SummaryHeader(
    summary: List<DailySummary>,
    selectedUnit: String,
    onUnitChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
        Text(
            text = "Daily Totals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Unit Toggle Buttons
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UnitToggleButton("steps", "Steps", selectedUnit, onUnitChange)
            UnitToggleButton("kcal", "Calories", selectedUnit, onUnitChange)
            UnitToggleButton("mins", "Minutes", selectedUnit, onUnitChange)
        }

        // Summary Cards Row
        if (summary.isEmpty()) {
            Text(
                text = "No data for '$selectedUnit' yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyRow(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(summary) { summaryItem ->
                    SummaryCard(summaryItem)
                }
            }
        }
    }
}

@Composable
fun UnitToggleButton(
    unitValue: String,
    label: String,
    selectedUnit: String,
    onUnitChange: (String) -> Unit
) {
    ElevatedButton(
        onClick = { onUnitChange(unitValue) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedUnit == unitValue) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selectedUnit == unitValue) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SummaryCard(summaryItem: DailySummary) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = summaryItem.day,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatValue(summaryItem.total),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LogList(
    logs: List<ActivityLog>,
    onLogClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(logs, key = { it.id }) { log ->
            LogItem(log = log, onClick = { onLogClick(log.id) })
        }
    }
}

@Composable
private fun LogItem(
    log: ActivityLog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = log.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = log.type, // e.g. "Food & Drinks"
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Display Logic: Show Sets if it's Strength, otherwise just Value + Unit
            val detailsText = if (log.type == "Strength" && log.sets > 0) {
                "${log.sets} Sets • ${formatValue(log.values)} ${log.unit}"
            } else {
                "${formatValue(log.values)} ${log.unit}"
            }

            Text(
                text = detailsText,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = formatDate(log.date),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --- HELPERS ---

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatValue(value: Double): String {
    val formatter = DecimalFormat("0.##")
    return formatter.format(value)
}