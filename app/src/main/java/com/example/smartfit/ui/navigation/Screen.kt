// In com/example/smartfit/ui/navigation/Screen.kt
package com.example.smartfit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

// Defines the routes, labels, and icons for your bottom nav bar
sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object ActivityLog : Screen("activity_log", "Log", Icons.Default.Favorite)
    object Tips : Screen("tips", "Tips", Icons.Default.Settings) // Example icon
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}