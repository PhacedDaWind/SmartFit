package com.example.smartfit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.StepSensorRepository
import com.example.smartfit.data.local.ExerciseDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val suggestions: List<ExerciseDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val stepSensorRepository: StepSensorRepository
) : ViewModel() {

    // API Suggestions State
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Step Counter State
    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps.asStateFlow()

    init {
        // fetchSuggestions() // Uncomment if you added back the API logic
        observeSteps()
    }

    /* // Uncomment if you are using the API functionality
    fun fetchSuggestions() {
        _uiState.value = HomeUiState(isLoading = true)
        viewModelScope.launch {
            try {
                val apiKey = "YOUR_API_KEY"
                val suggestions = activityRepository.fetchWorkoutSuggestions(apiKey)
                _uiState.value = HomeUiState(suggestions = suggestions)
            } catch (e: Exception) {
                _uiState.value = HomeUiState(error = e.message)
            }
        }
    }
    */

    private fun observeSteps() {
        viewModelScope.launch {
            stepSensorRepository.stepCount.collect { steps ->
                _currentSteps.value = steps
            }
        }
    }
}