package com.safestride.safestride

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Dashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usernameTextView: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout)

        // Set up WindowInsets for full-screen integration
        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mainLayout = findViewById<LinearLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle Menu Icon Click
        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle Navigation Drawer Item Clicks
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleMenuSelection(menuItem)
            drawerLayout.closeDrawers()
            true
        }

        // Handle Add Device Card Click
        val addDeviceCard: LinearLayout = findViewById(R.id.addDeviceCard)
        addDeviceCard.setOnClickListener {
            startActivity(Intent(this, SetUp::class.java))
        }

        // Handle Notification Bell Click
        val notificationBell: ImageView = findViewById(R.id.notificationBell)
        notificationBell.setOnClickListener {
            startActivity(Intent(this, Notification::class.java))
        }

        // Handle Patient Card Click
        val patientCard: LinearLayout = findViewById(R.id.patientCard)
        patientCard.setOnClickListener {
            startActivity(Intent(this, Patient::class.java))
        }

        // Handle Records Card Click
        val cardRecords = findViewById<LinearLayout>(R.id.cardRecords)
        cardRecords.setOnClickListener {
            startActivity(Intent(this, Records::class.java))
        }

        // Handle Reminder Card Click
        val reminderCard: LinearLayout = findViewById(R.id.cardReminder)
        reminderCard.setOnClickListener {
            startActivity(Intent(this, Reminder::class.java))
        }

        // Handle Profile Section Click
        val navigationHeaderView = navigationView.getHeaderView(0)
        val profileSection: LinearLayout? = navigationHeaderView?.findViewById(R.id.profileSection)

        profileSection?.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Get the username TextView from the header view
        usernameTextView = navigationHeaderView?.findViewById(R.id.usernameTextView) ?: return

        // Load the user's full name and set it in the Navigation Drawer header
        loadUserData()
    }

    private fun loadUserData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid

            // Fetch user data from Firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val fullName = documentSnapshot.getString("fullName") ?: "User"

                        // Set the username in the Navigation Drawer header
                        usernameTextView.text = fullName
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
            R.id.nav_sign_out -> logoutUser() // Handle sign out
        }
    }

    // Logout function to handle Firebase sign-out
    private fun logoutUser() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Clear SharedPreferences (if needed)
        sharedPreferences.edit().clear().apply()

        // Redirect to LandingPage or login screen
        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish() // Close the current activity after logout
    }
}
