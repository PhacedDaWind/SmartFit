package com.example.smartfit // Make sure this package is correct

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.smartfit.data.repository.ActivityRepository
import com.example.smartfit.data.repository.UserRepository
import com.example.smartfit.data.repository.UserPreferencesRepository
import com.example.smartfit.data.local.AppDatabase
import com.example.smartfit.data.repository.ChatRepository
import com.example.smartfit.data.repository.StepSensorRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class SmartFitApplication : Application(), ImageLoaderFactory {

    private val database by lazy { AppDatabase.getDatabase(this) }

    val repository by lazy { ActivityRepository(database.activityLogDao()) }

    val userRepository by lazy { UserRepository(database.userDao()) }

    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }

    val stepSensorRepository by lazy { StepSensorRepository(this) }

    val chatRepository by lazy { ChatRepository(database.chatDao()) }

    override fun onCreate() {
        super.onCreate()
        // Force database open for App Inspection
        database.openHelper.writableDatabase
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                // 3. Create a custom network client with longer timeouts
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS) // Wait 30s to connect
                    .readTimeout(30, TimeUnit.SECONDS)    // Wait 30s for data
                    .build()
            }
            .crossfade(true) // Enable smooth fade-in animation for all images
            .build()
    }
}