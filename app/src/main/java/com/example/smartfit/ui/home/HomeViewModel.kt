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

data class HomeStats(
    val steps: Int = 0,
    val stepGoal: Int = 2500, // Now dynamic based on user selection
    val totalBurned: Double = 0.0,
    val foodCalories: Double = 0.0,
    val cardioMins: Double = 0.0,
    val strengthSets: Int = 0
)

class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val stepSensorRepository: StepSensorRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(HomeStats())
    val stats: StateFlow<HomeStats> = _stats.asStateFlow()

    private val _timeFilter = MutableStateFlow("Daily")
    val timeFilter: StateFlow<String> = _timeFilter.asStateFlow()

    private var lastSensorValue = 0

    init {
        observeData()
        observeSensorForSaving()
    }

    fun updateFilter(filter: String) {
        _timeFilter.value = filter
    }

    // Updated: Saves the goal specifically for the currently logged-in user
    fun updateStepGoal(newGoal: Int) {
        viewModelScope.launch {
            val userId = userPreferencesRepository.currentUserId.first()
            if (userId != null) {
                userPreferencesRepository.updateStepGoal(userId, newGoal)
            }
        }
    }

    // --- 1. SENSOR LOGIC (Saving Steps to DB) ---
    private fun observeSensorForSaving() {
        viewModelScope.launch {
            val userId = userPreferencesRepository.currentUserId.filterNotNull().first()

            stepSensorRepository.stepCount.collect { currentSensorValue ->
                if (lastSensorValue == 0) {
                    lastSensorValue = currentSensorValue
                } else {
                    val delta = currentSensorValue - lastSensorValue
                    if (delta > 0) {
                        activityRepository.addStepsToToday(userId, delta)
                        lastSensorValue = currentSensorValue
                    }
                }
            }
        }
    }

    // --- 2. DISPLAY LOGIC (Reading DB + User Prefs) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        // Step 1: Get the current User ID
        userPreferencesRepository.currentUserId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(HomeStats()) // If no user, return empty stats
            } else {
                // Step 2: Combine Filter AND the User's specific Goal
                combine(
                    _timeFilter,
                    userPreferencesRepository.getStepGoal(userId)
                ) { filter, goal ->
                    Triple(userId, filter, goal)
                }.flatMapLatest { (uid, filter, goal) ->

                    val startTime = getStartTime(filter)

                    // Step 3: Combine Activity Logs (Workouts) AND Steps (DailyStep table)
                    combine(
                        activityRepository.getLogsFromDate(uid, startTime),
                        activityRepository.getStepsFromDate(uid, startTime)
                    ) { logs, totalSteps ->

                        var food = 0.0
                        var cardioMins = 0.0
                        var strengthSets = 0

                        logs.forEach { log ->
                            when (log.type) {
                                "Food & Drinks" -> food += log.values
                                "Cardio" -> cardioMins += log.values
                                "Strength" -> strengthSets += log.sets
                            }
                        }

                        val stepBurn = totalSteps * 0.04

                        HomeStats(
                            steps = totalSteps,
                            stepGoal = goal, // Pass the user's goal to the UI
                            totalBurned = stepBurn,
                            foodCalories = food,
                            cardioMins = cardioMins,
                            strengthSets = strengthSets
                        )
                    }
                }
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
            calendar.set(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }

    fun simulateSteps(amount: Int) {}
}