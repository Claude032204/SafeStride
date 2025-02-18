package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmergencyLogs : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EmergencyLogsAdapter
    private val dummyLogs = mutableListOf(
        "10:15 AM, Jan 26: Emergency triggered. Resolved by caregiver at 10:20 AM.",
        "3:30 PM, Jan 25: Emergency triggered. Ambulance dispatched.",
        "8:00 AM, Jan 24: Emergency triggered. Caregiver responded within 5 minutes."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emergency_logs)

        val emergencyLayout = findViewById<RelativeLayout>(R.id.emergency) // Ensure this matches the ID in your XML layout
            // Apply Window Insets listener
            ViewCompat.setOnApplyWindowInsetsListener(emergencyLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }

        // Back Arrow Click Listener
        findViewById<View>(R.id.backArrowIcon).setOnClickListener {
            finish()
        }

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerEmergencyLogs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with dummy logs
        adapter = EmergencyLogsAdapter(dummyLogs)
        recyclerView.adapter = adapter
    }

    // Handle the result from AddReportActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val date = data?.getStringExtra("DATE")
            val description = data?.getStringExtra("DESCRIPTION")
            if (date != null && description != null) {
                val newLog = "$date: $description"
                dummyLogs.add(newLog)
                adapter.notifyDataSetChanged()
            }
        }
    }
}