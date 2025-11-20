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

class ViewModelFactory(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val userRepository: UserRepository
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

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}