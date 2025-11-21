package com.example.smartfit.ui.logs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.data.local.ActivityLog
import com.example.smartfit.data.local.DailySummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ActivityLogViewModel(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // --- 1. FILTER STATE (Fixes "Unresolved reference 'filterType'") ---
    private val _filterType = MutableStateFlow("All")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    // --- 2. LOG LIST LOGIC (Filtered) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    val allLogs: StateFlow<List<ActivityLog>> =
        combine(
            userPreferencesRepository.currentUserId,
            _filterType
        ) { userId, type ->
            Pair(userId, type)
        }.flatMapLatest { (userId, type) ->
            if (userId != null) {
                // --- UPDATED LOGIC ---
                when (type) {
                    "All" -> activityRepository.getLogsForUser(userId)
                    "Workout" -> activityRepository.getWorkouts(userId)
                    "Food & Drinks" -> activityRepository.getFoodLogs(userId) // <--- NEW EXPLICIT CALL
                    else -> activityRepository.getLogsByType(userId, type) // Fallback for "Steps"
                }
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // --- 3. SUMMARY LOGIC ---
    private val _summaryUnit = MutableStateFlow("steps")
    val summaryUnit: StateFlow<String> = _summaryUnit.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailySummary: StateFlow<List<DailySummary>> =
        combine(
            userPreferencesRepository.currentUserId,
            _summaryUnit
        ) { userId, unit ->
            Pair(userId, unit)
        }.flatMapLatest { (userId, unit) ->
            if (userId != null) {
                activityRepository.getDailySummary(unit, userId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- 4. ACTIONS (Fixes "Unresolved reference 'updateFilter'") ---

    fun updateSummaryUnit(newUnit: String) {
        _summaryUnit.value = newUnit
    }

    fun updateFilter(newFilter: String) {
        _filterType.value = newFilter
    }
}