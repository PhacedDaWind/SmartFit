package com.example.smartfit.ui.theme.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.ActivityRepository
import com.example.smartfit.data.local.ActivityLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ActivityLogViewModel (
    private val activityRepository: ActivityRepository
) : ViewModel(){
    val allLogs: StateFlow<List<ActivityLog>> =
        activityRepository.getAllLogs()
            .stateIn(
                scope = viewModelScope, // The ViewModel's coroutine scope
                started = SharingStarted.WhileSubscribed(5000L), // Keep flow active for 5s after UI stops listening
                initialValue = emptyList() // Start with an empty list
            )
}