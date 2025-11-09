package com.example.smartfit

import android.app.Application
import com.example.smartfit.data.repository.UserPreferencesRepository

class SmartFitApplication : Application() {

    // We create the repository once when the app starts
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        // Initialize the repository
        userPreferencesRepository = UserPreferencesRepository(this)
    }
}