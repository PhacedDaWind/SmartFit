package com.example.smartfit.ui.logs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.data.local.ActivityLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ActivityLogViewModel(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val TAG = "ActivityLogViewModel"
    private val _filterType = MutableStateFlow("All")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allLogs: StateFlow<List<ActivityLog>> =
        combine(
            userPreferencesRepository.currentUserId,
            _filterType
        ) { userId, type ->
            Pair(userId, type)
        }.flatMapLatest { (userId, type) ->
            if (userId != null) {
                Log.d(TAG, "Loading logs with filter: $type")
                when (type) {
                    // 1. All Logs
                    "All" -> activityRepository.getLogsForUser(userId)

                    // 2. Specific Workout Types (These match your Database 'type' column)
                    "Cardio" -> activityRepository.getLogsByType(userId, "Cardio")
                    "Strength" -> activityRepository.getLogsByType(userId, "Strength")

                    // 3. Food (Matches UI string "Food & Drinks")
                    "Food & Drinks" -> activityRepository.getFoodLogs(userId)

                    // Fallback
                    else -> flowOf(emptyList())
                }
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFilter(newFilter: String) {
        Log.d(TAG, "Filter updated to: $newFilter")
        _filterType.value = newFilter
    }
}