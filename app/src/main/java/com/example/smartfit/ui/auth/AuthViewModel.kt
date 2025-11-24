package com.example.smartfit.ui.auth

import android.content.ContentValues.TAG
import android.util.Log
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
            if (userRepository.getUserByUsername(username) != null) {
                _authState.value = AuthState.Error("Username taken")
            } else {
                userRepository.registerUser(User(username = username, password = pass))
                _authState.value = AuthState.Success
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

    fun changePassword(username: String, oldPassInput: String, newPassInput: String) {
        viewModelScope.launch {

            // 1. Get the User from Database FIRST
            val user = userRepository.getUserByUsername(username)
            Log.d(TAG, "Password change request for: $username")

            if (user == null) {
                _authState.value = AuthState.Error("User does not exist")
                return@launch
            }

            // 2. CHECK: Is the New Password the same as the ACTUAL DB Password?
            // (We compare against user.password, not the input text)
            if (user.password == newPassInput) {
                _authState.value = AuthState.Error("New password cannot be the same as your old password")
                return@launch
            }

            // 3. CHECK: Did the user type the correct Current Password?
            if (user.password != oldPassInput) {
                Log.w(TAG, "Password change failed: Incorrect current password")
                _authState.value = AuthState.Error("Incorrect current password")
                return@launch
            }

            // 4. All checks passed -> Update
            Log.i(TAG, "Password updated successfully")
            userRepository.updatePassword(user.userId, newPassInput)
            _authState.value = AuthState.Success
        }
    }

    fun resetState() { _authState.value = AuthState.Idle }
}

sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}