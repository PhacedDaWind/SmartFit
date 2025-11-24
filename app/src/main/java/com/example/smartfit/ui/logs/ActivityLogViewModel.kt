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

    private val TAG = "ActivityLogViewModel" // <--- Log Tag
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
                Log.d(TAG, "Loading logs with filter: $type") // <--- Log
                when (type) {
                    "All" -> activityRepository.getLogsForUser(userId)
                    "Workout" -> activityRepository.getWorkouts(userId)
                    "Food & Drinks" -> activityRepository.getFoodLogs(userId)
                    else -> flowOf(emptyList())
                }
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFilter(newFilter: String) {
        Log.d(TAG, "Filter updated to: $newFilter") // <--- Log
        _filterType.value = newFilter
    }
}