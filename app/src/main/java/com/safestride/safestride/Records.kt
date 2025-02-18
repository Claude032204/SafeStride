package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener

class Records : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_records)

        findViewById<RelativeLayout>(R.id.records)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.records)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Search Bar Logic
        val searchInput: EditText = findViewById(R.id.searchInput)
        val clearIcon: ImageView = findViewById(R.id.clearIcon)
        val filterIcon: ImageView = findViewById(R.id.filterIcon)
        val filterPopup: LinearLayout = findViewById(R.id.filterPopup)

        // Set click listeners for each card
        findViewById<View>(R.id.cardEmergencyLogs).setOnClickListener {
            startActivity(Intent(this, EmergencyLogs::class.java))
        }

        findViewById<View>(R.id.cardAssistanceLogs).setOnClickListener {
            startActivity(Intent(this, AssistanceLogs::class.java))
        }

        findViewById<View>(R.id.cardNotes).setOnClickListener {
            startActivity(Intent(this, Note::class.java))
        }

        // Find the back arrow icon by its ID
        val backArrowIcon: ImageView = findViewById(R.id.backArrowIcon)

        // Set a click listener to navigate back to the Dashboard
        backArrowIcon.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish() // Optional: Close the current activity to avoid stacking
        }


        // Show or hide the clear icon when typing in the search bar
        searchInput.addTextChangedListener { text ->
            clearIcon.visibility = if (text.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
        }

        // Clear search input when clicking the clear icon
        clearIcon.setOnClickListener {
            searchInput.text.clear()
            clearIcon.visibility = View.INVISIBLE // Hide clear icon after clearing text
        }

        // Filter Popup Logic
        filterIcon.setOnClickListener {
            // Toggle the visibility of the filter popup
            filterPopup.visibility = if (filterPopup.visibility == View.VISIBLE) {
                View.GONE // Hide if already visible
            } else {
                View.VISIBLE // Show if hidden
            }

        }

        // Handle other interactions with filter buttons (like Date range and Log types)
        val dateToday: Button = findViewById(R.id.dateToday)
        val dateYesterday: Button = findViewById(R.id.dateYesterday)
        val dateLast7Days: Button = findViewById(R.id.dateLast7Days)
        val dateLast30Days: Button = findViewById(R.id.dateLast30Days)
        val dateCustomRange: Button = findViewById(R.id.dateCustomRange)

        val logTypeEmergency: Button = findViewById(R.id.logTypeEmergency)
        val logTypeAssistance: Button = findViewById(R.id.logTypeAssistance)
        val logTypeNotes: Button = findViewById(R.id.logTypeNotes)

        // Filter buttons handling
        dateToday.setOnClickListener {
            Toast.makeText(this, "Filter by Today", Toast.LENGTH_SHORT).show()
        }

        dateYesterday.setOnClickListener {
            Toast.makeText(this, "Filter by Yesterday", Toast.LENGTH_SHORT).show()
        }

        dateLast7Days.setOnClickListener {
            Toast.makeText(this, "Filter by Last 7 Days", Toast.LENGTH_SHORT).show()
        }

        dateLast30Days.setOnClickListener {
            Toast.makeText(this, "Filter by Last 30 Days", Toast.LENGTH_SHORT).show()
        }

        dateCustomRange.setOnClickListener {
            Toast.makeText(this, "Filter by Custom Date Range", Toast.LENGTH_SHORT).show()
        }

        logTypeEmergency.setOnClickListener {
            Toast.makeText(this, "Filter by Emergency Logs", Toast.LENGTH_SHORT).show()
        }

        logTypeAssistance.setOnClickListener {
            Toast.makeText(this, "Filter by Assistance Logs", Toast.LENGTH_SHORT).show()
        }

        logTypeNotes.setOnClickListener {
            Toast.makeText(this, "Filter by Notes", Toast.LENGTH_SHORT).show()
        }

        // Apply the selected filters when the "Show Results" button is clicked
        val showResults: Button = findViewById(R.id.showResults)
        showResults.setOnClickListener {
            // You can apply the selected filters here and close the popup
            filterPopup.visibility = View.GONE // Hide the filter popup after applying filter
            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("TouchEvent", "Touch detected at ${ev?.x}, ${ev?.y}")
        return super.dispatchTouchEvent(ev)
    }
}