package com.example.smartfit.data.repository

import com.example.smartfit.data.local.User
import com.example.smartfit.data.local.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User): Boolean {
        return try {
            userDao.registerUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(username: String, pass: String): User? {
        val user = userDao.getUserByUsername(username)
        if (user != null && user.password == pass) {
            return user
        }
        return null
    }

    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    suspend fun updatePassword(userId: Int, newPassword: String) {
        userDao.updatePassword(userId, newPassword)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    // --- NEW GOAL FUNCTIONS ---

    suspend fun updateStepGoal(userId: Int, goal: Int) {
        userDao.updateStepGoal(userId, goal)
    }

    fun getStepGoalStream(userId: Int): Flow<Int> {
        // If database returns null, default to 0
        return userDao.getStepGoal(userId).map { it ?: 0 }
    }
}