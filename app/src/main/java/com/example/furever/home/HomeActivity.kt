package com.example.furever.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.furever.R
import com.example.furever.login.LoginActivity
import com.example.furever.main.MainActivity
import com.example.furever.network.RetrofitClient
import com.example.furever.pets.Pet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvPets: RecyclerView
    private lateinit var adapter: PetAdapter
    
    private val activityScope = CoroutineScope(Dispatchers.Main + Job())
    private var allPetsList: List<Pet> = emptyList()
    
    private var currentSpeciesFilter = "All Pets"
    private var currentBreedFilter = "All Breeds"

    private val speciesData = mapOf(
        "Dog" to listOf("Aspin", "Golden Retriever", "Askal", "Poodle", "Bulldog", "Beagle", "Chihuahua"),
        "Cat" to listOf("Puspin", "Siamese", "Persian", "Maine Coon", "Bengal", "Munchkin", "Tabby"),
        "Rabbit" to listOf("Dutch", "Lionhead", "Rex", "Netherland Dwarf"),
        "Bird" to listOf("Parrot", "Canary", "Lovebird", "Cockatiel", "African Grey")
    )

    private lateinit var filterAll: TextView
    private lateinit var filterSpecies: TextView
    private lateinit var filterBreed: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout)
        rvPets = findViewById(R.id.rv_pets)
        val btnSettings = findViewById<ImageView>(R.id.btn_settings_gear)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        filterAll = findViewById(R.id.filter_all)
        filterSpecies = findViewById(R.id.filter_species)
        filterBreed = findViewById(R.id.filter_breed)

        // 2. Sidebar Header Setup with User Data and Avatar
        val headerView = navigationView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tv_user_name)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tv_user_email)
        val ivAvatar = headerView.findViewById<ImageView>(R.id.iv_user_avatar)
        
        val prefs = getSharedPreferences("furever_prefs", Context.MODE_PRIVATE)
        val username = prefs.getString("username", "User")
        val role = prefs.getString("user_role", "ADOPTER")
        val avatarUrl = prefs.getString("avatar_url", null)
        
        tvUserName.text = username
        tvUserEmail.text = "Role: $role"

        // Load Avatar if it exists, otherwise keep paw placeholder
        if (!avatarUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.paw)
                .error(R.drawable.paw)
                .into(ivAvatar)
        }

        // 3. RecyclerView Setup
        adapter = PetAdapter(emptyList(), 
            onAdoptClick = { pet -> 
                Toast.makeText(this, "Adoption request for ${pet.pName}", Toast.LENGTH_SHORT).show()
            },
            onDetailsClick = { pet ->
                Toast.makeText(this, "Details: ${pet.pDescription}", Toast.LENGTH_LONG).show()
            }
        )
        rvPets.adapter = adapter

        // 4. Listeners
        btnSettings.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> showLogoutConfirmationDialog()
                else -> Toast.makeText(this, menuItem.title, Toast.LENGTH_SHORT).show()
            }
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        // 5. Filter Dropdown Logic
        filterAll.setOnClickListener {
            currentSpeciesFilter = "All Pets"
            currentBreedFilter = "All Breeds"
            updateFilterUI()
            applyFilters()
        }

        filterSpecies.setOnClickListener { showSpeciesMenu() }
        filterBreed.setOnClickListener { showBreedMenu() }

        // 6. Fetch Data
        fetchPets()
        updateFilterUI()
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .show()
    }

    private fun showSpeciesMenu() {
        val popup = PopupMenu(this, filterSpecies)
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
        val popup = PopupMenu(this, filterBreed)
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
        setInactive(filterSpecies, "Species >")
        setInactive(filterBreed, "Breed >")
        if (currentSpeciesFilter == "All Pets") {
            setActive(filterAll, "All Pets")
            filterBreed.alpha = 0.5f 
        } else {
            setActive(filterSpecies, currentSpeciesFilter)
            if (currentBreedFilter != "All Breeds") setActive(filterBreed, currentBreedFilter)
            else setInactive(filterBreed, "Breed >")
        }
    }

    private fun applyFilters() {
        val filtered = allPetsList.filter { pet ->
            val isAvailable = pet.pStatus?.equals("available", ignoreCase = true) == true
            val matchesSpecies = currentSpeciesFilter == "All Pets" || 
                                 pet.pSpecies?.equals(currentSpeciesFilter, ignoreCase = true) == true
            val matchesBreed = currentBreedFilter == "All Breeds" || 
                               pet.pBreed?.equals(currentBreedFilter, ignoreCase = true) == true
            isAvailable && matchesSpecies && matchesBreed
        }
        adapter.updatePets(filtered)
    }

    private fun fetchPets() {
        val token = getSharedPreferences("furever_prefs", Context.MODE_PRIVATE).getString("auth_token", "") ?: ""
        activityScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getAllPets("Bearer $token")
                }
                if (response.isSuccessful) {
                    allPetsList = response.body() ?: emptyList()
                    applyFilters()
                }
            } catch (e: Exception) {
                Log.e("HomeActivity", "Fetch failed", e)
            }
        }
    }

    private fun performLogout() {
        getSharedPreferences("furever_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityScope.cancel()
    }
}
