package com.safestride.safestride

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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
        enableEdgeToEdge()
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

        // Fetch the user ID from Firebase Authentication
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // If user ID is available, fetch user data (username) from Firestore
        if (userId != null) {
            fetchUsernameFromFirestore(userId)
        }
    }

    // Function to fetch username from Firestore and display it in the profile section
    private fun fetchUsernameFromFirestore(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username =
                        document.getString("username") // Ensure that "username" field is correct in Firestore
                    if (!username.isNullOrEmpty()) {
                        // Set the username in the TextView
                        usernameTextView.text = username
                    } else {
                        Toast.makeText(this, "No Username Set", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error fetching username: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
                    Toast.makeText(
                        this,
                        "Enable GPS Tracking in Settings first!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            R.id.nav_about -> startActivity(Intent(this, About::class.java))
            R.id.nav_sign_out -> logoutUser() // Handle sign out
        }
    }

    // Logout function to handle Firebase sign-out
    private fun logoutUser() {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // 🔹 Stop Firestore operations before logging out
        db.clearPersistence().addOnCompleteListener {
            db.terminate().addOnCompleteListener {
                // ✅ Sign out after Firestore has been stopped
                auth.signOut()

                // ✅ Clear SharedPreferences (if needed)
                sharedPreferences.edit().clear().apply()

                // ✅ Redirect to LandingPage or login screen
                val intent = Intent(this, LandingPage::class.java)
                startActivity(intent)
                finish() // ✅ Close current activity after logout
            }
        }
    }
}