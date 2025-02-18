package com.safestride.safestride

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class Dashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout)

        findViewById<LinearLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
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
        val cardRecords = findViewById<View>(R.id.cardRecords)
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
