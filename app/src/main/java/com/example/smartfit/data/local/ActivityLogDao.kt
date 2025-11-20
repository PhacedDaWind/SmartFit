package com.example.smartfit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
        /**
         * Inserts a new log or updates an existing one if the primary key already exists.
         * @param log The ActivityLog object to be inserted or updated.
         */
        @Upsert // This is a modern annotation that handles both Insert and Update
        suspend fun upsertLog(log: ActivityLog)

        /**
         * Deletes a specific log from the database.
         * @param log The ActivityLog object to be deleted.
         */
        @Delete
        suspend fun deleteLog(log: ActivityLog)

        /**
         * Retrieves a single activity log by its ID.
         * @param id The primary key of the log to retrieve.
         * @return A Flow emitting the ActivityLog, or null if not found.
         */
        @Query("SELECT * FROM activity_logs WHERE id = :id")
        fun getLogById(id: Int): Flow<ActivityLog?>

        /**
         * Retrieves all activity logs from the database, ordered by date descending (newest first).
         * @return A Flow emitting the list of all ActivityLog objects.
         */
        @Query("SELECT * FROM activity_logs ORDER BY date DESC")
        fun getAllLogs(): Flow<List<ActivityLog>>

}