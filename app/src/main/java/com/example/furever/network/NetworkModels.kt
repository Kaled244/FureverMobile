package com.example.furever.network

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val data: T?,
    val message: String?,
    val status: Int?
) {
    // Convenience property since your backend uses status 200 for success
    val success: Boolean get() = status == 200
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val role: String,
    val username: String,
    @SerializedName("avatar_url")
    val avatarUrl: String? = null
)

data class RegisterRequest(
    val name: String?,
    @SerializedName("l_name")
    val lName: String?,
    val username: String,
    val password: String,
    val email: String,
    val address: String?
)
