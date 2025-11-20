package com.example.smartfit.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    // This links the 'userId' in this table to the 'userId' in the User table
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"], // The ID column in User.kt
            childColumns = ["userId"],  // The ID column in ActivityLog.kt
            onDelete = ForeignKey.CASCADE // If User is deleted, delete their logs too
        )
    ],
    // Adding an index makes searching for a user's logs much faster
    indices = [Index(value = ["userId"])]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val type: String,
    val name: String,
    val values: Double,
    val unit: String,

    // New Column: The ID of the user who owns this log
    val userId: Int
)