package com.safestride.safestride

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class EditProfileActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)  // Your profile layout file

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Find the RelativeLayout by its ID
        val editLayout = findViewById<RelativeLayout>(R.id.edit)

        // Apply Window Insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(editLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // Return the insets
        }

        // Initialize DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Handle the Menu Icon click to open the drawer
        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START) // Open the navigation drawer
        }

        // Handle Profile Section Click in Drawer
        val navigationHeaderView = navigationView.getHeaderView(0)
        val profileSection: LinearLayout? = navigationHeaderView?.findViewById(R.id.profileSection)
        profileSection?.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Handle Menu Item Clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleMenuSelection(menuItem)
            drawerLayout.closeDrawers() // Close drawer after selection
            true
        }

        // Initialize UI elements
        val profileImageView: ImageView = findViewById(R.id.profileIcon)
        val userNameTextView: TextView = findViewById(R.id.userName)

        // Retrieve Full Name & Profile Image from Intent
        val fullName = intent.getStringExtra("fullName")
        val profileImageUriString = intent.getStringExtra("profileImageUri")

        // Set Full Name
        userNameTextView.text = fullName ?: "User Name"

        // Set Profile Picture if Available
        if (!profileImageUriString.isNullOrEmpty()) {
            profileImageView.setImageURI(Uri.parse(profileImageUriString))
        }

        // Edit Profile Section
        findViewById<LinearLayout>(R.id.editProfile).setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }

        // View Assigned PWD Section
        findViewById<LinearLayout>(R.id.viewAssignedPWD).setOnClickListener {
            startActivity(Intent(this, Patient::class.java))
        }

        // Change Password Section
        findViewById<LinearLayout>(R.id.changePassword).setOnClickListener {
            startActivity(Intent(this, ChangePassword::class.java))
        }

        // Settings Section
        findViewById<LinearLayout>(R.id.settings).setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

        // Log Out Section
        findViewById<LinearLayout>(R.id.logOut).setOnClickListener {
            startActivity(Intent(this, LandingPage::class.java))
        }
    }

    private fun handleMenuSelection(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.nav_home -> startActivity(Intent(this, Dashboard::class.java))
            R.id.nav_settings -> startActivity(Intent(this, Settings::class.java))
            R.id.nav_maps -> {
                val isGpsEnabled = sharedPreferences.getBoolean("gps_tracking_enabled", false)
                if (isGpsEnabled) {
                    startActivity(Intent(this, MapsActivity::class.java))
                } else {
                    Toast.makeText(this, "Enable GPS Tracking in Settings first!", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_about -> startActivity(Intent(this, About::class.java))
            R.id.nav_sign_out -> startActivity(Intent(this, LandingPage::class.java))
        }
    }
}
