package com.example.handleusers.repository

import com.example.handleusers.models.User

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>

    suspend fun createUser(name: String, email: String): Result<User>

    suspend fun updateUser(
        id: String,
        name: String?,
        email: String?,
        active: Boolean?
    ): Result<User>

    suspend fun deleteUser(id: String): Result<Unit>

    suspend fun testConnection(): Boolean

    fun updateServerUrl(serverUrl: String)

    fun close()
}
