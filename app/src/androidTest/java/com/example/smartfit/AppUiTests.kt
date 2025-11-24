package com.example.smartfit

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import com.example.smartfit.ui.addedit.AddEditScreen
import com.example.smartfit.ui.auth.LoginScreen
import com.example.smartfit.ui.auth.RegisterScreen
import com.example.smartfit.ui.navigation.AppNavigation
import com.example.smartfit.ui.profile.ProfileScreen
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysCorrectContent() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onNavigateToRegister = {}, onNavigateToForgot = {})
        }
        composeTestRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Login").assertIsDisplayed()
        Thread.sleep(2000)
    }

    @Test
    fun registerScreen_displaysCorrectContent() {
        composeTestRule.setContent {
            RegisterScreen(onRegisterSuccess = {}, onNavigateBack = {})
        }
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()

        Thread.sleep(2000)
    }

    @Test
    fun profileScreen_displaysContent() {
        composeTestRule.setContent {
            ProfileScreen(onLogout = {})
        }
        // Matches "App Settings" or "Preferences" depending on your version
        composeTestRule.onNodeWithText("Dark Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log Out").assertIsDisplayed()

        Thread.sleep(2000)
    }

    @Test
    fun addEditScreen_displaysFormElements() {
        composeTestRule.setContent {
            AddEditScreen(onNavigateUp = {})
        }
        composeTestRule.onNodeWithText("New Entry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Workout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Save").assertIsDisplayed()

        Thread.sleep(2000)
    }


    @Test
    fun fullAppFlow_CompleteJourney_WithPasswordChange() {
        lateinit var navController: TestNavHostController

        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavigation(navController = navController)
        }

        val uniqueUser = "User${System.currentTimeMillis()}"
        // 1. NEW: Generate a unique email for the test
        val uniqueEmail = "test${System.currentTimeMillis()}@example.com"
        val password = "123456"
        val wrongPassword = "wrongpass"
        val newPassword = "newpassword123"

        // ==========================================
        // 1. REGISTER & LOGIN
        // ==========================================
        composeTestRule.onNodeWithText("New here? Create Account").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("Username").performTextInput(uniqueUser)

        // 2. NEW: Fill in the Email Field
        // (Assumes your label is "Email Address" or "Email")
        composeTestRule.onNodeWithText("Email Address").performTextInput(uniqueEmail)

        composeTestRule.onNodeWithText("Password").performTextInput(password)
        composeTestRule.onNodeWithText("Sign Up").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("Username").performTextClearance()
        composeTestRule.onNodeWithText("Username").performTextInput(uniqueUser)
        composeTestRule.onNodeWithText("Password").performTextClearance()
        composeTestRule.onNodeWithText("Password").performTextInput(password)
        composeTestRule.onNodeWithText("Login").performClick()

        // Wait for Dashboard
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Dashboard").fetchSemanticsNodes().isNotEmpty()
        }
        Thread.sleep(2000)

        // ==========================================
        // 2. NAVIGATE TO LOGS (ADD & EDIT)
        // ==========================================
        composeTestRule.onNodeWithText("Cardio", substring = true).performScrollTo().performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // --- ADD ---
        composeTestRule.onNodeWithContentDescription("Add new log").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Exercise Name", substring = true)
            .performTextInput("Morning Run")
        composeTestRule.onNodeWithText("Duration", substring = true).performTextInput("30")

        composeTestRule.onNodeWithContentDescription("Save").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Verify Added
        composeTestRule.onNodeWithText("Morning Run").assertIsDisplayed()

        // --- EDIT ---
        composeTestRule.onNodeWithText("Morning Run").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Exercise Name", substring = true).performTextClearance()
        composeTestRule.onNodeWithText("Exercise Name", substring = true)
            .performTextInput("Evening Run")

        composeTestRule.onNodeWithContentDescription("Save").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Verify Edit
        composeTestRule.onNodeWithText("Evening Run").assertIsDisplayed()

        // ==========================================
        // 3. NAVIGATE TO TIPS (FIXED HERE)
        // ==========================================
        composeTestRule.onNodeWithText("Tips").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("SmartCoach AI").assertIsDisplayed()

        composeTestRule.onNodeWithText("Message...", substring = true).performTextInput("Hello AI")
        composeTestRule.onNodeWithContentDescription("Send").performClick()

        Thread.sleep(4000)

        // ==========================================
        // 4. NAVIGATE TO PROFILE & LOG OUT
        // ==========================================
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNodeWithText("App Settings").assertIsDisplayed()

        composeTestRule.onNodeWithText("Log Out").performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Verify Logout
        composeTestRule.onNodeWithText("Welcome Back!").assertIsDisplayed()
    }
}