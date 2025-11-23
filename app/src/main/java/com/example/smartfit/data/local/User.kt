package com.example.smartfit.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users",
    indices = [Index(value = ["username"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0, // This is the new Primary Key
    val username: String,
    val password: String,
    val stepGoal: Int = 0
)