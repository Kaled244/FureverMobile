package com.example.furever.pets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.furever.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

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

        // Mapped to your updated Pet model (pName, pPrice, etc.)
        holder.tvName.text = pet.pName ?: "Unnamed Pet"
        holder.tvPrice.text = if (!pet.pPrice.isNullOrBlank()) "₱${pet.pPrice}" else "Free"
        holder.tvBreed.text = "Breed: ${pet.pBreed ?: "Unknown"}"
        holder.tvAgeGender.text = "Age: ${pet.pAge ?: 0} yrs | Sex: ${pet.pGender ?: "N/A"}"
        holder.tvSpecies.text = pet.pSpecies ?: "Pet"

        // Standardize status display (e.g., "AVAILABLE" -> "Available")
        val displayStatus = pet.pStatus?.lowercase(Locale.ROOT)
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            ?: "Available"
        holder.tvStatus.text = displayStatus

        // Logic for Image URL handling
        val imageUrl = when {
            pet.pImage.isNullOrEmpty() -> "https://placehold.co/400x300?text=No+Photo"
            pet.pImage.startsWith("http") -> pet.pImage
            else -> "https://furever-backend-bn81.onrender.com/uploads/${pet.pImage}"
        }

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.bg_filter_inactive) // Use a local placeholder
            .error(android.R.drawable.ic_menu_report_image)
            .centerCrop()
            .into(holder.ivPetImage)

        // Disable Adopt button if already adopted
        if (pet.pStatus?.lowercase() == "adopted") {
            holder.btnAdopt.isEnabled = false
            holder.btnAdopt.text = "Adopted"
            holder.btnAdopt.alpha = 0.5f
        } else {
            holder.btnAdopt.isEnabled = true
            holder.btnAdopt.text = "Adopt"
            holder.btnAdopt.alpha = 1.0f
        }

        holder.btnAdopt.setOnClickListener { onAdoptClick(pet) }
        holder.itemView.setOnClickListener { onDetailsClick(pet) }
    }

    override fun getItemCount() = pets.size

    fun updatePets(newPets: List<Pet>) {
        pets = newPets
        notifyDataSetChanged()
    }
}