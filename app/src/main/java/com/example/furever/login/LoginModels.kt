package com.example.furever.login

class LoginModels {
    data class LoginRequest(
        val email: String,
        val pass: String
    )

    data class ApiResponse<T>(
        val success: Boolean,
        val message: String?,
        val data: T?
    )

    data class LoginResponse(
        val token: String,
        val username: String
    )
}