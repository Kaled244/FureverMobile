package com.example.furever.pets

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.furever.R
import com.example.furever.application.ApplicationRequest
import com.example.furever.application.ExperienceLevel
import com.example.furever.network.ApiResponse
import com.example.furever.network.RetrofitClient
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.util.Locale

class PetFragment : Fragment() {

    private lateinit var rvPets: RecyclerView
    private lateinit var adapter: PetAdapter
    private var allPetsList: List<Pet> = emptyList()

    private var currentSpeciesFilter = "All Pets"
    private var currentBreedFilter = "All Breeds"

    private val speciesData = mapOf(
        "Dog" to listOf("Golden Ret", "German Shep", "Beagle", "Poodle", "Bulldog", "Labrador", "Pug", "Husky", "Aspin", "Maltese", "Shitzu"),
        "Cat" to listOf("Persian", "Main Coon", "Siamese", "British Shorthair", "Bengal", "Rogdoll", "Puspin", "Tabby"),
        "Rabbit" to listOf("Dutch", "Lionhead", "Rex", "Netherland Dwarf"),
        "Bird" to listOf("Parrot", "Canary", "Lovebird", "Cockatiel", "African Grey")
    )

    private lateinit var filterAll: TextView
    private lateinit var filterSpecies: TextView
    private lateinit var filterBreed: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_pet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvPets = view.findViewById(R.id.rv_pets)
        filterAll = view.findViewById(R.id.filter_all)
        filterSpecies = view.findViewById(R.id.filter_species)
        filterBreed = view.findViewById(R.id.filter_breed)

        adapter = PetAdapter(emptyList(),
            onAdoptClick = { pet -> showAdoptionForm(pet) },
            onDetailsClick = { pet ->
                Toast.makeText(context, "Details: ${pet.pDescription ?: "No description available"}", Toast.LENGTH_LONG).show()
            }
        )
        rvPets.adapter = adapter

        filterAll.setOnClickListener {
            currentSpeciesFilter = "All Pets"
            currentBreedFilter = "All Breeds"
            updateFilterUI()
            applyFilters()
        }

        filterSpecies.setOnClickListener { showSpeciesMenu() }
        filterBreed.setOnClickListener { showBreedMenu() }

