package com.example.furever.network

import com.example.furever.pets.Pet
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<ApiResponse<LoginResponse>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Map<String, Any>>>

    @GET("api/pets")
    suspend fun getAllPets(
        @Header("Authorization") token: String
    ): Response<List<Pet>> // Directly returns a list of pets
}
