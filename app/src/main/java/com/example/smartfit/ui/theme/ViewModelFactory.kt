package com.example.smartfit.ui.theme // Adjust package if needed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.smartfit.data.ActivityRepository
import com.example.smartfit.ui.theme.addedit.AddEditViewModel // You will create this ViewModel
import com.example.smartfit.ui.theme.logs.ActivityLogViewModel  // You will create this ViewModel

/**
 * Factory for manually creating ViewModels that have dependencies
 * (like ActivityRepository).
 */
class ViewModelFactory(
    private val activityRepository: ActivityRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

        // Check which ViewModel we are being asked to create

        if (modelClass.isAssignableFrom(ActivityLogViewModel::class.java)) {
            // For ActivityLogViewModel, just pass the repository
            @Suppress("UNCHECKED_CAST")
            return ActivityLogViewModel(activityRepository) as T
        }

        if (modelClass.isAssignableFrom(AddEditViewModel::class.java)) {
            // For AddEditViewModel, pass the repository AND the SavedStateHandle
            val savedStateHandle = extras.createSavedStateHandle()
            @Suppress("UNCHECKED_CAST")
            return AddEditViewModel(activityRepository, savedStateHandle) as T
        }

        // If it's an unknown ViewModel, throw an error
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}