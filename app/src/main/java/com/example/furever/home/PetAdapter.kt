package com.example.furever.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.furever.R
import com.example.furever.pets.Pet
import com.google.android.material.button.MaterialButton

class PetAdapter(
    private var pets: List<Pet>,
    private val onAdoptClick: (Pet) -> Unit,
    private val onDetailsClick: (Pet) -> Unit
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPetImage: ImageView = view.findViewById(R.id.iv_pet_image)
        val tvSpecies: TextView = view.findViewById(R.id.tv_species_badge)
        val tvStatus: TextView = view.findViewById(R.id.tv_status_badge)
        val tvName: TextView = view.findViewById(R.id.tv_pet_name)
        val tvPrice: TextView = view.findViewById(R.id.tv_pet_price)
        val tvBreed: TextView = view.findViewById(R.id.tv_pet_breed)
        val tvAgeGender: TextView = view.findViewById(R.id.tv_pet_age_gender)
        val btnAdopt: MaterialButton = view.findViewById(R.id.btn_adopt)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet_card, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        
        holder.tvName.text = pet.pName
        holder.tvPrice.text = "$${pet.pPrice}"
        holder.tvBreed.text = "Breed : ${pet.pBreed}"
        holder.tvAgeGender.text = "Age : ${pet.pAge} yrs | Sex : ${pet.pGender}"
        holder.tvSpecies.text = pet.pSpecies
        holder.tvStatus.text = pet.pStatus

        val baseUrl = "https://furever-backend-bn81.onrender.com"
        val imageUrl = when {
            pet.pImage.isNullOrEmpty() -> "https://placehold.co/400x300?text=Pet+Photo"
            pet.pImage.startsWith("http") -> pet.pImage
            pet.pImage.startsWith("/api/") -> "$baseUrl${pet.pImage}"
            else -> "$baseUrl/uploads/${pet.pImage}"
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.ivPetImage)

        holder.btnAdopt.setOnClickListener { onAdoptClick(pet) }
        holder.itemView.setOnClickListener { onDetailsClick(pet) }
    }

    override fun getItemCount() = pets.size

    fun updatePets(newPets: List<Pet>) {
        pets = newPets
        notifyDataSetChanged()
    }
}
