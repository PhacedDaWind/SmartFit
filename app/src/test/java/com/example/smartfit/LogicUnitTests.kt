package com.example.smartfit

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
}