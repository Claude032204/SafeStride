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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var userNameTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var db: FirebaseFirestore

    companion object {
        private const val REQUEST_CODE_EDIT_PROFILE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        sharedPreferences = getSharedPreferences("UserProfileData_${FirebaseAuth.getInstance().currentUser?.uid}", MODE_PRIVATE)
        db = FirebaseFirestore.getInstance()

        val editLayout = findViewById<RelativeLayout>(R.id.edit)
        ViewCompat.setOnApplyWindowInsetsListener(editLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        userNameTextView = findViewById(R.id.userName)

        // Load the saved full name from SharedPreferences
        userNameTextView.text = sharedPreferences.getString("fullName", "User Name")

        fetchUsernameFromFirestore()

        val profileImageView: ImageView = findViewById(R.id.profileIcon)

        val profileImageUriString = sharedPreferences.getString("profileImageUri", null)
        if (!profileImageUriString.isNullOrEmpty()) {
            profileImageView.setImageURI(Uri.parse(profileImageUriString))
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        val menuIcon: ImageView = findViewById(R.id.menuIcon)
        menuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }

        val navigationHeaderView = navigationView.getHeaderView(0)
        val profileSection: LinearLayout? = navigationHeaderView?.findViewById(R.id.profileSection)
        profileSection?.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleMenuSelection(menuItem)
            drawerLayout.closeDrawers()
            true
        }

        findViewById<LinearLayout>(R.id.editProfile).setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE)
        }

        findViewById<LinearLayout>(R.id.viewAssignedPWD).setOnClickListener {
            startActivity(Intent(this, Patient::class.java))
        }

        findViewById<LinearLayout>(R.id.changePassword).setOnClickListener {
            startActivity(Intent(this, ChangePassword::class.java))
        }

        findViewById<LinearLayout>(R.id.settings).setOnClickListener {
            startActivity(Intent(this, Settings::class.java))
        }

        findViewById<LinearLayout>(R.id.logOut).setOnClickListener {
            logoutUser()
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
            R.id.nav_sign_out -> logoutUser()
        }
    }

    private fun logoutUser() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        sharedPreferences.edit().clear().apply()

        val intent = Intent(this, LandingPage::class.java)
        startActivity(intent)
        finish()
    }

    private fun fetchUsernameFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val fullName = document.getString("fullName")
                        if (!fullName.isNullOrEmpty()) {
                            userNameTextView.text = fullName

                            val editor = sharedPreferences.edit()
                            editor.putString("fullName", fullName)
                            editor.apply()
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error fetching username: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            val updatedFullName = data?.getStringExtra("updatedFullName")
            if (!updatedFullName.isNullOrEmpty()) {
                userNameTextView.text = updatedFullName

                val editor = sharedPreferences.edit()
                editor.putString("fullName", updatedFullName)
                editor.apply()
            }
        }
    }
}
