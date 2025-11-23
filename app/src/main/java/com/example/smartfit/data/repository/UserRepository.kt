package com.example.smartfit.data.repository

import com.example.smartfit.data.local.User
import com.example.smartfit.data.local.UserDao

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

    // --- NEW FUNCTIONS NEEDED FOR SPECIFIC ERROR MESSAGES ---

    // 1. Expose finding a user (so ViewModel can check if they exist)
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }

    // 2. Expose updating password (so ViewModel can call this after checking validity)
    suspend fun updatePassword(userId: Int, newPassword: String) {
        userDao.updatePassword(userId, newPassword)
    }
}