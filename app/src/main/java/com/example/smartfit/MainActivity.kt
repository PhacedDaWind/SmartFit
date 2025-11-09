// In com/example/smartfit/MainActivity.kt
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartfit.di.AppViewModelFactory
import com.example.smartfit.ui.MainViewModel
import com.example.smartfit.ui.navigation.AppNavigation // Make sure to create this file
import com.example.smartfit.ui.theme.SmartFitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Get the MainViewModel
            val mainViewModel: MainViewModel = viewModel(factory = AppViewModelFactory)
            // Collect the theme state
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

            // Pass the state to your theme!
            SmartFitTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // This is your NavHost from the previous guide
                    // Make sure it's in the 'com.example.smartfit.ui.navigation' package
                    AppNavigation()
                }
            }
        }
    }
}