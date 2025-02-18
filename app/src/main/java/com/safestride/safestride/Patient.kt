package com.safestride.safestride

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.imageview.ShapeableImageView

class Patient : AppCompatActivity() {

    // Personal Information Fields
    private lateinit var fullNameEditText: EditText
    private lateinit var fullNameTextView: TextView
    private lateinit var dobEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var bloodTypeEditText: EditText
    private lateinit var emergencyContactEditText: EditText

    // Medical Information Fields
    private lateinit var primaryConditionEditText: EditText
    private lateinit var allergiesEditText: EditText
    private lateinit var medicationsEditText: EditText
    private lateinit var mobilityStatusEditText: EditText
    private lateinit var communicationNeedsEditText: EditText

    // Emergency Details Fields
    private lateinit var hospitalEditText: EditText
    private lateinit var medicalAlertEditText: EditText
    private lateinit var guardianContactEditText: EditText

    // Clear buttons for all fields
    private lateinit var clearFullName: ImageView
    private lateinit var clearDob: ImageView
    private lateinit var clearGender: ImageView
    private lateinit var clearBloodType: ImageView
    private lateinit var clearEmergencyContact: ImageView

    private lateinit var clearPrimaryCondition: ImageView
    private lateinit var clearAllergies: ImageView
    private lateinit var clearMedications: ImageView
    private lateinit var clearMobilityStatus: ImageView
    private lateinit var clearCommunicationNeeds: ImageView

    private lateinit var clearHospital: ImageView
    private lateinit var clearMedicalAlert: ImageView
    private lateinit var clearGuardianContact: ImageView

