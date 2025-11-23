package com.example.smartfit.data.repository

import com.example.smartfit.data.local.ActivityLog
import com.example.smartfit.data.local.ActivityLogDao
import com.example.smartfit.data.local.DailyStep
import com.example.smartfit.data.local.DailySummary
import com.example.smartfit.data.local.MonthlySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class ActivityRepository(
    private val activityLogDao: ActivityLogDao
) {
    // --- ACTIVITY LOGS (Workouts) ---
    fun getLogsForUser(userId: Int): Flow<List<ActivityLog>> = activityLogDao.getLogsForUser(userId)
    fun getLogById(id: Int): Flow<ActivityLog?> = activityLogDao.getLogById(id)
    suspend fun upsertLog(log: ActivityLog) = activityLogDao.upsertLog(log)
    suspend fun deleteLog(log: ActivityLog) = activityLogDao.deleteLog(log)

    fun getWorkouts(userId: Int) = activityLogDao.getWorkoutsForUser(userId)
    fun getFoodLogs(userId: Int) = activityLogDao.getFoodLogsForUser(userId)

    // --- MISSING FUNCTION (Add this!) ---
    fun getLogsByType(userId: Int, type: String): Flow<List<ActivityLog>> {
        return activityLogDao.getLogsForUserByType(userId, type)
    }

    // Gets logs (workouts/food) for today
    fun getLogsFromDate(userId: Int, startTime: Long): Flow<List<ActivityLog>> {
        return activityLogDao.getLogsAfterTime(userId, startTime)
    }

    // --- STEPS LOGIC (Separate Table) ---

    // 1. Get Steps for Home Screen (Returns Flow<Int>)
    fun getStepsFromDate(userId: Int, startTime: Long): Flow<Int> {
        return activityLogDao.getStepsSinceDate(userId, startTime).map { it ?: 0 }
    }

    // 2. Add Steps (Saves to 'daily_steps' ONLY)
    suspend fun addStepsToToday(userId: Int, newStepsToAdd: Int) {
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val existingEntry = activityLogDao.getDailyStep(userId, midnight)

        if (existingEntry != null) {
            val updatedEntry = existingEntry.copy(
                stepCount = existingEntry.stepCount + newStepsToAdd
            )
            activityLogDao.upsertDailyStep(updatedEntry)
        } else {
            val newEntry = DailyStep(
                userId = userId,
                date = midnight,
                stepCount = newStepsToAdd
            )
            activityLogDao.upsertDailyStep(newEntry)
        }
    }

    // --- SUMMARIES ---
    fun getDailySummary(unit: String, userId: Int): Flow<List<DailySummary>> {
        return activityLogDao.getDailySummaryForUser(unit, userId)
    }
    fun getMonthlySummary(unit: String, userId: Int): Flow<List<MonthlySummary>> {
        return activityLogDao.getMonthlySummaryForUser(unit, userId)
    }

    fun getLogsBetween(userId: Int, start: Long, end: Long) =
        activityLogDao.getLogsBetween(userId, start, end)

    fun getStepsBetween(userId: Int, start: Long, end: Long) =
        activityLogDao.getStepsBetween(userId, start, end).map { it ?: 0 }
}