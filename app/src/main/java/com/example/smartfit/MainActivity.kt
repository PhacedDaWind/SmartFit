package com.example.smartfit

import android.os.Bundle
import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartfit.ui.theme.addedit.AddEditScreen
import com.example.smartfit.ui.theme.logs.ActivityLogScreen
import com.example.smartfit.ui.theme.SmartFitTheme

// --- Define ALL screen routes for the whole team ---

// Main screens on the bottom bar
sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomBarScreen("home", "Home", Icons.Default.Home)
    object Logs : BottomBarScreen("logs", "Logs", Icons.Default.ListAlt)
    object Profile : BottomBarScreen("profile", "Profile", Icons.Default.AccountCircle)
}

// Other screens (not on the bottom bar)
object OtherAppRoutes {
    const val ADD_EDIT_LOG = "addEditLog"
    const val LOG_ID_ARG = "logId"
    // Your teammate might add a "Suggestions" screen route here
}

// List of screens for the bottom bar
val bottomBarItems = listOf(
    BottomBarScreen.Home,
    BottomBarScreen.Logs,
    BottomBarScreen.Profile
)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartFitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppScreen()
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        // The NavHost is placed inside the Scaffold
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        bottomBarItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid re-selecting the same item
                        launchSingleTop = true
                        // Restore state when re-selecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route, // Start on the Home screen
        modifier = modifier
    ) {

        // --- Teammate's Home Screen ---
        composable(route = BottomBarScreen.Home.route) {
            HomeScreenPlaceholder() // Teammate will replace this
        }

        // --- YOUR Activity Log List Screen ---
        composable(route = BottomBarScreen.Logs.route) {
            ActivityLogScreen(
                onLogClick = { logId ->
                    // This navigation stays the same!
                    navController.navigate("${OtherAppRoutes.ADD_EDIT_LOG}/$logId")
                },
                onAddLogClick = {
                    navController.navigate("${OtherAppRoutes.ADD_EDIT_LOG}/-1")
                }
            )
        }

        // --- Teammate's Profile Screen ---
        composable(route = BottomBarScreen.Profile.route) {
            ProfileScreenPlaceholder() // Teammate will replace this
        }

        // --- YOUR Add/Edit Log Screen (NOT on the bottom bar) ---
        composable(
            route = "${OtherAppRoutes.ADD_EDIT_LOG}/{${OtherAppRoutes.LOG_ID_ARG}}",
            arguments = listOf(
                navArgument(OtherAppRoutes.LOG_ID_ARG) {
                    type = NavType.IntType
                }
            )
        ) {
            AddEditScreen(
                onNavigateUp = {
                    navController.popBackStack()
                }
            )
        }

        // --- Teammate's other screens (e.g., Suggestions) can be added here ---
    }
}

// --- Placeholder Screens for Teammates ---
// Your teammates can delete these and put their real screens in the NavHost.

@Composable
fun HomeScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Home Screen (Teammate's part)")
    }
}

@Composable
fun ProfileScreenPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Profile Screen (Teammate's part)")
    }
}