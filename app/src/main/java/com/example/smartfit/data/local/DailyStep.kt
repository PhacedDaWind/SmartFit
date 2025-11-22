package com.example.smartfit.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_steps",
    // Link to User table (same as ActivityLog)
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Index userId to quickly load a specific user's step history
    indices = [Index(value = ["userId"])]
)
data class DailyStep(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long = System.currentTimeMillis(), // Store the date of the step count
    val stepCount: Int,     // The actual number of steps (e.g., 5000, 10000)
    val caloriesBurned: Double = 0.0, // Optional: calculated based on steps
    val userId: Int
)