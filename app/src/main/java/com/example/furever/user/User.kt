package com.example.furever.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("user_id")
    val userId: Int? = null,
    val name: String?,
    val username: String?,
    val email: String,
    val address: String?
)

@Serializable
data class RegisterRequest(
    val name: String?,
    val username: String?,
    val email: String,
    val address: String?
)
