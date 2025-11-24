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
        Log.d(TAG, "Register requested for user: $username") // Log request
        viewModelScope.launch {
            if (userRepository.getUserByUsername(username) != null) {
                Log.w(TAG, "Registration failed: Username '$username' already exists") // Log specific failure
                _authState.value = AuthState.Error("Username taken")
            } else {
                userRepository.registerUser(User(username = username, password = pass))
                Log.i(TAG, "Registration successful for: $username") // Log success
                _authState.value = AuthState.Success
            }
        }
    }

    fun login(username: String, pass: String) {
        Log.d(TAG, "Login requested for user: $username") // Log request
        viewModelScope.launch {
            val user = userRepository.login(username, pass)
            if (user != null) {
                Log.i(TAG, "Login successful. User ID: ${user.userId}") // Log success
                userPreferencesRepository.saveCurrentUserId(user.userId)
                _authState.value = AuthState.Success
            } else {
                Log.w(TAG, "Login failed: Invalid credentials for $username") // Log failure
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
                Log.w(TAG, "Change Password failed: User not found") // Log error
                _authState.value = AuthState.Error("User does not exist")
                return@launch
            }

            // 2. CHECK: Is the New Password the same as the ACTUAL DB Password?
            // (We compare against user.password, not the input text)
            if (user.password == newPassInput) {
                Log.w(TAG, "Change Password failed: New password is same as old") // Log error
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
            Log.i(TAG, "Password updated successfully for user: $username")
            userRepository.updatePassword(user.userId, newPassInput)
            _authState.value = AuthState.Success
        }
    }

    fun resetState() {
        // Log.d(TAG, "Resetting AuthState to Idle") // Optional log
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}