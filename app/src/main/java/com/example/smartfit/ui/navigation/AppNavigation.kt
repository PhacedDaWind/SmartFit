package com.example.smartfit.ui.navigation

import HomeScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

// --- IMPORTS FOR YOUR SCREENS ---
import com.example.smartfit.ui.logs.ActivityLogScreen
import com.example.smartfit.ui.addedit.AddEditScreen
import com.example.smartfit.ui.profile.ProfileScreen
import com.example.smartfit.ui.tips.TipsScreen
import com.example.smartfit.ui.auth.LoginScreen
import com.example.smartfit.ui.auth.RegisterScreen
import com.example.smartfit.ui.auth.ChangePasswordScreen

// --- ROUTE DEFINITIONS ---

// 1. Auth Routes (Login flows)
object AuthRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
}

// 2. Main App Screens (Bottom Navigation)
// *** REMOVED: sealed class Screen *** // (It is now correctly read from your Screen.kt file)

// 3. Other App Routes (Hidden from bottom bar)
object OtherRoutes {
    const val ADD_EDIT = "add_edit_log"
    const val LOG_ID_ARG = "logId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // List of items to show in the Bottom Navigation Bar
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.ActivityLog,
        Screen.Tips,
        Screen.Profile,
    )

    // Determine if we should show the bottom bar based on the current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Show bottom bar ONLY if we are on one of the main screens
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
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
                            icon = { Icon(screen.icon, contentDescription = screen.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AuthRoutes.LOGIN, // <--- APP STARTS HERE
            modifier = Modifier.padding(innerPadding)
        ) {

            // ===========================
            //      AUTH GRAPH
            // ===========================

            composable(AuthRoutes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        // Navigate to Home and clear the Login screen from history
                        navController.navigate(Screen.Home.route) {
                            popUpTo(AuthRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(AuthRoutes.REGISTER)
                    },
                    onNavigateToForgot = {
                        navController.navigate(AuthRoutes.FORGOT_PASSWORD)
                    }
                )
            }

            composable(AuthRoutes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.popBackStack() // Go back to Login after registering
                    }
                )
            }

            composable(AuthRoutes.FORGOT_PASSWORD) {
                ChangePasswordScreen(
                    onSuccess = {
                        navController.popBackStack() // Go back to Login after changing password
                    }
                )
            }

            // ===========================
            //      MAIN APP GRAPH
            // ===========================

            composable(Screen.Home.route) { HomeScreen() }

            // --- Activity Log Screen ---
            composable(Screen.ActivityLog.route) {
                ActivityLogScreen(
                    onLogClick = { logId ->
                        navController.navigate("${OtherRoutes.ADD_EDIT}/$logId")
                    },
                    onAddLogClick = {
                        navController.navigate("${OtherRoutes.ADD_EDIT}/-1")
                    }
                )
            }

            // --- Add/Edit Screen ---
            composable(
                route = "${OtherRoutes.ADD_EDIT}/{${OtherRoutes.LOG_ID_ARG}}",
                arguments = listOf(
                    navArgument(OtherRoutes.LOG_ID_ARG) { type = NavType.IntType }
                )
            ) {
                AddEditScreen(
                    onNavigateUp = {
                        navController.popBackStack()
                    }
                )
            }

            // --- Tips Screen ---
            composable(Screen.Tips.route) { TipsScreen() }

            // --- Profile Screen ---
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}