        fetchPets()
        updateFilterUI()
    }

    private fun showAdoptionForm(pet: Pet) {
        val ctx = context ?: return
        val dialogView = LayoutInflater.from(ctx).inflate(R.layout.dialog_adoption_form, null)
        val tvPetName = dialogView.findViewById<TextView>(R.id.tv_pet_name_header)
        val etContact = dialogView.findViewById<EditText>(R.id.et_contact)
        val etHomeType = dialogView.findViewById<EditText>(R.id.et_home_type)
        val etNewName = dialogView.findViewById<EditText>(R.id.et_new_name)
        val etAnswers = dialogView.findViewById<EditText>(R.id.et_answers)
        val spinnerExperience = dialogView.findViewById<Spinner>(R.id.spinner_experience)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit_app)

        tvPetName.text = "For: ${pet.pName}"

        val experienceLevels = ExperienceLevel.entries.map {
            it.name.replace("_", " ").lowercase().replaceFirstChar { char -> char.uppercase() }
        }
        val spinnerAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, experienceLevels)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExperience.adapter = spinnerAdapter

        val dialog = MaterialAlertDialogBuilder(ctx).setView(dialogView).create()

        btnSubmit.setOnClickListener {
            val contact = etContact.text.toString().trim()
            val homeType = etHomeType.text.toString().trim()
            val answers = etAnswers.text.toString().trim()
            val newName = etNewName.text.toString().trim()

            // FIX: Get the name as a String to match the updated ApplicationRequest
            val experience = ExperienceLevel.entries[spinnerExperience.selectedItemPosition].name

            if (contact.isEmpty() || homeType.isEmpty() || answers.isEmpty()) {
                Toast.makeText(ctx, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // FIX: Use pId to match your Pet data class
            val petId = pet.pId
            if (petId == null) {
                Toast.makeText(ctx, "Error: Pet ID is missing.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val request = ApplicationRequest(petId, contact, homeType, experience, newName, answers)
            submitApplication(request, dialog)
        }
        dialog.show()
    }

    private fun submitApplication(request: ApplicationRequest, dialog: androidx.appcompat.app.AlertDialog) {
        val token = context?.getSharedPreferences("furever_prefs", Context.MODE_PRIVATE)?.getString("auth_token", "") ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.submitApplication("Bearer $token", request)
                }

                if (response.isSuccessful) {
                    Toast.makeText(context, "Application submitted successfully!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                } else if (isAdded) {
                    val errorBody = response.errorBody()?.string()
                    val errorResponse = try { Gson().fromJson(errorBody, ApiResponse::class.java) } catch (e: Exception) { null }
                    val message = errorResponse?.message ?: "Submission failed: ${response.code()}"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                if (isAdded) Toast.makeText(context, "Network Error. Please try again.", Toast.LENGTH_SHORT).show()
                Log.e("PetFragment", "Submit error", e)
            }
        }
    }

    private fun showSpeciesMenu() {
        val popup = PopupMenu(context, filterSpecies)
        popup.menu.add("All Pets")
        speciesData.keys.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener { item ->
            currentSpeciesFilter = item.title.toString()
            currentBreedFilter = "All Breeds"
            updateFilterUI()
            applyFilters()
            true
        }
        popup.show()
    }

    private fun showBreedMenu() {
        if (currentSpeciesFilter == "All Pets") return
        val popup = PopupMenu(context, filterBreed)
        popup.menu.add("All Breeds")
        speciesData[currentSpeciesFilter]?.forEach { popup.menu.add(it) }
        popup.setOnMenuItemClickListener { item ->
            currentBreedFilter = item.title.toString()
            updateFilterUI()
            applyFilters()
            true
        }
        popup.show()
    }

    private fun updateFilterUI() {
        fun setInactive(view: TextView, text: String) {
            view.text = text
            view.setBackgroundResource(R.drawable.bg_filter_inactive)
            view.setTextColor(Color.parseColor("#3A2525"))
            view.alpha = 1.0f
        }
        fun setActive(view: TextView, text: String) {
            view.text = text
            view.setBackgroundResource(R.drawable.bg_filter_active)
            view.setTextColor(Color.WHITE)
            view.alpha = 1.0f
        }

        setInactive(filterAll, "All Pets")
        setInactive(filterSpecies, if (currentSpeciesFilter == "All Pets") "Species >" else currentSpeciesFilter)
        setInactive(filterBreed, if (currentBreedFilter == "All Breeds") "Breed >" else currentBreedFilter)

        if (currentSpeciesFilter == "All Pets") {
            setActive(filterAll, "All Pets")
            filterBreed.alpha = 0.5f
        } else {
            setActive(filterSpecies, currentSpeciesFilter)
            if (currentBreedFilter != "All Breeds") setActive(filterBreed, currentBreedFilter)
        }
    }

    private fun applyFilters() {
        val filtered = allPetsList.filter { pet ->
            // FIX: Use pStatus and lowercase safely to match "available"
            val status = pet.pStatus?.lowercase(Locale.getDefault()) ?: ""
            if (status != "available") return@filter false

            val matchesSpecies = currentSpeciesFilter == "All Pets" ||
                    pet.pSpecies?.equals(currentSpeciesFilter, ignoreCase = true) == true
            val matchesBreed = currentBreedFilter == "All Breeds" ||
                    pet.pBreed?.equals(currentBreedFilter, ignoreCase = true) == true
            matchesSpecies && matchesBreed
        }
        adapter.updatePets(filtered)
    }

    private fun fetchPets() {
        val token = context?.getSharedPreferences("furever_prefs", Context.MODE_PRIVATE)?.getString("auth_token", "") ?: ""
        if (token.isEmpty()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllPets("Bearer $token")
                }
                if (response.isSuccessful && isAdded) {
                    allPetsList = response.body() ?: emptyList()
                    Log.d("PetFragment", "Successfully fetched ${allPetsList.size} pets")
                    applyFilters()
                } else {
                    Log.e("PetFragment", "Fetch error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PetFragment", "Fetch failed", e)
            }
        }
    }
}