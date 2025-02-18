package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LandingPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_landing_page)

        // Find the RelativeLayout by its ID
        val landingLayout = findViewById<RelativeLayout>(R.id.landing)

        // Apply Window Insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(landingLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
    }


    // Log In Button Click Listener
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            // Navigate to Log In activity
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }

        // Sign Up Button Click Listener
        findViewById<Button>(R.id.signupButton).setOnClickListener {
            // Navigate to Sign Up activity
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
    }
}