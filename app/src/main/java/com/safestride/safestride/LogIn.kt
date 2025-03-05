package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class LogIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailEditText: EditText = findViewById(R.id.emailField)
        val passwordEditText: EditText = findViewById(R.id.passwordField)
        val logInButton: Button = findViewById(R.id.loginButton)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        val signUpLinkText: TextView = findViewById(R.id.signUpLinkText)
        val eyeIconPassword: ImageView = findViewById(R.id.eyeIconPassword)

        logInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Check if both email and password fields are not empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Firebase Authentication login
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val currentUser = auth.currentUser

                            // Force reload the user to get the latest status on email verification
                            currentUser?.reload()?.addOnCompleteListener { reloadTask ->
                                if (reloadTask.isSuccessful) {
                                    // Send the verification email immediately after login
                                    sendVerificationEmailAgain()

                                    // Show verification card
                                    showVerificationCard(currentUser)
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Error verifying user data: ${reloadTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
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
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                eyeIconPassword.setImageResource(R.drawable.openeye)
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
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

    // Show a dialog informing the user to verify their email
    private fun showVerificationCard(currentUser: FirebaseUser) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_verification, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)  // Prevent the user from closing it until they verify
            .create()

        val confirmButton = dialogView.findViewById<Button>(R.id.confirmVerificationButton)
        val generateLinkButton = dialogView.findViewById<Button>(R.id.generateVerificationLinkButton)
        val timerTextView = dialogView.findViewById<TextView>(R.id.timerTextView)

        // Set up the timer (1 minute countdown)
        val timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerTextView.text = "Time remaining: $secondsRemaining sec"
            }

            override fun onFinish() {
                // When timer finishes, enable "Generate new verification link" button
                generateLinkButton.isEnabled = true
            }
        }

        timer.start()

        // When the user confirms they have verified their email
        confirmButton.setOnClickListener {
            // Perform verification check before proceeding
            checkEmailVerificationStatus(dialog)
        }

        // When the user requests a new verification link
        generateLinkButton.setOnClickListener {
            // Generate a new verification link after the timeout
            sendVerificationEmailAgain()
        }

        dialog.show()
    }

    // Send the verification email again
    private fun sendVerificationEmailAgain() {
        val currentUser = auth.currentUser
        currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Verification email sent again!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to resend verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Check email verification status
    private fun checkEmailVerificationStatus(dialog: AlertDialog) {
        val currentUser = auth.currentUser
        currentUser?.reload()?.addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                // Perform a check again after reload
                if (currentUser.isEmailVerified) {
                    // Email verified, allow user to go to the Dashboard
                    val intent = Intent(this, Dashboard::class.java)
                    startActivity(intent)
                    finish()
                    dialog.dismiss()
                } else {
                    // Email not verified yet, show message again
                    Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Failed to reload user data.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
