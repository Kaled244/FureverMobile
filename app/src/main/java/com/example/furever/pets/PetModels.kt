package com.example.furever.pets

import com.google.gson.annotations.SerializedName

data class Pet(
    @SerializedName("user_id") // Backend uses user_id for the owner/relation potentially? 
    val userId: Int? = null,
    
    @SerializedName("pet_id")
    val pId: Int? = null,

    // Matching backend Java Entity names (name, price, image, etc.)
    // and supporting the "p" prefix from your React code just in case
    @SerializedName("name", alternate = ["pName"])
    val pName: String?,
    
    @SerializedName("species", alternate = ["pSpecies"])
    val pSpecies: String?,
    
    @SerializedName("breed", alternate = ["pBreed"])
    val pBreed: String?,
    
    @SerializedName("age", alternate = ["pAge"])
    val pAge: Int?,
    
    @SerializedName("gender", alternate = ["pGender"])
    val pGender: String?,
    
    @SerializedName("description", alternate = ["pDescription"])
    val pDescription: String?,
    
    @SerializedName("status", alternate = ["pStatus"])
    val pStatus: String?,
    
    @SerializedName("price", alternate = ["pPrice"])
    val pPrice: String?, // Backend sends price as String in the addPet method
    
    @SerializedName("image", alternate = ["pImage"])
    val pImage: String?,

    val healthRecords: List<HealthRecord>? = null
)

data class HealthRecord(
    val vacType: String,
    val vacDate: String
)
