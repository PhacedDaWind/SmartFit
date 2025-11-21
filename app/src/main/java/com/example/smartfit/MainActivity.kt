package com.example.smartfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.smartfit.ui.navigation.AppNavigation
import com.example.smartfit.ui.theme.SmartFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1. Get the repository from the Application class
            val app = application as SmartFitApplication
            val userPreferences = app.userPreferencesRepository

            // 2. Observe the "Dark Mode" setting as a State
            // "initial = false" means it starts in Light Mode until the real saved value loads
            val isDarkTheme by userPreferences.isDarkTheme.collectAsState(initial = false)

            // 3. Pass the value to your Theme
            SmartFitTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}