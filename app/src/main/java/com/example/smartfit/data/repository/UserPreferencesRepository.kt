package com.example.smartfit.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a single instance of DataStore for the app named "user_settings"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    // --- KEYS ---
    private companion object {
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val CURRENT_USER_ID_KEY = intPreferencesKey("current_user_id")
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

    // --- STEP GOAL (USER SPECIFIC) ---

    // NEW: We ask for the goal of a SPECIFIC userId
    fun getStepGoal(userId: Int): Flow<Int> {
        return context.dataStore.data.map { preferences ->
            // Dynamic Key: "step_goal_1", "step_goal_5", etc.
            val key = intPreferencesKey("step_goal_$userId")
            preferences[key] ?: 2500 // Default to 2500 if not set
        }
    }

    // NEW: We save the goal for a SPECIFIC userId
    suspend fun updateStepGoal(userId: Int, goal: Int) {
        context.dataStore.edit { preferences ->
            val key = intPreferencesKey("step_goal_$userId")
            preferences[key] = goal
        }
    }
}