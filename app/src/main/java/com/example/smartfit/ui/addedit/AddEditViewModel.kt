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
        // Load existing log if editing
        if (logId != null && logId != -1) {
            viewModelScope.launch {
                currentLog = activityRepository.getLogById(logId).first()
                currentLog?.let { log ->
                    val cat = if (log.type == "Food" || log.type == "Food & Drinks") "Food & Drinks" else "Workout"
                    val wType = if (log.type == "Strength") "Strength" else "Cardio"

                    // Logic: If saved value is 0.0, show empty string in the UI
                    val displayValue = if (log.values == 0.0) "" else log.values.toString()

                    _uiState.update {
                        it.copy(
                            category = cat,
                            workoutType = wType,
                            name = log.name,
                            value = displayValue,
                            sets = if (wType == "Strength") log.sets.toString() else "",
                            reps = if (wType == "Strength") log.reps.toString() else "",
                            isEntryValid = true,
                            isEditing = true
                        )
                    }
                }
            }
        }
    }

    // --- INPUT HANDLERS ---

    fun onCategoryChange(cat: String) {
        _uiState.update { it.copy(category = cat, isEntryValid = validateInput(cat, it.workoutType, it.name, it.value, it.sets, it.reps)) }
    }
    fun onWorkoutTypeChange(wType: String) {
        _uiState.update {
            val newSets = if (wType == "Cardio") "" else it.sets
            val newReps = if (wType == "Cardio") "" else it.reps
            it.copy(workoutType = wType, sets = newSets, reps = newReps, isEntryValid = validateInput(it.category, wType, it.name, it.value, newSets, newReps))
        }
    }
    fun onNameChange(v: String) {
        _uiState.update { it.copy(name = v, isEntryValid = validateInput(it.category, it.workoutType, v, it.value, it.sets, it.reps)) }
    }
    fun onValueChange(v: String) {
        _uiState.update { it.copy(value = v, isEntryValid = validateInput(it.category, it.workoutType, it.name, v, it.sets, it.reps)) }
    }
    fun onSetsChange(v: String) {
        _uiState.update { it.copy(sets = v, isEntryValid = validateInput(it.category, it.workoutType, it.name, it.value, v, it.reps)) }
    }
    fun onRepsChange(v: String) {
        _uiState.update { it.copy(reps = v, isEntryValid = validateInput(it.category, it.workoutType, it.name, it.value, it.sets, v)) }
    }

    // --- SAVE LOGIC ---
    fun saveLog() {
        if (!uiState.value.isEntryValid) return

        viewModelScope.launch {
            val userId = userPreferencesRepository.currentUserId.first() ?: return@launch
            val state = _uiState.value

            val finalType: String
            val finalUnit: String
            var finalSets = 0
            var finalReps = 0

            if (state.category == "Food & Drinks") {
                finalType = "Food & Drinks"
                finalUnit = "kcal"
            } else {
                if (state.workoutType == "Cardio") {
                    finalType = "Cardio"
                    finalUnit = "mins"
                    // sets/reps stay 0
                } else {
                    finalType = "Strength"
                    finalUnit = "kg"
                    finalSets = state.sets.toIntOrNull() ?: 0
                    finalReps = state.reps.toIntOrNull() ?: 0
                }
            }

            // If user left value blank, save as 0.0 (Bodyweight)
            val finalValue = state.value.toDoubleOrNull() ?: 0.0

            activityRepository.upsertLog(
                ActivityLog(
                    id = if (logId == null || logId == -1) 0 else logId,
                    userId = userId,
                    type = finalType,
                    name = state.name,
                    values = finalValue,
                    unit = finalUnit,
                    sets = finalSets,
                    reps = finalReps
                )
            )
        }
    }

    fun deleteLog() {
        if (logId != null && currentLog != null) {
            viewModelScope.launch { activityRepository.deleteLog(currentLog!!) }
        }
    }

    // --- VALIDATION ---
    private fun validateInput(
        cat: String,
        wType: String,
        name: String,
        value: String,
        sets: String,
        reps: String
    ): Boolean {
        if (name.isBlank()) return false

        // 1. Strength Validation
        if (cat == "Workout" && wType == "Strength") {
            // Weight can be blank (Bodyweight). If not blank, must be a number.
            val isWeightValid = value.isBlank() || value.toDoubleOrNull() != null

            // Sets and Reps MUST be filled
            val areSetsRepsValid = sets.isNotBlank() && sets.toIntOrNull() != null &&
                    reps.isNotBlank() && reps.toIntOrNull() != null

            return isWeightValid && areSetsRepsValid
        }

        // 2. Cardio & Food Validation (Value/Duration IS Required)
        return value.isNotBlank() && value.toDoubleOrNull() != null
    }
}