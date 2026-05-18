package com.example.furever.network

import com.example.furever.application.ApplicationRequest
import com.example.furever.application.ApplicationResponse
import com.example.furever.pets.Pet
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<ApiResponse<LoginResponse>>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<Map<String, Any>>>

    @GET("api/pets")
    suspend fun getAllPets(
        @Header("Authorization") token: String
    ): Response<List<Pet>>

    @POST("api/applications/submit")
    suspend fun submitApplication(
        @Header("Authorization") token: String,
        @Body request: ApplicationRequest
    ): Response<ApiResponse<ApplicationResponse>>

    @GET("api/applications/my-submissions")
    suspend fun getMyApplications(
        @Header("Authorization") token: String
    ): Response<ApiResponse<List<ApplicationResponse>>> // Correctly wrapped in ApiResponse

    @PUT("api/applications/{id}/status")
    suspend fun updateApplicationStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body statusUpdate: Map<String, String>
    ): Response<ApiResponse<ApplicationResponse>>
}
