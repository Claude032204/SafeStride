package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth  // Firebase Auth instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)
        // Find the RelativeLayout by its ID

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.emailField)
        val passwordEditText: EditText = findViewById(R.id.passwordField)
        val logInButton: Button = findViewById(R.id.loginButton)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        val signUpLinkText: TextView = findViewById(R.id.signUpLinkText)
        val eyeIconPassword: ImageView = findViewById(R.id.eyeIconPassword)


        // Log In Button Click Listener
        logInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check if both email and password fields are not empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show()
            } else {
                // Firebase Authentication login
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Navigate to the Dashboard activity if login is successful
                            val intent = Intent(this, Dashboard::class.java)  // Replace with your Dashboard Activity
                            startActivity(intent)
                            finish()  // Close the Log In screen
                        } else {
                            // Show error message if login fails
                            Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        // Sign Up Click Listener
        signUpLinkText.setOnClickListener {
            // Navigate to Sign Up activity
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        // Forgot Password Click Listener
        forgotPasswordText.setOnClickListener {
            // Navigate to Forgot Password Activity
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        // Eye Icon Click Listener to toggle visibility of Password
        eyeIconPassword.setOnClickListener {
            // Toggle password visibility
            if (passwordEditText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                eyeIconPassword.setImageResource(R.drawable.openeye) // Update the icon to 'open eye'
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIconPassword.setImageResource(R.drawable.eyeclosed) // Update the icon to 'closed eye'
            }
            passwordEditText.setSelection(passwordEditText.text.length) // Move cursor to the end
        }

        // Back Arrow Click Listener
        findViewById<ImageView>(R.id.backArrowIcon).setOnClickListener {
            // Navigate to the Landing Page activity
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
            finish()  // Close the Log In activity to prevent going back
        }
    }
}
