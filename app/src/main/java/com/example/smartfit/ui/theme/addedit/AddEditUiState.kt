package com.example.smartfit.ui.theme.addedit

data class AddEditUiState (
    val type: String = "",
    val name: String = "",
    val value: String = "", // Use String for text fields, convert to Double on save
    val unit: String = "",
    val isEntryValid: Boolean = false,
    val isEditing: Boolean = false
)
