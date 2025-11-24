package com.example.smartfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    // FIX: Add DailyStep::class to this list so Room knows to create the table
    entities = [ActivityLog::class, User::class, DailyStep::class, ChatMessageEntity::class],
    version = 11, // Increment this version number (e.g., from 6 to 7)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // --- DAOs ---
    abstract fun activityLogDao(): ActivityLogDao
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao

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
                    // This allows the app to delete/recreate the DB when you change version
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}