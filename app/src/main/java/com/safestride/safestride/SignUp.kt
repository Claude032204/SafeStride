package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth  // Firebase Auth instance
    private lateinit var db: FirebaseFirestore  // Firestore instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // References to the fields in your layout
        val usernameEditText: EditText = findViewById(R.id.usernameField) // Use the username field
        val emailEditText: EditText = findViewById(R.id.emailField)
        val passwordEditText: EditText = findViewById(R.id.passwordField)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPasswordField)
        val signUpButton: Button = findViewById(R.id.signupButton)
        val alreadyHaveAccountText: TextView = findViewById(R.id.loginText)
        val eyeIconPassword: ImageView = findViewById(R.id.eyeIconPassword)
        val eyeIconConfirmPassword: ImageView = findViewById(R.id.eyeIconConfirmPassword)
        val passwordRequirementsText: TextView = findViewById(R.id.passwordRequirementsText)

        // Initially disable the Sign Up button
        signUpButton.isEnabled = false

        // TextWatcher to monitor form fields
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isFormFilled = usernameEditText.text.isNotEmpty() &&
                        emailEditText.text.isNotEmpty() &&
                        passwordEditText.text.isNotEmpty() &&
                        confirmPasswordEditText.text.isNotEmpty() &&
                        isValidEmail(emailEditText.text.toString()) &&
                        passwordEditText.text.toString() == confirmPasswordEditText.text.toString() // Ensure passwords match
                signUpButton.isEnabled = isFormFilled
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        // Attach TextWatcher to each field
        usernameEditText.addTextChangedListener(textWatcher)
        emailEditText.addTextChangedListener(textWatcher)
        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Show password requirements only if password is not empty
                if (passwordEditText.text.isNotEmpty() && !isPasswordValid(passwordEditText.text.toString())) {
                    passwordRequirementsText.visibility = TextView.VISIBLE
                } else {
                    passwordRequirementsText.visibility = TextView.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        confirmPasswordEditText.addTextChangedListener(textWatcher)

        // Ensure password input is hidden initially
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        // Eye Icon for password toggle
        eyeIconPassword.setOnClickListener {
            togglePasswordVisibility(passwordEditText, eyeIconPassword)
        }

        // Eye Icon for confirm password toggle
        eyeIconConfirmPassword.setOnClickListener {
            togglePasswordVisibility(confirmPasswordEditText, eyeIconConfirmPassword)
        }

        // Sign-Up Button Click Listener
        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString() // Use usernameEditText here
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(email)) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            } else if (!isPasswordValid(password)) {
                Toast.makeText(this, "Password does not meet the requirements", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed with Firebase sign-up logic
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Get the user's UID
                            val userId = auth.currentUser?.uid

                            // Create a user object to store in Firestore
                            val user = hashMapOf(
                                "username" to username, // Save the username here
                                "email" to email
                            )

                            // Save the user information in Firestore under their UID
                            userId?.let {
                                db.collection("users").document(it).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Sign-up successful!", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this, Dashboard::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Error saving user info: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            Toast.makeText(this, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Navigate to Log In when "Already have an account?" is clicked
        alreadyHaveAccountText.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }

        // Back Arrow Click Listener
        findViewById<ImageView>(R.id.backArrowIcon).setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Basic email validation method
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Password validation method
    private fun isPasswordValid(password: String): Boolean {
        // Regular expression to check password criteria
        val regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#\$%^&*(),.?\":{}|<>])[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]{8,}\$"
        return password.matches(regex.toRegex())
    }

    // Toggle password visibility
    private fun togglePasswordVisibility(passwordEditText: EditText, eyeIcon: ImageView) {
        if (passwordEditText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            eyeIcon.setImageResource(R.drawable.openeye)
        } else {
            passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            eyeIcon.setImageResource(R.drawable.eyeclosed)
        }
        passwordEditText.setSelection(passwordEditText.text.length)
    }
}
