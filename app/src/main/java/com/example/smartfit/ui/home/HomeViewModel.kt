package com.example.smartfit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.StepSensorRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.data.repository.UserRepository // <--- New Import
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
    private val userRepository: UserRepository, // <--- Add UserRepository here
    private val stepSensorRepository: StepSensorRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(HomeStats())
    val stats: StateFlow<HomeStats> = _stats.asStateFlow()

    private val _timeFilter = MutableStateFlow("Daily")
    val timeFilter: StateFlow<String> = _timeFilter.asStateFlow()

    private val _currentDate = MutableStateFlow(System.currentTimeMillis())

    private val _dateLabel = MutableStateFlow("Today")
    val dateLabel: StateFlow<String> = _dateLabel.asStateFlow()

    // NEW: Stores the username
    private val _username = MutableStateFlow("User")
    val username: StateFlow<String> = _username.asStateFlow()

    private var lastSensorValue = 0

    init {
        observeData()
        observeSensorForSaving()
        fetchUsername() // <--- Trigger fetching
    }

    // NEW: Function to get username from DB
    private fun fetchUsername() {
        viewModelScope.launch {
            userPreferencesRepository.currentUserId.collect { userId ->
                if (userId != null) {
                    val user = userRepository.getUserById(userId)
                    if (user != null) {
                        _username.value = user.username
                    }
                }
            }
        }
    }

    // ... (Rest of your existing functions: updateFilter, updateDate, etc.) ...
    // ... (Copy them exactly as they were in the previous code) ...

    fun updateFilter(filter: String) {
        _timeFilter.value = filter
    }

    fun updateDate(newTimestamp: Long) {
        _currentDate.value = newTimestamp
    }

    fun previousPeriod() {
        _currentDate.value = moveDate(_currentDate.value, -1)
    }

    fun nextPeriod() {
        _currentDate.value = moveDate(_currentDate.value, 1)
    }

    private fun moveDate(timestamp: Long, amount: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        if (_timeFilter.value == "Daily") calendar.add(Calendar.DAY_OF_YEAR, amount)
        else calendar.add(Calendar.MONTH, amount)
        return calendar.timeInMillis
    }

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
                if (lastSensorValue == 0) lastSensorValue = currentSensorValue
                else {
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
                combine(
                    _timeFilter,
                    userPreferencesRepository.getStepGoal(userId),
                    _currentDate
                ) { filter, goal, date ->
                    Triple(filter, goal, date)
                }.flatMapLatest { (filter, goal, date) ->
                    val timeRange = getTimeRange(filter, date)
                    val startTime = timeRange.first
                    val endTime = timeRange.second
                    updateDateLabel(filter, startTime)

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
                        HomeStats(totalSteps, goal, stepBurn, food, cardioCount, strengthCount)
                    }
                }
            }
        }.onEach { _stats.value = it }.launchIn(viewModelScope)
    }

    private fun getTimeRange(filter: String, date: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        if (filter == "Daily") {
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val end = calendar.timeInMillis
            return Pair(start, end)
        } else {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            val start = calendar.timeInMillis
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
        if (filter == "Daily" && isToday) _dateLabel.value = "Today" else _dateLabel.value = sdf.format(startTime)
    }

    fun simulateSteps(amount: Int) {}
}