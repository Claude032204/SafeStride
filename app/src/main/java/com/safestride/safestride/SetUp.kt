package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SetUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle the "Connect Device" button click
        val connectDeviceButton: Button = findViewById(R.id.connectDeviceButton)
        connectDeviceButton.setOnClickListener {
            val intent = Intent(this, Connect::class.java)
            startActivity(intent)
        }

        // Handle the "Back Arrow" click
        val backArrowIcon: ImageView = findViewById(R.id.backArrowIcon)
        backArrowIcon.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish() // Optional: Close the current activity to prevent returning here with "back"
        }
    }
}

