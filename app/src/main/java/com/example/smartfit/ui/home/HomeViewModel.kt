package com.example.smartfit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.StepSensorRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HomeStats(
    val steps: Int = 0,
    val stepGoal: Int = 2500,
    val totalBurned: Double = 0.0,
    val foodCalories: Double = 0.0,
    val cardioCount: Int = 0,
    val strengthCount: Int = 0
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

    // CHANGED: Now we track the ACTUAL timestamp instead of an offset
    private val _currentDate = MutableStateFlow(System.currentTimeMillis())

    private val _dateLabel = MutableStateFlow("Today")
    val dateLabel: StateFlow<String> = _dateLabel.asStateFlow()

    private var lastSensorValue = 0

    init {
        observeData()
        observeSensorForSaving()
    }

    fun updateFilter(filter: String) {
        _timeFilter.value = filter
        // Optional: Reset to "Today" when switching filters?
        // _currentDate.value = System.currentTimeMillis()
    }

    // NEW: Jump to a specific date (from Date Picker)
    fun updateDate(newTimestamp: Long) {
        _currentDate.value = newTimestamp
    }

    // UPDATED: Move Back/Forward relative to the CURRENT date
    fun previousPeriod() {
        _currentDate.value = moveDate(_currentDate.value, -1)
    }

    fun nextPeriod() {
        _currentDate.value = moveDate(_currentDate.value, 1)
    }

    // Helper to add/subtract Days or Months
    private fun moveDate(timestamp: Long, amount: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        if (_timeFilter.value == "Daily") {
            calendar.add(Calendar.DAY_OF_YEAR, amount)
        } else {
            calendar.add(Calendar.MONTH, amount)
        }
        return calendar.timeInMillis
    }

    // ... (Step Goal and Sensor logic remains the same) ...
    fun updateStepGoal(newGoal: Int) {
        viewModelScope.launch {
            val userId = userPreferencesRepository.currentUserId.first()
            if (userId != null) userPreferencesRepository.updateStepGoal(userId, newGoal)
        }
    }

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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        userPreferencesRepository.currentUserId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(HomeStats())
            } else {
                // Combine Filter, Goal, and CurrentDate
                combine(
                    _timeFilter,
                    userPreferencesRepository.getStepGoal(userId),
                    _currentDate
                ) { filter, goal, date ->
                    Triple(filter, goal, date)
                }.flatMapLatest { (filter, goal, date) ->

                    // 1. Calculate Range based on the specific date
                    val timeRange = getTimeRange(filter, date)
                    val startTime = timeRange.first
                    val endTime = timeRange.second

                    updateDateLabel(filter, startTime)

                    // 2. Fetch Data
                    combine(
                        activityRepository.getLogsBetween(userId, startTime, endTime),
                        activityRepository.getStepsBetween(userId, startTime, endTime)
                    ) { logs, totalSteps ->

                        var food = 0.0
                        var cardioCount = 0
                        var strengthCount = 0

                        logs.forEach { log ->
                            when (log.type) {
                                "Food & Drinks" -> food += log.values
                                "Cardio" -> cardioCount++
                                "Strength" -> strengthCount++
                            }
                        }
                        val stepBurn = totalSteps * 0.04

                        HomeStats(
                            steps = totalSteps,
                            stepGoal = goal,
                            totalBurned = stepBurn,
                            foodCalories = food,
                            cardioCount = cardioCount,
                            strengthCount = strengthCount
                        )
                    }
                }
            }
        }.onEach {
            _stats.value = it
        }.launchIn(viewModelScope)
    }

    // Helper: Calculates Start/End from a specific timestamp
    private fun getTimeRange(filter: String, date: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date

        if (filter == "Daily") {
            // Start of selected day
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            // Start of next day
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val end = calendar.timeInMillis

            return Pair(start, end)
        } else {
            // Start of selected month
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis

            // Start of next month
            calendar.add(Calendar.MONTH, 1)
            val end = calendar.timeInMillis

            return Pair(start, end)
        }
    }

    private fun updateDateLabel(filter: String, startTime: Long) {
        val format = if (filter == "Daily") "EEE, MMM dd" else "MMMM yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())

        val now = System.currentTimeMillis()
        val isToday = (now - startTime) < 86400000 && (now >= startTime)

        if (filter == "Daily" && isToday) {
            _dateLabel.value = "Today"
        } else {
            _dateLabel.value = sdf.format(startTime)
        }
    }

    fun simulateSteps(amount: Int) {}
}