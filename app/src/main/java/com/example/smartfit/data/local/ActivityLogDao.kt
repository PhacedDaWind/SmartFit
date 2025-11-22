package com.example.smartfit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
        @Query("SELECT * FROM activity_logs WHERE userId = :userId ORDER BY date DESC")
        fun getLogsForUser(userId: Int): Flow<List<ActivityLog>>

        @Query("""
        SELECT 
            strftime('%Y-%m-%d', date / 1000, 'unixepoch') as day, 
            SUM(`values`) as total
        FROM activity_logs
        WHERE unit = :unit AND userId = :userId 
        GROUP BY day
        ORDER BY day DESC
    """)
        fun getDailySummaryForUser(unit: String, userId: Int): Flow<List<DailySummary>>
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

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND type = :type ORDER BY date DESC")
        fun getLogsForUserByType(userId: Int, type: String): Flow<List<ActivityLog>>

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND type IN ('Cardio', 'Strength') ORDER BY date DESC")
        fun getWorkoutsForUser(userId: Int): Flow<List<ActivityLog>>

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND type = 'Food & Drinks' ORDER BY date DESC")
        fun getFoodLogsForUser(userId: Int): Flow<List<ActivityLog>>

        @Query("""
        SELECT 
            strftime('%Y-%m', date / 1000, 'unixepoch', 'localtime') as month, 
            SUM(`values`) as total
        FROM activity_logs
        WHERE unit = :unit AND userId = :userId
        GROUP BY month
        ORDER BY month DESC
    """)
        fun getMonthlySummaryForUser(unit: String, userId: Int): Flow<List<MonthlySummary>>

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND date >= :startTime")
        fun getLogsAfterTime(userId: Int, startTime: Long): Flow<List<ActivityLog>>
}
