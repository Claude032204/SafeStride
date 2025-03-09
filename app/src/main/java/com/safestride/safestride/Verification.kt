package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import com.google.firebase.auth.FirebaseAuth

class Verification : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var resendCodeText: TextView
    private lateinit var verificationCodeField: EditText
    private lateinit var confirmCodeButton: Button
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 60000  // 1 minute
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        // Find the RelativeLayout by its ID
        val verifyLayout = findViewById<RelativeLayout>(R.id.verify)

        // Apply Window Insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(verifyLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // Back Arrow Click Listener
        findViewById<ImageView>(R.id.backArrowIcon).setOnClickListener {
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
            finish()
        }

        // Retrieve the email passed from ForgotPasswordActivity
        val userEmail = intent.getStringExtra("user_email")

        // Set the email text dynamically in the TextView
        val verificationSentText: TextView = findViewById(R.id.verificationSentText)
        verificationSentText.text = "Verification sent to: $userEmail"

        // Initialize views for verification code input and button
        verificationCodeField = findViewById(R.id.verificationCodeField)
        confirmCodeButton = findViewById(R.id.confirmCodeButton)
        timerText = findViewById(R.id.timerText)
        resendCodeText = findViewById(R.id.resendCodeText)

        // Start the timer
        startTimer()

        // Resend code click listener
        resendCodeText.setOnClickListener {
            resendVerificationCode(userEmail)
            restartTimer()
        }

        // Confirm Code Button click listener
        confirmCodeButton.setOnClickListener {
            val code = verificationCodeField.text.toString()
            if (code.isNullOrEmpty()) {
                Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show()
            } else {
                verifyResetCode(code)
            }
        }
    }

    // Start the countdown timer
    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                val seconds = (millisUntilFinished / 1000).toInt()
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                timerText.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                timerText.text = "00:00"
                resendCodeText.isClickable = true  // Enable the "Resend" text when timer finishes
            }
        }.start()
    }

    // Restart the timer for resend functionality
    private fun restartTimer() {
        timeLeftInMillis = 60000  // Reset timer to 1 minute
        timer?.cancel()
        startTimer()  // Start the timer again
    }

    // Resend verification code logic
    private fun resendVerificationCode(email: String?) {
        if (!email.isNullOrEmpty()) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to resend email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // Verify the password reset code entered by the user
    private fun verifyResetCode(code: String) {
        auth.verifyPasswordResetCode(code)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // If the verification code is valid, proceed to the NewPassword page
                    val intent = Intent(this, NewPassword::class.java)
                    intent.putExtra("verificationCode", code)  // Pass the valid code to NewPassword activity
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid verification code", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
