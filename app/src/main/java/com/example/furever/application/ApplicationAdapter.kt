package com.example.furever.application

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.furever.R
import java.util.Locale

class ApplicationAdapter(
    private var applications: List<ApplicationResponse>,
    private val onClick: (ApplicationResponse) -> Unit
) : RecyclerView.Adapter<ApplicationAdapter.ApplicationViewHolder>() {

    class ApplicationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPetImage: ImageView = view.findViewById(R.id.iv_app_pet_image)
        val tvPetName: TextView = view.findViewById(R.id.tv_app_pet_name)
        val tvDate: TextView = view.findViewById(R.id.tv_app_date)
        val tvStatus: TextView = view.findViewById(R.id.tv_app_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_application_card, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        val app = applications[position]

        // FIX: Changed appPet to pet (Matches updated ApplicationResponse)
        // FIX: Pet model uses pName
        holder.tvPetName.text = app.pet?.pName ?: "Unknown Pet"

        // Format the date (Backend sends 2024-05-18T...)
        val dateOnly = app.appDate?.split("T")?.get(0) ?: "N/A"
        holder.tvDate.text = "Submitted: $dateOnly"

        // FIX: Changed appStatus to status
        val status = app.status?.uppercase(Locale.ROOT) ?: "PENDING"
        holder.tvStatus.text = status

        // Dynamic Background based on Status
        val statusBg = when (status) {
            "APPROVED", "ADOPTED", "READY_TO_CLAIM" -> R.drawable.bg_filter_active
            "REJECTED" -> R.drawable.bg_badge_charcoal // Assuming you have this
            else -> R.drawable.bg_badge_yellow // Assuming you have this
        }

        holder.tvStatus.setBackgroundResource(statusBg)

        // Text color for better contrast
        holder.tvStatus.setTextColor(if (status == "PENDING") Color.parseColor("#3A2525") else Color.WHITE)

        // FIX: Changed appPet to pet
        // FIX: Pet model uses pImage
        val imageUrl = app.pet?.pImage ?: "https://placehold.co/400x300?text=No+Photo"

        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(holder.ivPetImage)

        holder.itemView.setOnClickListener { onClick(app) }
    }

    override fun getItemCount() = applications.size

    fun updateApplications(newApps: List<ApplicationResponse>) {
        applications = newApps
        notifyDataSetChanged()
    }
}