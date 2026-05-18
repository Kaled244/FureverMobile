package com.example.furever.user

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("user_id", alternate = ["userId", "id"])
    val userId: Int? = null,
    val name: String? = null,
    val username: String? = null,
    val email: String,
    val address: String? = null
)

data class RegisterRequest(
    val name: String?,
    val username: String?,
    val email: String,
    val address: String?
)
