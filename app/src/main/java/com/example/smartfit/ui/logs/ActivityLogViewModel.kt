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

    @OptIn(ExperimentalCoroutinesApi::class)
    val allLogs: StateFlow<List<ActivityLog>> =
        userPreferencesRepository.currentUserId.flatMapLatest { userId ->
            if (userId != null) activityRepository.getLogsForUser(userId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _summaryUnit = MutableStateFlow("steps")
    val summaryUnit: StateFlow<String> = _summaryUnit.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailySummary: StateFlow<List<DailySummary>> =
        combine(userPreferencesRepository.currentUserId, _summaryUnit) { userId, unit ->
            Pair(userId, unit)
        }.flatMapLatest { (userId, unit) ->
            if (userId != null) activityRepository.getDailySummary(unit, userId) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSummaryUnit(newUnit: String) { _summaryUnit.value = newUnit }
}