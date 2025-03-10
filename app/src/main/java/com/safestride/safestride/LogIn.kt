package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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

            // Check if both email and password fields are not empty
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Firebase Authentication login
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, Dashboard::class.java)
                            startActivity(intent)
                            finish()
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
}
