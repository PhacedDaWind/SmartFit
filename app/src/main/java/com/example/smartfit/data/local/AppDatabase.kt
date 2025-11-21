package com.example.smartfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main database class for the application.
 * This class defines the database configuration and serves as the main
 * access point for the underlying connection.
 */
@Database(
    // 1. REGISTER ALL ENTITIES HERE
    entities = [ActivityLog::class, User::class],
    // 2. INCREMENT VERSION (Since we added a new table)
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // --- DAOs ---
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun userDao(): UserDao // <--- You must have this abstract function

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartfit_database"
                )
                    // This deletes the old database if the version changes (simplifies development)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}