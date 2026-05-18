package com.example.furever.application

import com.example.furever.pets.Pet
import com.example.furever.user.User
import com.google.gson.annotations.SerializedName

enum class ExperienceLevel {
    @SerializedName("FIRST_TIMER") FIRST_TIMER,
    @SerializedName("INTERMEDIATE") INTERMEDIATE,
    @SerializedName("PRO_PARENTING") PRO_PARENTING
}

// MATCHES YOUR JAVA RECORD: ApplicationRequest
// Used when SUBMITTING an application
data class ApplicationRequest(
    @SerializedName("petId") val petId: Int,
    @SerializedName("appContact") val appContact: String,
    @SerializedName("appHomeType") val appHomeType: String,

    // Changed to String to resolve Fragment Type Mismatch
    // and match Spring Boot Enum mapping
    @SerializedName("appExperience") val appExperience: String,

    @SerializedName("appNewpetname") val appNewpetname: String,
    @SerializedName("appAnswer") val appAnswer: String
)

// MATCHES YOUR JAVA ENTITY: ApplicationEntity
// Used when RETRIEVING applications (My Submissions)
data class ApplicationResponse(
    @SerializedName("id") // Java field name is 'id'
    val id: Int? = null,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("pet")
    val pet: Pet? = null,

    @SerializedName("contactNumber") // Matches Java: String contactNumber
    val contactNumber: String? = null,

    @SerializedName("homeType") // Matches Java: String homeType
    val homeType: String? = null,

    @SerializedName("experience") // Matches Java: ExperienceLevel experience
    val experience: ExperienceLevel? = null,

    @SerializedName("newPetName") // Matches Java: String newPetName
    val newPetName: String? = null,

    @SerializedName("answers") // Matches Java: String answers
    val answers: String? = null,

    @SerializedName("status") // Matches Java: String status
    val status: String? = "PENDING",

    @SerializedName("appDate") // Matches Java: LocalDateTime appDate
    val appDate: String? = null
)