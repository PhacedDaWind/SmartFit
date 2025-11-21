package com.example.smartfit.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // --- State: Dark Mode ---
    val isDarkMode: StateFlow<Boolean> = userPreferencesRepository.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // --- Function: Toggle Theme ---
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkTheme(isDark)
        }
    }

    // --- Function: Log Out ---
    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            // 1. Clear the session from DataStore
            userPreferencesRepository.clearSession()
            // 2. Inform the UI to navigate away
            onLogoutComplete()
        }
    }
}