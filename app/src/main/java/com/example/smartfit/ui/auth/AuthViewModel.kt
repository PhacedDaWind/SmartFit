package com.example.smartfit.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfit.data.repository.UserRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.data.local.User
import com.example.smartfit.utils.EmailSender // <--- Ensure EmailSender is imported
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class AuthViewModel(
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val TAG = "AuthViewModel"
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    // Temporary storage for Forgot Password flow
    private var generatedOtp: String = ""
    private var otpEmail: String = ""

    // --- 1. REGISTER (With Email) ---
    fun register(username: String, email: String, pass: String) {
        Log.d(TAG, "Register requested: $username, $email") // <--- LOG

        viewModelScope.launch {
            if (userRepository.getUserByUsername(username) != null) {
                Log.w(TAG, "Registration failed: Username '$username' already exists") // <--- LOG
                _authState.value = AuthState.Error("Username taken")
            } else if (userRepository.getUserByEmail(email) != null) {
                Log.w(TAG, "Registration failed: Email '$email' already exists") // <--- LOG
                _authState.value = AuthState.Error("Email already registered")
            } else {
                userRepository.registerUser(User(username = username, email = email, password = pass))
                Log.i(TAG, "Registration success for: $username") // <--- LOG
                _authState.value = AuthState.Success
            }
        }
    }

    // --- 2. LOGIN ---
    fun login(username: String, pass: String) {
        Log.d(TAG, "Login requested: $username") // <--- LOG

        viewModelScope.launch {
            val user = userRepository.login(username, pass)
            if (user != null) {
                Log.i(TAG, "Login success. User ID: ${user.userId}") // <--- LOG
                userPreferencesRepository.saveCurrentUserId(user.userId)
                _authState.value = AuthState.Success
            } else {
                Log.w(TAG, "Login failed: Invalid credentials") // <--- LOG
                _authState.value = AuthState.Error("Invalid credentials")
            }
        }
    }

    // --- 3. CHANGE PASSWORD (Logged In User) ---
    fun changePassword(username: String, oldPassInput: String, newPassInput: String) {
        Log.d(TAG, "Password change request for: $username") // <--- LOG

        viewModelScope.launch {
            // 1. Get the User from Database FIRST
            val user = userRepository.getUserByUsername(username)

            if (user == null) {
                Log.w(TAG, "Change Password failed: User not found") // <--- LOG
                _authState.value = AuthState.Error("User does not exist")
                return@launch
            }

            // 2. CHECK: Is the New Password the same as the ACTUAL DB Password?
            if (user.password == newPassInput) {
                Log.w(TAG, "Change Password failed: New password is same as old") // <--- LOG
                _authState.value = AuthState.Error("New password cannot be the same as your old password")
                return@launch
            }

            // 3. CHECK: Did the user type the correct Current Password?
            if (user.password != oldPassInput) {
                Log.w(TAG, "Password change failed: Incorrect current password") // <--- LOG
                _authState.value = AuthState.Error("Incorrect current password")
                return@launch
            }

            // 4. All checks passed -> Update
            userRepository.updatePassword(user.userId, newPassInput)
            Log.i(TAG, "Password changed via Profile successfully") // <--- LOG
            _authState.value = AuthState.Success
        }
    }

    // --- 4. FORGOT PASSWORD (OTP Flow) ---

    // Step A: Send OTP via Email
    fun sendOtp(email: String) {
        viewModelScope.launch {
            Log.d(TAG, "Searching for user with email: $email") // <--- LOG
            val user = userRepository.getUserByEmail(email)

            if (user == null) {
                Log.w(TAG, "Email not found in database") // <--- LOG
                _authState.value = AuthState.Error("Email not registered")
            } else {
                val otp = Random.nextInt(1000, 9999).toString()
                generatedOtp = otp
                otpEmail = email // Remember the email for the next step

                Log.d(TAG, "Sending OTP $otp to $email...") // <--- LOG

                // CALL REAL EMAIL SENDER
                val success = EmailSender.sendOtpEmail(email, otp)

                if (success) {
                    Log.i(TAG, "Email sent successfully") // <--- LOG
                    _authState.value = AuthState.OtpSent(otp)
                } else {
                    Log.e(TAG, "Email send failed") // <--- LOG
                    _authState.value = AuthState.Error("Failed to send email. Check Internet.")
                }
            }
        }
    }

    // Step B: Verify & Reset
    fun verifyAndResetPassword(inputOtp: String, newPass: String) {
        Log.d(TAG, "Verifying OTP: Input=$inputOtp vs Actual=$generatedOtp") // <--- LOG

        if (inputOtp != generatedOtp) {
            Log.w(TAG, "OTP Verification Failed") // <--- LOG
            _authState.value = AuthState.Error("Invalid OTP Code")
            return
        }

        viewModelScope.launch {
            // Optional: Prevent reusing old password even in reset
            val user = userRepository.getUserByEmail(otpEmail)
            if (user != null && user.password == newPass) {
                Log.w(TAG, "Reset Failed: New password is same as old") // <--- LOG
                _authState.value = AuthState.Error("New password cannot be the same as old")
                return@launch
            }

            // Update Password
            userRepository.updatePasswordByEmail(otpEmail, newPass)
            Log.i(TAG, "Password reset via OTP success for: $otpEmail") // <--- LOG
            _authState.value = AuthState.Success
        }
    }

    fun resetState() {
        // Log.v(TAG, "Resetting Auth State to Idle") // Optional Verbose Log
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
    data class OtpSent(val otp: String) : AuthState()
}