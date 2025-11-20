package com.example.smartfit.data

import com.example.smartfit.data.local.ActivityLog
import com.example.smartfit.data.local.ActivityLogDao
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing ActivityLog data.
 * This class abstracts the data source (the Room DAO) from the rest of the app.
 *
 * @param activityLogDao The Data Access Object for activity logs.
 */
class ActivityRepository(
    private val activityLogDao: ActivityLogDao
) {

    /**
     * Retrieves all logs as a reactive stream (Flow).
     * The UI layer will observe this Flow for updates.
     */
    fun getAllLogs(): Flow<List<ActivityLog>> {
        return activityLogDao.getAllLogs()
    }

    /**
     * Retrieves a single log by its ID as a reactive stream (Flow).
     *
     * @param id The ID of the log to fetch.
     */
    fun getLogById(id: Int): Flow<ActivityLog?> {
        return activityLogDao.getLogById(id)
    }

    /**
     * Inserts or updates a log in the database.
     * This is a suspend function, so it must be called from a coroutine.
     *
     * @param log The ActivityLog to be upserted.
     */
    suspend fun upsertLog(log: ActivityLog) {
        activityLogDao.upsertLog(log)
    }

    /**
     * Deletes a log from the database.
     * This is a suspend function, so it must be called from a coroutine.
     *
     * @param log The ActivityLog to be deleted.
     */
    suspend fun deleteLog(log: ActivityLog) {
        activityLogDao.deleteLog(log)
    }
}