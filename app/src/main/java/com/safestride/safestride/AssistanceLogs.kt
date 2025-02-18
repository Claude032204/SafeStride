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

class AssistanceLogs : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AssistanceLogsAdapter
    private val dummyLogs = mutableListOf(
        "2:00 PM, Jan 26: Assistance requested. Caregiver responded at 2:05 PM.",
        "4:15 PM, Jan 25: Help requested. Message sent to caregiver.",
        "9:00 AM, Jan 24: Assistance requested. Resolved within 10 minutes."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_assistance_logs)

        // Ensure this matches the ID in your XML layout
        val assistanceLayout = findViewById<RelativeLayout>(R.id.assistance)

        // Apply Window Insets listener
        ViewCompat.setOnApplyWindowInsetsListener(assistanceLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back Arrow Click Listener
        findViewById<View>(R.id.backArrowIcon).setOnClickListener {
            finish()
        }

    // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerAssistanceLogs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter with dummy logs
        adapter = AssistanceLogsAdapter(dummyLogs)
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
