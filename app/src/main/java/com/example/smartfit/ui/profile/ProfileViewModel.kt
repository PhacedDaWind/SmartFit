package com.example.smartfit.ui.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class ProfileViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // --- Existing Features (Theme & Image) ---
    val isDarkMode: StateFlow<Boolean> = userPreferencesRepository.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val profileImagePath: StateFlow<String?> = userPreferencesRepository.profileImagePath
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- BMI Calculator State (Temporary) ---
    var weightInput by mutableStateOf("")
    var heightInput by mutableStateOf("")

    private val _bmiResult = MutableStateFlow(0.0)
    val bmiResult: StateFlow<Double> = _bmiResult.asStateFlow()

    // --- Pure Calculation Function (No Saving) ---
    fun calculateBMI() {
        val weight = weightInput.toDoubleOrNull()
        val heightCm = heightInput.toDoubleOrNull()

        if (weight != null && heightCm != null && heightCm > 0) {
            // Convert cm to meters
            val heightM = heightCm / 100.0
            val bmi = weight / (heightM * heightM)

            // Round and update state
            _bmiResult.value = BigDecimal(bmi).setScale(2, RoundingMode.HALF_UP).toDouble()
        } else {
            _bmiResult.value = 0.0
        }
    }

    // --- Existing Helpers ---
    fun toggleTheme(isDark: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setDarkTheme(isDark) }
    }

    fun onImageSelected(uri: Uri?) {
        uri?.let { viewModelScope.launch { userPreferencesRepository.saveProfileImage(it) } }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            userPreferencesRepository.clearSession()
            onLogoutComplete()
        }
    }
}