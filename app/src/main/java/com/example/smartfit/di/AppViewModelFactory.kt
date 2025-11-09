package com.example.smartfit.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.smartfit.SmartFitApplication
import com.example.smartfit.ui.MainViewModel
import com.example.smartfit.ui.profile.ProfileViewModel

object AppViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

        // Get the application instance
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as SmartFitApplication

        // Get the repository
        val prefsRepository = application.userPreferencesRepository

        // Create ViewModels based on which one is requested
        return when {
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(prefsRepository) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(prefsRepository) as T
            }

            // Later, your teammate will add their ViewModels here:
            // modelClass.isAssignableFrom(LogViewModel::class.java) -> {
            //    LogViewModel(application.smartFitRepository) as T
            // }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}