package com.example.handleusers.repository

import com.example.handleusers.models.User
import com.example.handleusers.network.AxumApiClient

class KtorUserRepository(
    serverUrl: String
) : UserRepository {
    private val apiClient = AxumApiClient(serverUrl)

    override suspend fun getUsers(): Result<List<User>> = apiClient.getUsers()

    override suspend fun createUser(name: String, email: String): Result<User> =
        apiClient.createUser(name, email)

    override suspend fun updateUser(
        id: String,
        name: String?,
        email: String?,
        active: Boolean?
    ): Result<User> = apiClient.updateUser(id, name, email, active)

    override suspend fun deleteUser(id: String): Result<Unit> = apiClient.deleteUser(id)

    override suspend fun testConnection(): Boolean = apiClient.testConnection()

    override fun updateServerUrl(serverUrl: String) {
        apiClient.baseUrl = serverUrl.trimEnd('/')
    }

    override fun close() {
        apiClient.close()
    }
}
