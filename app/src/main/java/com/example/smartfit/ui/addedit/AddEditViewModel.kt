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
                    _uiState.update {
                        it.copy(
                            type = log.type, name = log.name,
                            value = log.values.toString(), unit = log.unit,
                            isEntryValid = true, isEditing = true
                        )
                    }
                }
            }
        }
    }

    // ... onTypeChange, onNameChange, etc ...
    fun onTypeChange(v: String) { _uiState.update { it.copy(type = v, isEntryValid = validate(v, it.name, it.value)) } }
    fun onNameChange(v: String) { _uiState.update { it.copy(name = v, isEntryValid = validate(it.type, v, it.value)) } }
    fun onValueChange(v: String) { _uiState.update { it.copy(value = v, isEntryValid = validate(it.type, it.name, v)) } }
    fun onUnitChange(v: String) { _uiState.update { it.copy(unit = v) } }

    fun saveLog() {
        if (!validate()) return
        viewModelScope.launch {
            val userId = userPreferencesRepository.currentUserId.first()
            if (userId != null) {
                activityRepository.upsertLog(
                    ActivityLog(
                        id = if (logId == null || logId == -1) 0 else logId,
                        type = _uiState.value.type,
                        name = _uiState.value.name,
                        values = _uiState.value.value.toDoubleOrNull() ?: 0.0,
                        unit = _uiState.value.unit,
                        userId = userId // Save with User ID
                    )
                )
            }
        }
    }

    fun deleteLog() {
        if (logId != null && currentLog != null) {
            viewModelScope.launch { activityRepository.deleteLog(currentLog!!) }
        }
    }

    private fun validate(type: String = _uiState.value.type, name: String = _uiState.value.name, value: String = _uiState.value.value): Boolean {
        return type.isNotBlank() && name.isNotBlank() && value.isNotBlank() && value.toDoubleOrNull() != null
    }
}