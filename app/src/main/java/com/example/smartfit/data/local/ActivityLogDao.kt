package com.example.smartfit.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
        // --- SECTION 1: ACTIVITY LOGS (Workouts & Food Only) ---

        @Query("SELECT * FROM activity_logs WHERE userId = :userId ORDER BY date DESC")
        fun getLogsForUser(userId: Int): Flow<List<ActivityLog>>

        @Query("SELECT * FROM activity_logs WHERE id = :id")
        fun getLogById(id: Int): Flow<ActivityLog?>

        @Upsert
        suspend fun upsertLog(log: ActivityLog)

        @Delete
        suspend fun deleteLog(log: ActivityLog)

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND type = :type ORDER BY date DESC")
        fun getLogsForUserByType(userId: Int, type: String): Flow<List<ActivityLog>>

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND type IN ('Cardio', 'Strength') ORDER BY date DESC")
        fun getWorkoutsForUser(userId: Int): Flow<List<ActivityLog>>

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND type = 'Food & Drinks' ORDER BY date DESC")
        fun getFoodLogsForUser(userId: Int): Flow<List<ActivityLog>>

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND date >= :startTime")
        fun getLogsAfterTime(userId: Int, startTime: Long): Flow<List<ActivityLog>>


        // --- SECTION 2: DAILY STEPS (New Separate Table) ---

        // Check if a step entry exists for a specific day
        @Query("SELECT * FROM daily_steps WHERE userId = :userId AND date = :date LIMIT 1")
        suspend fun getDailyStep(userId: Int, date: Long): DailyStep?

        // Insert or Update the step count
        @Upsert
        suspend fun upsertDailyStep(dailyStep: DailyStep)

        // Get total steps starting from a specific date (used for Daily/Monthly view)
        @Query("SELECT SUM(stepCount) FROM daily_steps WHERE userId = :userId AND date >= :startTime")
        fun getStepsSinceDate(userId: Int, startTime: Long): Flow<Int?>


        // --- SECTION 3: SUMMARIES (Unchanged) ---
        @Query("""
        SELECT 
            strftime('%Y-%m-%d', date / 1000, 'unixepoch') as day, 
            SUM(`values`) as total
        FROM activity_logs
        WHERE unit = :unit AND userId = :userId 
        GROUP BY day ORDER BY day DESC
    """)
        fun getDailySummaryForUser(unit: String, userId: Int): Flow<List<DailySummary>>

        @Query("""
        SELECT 
            strftime('%Y-%m', date / 1000, 'unixepoch', 'localtime') as month, 
            SUM(`values`) as total
        FROM activity_logs
        WHERE unit = :unit AND userId = :userId
        GROUP BY month ORDER BY month DESC
    """)
        fun getMonthlySummaryForUser(unit: String, userId: Int): Flow<List<MonthlySummary>>

        @Query("SELECT * FROM activity_logs WHERE userId = :userId AND date >= :startDate AND date < :endDate ORDER BY date DESC")
        fun getLogsBetween(userId: Int, startDate: Long, endDate: Long): Flow<List<ActivityLog>>

        // NEW: Get steps between two dates
        @Query("SELECT SUM(stepCount) FROM daily_steps WHERE userId = :userId AND date >= :startDate AND date < :endDate")
        fun getStepsBetween(userId: Int, startDate: Long, endDate: Long): Flow<Int?>
}
