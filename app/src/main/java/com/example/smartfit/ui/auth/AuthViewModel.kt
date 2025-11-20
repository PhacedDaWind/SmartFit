package com.example.smartfit.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.UserRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.data.local.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun register(username: String, pass: String) {
        viewModelScope.launch {
            if (userRepository.registerUser(User(username = username, password = pass))) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Username taken")
            }
        }
    }

    fun login(username: String, pass: String) {
        viewModelScope.launch {
            val user = userRepository.login(username, pass)
            if (user != null) {
                userPreferencesRepository.saveCurrentUserId(user.userId)
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Invalid credentials")
            }
        }
    }

    fun changePassword(username: String, oldPass: String, newPass: String) {
        viewModelScope.launch {
            if (userRepository.changePassword(username, oldPass, newPass)) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Incorrect current password")
            }
        }
    }

    fun resetState() { _authState.value = AuthState.Idle }
}

sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}