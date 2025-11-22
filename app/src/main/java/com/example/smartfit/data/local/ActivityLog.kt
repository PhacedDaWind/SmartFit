package com.example.smartfit.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val type: String,   // e.g., "Cardio", "Strength"
    val name: String,   // e.g., "Treadmill", "Bench Press"
    val values: Double, // e.g., Distance in km, or Weight in kg
    val unit: String,   // e.g., "km", "kg", "minutes"
    val sets: Int = 0,  // Number of sets
    val reps: Int = 0,  // NEW: Number of reps per set
    val userId: Int
)