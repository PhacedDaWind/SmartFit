package com.example.smartfit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.StepSensorRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// Holds all the stats for the Home Screen
data class HomeStats(
    val steps: Int = 0,
    val totalBurned: Double = 0.0, // Combined: (Steps * 0.04) + (Cardio * 8) + (Strength * 12)
    val stepsCalories: Double = 0.0, // Just from walking
    val foodCalories: Double = 0.0,
    val cardioMins: Double = 0.0,
    val strengthSets: Int = 0
)

class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val stepSensorRepository: StepSensorRepository
) : ViewModel() {

    // UI State
    private val _stats = MutableStateFlow(HomeStats())
    val stats: StateFlow<HomeStats> = _stats.asStateFlow()

    // Filter (Default is "Daily")
    private val _timeFilter = MutableStateFlow("Daily")
    val timeFilter: StateFlow<String> = _timeFilter.asStateFlow()

    init {
        observeData()
    }

    fun updateFilter(filter: String) {
        _timeFilter.value = filter
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        // We combine 3 streams: Live Steps, User ID, and Time Filter
        combine(
            stepSensorRepository.stepCount.onStart { emit(0) }, // Ensure it starts with 0
            userPreferencesRepository.currentUserId,
            _timeFilter
        ) { steps, userId, filter ->
            Triple(steps, userId, filter)
        }.flatMapLatest { (sensorSteps, userId, filter) ->
            if (userId != null) {
                val startTime = getStartTime(filter)

                // Fetch logs from DB for this user and time period
                activityRepository.getTodayLogs(userId, startTime).map { logs ->

                    // --- 1. Calculate Steps ---
                    // If filter is Monthly, we ignore live sensor (show 0 or aggregate if you had history)
                    // For Daily, we use the live sensor
                    val displaySteps = if (filter == "Daily") sensorSteps else 0
                    val stepBurn = displaySteps * 0.04

                    // --- 2. Calculate Manual Logs ---
                    var food = 0.0
                    var cardioMins = 0.0
                    var strengthSets = 0
                    var workoutBurn = 0.0

                    logs.forEach { log ->
                        when (log.type) {
                            "Food & Drinks" -> {
                                food += log.values
                            }
                            "Cardio" -> {
                                cardioMins += log.values
                                // ESTIMATE: 8 kcal per minute
                                workoutBurn += (log.values * 8)
                            }
                            "Strength" -> {
                                strengthSets += log.sets
                                // ESTIMATE: 12 kcal per set
                                workoutBurn += (log.sets * 12)
                            }
                        }
                    }

                    // --- 3. Combine for Totals ---
                    val combinedBurn = stepBurn + workoutBurn

                    HomeStats(
                        steps = displaySteps,
                        totalBurned = combinedBurn,
                        stepsCalories = stepBurn,
                        foodCalories = food,
                        cardioMins = cardioMins,
                        strengthSets = strengthSets
                    )
                }
            } else {
                flowOf(HomeStats()) // No user logged in
            }
        }.onEach {
            _stats.value = it
        }.launchIn(viewModelScope)
    }

    private fun getStartTime(filter: String): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (filter == "Monthly") {
            calendar.set(Calendar.DAY_OF_MONTH, 1) // Start of this month
        }
        return calendar.timeInMillis
    }
}