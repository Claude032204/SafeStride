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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val PROFILE_IMAGE_URI_KEY = "profileImageUri"
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

        // Set the email address from Firebase Authentication
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            emailAddressEditText.setText(user.email) // Pre-fill email
            // Initialize SharedPreferences for the current user
            sharedPreferences = getUserPreferences(user.uid)
        }

        // Load user data from SharedPreferences (for persistence across app restarts)
        loadUserDataLocally()

        // Load data from Firestore to ensure up-to-date profile data
        loadUserDataFromFirestore()

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

        // Save Button Click - Save All Data and Update Firestore
        saveButton.setOnClickListener {
            saveUserDataToFirestore()
        }

        // Profile Image Click - Open Image Picker
        profileImageView.setOnClickListener {
            checkStoragePermission()
        }
    }

    // Function to Check and Request Storage Permission
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PICK_IMAGE_REQUEST
                )
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

                // Save the image URI in SharedPreferences (or Firestore if needed)
                val editor = sharedPreferences.edit()
                editor.putString(PROFILE_IMAGE_URI_KEY, selectedImageUri.toString())
                editor.apply()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to Load User Data from SharedPreferences
    private fun loadUserDataLocally() {
        val fullName = sharedPreferences.getString("fullName", "")
        val contactNumber = sharedPreferences.getString("contactNumber", "")
        val birthdate = sharedPreferences.getString("birthdate", "")
        val address = sharedPreferences.getString("address", "")

        // Populate the fields from SharedPreferences data
        fullNameEditText.setText(fullName)
        contactNumberEditText.setText(contactNumber)
        birthdateEditText.setText(birthdate)
        addressEditText.setText(address)
    }

    // Function to Save User Data into Firestore and SharedPreferences
    private fun saveUserDataToFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid

            // Get values from the input fields
            val fullName = fullNameEditText.text.toString()
            val contactNumber = contactNumberEditText.text.toString()
            val birthdate = birthdateEditText.text.toString()
            val address = addressEditText.text.toString()

            // Save data locally (SharedPreferences)
            saveUserDataLocally(fullName, contactNumber, birthdate, address)

            // Create a map of updated user data
            val userData: MutableMap<String, Any> = hashMapOf(
                "fullName" to fullName,
                "contactNumber" to contactNumber,
                "birthdate" to birthdate,
                "address" to address
            )

            // Update Firestore with the new data
            db.collection("users").document(userId)
                .update(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Optionally, navigate back to the profile page
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Function to Save User Data Locally to SharedPreferences
    private fun saveUserDataLocally(fullName: String, contactNumber: String, birthdate: String, address: String) {
        val editor = sharedPreferences.edit()
        editor.putString("fullName", fullName)
        editor.putString("contactNumber", contactNumber)
        editor.putString("birthdate", birthdate)
        editor.putString("address", address)
        editor.apply()
    }

    // Function to Load User Data from Firestore
    private fun loadUserDataFromFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val fullName = documentSnapshot.getString("fullName") ?: ""
                        val contactNumber = documentSnapshot.getString("contactNumber") ?: ""
                        val birthdate = documentSnapshot.getString("birthdate") ?: ""
                        val address = documentSnapshot.getString("address") ?: ""

                        // Populate fields with data from Firestore
                        fullNameEditText.setText(fullName)
                        contactNumberEditText.setText(contactNumber)
                        birthdateEditText.setText(birthdate)
                        addressEditText.setText(address)

                        // Optionally, update SharedPreferences to reflect Firestore data
                        saveUserDataLocally(fullName, contactNumber, birthdate, address)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to load user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Helper function to get SharedPreferences specific to the current user (based on UID)
    private fun getUserPreferences(userId: String): SharedPreferences {
        return getSharedPreferences("UserProfileData_$userId", Context.MODE_PRIVATE)
    }
}
