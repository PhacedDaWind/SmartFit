package com.example.smartfit

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.smartfit.ui.auth.LoginScreen
import com.example.smartfit.ui.auth.RegisterScreen
import com.example.smartfit.ui.profile.ProfileScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppUiTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- TEST 1: Login Screen UI ---
    @Test
    fun loginScreen_displaysCorrectContent() {
        // 1. Launch the Login Screen
        composeTestRule.setContent {
            LoginScreen(
                onLoginSuccess = {},
                onNavigateToRegister = {},
                onNavigateToForgot = {}
            )
        }

        // 2. Check for the NEW Title "Welcome Back!"
        // (This was failing because it used to look for "SmartFit Login")
        composeTestRule.onNodeWithText("Welcome Back!").assertIsDisplayed()

        // 3. Check for the Subtitle
        composeTestRule.onNodeWithText("Sign in to continue to SmartFit").assertIsDisplayed()

        // 4. Check if the Login button exists
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
    }

    // --- TEST 2: Register Screen UI (NEW) ---
    @Test
    fun registerScreen_displaysCorrectContent() {
        composeTestRule.setContent {
            RegisterScreen(
                onRegisterSuccess = {},
                onNavigateBack = {}
            )
        }

        // Check for the Title "Create Account"
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()

        // Check for the Button "Sign Up"
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
    }

    // --- TEST 3: Profile Screen UI ---
    @Test
    fun profileScreen_showsSettingsAndLogout() {
        composeTestRule.setContent {
            ProfileScreen(onLogout = {})
        }

        // Check if "App Settings" header exists
        composeTestRule.onNodeWithText("App Settings").assertIsDisplayed()

        // Check if "Log Out" button exists
        composeTestRule.onNodeWithText("Log Out").assertIsDisplayed()
    }
}