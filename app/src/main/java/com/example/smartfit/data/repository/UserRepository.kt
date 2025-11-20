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

    // Returns the User object if login succeeds, null otherwise
    suspend fun login(username: String, pass: String): User? {
        val user = userDao.getUserByUsername(username)
        if (user != null && user.password == pass) {
            return user
        }
        return null
    }

    suspend fun changePassword(username: String, currentPass: String, newPass: String): Boolean {
        val user = userDao.getUserByUsername(username)
        if (user != null && user.password == currentPass) {
            userDao.updatePassword(username, newPass)
            return true
        }
        return false
    }
}