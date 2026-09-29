package com.example.handleusers.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val active: Boolean
)

@Serializable
data class CreateUserPayload(
    val name: String,
    val email: String
)

@Serializable
data class UpdateUserPayload(
    val name: String? = null,
    val email: String? = null,
    val active: Boolean? = null
)

@Serializable
data class ErrorResponse(
    val error: String
)
