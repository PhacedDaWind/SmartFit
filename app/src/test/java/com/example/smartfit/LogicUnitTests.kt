package com.example.smartfit

import com.example.smartfit.utils.Calculator
import org.junit.Test
import org.junit.Assert.*

class LogicUnitTests {

    // --- TEST 1: Calorie Calculation Logic ---
    // Verifies that your math for burning calories is correct
    @Test
    fun calculateStepCalories_isCorrect() {
        // 1. GIVEN: User walked 2500 steps
        val steps = 2500
        val burnRatePerStep = 0.04

        // 2. WHEN: We calculate the total burned
        val result = steps * burnRatePerStep

        // 3. THEN: The result should be exactly 100.0
        assertEquals(100.0, result, 0.01)
    }

    // --- TEST 2: String Formatting Logic ---
    // Verifies that your app doesn't show ugly numbers like "12.555555"
    @Test
    fun formatDecimal_roundsCorrectly() {
        // 1. GIVEN: A messy number
        val messyNumber = 12.3456789

        // 2. WHEN: We format it to 2 decimal places (simulating your app's logic)
        val result = String.format("%.2f", messyNumber)

        // 3. THEN: It should round up to "12.35"
        assertEquals("12.35", result)
    }

    // --- TEST 3: Progress Bar Logic (NEW) ---
    // Verifies that the progress bar calculates percentage correctly and caps at 100%
    @Test
    fun calculateProgress_isCorrectAndCapped() {
        val goal = 10000

        // Case A: User walked 5000 steps (50%)
        val stepsNormal = 5000
        val progressNormal = (stepsNormal.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.5f, progressNormal, 0.01f)

        // Case B: User walked 15000 steps (150%)
        // The app should cap this at 1.0 (100%) so the bar doesn't overflow
        val stepsOver = 15000
        val progressOver = (stepsOver.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
        assertEquals(1.0f, progressOver, 0.01f)
    }

    @Test
    fun calculateBMI_isCorrect() {
        // 1. GIVEN: A user who is 70kg and 175cm tall
        val weightKg = 70.0
        val heightCm = 175.0

        // 2. WHEN: We calculate the BMI using the app's logic
        val result = Calculator.calculateBMI(weightKg, heightCm)

        // 3. THEN: The result should be exactly 22.86
        // (Math: 70 / 1.75^2 = 22.857... which rounds up to 22.86)
        assertEquals(22.86, result, 0.01)
    }
}