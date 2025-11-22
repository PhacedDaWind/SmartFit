package com.example.smartfit.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.data.local.ActivityLog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AddEditViewModel(
    private val activityRepository: ActivityRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val logId: Int? = savedStateHandle["logId"]
    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()
    private var currentLog: ActivityLog? = null

    init {
        if (logId != null && logId != -1) {
            viewModelScope.launch {
                currentLog = activityRepository.getLogById(logId).first()
                currentLog?.let { log ->
                    // Logic to determine UI state from DB data
                    val cat = if (log.type == "Food" || log.type == "Food & Drinks") "Food & Drinks" else "Workout"
                    val wType = if (log.type == "Strength") "Strength" else "Cardio"

                    _uiState.update {
                        it.copy(
                            category = cat,
                            workoutType = wType,
                            name = log.name,
                            value = log.values.toString(),
                            sets = log.sets.toString(),
                            isEntryValid = true,
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    // --- Inputs ---
    fun onCategoryChange(cat: String) {
        _uiState.update { it.copy(category = cat, isEntryValid = validateInput(cat, it.workoutType, it.name, it.value, it.sets)) }
    }

    fun onWorkoutTypeChange(wType: String) {
        _uiState.update { it.copy(workoutType = wType, isEntryValid = validateInput(it.category, wType, it.name, it.value, it.sets)) }
    }

    fun onNameChange(v: String) {
        _uiState.update { it.copy(name = v, isEntryValid = validateInput(it.category, it.workoutType, v, it.value, it.sets)) }
    }

    fun onValueChange(v: String) {
        _uiState.update { it.copy(value = v, isEntryValid = validateInput(it.category, it.workoutType, it.name, v, it.sets)) }
    }

    fun onSetsChange(v: String) {
        _uiState.update { it.copy(sets = v, isEntryValid = validateInput(it.category, it.workoutType, it.name, it.value, v)) }
    }

    // --- Save Logic ---
    fun saveLog() {
        if (!uiState.value.isEntryValid) return

        viewModelScope.launch {
            // Get Current User ID
            val userId = userPreferencesRepository.currentUserId.first() ?: return@launch
            val state = _uiState.value

            // Determine final Type and Unit
            val finalType: String
            val finalUnit: String
            var finalSets = 0

            if (state.category == "Food & Drinks") {
                finalType = "Food & Drinks"
                finalUnit = "kcal"
            } else {
                if (state.workoutType == "Cardio") {
                    finalType = "Cardio"
                    finalUnit = "mins"
                } else {
                    finalType = "Strength"
                    finalUnit = "mins"
                    finalSets = state.sets.toIntOrNull() ?: 0
                }
            }

            activityRepository.upsertLog(
                ActivityLog(
                    id = if (logId == null || logId == -1) 0 else logId,
                    userId = userId, // Save with User ID
                    type = finalType,
                    name = state.name,
                    values = state.value.toDoubleOrNull() ?: 0.0,
                    unit = finalUnit,
                    sets = finalSets
                )
            )
        }
    }

    fun deleteLog() {
        if (logId != null && currentLog != null) {
            viewModelScope.launch { activityRepository.deleteLog(currentLog!!) }
        }
    }

    private fun validateInput(cat: String, wType: String, name: String, value: String, sets: String): Boolean {
        val basicCheck = name.isNotBlank() && value.isNotBlank() && value.toDoubleOrNull() != null
        if (!basicCheck) return false

        if (cat == "Workout" && wType == "Strength") {
            return sets.isNotBlank() && sets.toIntOrNull() != null
        }
        return true
    }
}