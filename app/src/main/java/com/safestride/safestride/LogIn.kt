package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LogIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var failedAttempts = 0
    private var lockTime: Long = 0 // Variable to store the time when the account is locked
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.emailField)
        val passwordEditText: EditText = findViewById(R.id.passwordField)
        val logInButton: Button = findViewById(R.id.loginButton)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        val signUpLinkText: TextView = findViewById(R.id.signUpLinkText)
        val eyeIconPassword: ImageView = findViewById(R.id.eyeIconPassword)

        logInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check if account is locked
            if (System.currentTimeMillis() < lockTime) {
                val remainingTime = (lockTime - System.currentTimeMillis()) / 1000
                Toast.makeText(this, "Account locked. Try again in $remainingTime seconds.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Check if both email and password fields are not empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT).show()
            } else {
                // Firebase Authentication login
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                            updateLoginAttempts(userId, true) // Successful login

                            val intent = Intent(this, Dashboard::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            failedAttempts++
                            if (failedAttempts >= 3) {
                                // Lock the account for 1 minute (60,000 milliseconds)
                                lockTime = System.currentTimeMillis() + 60000
                                showLockAccountDialog()

                                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                                updateLoginAttempts(userId, false) // Failed login after lockout
                            } else {
                                Toast.makeText(
                                    this,
                                    "Authentication Failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }

        signUpLinkText.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        eyeIconPassword.setOnClickListener {
            if (passwordEditText.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                eyeIconPassword.setImageResource(R.drawable.openeye)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIconPassword.setImageResource(R.drawable.eyeclosed)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        // Back Arrow Click Listener
        findViewById<ImageView>(R.id.backArrowIcon).setOnClickListener {
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showLockAccountDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Incorrect Credentials")
            .setMessage("Please enter the correct credentials. Your account will be locked after another failed attempt.")
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ ->
                // Reset failed attempts to allow retrying
                failedAttempts = 0
                // Reset the lock time to allow immediate retry
                lockTime = System.currentTimeMillis() - 1

                // Proceed with login attempt after retry
                val email = findViewById<EditText>(R.id.emailField).text.toString()
                val password = findViewById<EditText>(R.id.passwordField).text.toString()

                // Only retry login after resetting the lockTime
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                            updateLoginAttempts(userId, true)

                            val intent = Intent(this, Dashboard::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .setNegativeButton("Reset Password") { _, _ ->
                val intent = Intent(this, ForgotPassword::class.java)
                startActivity(intent)
            }
            .create()
        dialog.show()
    }

    private fun updateLoginAttempts(userId: String, isSuccess: Boolean) {
        // Reference to Firestore collection for login attempts
        val userDocRef = db.collection("loginAttempts").document(userId)

        // Get current document to check or create new document
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Update successful/failed login count based on the action
                val currentSuccessCount = document.getLong("successfulLogins") ?: 0
                val currentFailedCount = document.getLong("failedLogins") ?: 0
                if (isSuccess) {
                    userDocRef.update("successfulLogins", currentSuccessCount + 1)
                } else {
                    userDocRef.update("failedLogins", currentFailedCount + 1)
                }
            } else {
                // If no document exists, create a new one with the appropriate values
                if (isSuccess) {
                    userDocRef.set(mapOf("successfulLogins" to 1, "failedLogins" to 0))
                } else {
                    userDocRef.set(mapOf("successfulLogins" to 0, "failedLogins" to 1))
                }
            }
        }
    }
}