package com.example.handleusers.network

import com.example.handleusers.models.CreateUserPayload
import com.example.handleusers.models.ErrorResponse
import com.example.handleusers.models.UpdateUserPayload
import com.example.handleusers.models.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AxumApiClient(
    initialBaseUrl: String
) {
    var baseUrl: String = initialBaseUrl.trimEnd('/')

    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }

    private fun buildUrl(path: String): String {
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "${baseUrl.trimEnd('/')}$cleanPath"
    }

    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response: HttpResponse = client.get(buildUrl("/api/v1/users"))
            if (response.status == HttpStatusCode.OK) {
                val users: List<User> = response.body()
                Result.success(users)
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.message ?: "No se pudo conectar al servidor Axum"}"))
        }
    }

    suspend fun getUserById(id: String): Result<User> {
        return try {
            val response: HttpResponse = client.get(buildUrl("/api/v1/users/$id"))
            if (response.status == HttpStatusCode.OK) {
                val user: User = response.body()
                Result.success(user)
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al obtener usuario: ${e.message}"))
        }
    }

    suspend fun createUser(name: String, email: String): Result<User> {
        return try {
            val payload = CreateUserPayload(name = name, email = email)
            val response: HttpResponse = client.post(buildUrl("/api/v1/users")) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                val createdUser: User = response.body()
                Result.success(createdUser)
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al crear usuario: ${e.message}"))
        }
    }

    suspend fun updateUser(id: String, name: String?, email: String?, active: Boolean?): Result<User> {
        return try {
            val payload = UpdateUserPayload(name = name, email = email, active = active)
            val response: HttpResponse = client.put(buildUrl("/api/v1/users/$id")) {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            if (response.status == HttpStatusCode.OK) {
                val updatedUser: User = response.body()
                Result.success(updatedUser)
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al actualizar usuario: ${e.message}"))
        }
    }

    suspend fun deleteUser(id: String): Result<Unit> {
        return try {
            val response: HttpResponse = client.delete(buildUrl("/api/v1/users/$id"))
            if (response.status == HttpStatusCode.NoContent || response.status == HttpStatusCode.OK) {
                Result.success(Unit)
            } else {
                val errorMsg = parseErrorMessage(response)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al eliminar usuario: ${e.message}"))
        }
    }

    suspend fun testConnection(): Boolean {
        return try {
            val response: HttpResponse = client.get(buildUrl("/api/v1/users"))
            response.status == HttpStatusCode.OK
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun parseErrorMessage(response: HttpResponse): String {
        return try {
            val bodyText = response.bodyAsText()
            val errorResponse = jsonConfig.decodeFromString<ErrorResponse>(bodyText)
            errorResponse.error
        } catch (e: Exception) {
            "Error HTTP ${response.status.value}: ${response.status.description}"
        }
    }

    fun close() {
        client.close()
    }
}
