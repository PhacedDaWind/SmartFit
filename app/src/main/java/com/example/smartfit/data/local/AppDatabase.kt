package com.example.smartfit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main database class for the application.
 * This class defines the database configuration and serves as the main
 * access point for the underlying connection.
 *
 * [entities] - Lists all the @Entity classes that are part of this database.
 * [version] - The database version. Must be incremented if you change the schema.
 * [exportSchema] - true by default, saves the schema to a folder.
 */
@Database(
    entities = [ActivityLog::class],
    version = 1,
    exportSchema = false // You can set this to true for production apps
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract function that returns the Data Access Object (DAO) for activity logs.
     * Room will auto-implement this.
     */
    abstract fun activityLogDao(): ActivityLogDao

    /**
     * Companion object to provide a singleton instance of the database.
     * This prevents having multiple instances of the database open at the same time.
     */
    companion object {

        // @Volatile ensures that the value of INSTANCE is always up-to-date
        // and the same for all execution threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton database instance.
         * If the instance doesn't exist, it creates one in a thread-safe way.
         *
         * @param context The application context.
         * @return The singleton AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Return the instance if it already exists
            return INSTANCE ?: synchronized(this) {
                // If instance is null, create the database
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartfit_database" // This is the file name of your database
                )
                    // .fallbackToDestructiveMigration() // Use this only during development
                    .build()

                INSTANCE = instance
                // Return the newly created instance
                instance
            }
        }
    }
}