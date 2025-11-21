package com.example.smartfit.ui.theme // Adjust package if needed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.UserRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.ui.auth.AuthViewModel
import com.example.smartfit.ui.addedit.AddEditViewModel
import com.example.smartfit.ui.logs.ActivityLogViewModel
import com.example.smartfit.ui.profile.ProfileViewModel
import com.example.smartfit.ui.home.HomeViewModel
import com.example.smartfit.data.repository.StepSensorRepository

class ViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository,
    private val stepSensorRepository: StepSensorRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

        if (modelClass.isAssignableFrom(ActivityLogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivityLogViewModel(activityRepository, userPreferencesRepository) as T
        }

        if (modelClass.isAssignableFrom(AddEditViewModel::class.java)) {
            val savedStateHandle = extras.createSavedStateHandle()
            @Suppress("UNCHECKED_CAST")
            return AddEditViewModel(activityRepository, userPreferencesRepository, savedStateHandle) as T
        }

        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(userRepository, userPreferencesRepository) as T
        }

        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userPreferencesRepository) as T
        }

        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Pass both ActivityRepository (for API) and StepSensorRepository (for Steps)
            return HomeViewModel(activityRepository, stepSensorRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}