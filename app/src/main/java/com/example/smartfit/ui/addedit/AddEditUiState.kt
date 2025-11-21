package com.example.smartfit.ui.addedit

data class AddEditUiState (
    val name: String = "",
    val value: String = "", // Use String for text fields, convert to Double on save
    val sets: String = "",
    val category: String = "Workout",
    val workoutType: String = "Cardio",
    val isEntryValid: Boolean = false,
    val isEditing: Boolean = false
)
