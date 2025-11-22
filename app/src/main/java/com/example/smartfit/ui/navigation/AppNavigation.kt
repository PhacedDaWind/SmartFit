package com.example.smartfit.ui.navigation

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
import com.example.smartfit.ui.home.HomeScreen

// --- ROUTE DEFINITIONS ---

// 1. Auth Routes
object AuthRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
}

// 2. Other App Routes
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

    // Determine if we should show the bottom bar
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Show bottom bar ONLY if we are on one of the main screens
    // Note: We use startswith because ActivityLog route now has parameters (e.g. "activity_log?filter=Cardio")
    val showBottomBar = bottomNavItems.any {
        currentRoute?.startsWith(it.route) == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        // Check if current route matches this screen (handling arguments)
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route?.startsWith(screen.route) == true
                        } == true

                        NavigationBarItem(
                            label = { Text(screen.label) },
                            selected = isSelected,
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
            startDestination = AuthRoutes.LOGIN,
            modifier = Modifier.padding(innerPadding)
        ) {

            // ===========================
            //      AUTH GRAPH
            // ===========================

            composable(AuthRoutes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
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
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(AuthRoutes.FORGOT_PASSWORD) {
                ChangePasswordScreen(
                    onSuccess = {
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // ===========================
            //      MAIN APP GRAPH
            // ===========================

            // --- 1. Home Screen ---
            composable(Screen.Home.route) {
                HomeScreen(
                    onWorkoutTypeClick = { type ->
                        // Navigate to Activity Log passing the filter (Cardio or Strength)
                        // Example: "activity_log?filter=Cardio"
                        navController.navigate(Screen.ActivityLog.route + "?filter=$type")
                    }
                )
            }

            // --- 2. Activity Log Screen (Updated to accept Filter) ---
            composable(
                // Define route with optional parameter: "activity_log?filter={filter}"
                route = Screen.ActivityLog.route + "?filter={filter}",
                arguments = listOf(
                    navArgument("filter") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                // Extract the argument
                val filterArg = backStackEntry.arguments?.getString("filter")

                ActivityLogScreen(
                    initialFilter = filterArg, // Pass it to the screen
                    onLogClick = { logId ->
                        navController.navigate("${OtherRoutes.ADD_EDIT}/$logId")
                    },
                    onAddLogClick = {
                        navController.navigate("${OtherRoutes.ADD_EDIT}/-1")
                    }
                )
            }

            // --- 3. Add/Edit Screen ---
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

            // --- 4. Tips Screen ---
            composable(Screen.Tips.route) { TipsScreen() }

            // --- 5. Profile Screen ---
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(AuthRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}