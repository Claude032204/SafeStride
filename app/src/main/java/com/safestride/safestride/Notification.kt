package com.safestride.safestride

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Notification : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)  // Ensure this refers to the correct layout file

        // Find the CoordinatorLayout by its ID
        val notificationLayout = findViewById<CoordinatorLayout>(R.id.notification)

        // Apply Window Insets listener to adjust padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(notificationLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets  // Return the insets
        }


    // Sample notification data
        val notifications = listOf(
            Notif(
                "Emergency Alert!",
                "The patient pressed the RED button for an emergency.",
                "5 mins ago"
            ),
            Notif(
                "Need Assistance",
                "The patient pressed the Yellow button for assistance.",
                "10 mins ago"
            ),
            Notif("Track Location", "The patient was outside the safezone!", "40 mins ago"),
            Notif(
                "Need Assistance",
                "The patient pressed the Yellow button for assistance.",
                "1 hour ago"
            )
        )

        // Set up RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerNotifications)
        val adapter = NotificationAdapter(notifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Back button functionality
        findViewById<ImageView>(R.id.backArrowIcon).setOnClickListener {
            onBackPressed()  // Go back to the previous screen
        }
        val settingsIcon: ImageView = findViewById(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }
    }
}