package com.example.smartfit.ui.logs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.data.local.ActivityLog
import com.example.smartfit.ui.theme.ViewModelFactory
// Import your application class
import com.example.smartfit.SmartFitApplication
import androidx.compose.ui.platform.LocalContext
import java.text.DecimalFormat

// Simple date formatter utility (you can replace this with a better one)
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ActivityLogScreen(
    onLogClick: (Int) -> Unit, // Navigation: Called when a log is clicked
    onAddLogClick: () -> Unit, // Navigation: Called when FAB is clicked
) {
    // --- ViewModel Setup ---
    // Get the application context to find the repository
    val application = LocalContext.current.applicationContext as SmartFitApplication
    // Create the ViewModel using the factory
    val viewModel: ActivityLogViewModel = viewModel(
        factory = ViewModelFactory(application.repository,
            application.userPreferencesRepository,
            application.userRepository)
    )

    // --- State Collection ---
    // Collect the list of logs from the ViewModel as state
    val logs by viewModel.allLogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity Logs") }, // You can use stringResource(R.string....)
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddLogClick, // Navigate to Add screen
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new log") // Req 1e: Accessibility
            }
        }
    ) { paddingValues ->
        // --- UI Body ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (logs.isEmpty()) {
                // Show a message if the list is empty
                Text(
                    text = "No activities logged yet. Tap the '+' button to add one.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // Show the list of logs
                LogList(
                    logs = logs,
                    onLogClick = onLogClick,
                    modifier = Modifier.fillMaxSize()
                )
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(logs, key = { it.id }) { log ->
            LogItem(
                log = log,
                onClick = { onLogClick(log.id) }
            )
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
            .clickable { onClick() }, // Make the whole card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = log.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Type: ${log.type}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Amount: ${formatValue(log.values)} ${log.unit}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatDate(log.date), // Use a helper to format the date
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Simple helper function to format the timestamp
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatValue(value: Double): String {
    val formatter = DecimalFormat("0.##")
    return formatter.format(value)
}