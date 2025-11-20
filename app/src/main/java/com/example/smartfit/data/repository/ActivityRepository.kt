package com.example.smartfit.data.repository

import com.example.smartfit.data.local.ActivityLog
import com.example.smartfit.data.local.ActivityLogDao
import com.example.smartfit.data.local.DailySummary
import kotlinx.coroutines.flow.Flow

class ActivityRepository(
    private val activityLogDao: ActivityLogDao
) {
    fun getLogsForUser(userId: Int): Flow<List<ActivityLog>> = activityLogDao.getLogsForUser(userId)
    fun getLogById(id: Int): Flow<ActivityLog?> = activityLogDao.getLogById(id)
    suspend fun upsertLog(log: ActivityLog) = activityLogDao.upsertLog(log)
    suspend fun deleteLog(log: ActivityLog) = activityLogDao.deleteLog(log)

    fun getDailySummary(unit: String, userId: Int): Flow<List<DailySummary>> {
        return activityLogDao.getDailySummaryForUser(unit, userId)
    }
}