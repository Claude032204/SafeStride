package com.safestride.safestride

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class Settings : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var switchGpsTracking: Switch
    private lateinit var buttonViewLastKnownLocation: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)


        // Find the RelativeLayout by its ID
        val settingsLayout = findViewById<RelativeLayout>(R.id.settings)

        // Apply Window Insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(settingsLayout) { v, insets ->
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

        // Handle Profile Section Click
        val navigationHeaderView = navigationView.getHeaderView(0)
        val profileSection: LinearLayout? = navigationHeaderView?.findViewById(R.id.profileSection)

        profileSection?.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Handle menu item selection
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleMenuSelection(menuItem)
            drawerLayout.closeDrawers() // Close drawer after selection
            true
        }

        // Switches
        val switchShowConnectedDevice: Switch = findViewById(R.id.switchShowConnectedDevice)
        switchGpsTracking = findViewById(R.id.switchGpsTracking)

        // Buttons
        val buttonReconnectWatch: Button = findViewById(R.id.buttonReconnectWatch)
        buttonViewLastKnownLocation = findViewById(R.id.buttonViewLastKnownLocation)
        val buttonEditProfile: Button = findViewById(R.id.buttonEditProfile)
        val buttonChangePassword: Button = findViewById(R.id.buttonChangePassword)
        val buttonLogout: Button = findViewById(R.id.buttonLogout)

        // Load GPS Tracking status from SharedPreferences
        val isGpsEnabled = sharedPreferences.getBoolean("gps_tracking_enabled", false)
        switchGpsTracking.isChecked = isGpsEnabled
        buttonViewLastKnownLocation.isEnabled = isGpsEnabled // Enable only if GPS was enabled before

        // Handle GPS Tracking switch toggle
        switchGpsTracking.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showLocationPermissionDialog()
            } else {
                saveGpsTrackingStatus(false)
                buttonViewLastKnownLocation.isEnabled = false // Disable button if switch is OFF
            }
        }

        // View Last Location (Google Maps) button
        buttonViewLastKnownLocation.setOnClickListener {
            if (switchGpsTracking.isChecked) {
                startActivity(Intent(this, MapsActivity::class.java))
            } else {
                Toast.makeText(this, "Enable GPS Tracking first!", Toast.LENGTH_SHORT).show()
            }
        }

        // Reconnect Watch
        buttonReconnectWatch.setOnClickListener {
            showReconnectDialog()
        }

        // Edit Profile
        buttonEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Change Password
        buttonChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePassword::class.java))
        }

        // Logout Button
        buttonLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun showLocationPermissionDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Allow \"Maps\" to use your location?")
        builder.setMessage("SafeStride uses location data to provide accurate GPS tracking.")

        builder.setPositiveButton("Allow") { _: DialogInterface, _: Int ->
            requestLocationPermission()
        }

        builder.setNegativeButton("Deny") { dialog: DialogInterface, _: Int ->
            switchGpsTracking.isChecked = false
            saveGpsTrackingStatus(false)
            buttonViewLastKnownLocation.isEnabled = false
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            switchGpsTracking.isChecked = true
            saveGpsTrackingStatus(true)
            buttonViewLastKnownLocation.isEnabled = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switchGpsTracking.isChecked = true
                saveGpsTrackingStatus(true)
                buttonViewLastKnownLocation.isEnabled = true
            } else {
                switchGpsTracking.isChecked = false
                saveGpsTrackingStatus(false)
                buttonViewLastKnownLocation.isEnabled = false
            }
        }
    }

    private fun saveGpsTrackingStatus(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("gps_tracking_enabled", isEnabled).apply()
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


private fun showReconnectDialog() {
        // Show a reconnect dialog
    }

    private fun logoutUser() {
        startActivity(Intent(this, LandingPage::class.java))
        finish()
    }
}
