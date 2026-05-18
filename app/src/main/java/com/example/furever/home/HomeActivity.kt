package com.example.furever.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.furever.R
import com.example.furever.application.ApplicationsFragment
import com.example.furever.main.MainActivity
import com.example.furever.pets.PetFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvTitle: TextView
    private lateinit var tvSubtitle: TextView
    
    private var homeFragment: HomeFragment? = null
    private var petFragment: PetFragment? = null
    private var applicationsFragment: ApplicationsFragment? = null
    private var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Initialize Views
        drawerLayout = findViewById(R.id.drawer_layout)
        val btnSettings = findViewById<ImageView>(R.id.btn_settings_gear)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        tvTitle = findViewById(R.id.main_header_title)
        tvSubtitle = findViewById(R.id.main_header_subtitle)

        // 2. Setup Sidebar Header
        try {
            val headerView = navigationView.getHeaderView(0)
            val prefs = getSharedPreferences("furever_prefs", Context.MODE_PRIVATE)
            
            headerView.findViewById<TextView>(R.id.tv_user_name).text = prefs.getString("username", "User")
            headerView.findViewById<TextView>(R.id.tv_user_role).text = "Role: ${prefs.getString("user_role", "ADOPTER")}"
            
            val avatarUrl = prefs.getString("avatar_url", null)
            if (!avatarUrl.isNullOrEmpty()) {
                Glide.with(this).load(avatarUrl).circleCrop().into(headerView.findViewById(R.id.iv_user_avatar))
            }
        } catch (e: Exception) { Log.e("HomeActivity", "Header init error") }

        // 3. Handle Fragment Recreation
        val fm = supportFragmentManager
        if (savedInstanceState == null) {
            homeFragment = HomeFragment()
            petFragment = PetFragment()
            applicationsFragment = ApplicationsFragment()

            fm.beginTransaction()
                .add(R.id.fragment_container, applicationsFragment!!, "APPS").hide(applicationsFragment!!)
                .add(R.id.fragment_container, petFragment!!, "PETS").hide(petFragment!!)
                .add(R.id.fragment_container, homeFragment!!, "HOME")
                .commit()
            activeFragment = homeFragment
            updateHeader("Home", "Welcome back to FurEver!")
        } else {
            homeFragment = fm.findFragmentByTag("HOME") as? HomeFragment
            petFragment = fm.findFragmentByTag("PETS") as? PetFragment
            applicationsFragment = fm.findFragmentByTag("APPS") as? ApplicationsFragment
            activeFragment = fm.findFragmentById(R.id.fragment_container)
        }

        // 4. Listeners
        btnSettings.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) showLogoutConfirmationDialog()
            drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> { switchFragment(homeFragment, "Home", "Welcome back to FurEver!"); true }
                R.id.nav_pet -> { switchFragment(petFragment, "Find Your Bestfriend", "Choose the pet you want to adopt."); true }
                R.id.nav_application -> { switchFragment(applicationsFragment, "My Applications", "Track your adoption requests below."); true }
                else -> false
            }
        }
    }

    private fun switchFragment(target: Fragment?, title: String, subtitle: String) {
        if (target == null || activeFragment == target) return
        
        updateHeader(title, subtitle)
        
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .hide(activeFragment!!)
            .show(target)
            .commitAllowingStateLoss()
            
        activeFragment = target
    }

    private fun updateHeader(title: String, subtitle: String) {
        tvTitle.text = title
        tvSubtitle.text = subtitle
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Logout") { _, _ -> performLogout() }
            .show()
    }

    private fun performLogout() {
        getSharedPreferences("furever_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
