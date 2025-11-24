package com.example.smartfit.utils

import java.math.BigDecimal
import java.math.RoundingMode

object Calculator {

    // 1. BMI Logic
    fun calculateBMI(weightKg: Double, heightCm: Double): Double {
        if (heightCm <= 0) return 0.0
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        return BigDecimal(bmi).setScale(2, RoundingMode.HALF_UP).toDouble()
    }

    // 2. Heart Rate Logic (Max HR = 220 - Age)
    fun calculateMaxHeartRate(age: Int): Int {
        return 220 - age
    }

    // 3. Calorie Burn Logic (e.g., 0.04 kcal per step)
    fun calculateStepCalories(steps: Int): Double {
        return steps * 0.04
    }
}