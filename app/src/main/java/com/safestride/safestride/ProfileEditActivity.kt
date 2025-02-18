package com.safestride.safestride

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException
import java.util.*

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var birthdateEditText: EditText
    private lateinit var calendarIcon: ImageView
    private lateinit var profileImageView: ImageView
    private lateinit var fullNameEditText: EditText
    private lateinit var emailAddressEditText: EditText
    private lateinit var contactNumberEditText: EditText
    private lateinit var addressEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val PREF_NAME = "UserProfileData"
        private const val PROFILE_IMAGE_URI_KEY = "profileImageUri"
        private const val FULL_NAME_KEY = "fullName"
        private const val EMAIL_KEY = "emailAddress"
        private const val CONTACT_KEY = "contactNumber"
        private const val BIRTHDATE_KEY = "birthdate"
        private const val ADDRESS_KEY = "address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile_edit)

        // Find the RelativeLayout by its ID
        val profileLayout = findViewById<RelativeLayout>(R.id.profile)

        // Apply Window Insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(profileLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // Return the insets
        }
        // Back Arrow Click Listener
        findViewById<View>(R.id.backArrowIcon).setOnClickListener {
            finish()
        }
        // Initialize UI elements
        birthdateEditText = findViewById(R.id.birthdateEditText)
        calendarIcon = findViewById(R.id.calendarIcon)
        profileImageView = findViewById(R.id.profileIcon)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailAddressEditText = findViewById(R.id.emailAddressEditText)
        contactNumberEditText = findViewById(R.id.contactNumberEditText)
        addressEditText = findViewById(R.id.addressEditText)
        saveButton = findViewById(R.id.saveButton)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // Load saved data into fields
        loadSavedData()

        // Handle Date Picker for Birthdate
        val dateClickListener = {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                birthdateEditText.setText("$selectedDay/${selectedMonth + 1}/$selectedYear")
            }, year, month, day)

            datePicker.show()
        }

        birthdateEditText.setOnClickListener { dateClickListener() }
        calendarIcon.setOnClickListener { dateClickListener() }

        // Save Button Click - Save All Data and Pass Only Full Name & Profile Picture
        saveButton.setOnClickListener {
            saveUserData()

            // Retrieve saved data
            val fullName = fullNameEditText.text.toString()
            val profileImageUri = sharedPreferences.getString(PROFILE_IMAGE_URI_KEY, "")

            // Pass only Full Name & Profile Picture to EditProfileActivity
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("fullName", fullName)
            intent.putExtra("profileImageUri", profileImageUri)
            startActivity(intent)
        }

        // Profile Image Click - Open Image Picker
        profileImageView.setOnClickListener {
            checkStoragePermission()
        }
    }

    // Function to Check and Request Storage Permission
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PICK_IMAGE_REQUEST)
            } else {
                openImagePicker()
            }
        } else {
            openImagePicker() // For Android 10+ (Scoped Storage)
        }
    }

    // Function to Open Image Picker
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle Image Selection from Gallery Safely
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImageUri: Uri = data.data!!

            try {
                val inputStream = contentResolver.openInputStream(selectedImageUri)
                profileImageView.setImageURI(selectedImageUri)

                // Save the image URI in SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString(PROFILE_IMAGE_URI_KEY, selectedImageUri.toString())
                editor.apply()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to Save All Data into SharedPreferences
    private fun saveUserData() {
        val editor = sharedPreferences.edit()
        editor.putString(FULL_NAME_KEY, fullNameEditText.text.toString())
        editor.putString(EMAIL_KEY, emailAddressEditText.text.toString())
        editor.putString(CONTACT_KEY, contactNumberEditText.text.toString())
        editor.putString(BIRTHDATE_KEY, birthdateEditText.text.toString())
        editor.putString(ADDRESS_KEY, addressEditText.text.toString())
        editor.apply()
    }

    // Function to Load Saved Data into Fields
    private fun loadSavedData() {
        fullNameEditText.setText(sharedPreferences.getString(FULL_NAME_KEY, ""))
        emailAddressEditText.setText(sharedPreferences.getString(EMAIL_KEY, ""))
        contactNumberEditText.setText(sharedPreferences.getString(CONTACT_KEY, ""))
        birthdateEditText.setText(sharedPreferences.getString(BIRTHDATE_KEY, ""))
        addressEditText.setText(sharedPreferences.getString(ADDRESS_KEY, ""))

        // Load profile image safely
        val profileImageUriString = sharedPreferences.getString(PROFILE_IMAGE_URI_KEY, null)
        if (!profileImageUriString.isNullOrEmpty()) {
            try {
                val inputStream = contentResolver.openInputStream(Uri.parse(profileImageUriString))
                profileImageView.setImageURI(Uri.parse(profileImageUriString))
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load saved image", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
