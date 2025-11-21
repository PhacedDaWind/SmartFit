package com.example.smartfit // Make sure this package is correct

import android.app.Application
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.UserRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.data.local.AppDatabase
import com.example.smartfit.data.repository.StepSensorRepository

class SmartFitApplication : Application() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val repository by lazy { ActivityRepository(database.activityLogDao()) }

    val userRepository by lazy { UserRepository(database.userDao()) }

    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    val stepSensorRepository by lazy { StepSensorRepository(this) }

    override fun onCreate() {
        super.onCreate()
        // Force database open for App Inspection
        database.openHelper.writableDatabase
    }
}