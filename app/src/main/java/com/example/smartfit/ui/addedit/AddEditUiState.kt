package com.example.smartfit.ui.addedit

data class AddEditUiState (
    val name: String = "",
    val value: String = "",
    val sets: String = "",
    val reps: String = "", // NEW: Holds the reps input
    val category: String = "Workout",
    val workoutType: String = "Cardio",
    val isEntryValid: Boolean = false,
    val isEditing: Boolean = false
)