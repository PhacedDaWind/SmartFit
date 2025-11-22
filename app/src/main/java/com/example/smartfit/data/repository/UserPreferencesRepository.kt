package com.example.smartfit.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

// Creates a single instance of DataStore for the app named "user_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    // --- KEYS ---
    private companion object {
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val CURRENT_USER_ID_KEY = intPreferencesKey("current_user_id")
        val PROFILE_IMAGE_PATH_KEY = stringPreferencesKey("profile_image_path") // New Key
    }

    // --- READ DATA (Flows) ---

    // 1. Theme
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE_KEY] ?: false
        }

    // 2. Auth: Returns the currently logged-in User ID
    val currentUserId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_USER_ID_KEY]
        }

    // 3. Profile Image Path
    val profileImagePath: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PROFILE_IMAGE_PATH_KEY]
        }

    // --- WRITE DATA (Functions) ---

    // 1. Save Theme
    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark
        }
    }

    // 2. Save User Session
    suspend fun saveCurrentUserId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID_KEY] = id
        }
    }

    // 3. Clear Session
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID_KEY)
        }
    }

    // 4. Save Profile Image (Copies from Gallery to App Storage)
    suspend fun saveProfileImage(uri: Uri) {
        // Create a permanent file in the app's internal storage
        val fileName = "profile_picture.jpg"
        val file = File(context.filesDir, fileName)

        // Copy the data from the Gallery Uri to our new file
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        // Save the absolute path of our new file to DataStore
        context.dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_PATH_KEY] = file.absolutePath
        }
    }

    // --- STEP GOAL (USER SPECIFIC) ---

    // We ask for the goal of a SPECIFIC userId
    fun getStepGoal(userId: Int): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            // Dynamic Key: "step_goal_1", "step_goal_5", etc.
            val key = intPreferencesKey("step_goal_$userId")
            preferences[key] ?: 2500 // Default to 2500 if not set
        }
    }

    // We save the goal for a SPECIFIC userId
    suspend fun updateStepGoal(userId: Int, goal: Int) {
        context.dataStore.edit { preferences ->
            val key = intPreferencesKey("step_goal_$userId")
            preferences[key] = goal
        }
    }
}