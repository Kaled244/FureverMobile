package com.example.furever.pets

import com.google.gson.annotations.SerializedName

data class Pet(
    // The logs show the backend sends "pId", "pName", etc.
    // We must match those exactly.
    @SerializedName("pId", alternate = ["id", "petId"])
    val pId: Int? = null,

    @SerializedName("pName")
    val pName: String? = null,

    @SerializedName("pSpecies")
    val pSpecies: String? = null,

    @SerializedName("pBreed")
    val pBreed: String? = null,

    @SerializedName("pAge")
    val pAge: Int? = null,

    @SerializedName("pGender")
    val pGender: String? = null,

    @SerializedName("pDescription")
    val pDescription: String? = null,

    @SerializedName("pStatus") // Changed from "status" to "pStatus"
    val pStatus: String? = null,

    @SerializedName("pPrice")
    val pPrice: String? = null,

    @SerializedName("pImage") // Changed from "image" to "pImage"
    val pImage: String? = null,

    val healthRecords: List<HealthRecord>? = null
)

data class HealthRecord(
    // Check your logs: backend uses healthId, vacType, vacDate
    val healthId: Int? = null,
    val vacType: String? = null,
    val vacDate: String? = null
)