package com.example.smartfit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName="activity_logs")
data class ActivityLog (
    @PrimaryKey(autoGenerate=true)
    val id:Int=0,
    val date: Long= System.currentTimeMillis(),
    val type: String,
    val name: String,
    val values: Double,
    val unit : String
)