    // Profile and Submit/Edit buttons
    private lateinit var profileIcon: ShapeableImageView
    private lateinit var submitButton: Button
    private lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient)

        // Find the RelativeLayout by its ID
        val patientLayout = findViewById<RelativeLayout>(R.id.patient)

        // Apply Window Insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(patientLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets // Return the insets
        }

        // Initialize views (same as before)
        profileIcon = findViewById(R.id.profileIcon)
        submitButton = findViewById(R.id.submitButton)
        editButton = findViewById(R.id.editButton)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        fullNameTextView = findViewById(R.id.fullName) // Reference for displaying full name below the profile


        // Initialize all the EditTexts for Personal, Medical, and Emergency Information
        fullNameEditText = findViewById(R.id.fullNameEditText)
        dobEditText = findViewById(R.id.dobEditText)
        genderEditText = findViewById(R.id.genderEditText)
        bloodTypeEditText = findViewById(R.id.bloodTypeEditText)
        emergencyContactEditText = findViewById(R.id.emergencyContactEditText)

        primaryConditionEditText = findViewById(R.id.primaryConditionEditText)
        allergiesEditText = findViewById(R.id.allergiesEditText)
        medicationsEditText = findViewById(R.id.medicationsEditText)
        mobilityStatusEditText = findViewById(R.id.mobilityStatusEditText)
        communicationNeedsEditText = findViewById(R.id.communicationNeedsEditText)

        hospitalEditText = findViewById(R.id.hospitalEditText)
        medicalAlertEditText = findViewById(R.id.medicalAlertEditText)
        guardianContactEditText = findViewById(R.id.guardianContactEditText)

        // Initialize clear buttons for all fields
        clearFullName = findViewById(R.id.clearFullName)
        clearDob = findViewById(R.id.clearDob)
        clearGender = findViewById(R.id.clearGender)
        clearBloodType = findViewById(R.id.clearBloodType)
        clearEmergencyContact = findViewById(R.id.clearEmergencyContact)

        clearPrimaryCondition = findViewById(R.id.clearPrimaryCondition)
        clearAllergies = findViewById(R.id.clearAllergies)
        clearMedications = findViewById(R.id.clearMedications)
        clearMobilityStatus = findViewById(R.id.clearMobilityStatus)
        clearCommunicationNeeds = findViewById(R.id.clearCommunicationNeeds)

        clearHospital = findViewById(R.id.clearHospital)
        clearMedicalAlert = findViewById(R.id.clearMedicalAlert)
        clearGuardianContact = findViewById(R.id.clearGuardianContact)

        // Set listeners for clearing the fields
        setClearButtonListeners()

        // Set click listener for profile icon
        profileIcon.setOnClickListener {
            openGallery()
        }

        // Submit button click listener
        submitButton.setOnClickListener {
            saveData()
        }
        // Set click listener for the submit button
        submitButton.setOnClickListener {
            // Get the full name from the EditText and display it in the TextView
            val fullName = fullNameEditText.text.toString()
            fullNameTextView.text = fullName // Update the TextView with the typed full name
        }

        // Edit button click listener
        editButton.setOnClickListener {
            toggleEditMode(true)
        }
        // Find the back arrow icon by its ID
        val backArrowIcon: ImageView = findViewById(R.id.backArrowIcon)

        // Set a click listener to navigate back to the Dashboard
        backArrowIcon.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish() // Optional: Close the current activity to avoid stacking
        }

        // Load saved data from SharedPreferences (if any)
        loadData()
    }

    private fun setClearButtonListeners() {
        clearFullName.setOnClickListener { clearField(fullNameEditText) }
        clearDob.setOnClickListener { clearField(dobEditText) }
        clearGender.setOnClickListener { clearField(genderEditText) }
        clearBloodType.setOnClickListener { clearField(bloodTypeEditText) }
        clearEmergencyContact.setOnClickListener { clearField(emergencyContactEditText) }

        clearPrimaryCondition.setOnClickListener { clearField(primaryConditionEditText) }
        clearAllergies.setOnClickListener { clearField(allergiesEditText) }
        clearMedications.setOnClickListener { clearField(medicationsEditText) }
        clearMobilityStatus.setOnClickListener { clearField(mobilityStatusEditText) }
        clearCommunicationNeeds.setOnClickListener { clearField(communicationNeedsEditText) }

        clearHospital.setOnClickListener { clearField(hospitalEditText) }
        clearMedicalAlert.setOnClickListener { clearField(medicalAlertEditText) }
        clearGuardianContact.setOnClickListener { clearField(guardianContactEditText) }
    }

    private fun clearField(field: EditText) {
        if (field.isFocusable) field.text.clear() // Clears the field only if it is editable
    }

    // Launch the gallery to pick an image
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 100)
    }

    // Handle the result of image selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            profileIcon.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Save form data into SharedPreferences
    private fun saveData() {
        val sharedPreferences = getSharedPreferences("PatientData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save all data for personal, medical, and emergency information
        editor.putString("fullName", fullNameEditText.text.toString())
        editor.putString("dob", dobEditText.text.toString())
        editor.putString("gender", genderEditText.text.toString())
        editor.putString("bloodType", bloodTypeEditText.text.toString())
        editor.putString("emergencyContact", emergencyContactEditText.text.toString())

        editor.putString("primaryCondition", primaryConditionEditText.text.toString())
        editor.putString("allergies", allergiesEditText.text.toString())
        editor.putString("medications", medicationsEditText.text.toString())
        editor.putString("mobilityStatus", mobilityStatusEditText.text.toString())
        editor.putString("communicationNeeds", communicationNeedsEditText.text.toString())

        editor.putString("hospital", hospitalEditText.text.toString())
        editor.putString("medicalAlert", medicalAlertEditText.text.toString())
        editor.putString("guardianContact", guardianContactEditText.text.toString())

        editor.apply()

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
        toggleEditMode(false)
    }

    // Load data from SharedPreferences
    private fun loadData() {
        val sharedPreferences = getSharedPreferences("PatientData", MODE_PRIVATE)

        // Load all saved data for personal, medical, and emergency information
        fullNameEditText.setText(sharedPreferences.getString("fullName", ""))
        dobEditText.setText(sharedPreferences.getString("dob", ""))
        genderEditText.setText(sharedPreferences.getString("gender", ""))
        bloodTypeEditText.setText(sharedPreferences.getString("bloodType", ""))
        emergencyContactEditText.setText(sharedPreferences.getString("emergencyContact", ""))

        primaryConditionEditText.setText(sharedPreferences.getString("primaryCondition", ""))
        allergiesEditText.setText(sharedPreferences.getString("allergies", ""))
        medicationsEditText.setText(sharedPreferences.getString("medications", ""))
        mobilityStatusEditText.setText(sharedPreferences.getString("mobilityStatus", ""))
        communicationNeedsEditText.setText(sharedPreferences.getString("communicationNeeds", ""))

        hospitalEditText.setText(sharedPreferences.getString("hospital", ""))
        medicalAlertEditText.setText(sharedPreferences.getString("medicalAlert", ""))
        guardianContactEditText.setText(sharedPreferences.getString("guardianContact", ""))

        toggleEditMode(false)
    }

    // Toggle the edit mode for all fields
    private fun toggleEditMode(isEditable: Boolean) {
        // Personal Information Fields
        toggleFieldEditability(isEditable, fullNameEditText, clearFullName)
        toggleFieldEditability(isEditable, dobEditText, clearDob)
        toggleFieldEditability(isEditable, genderEditText, clearGender)
        toggleFieldEditability(isEditable, bloodTypeEditText, clearBloodType)
        toggleFieldEditability(isEditable, emergencyContactEditText, clearEmergencyContact)

        // Medical Information Fields
        toggleFieldEditability(isEditable, primaryConditionEditText, clearPrimaryCondition)
        toggleFieldEditability(isEditable, allergiesEditText, clearAllergies)
        toggleFieldEditability(isEditable, medicationsEditText, clearMedications)
        toggleFieldEditability(isEditable, mobilityStatusEditText, clearMobilityStatus)
        toggleFieldEditability(isEditable, communicationNeedsEditText, clearCommunicationNeeds)

        // Emergency Details Fields
        toggleFieldEditability(isEditable, hospitalEditText, clearHospital)
        toggleFieldEditability(isEditable, medicalAlertEditText, clearMedicalAlert)
        toggleFieldEditability(isEditable, guardianContactEditText, clearGuardianContact)

        // Submit button enable/disable
        submitButton.isEnabled = isEditable
    }

    // Helper to toggle editability and clear button visibility
    private fun toggleFieldEditability(isEditable: Boolean, field: EditText, clearButton: ImageView) {
        field.isFocusable = isEditable
        field.isFocusableInTouchMode = isEditable
        clearButton.visibility = if (isEditable) View.VISIBLE else View.GONE
    }
}
