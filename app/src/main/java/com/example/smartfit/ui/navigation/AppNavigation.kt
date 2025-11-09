package com.example.smartfit.ui.navigation

import HomeScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.smartfit.ui.profile.ProfileScreen

import com.example.smartfit.ui.log.LogScreen
import com.example.smartfit.ui.tips.TipsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // List of your main screens for the bottom bar
    val items = listOf(
        Screen.Home,
        Screen.ActivityLog,
        Screen.Tips,
        Screen.Profile,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = "${screen.label} Icon") }
                    )
                }
            }
        }
    ) { innerPadding ->
        // This NavHost is the main content area that swaps screens
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Your Home Screen
            composable(Screen.Home.route) { HomeScreen() }

            // Your Teammate's Log Screen
            composable(Screen.ActivityLog.route) { LogScreen() }

            // Your Tips Screen
            composable(Screen.Tips.route) { TipsScreen() }

            // Your Profile Screen
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}