package com.example.smartfit.ui.theme.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.ActivityRepository
import com.example.smartfit.data.local.ActivityLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Add/Edit screen.
 *
 * @param activityRepository The repository for data operations.
 * @param savedStateHandle Handle to access navigation arguments (e.g., the log ID).
 */
class AddEditViewModel(
    private val activityRepository: ActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Get the log ID from navigation arguments. "logId" must match the navigation route.
    // If it's a new log, this will be null.
    private val logId: Int? = savedStateHandle["logId"]

    // Backing property for the UI state
    private val _uiState = MutableStateFlow(AddEditUiState())
    // Public, read-only StateFlow for the UI to observe
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    // A holder for the current log being edited, if any
    private var currentLog: ActivityLog? = null

    init {
        // --- THIS IS THE FIRST FIX ---
        // Only load if logId is not null AND not -1 (which means "new log")
        if (logId != null && logId != -1) {
            viewModelScope.launch {
                currentLog = activityRepository.getLogById(logId).first()
                currentLog?.let { log ->
                    _uiState.update {
                        it.copy(
                            type = log.type,
                            name = log.name,
                            value = log.values.toString(), // Your 'values' field is correct
                            unit = log.unit,
                            isEntryValid = true,
                            isEditing = true // <-- SET THE NEW STATE
                        )
                    }
                }
            }
        }
    }

    // --- Public functions called by the UI ---

    fun onTypeChange(newType: String) {
        _uiState.update { it.copy(type = newType, isEntryValid = validateInput(newType, it.name, it.value)) }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, isEntryValid = validateInput(it.type, newName, it.value)) }
    }

    fun onValueChange(newValue: String) {
        _uiState.update { it.copy(value = newValue, isEntryValid = validateInput(it.type, it.name, newValue)) }
    }

    fun onUnitChange(newUnit: String) {
        _uiState.update { it.copy(unit = newUnit) } // Unit can be optional
    }

    fun saveLog() {
        if (!validateInput()) return // Final check

        viewModelScope.launch {
            activityRepository.upsertLog(
                ActivityLog(
                    id = if (logId == null || logId == -1) 0 else logId, // Use existing ID or 0 for new log
                    date = System.currentTimeMillis(),
                    type = _uiState.value.type,
                    name = _uiState.value.name,
                    values = _uiState.value.value.toDoubleOrNull() ?: 0.0,
                    unit = _uiState.value.unit
                )
            )
        }
    }

    fun deleteLog() {
        // We can only delete a log that already exists (has an ID)
        if (logId != null && currentLog != null) {
            viewModelScope.launch {
                activityRepository.deleteLog(currentLog!!)
            }
        }
    }

    // --- Private helper function ---

    private fun validateInput(
        type: String = _uiState.value.type,
        name: String = _uiState.value.name,
        value: String = _uiState.value.value
    ): Boolean {
        // Basic validation: type, name, and value must not be blank
        // and value must be a valid number.
        return type.isNotBlank() &&
                name.isNotBlank() &&
                value.isNotBlank() &&
                value.toDoubleOrNull() != null
    }
}