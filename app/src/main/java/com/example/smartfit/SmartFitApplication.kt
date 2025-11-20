package com.example.smartfit // Make sure this package is correct

import android.app.Application
import com.example.smartfit.data.ActivityRepository
import com.example.smartfit.data.local.AppDatabase
import com.example.smartfit.data.repository.UserPreferencesRepository

/**
 * The Application class for SmartFit.
 * This class is the first thing that runs when your app starts.
 * We use it to create and hold the single instance of our database
 * and repository (this is our manual dependency injection).
 */
class SmartFitApplication : Application() {

    // Lazily create the database. It won't be built until it's first needed.
    private val database by lazy { AppDatabase.getDatabase(this) }

    // Lazily create the repository using the database's DAO.
    // All ViewModels will share this single repository instance.
    val repository by lazy { ActivityRepository(database.activityLogDao()) }

    val userPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }
}