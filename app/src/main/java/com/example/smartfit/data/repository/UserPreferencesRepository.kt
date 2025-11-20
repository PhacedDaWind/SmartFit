package com.example.smartfit.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates a single instance of DataStore for the app
// We use one DataStore named "user_settings" for everything
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    // --- KEYS ---
    private companion object {
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val CURRENT_USER_ID_KEY = intPreferencesKey("current_user_id")
    }

    // --- READ DATA (Flows) ---

    // 1. Theme: Returns true if dark mode is enabled, false otherwise
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE_KEY] ?: false // Default to Light Mode
        }

    // 2. Auth: Returns the currently logged-in User ID (or null if logged out)
    val currentUserId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[CURRENT_USER_ID_KEY]
        }

    // --- WRITE DATA (Functions) ---

    // 1. Save Theme Preference
    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = isDark
        }
    }

    // 2. Save User Session (Call this on Login)
    suspend fun saveCurrentUserId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID_KEY] = id
        }
    }

    // 3. Clear Session (Call this on Logout)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(CURRENT_USER_ID_KEY)
        }
    }
}