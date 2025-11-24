package com.example.smartfit.ui.logs

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    initialFilter: String? = null
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

    LaunchedEffect(initialFilter) {
        if (initialFilter != null) {
            viewModel.updateFilter(initialFilter)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity History", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLogClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new log")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                // --- UPDATED FILTERS ---
                // MUST MATCH ViewModel exactly
                val filters = listOf("All", "Cardio", "Strength", "Food & Drinks")

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    // ScrollableTabRow handles longer text like "Food & Drinks" better
                    ScrollableTabRow(
                        selectedTabIndex = filters.indexOf(selectedFilter).coerceAtLeast(0),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 16.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[filters.indexOf(selectedFilter).coerceAtLeast(0)]),
                                height = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        filters.forEach { title ->
                            Tab(
                                selected = selectedFilter == title,
                                onClick = { viewModel.updateFilter(title) },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedFilter == title) FontWeight.Bold else FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            )
                        }
                    }
                }

                // --- LOG LIST ---
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (logs.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No logs found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
}

// ... (Keep LogList, LogItem, formatDate, formatValue exactly as they were) ...
@Composable
private fun LogList(logs: List<ActivityLog>, onLogClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(logs, key = { it.id }) { log -> LogItem(log, { onLogClick(log.id) }) }
    }
}

@Composable
private fun LogItem(log: ActivityLog, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (icon, color) = when (log.type) {
        "Cardio" -> Pair(Icons.Default.DirectionsRun, Color(0xFF2196F3))
        "Strength" -> Pair(Icons.Default.FitnessCenter, Color(0xFF9C27B0))
        "Food & Drinks" -> Pair(Icons.Default.Restaurant, Color(0xFF4CAF50))
        else -> Pair(Icons.Default.History, MaterialTheme.colorScheme.secondary)
    }
    Card(modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick() }, elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) { Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = color) } }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(4.dp))
                val detailsText = if (log.type == "Strength") { val weightDisplay = if (log.values == 0.0) "Bodyweight" else "${formatValue(log.values)} kg"; "${log.sets} Sets • ${log.reps} Reps • $weightDisplay" } else { "${formatValue(log.values)} ${log.unit}" }
                Text(text = detailsText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = formatDate(log.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "View Details", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}
private fun formatDate(timestamp: Long): String = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(timestamp))
private fun formatValue(value: Double): String = DecimalFormat("0.##").format(value)