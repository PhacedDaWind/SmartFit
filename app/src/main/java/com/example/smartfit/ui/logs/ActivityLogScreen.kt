package com.example.smartfit.ui.logs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
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
    val application = LocalContext.current.applicationContext as SmartFitApplication
    val viewModel: ActivityLogViewModel = viewModel(
        factory = ViewModelFactory(
            application.repository,
            application.userPreferencesRepository,
            application.userRepository,
            application.stepSensorRepository
        )
    )

    val logs by viewModel.allLogs.collectAsState()
    val selectedFilter by viewModel.filterType.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Logs") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                windowInsets = WindowInsets(0.dp)
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
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // --- FILTER TABS ---
            val filters = listOf("All", "Workout", "Food & Drinks")

            TabRow(
                selectedTabIndex = filters.indexOf(selectedFilter),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                filters.forEach { title ->
                    Tab(
                        selected = selectedFilter == title,
                        onClick = { viewModel.updateFilter(title) },
                        text = { Text(title, fontWeight = if (selectedFilter == title) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // --- LOG LIST ---
            // FIXED: Added .fillMaxWidth() here so the box stretches across the screen
            // allowing Alignment.Center to work horizontally
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (logs.isEmpty()) {
                    Text(
                        text = "No logs found.",
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
        modifier = modifier.fillMaxWidth().clickable { onClick() },
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
                Text(text = log.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = log.type, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            val detailsText = if (log.type == "Strength" && log.sets > 0) {
                "${log.sets} Sets • ${formatValue(log.values)} ${log.unit}"
            } else {
                "${formatValue(log.values)} ${log.unit}"
            }

            Text(text = detailsText, style = MaterialTheme.typography.bodyLarge)
            Text(text = formatDate(log.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatValue(value: Double): String {
    val formatter = DecimalFormat("0.##")
    return formatter.format(value)
}