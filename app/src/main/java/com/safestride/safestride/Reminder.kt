package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.CalendarView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Reminder : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter
    private val remindersList = mutableListOf<ReminderClass>()

    private val ADD_REMINDER_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reminder)

        findViewById<LinearLayout>(R.id.reminder)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reminder)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // RecyclerView Setup
        recyclerView = findViewById(R.id.recyclerReminders)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Adapter Setup
        adapter = ReminderAdapter(remindersList) { saveReminders() }
        recyclerView.adapter = adapter

        // Load saved reminders from SharedPreferences after adapter is initialized
        loadReminders()

        // Calendar Setup
        val calendarView: CalendarView = findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            Log.d("Calendar", "Selected Date: $dayOfMonth/${month + 1}/$year")
        }

        // Add Reminder Button
        findViewById<FloatingActionButton>(R.id.fabAddReminder).setOnClickListener {
            // Start AddReminderActivity to add a new reminder
            val intent = Intent(this, AddReminderActivity::class.java)
            startActivityForResult(intent, ADD_REMINDER_REQUEST_CODE)
        }

        // Back Arrow Click Listener
        findViewById<ImageView>(R.id.backArrowIcon).setOnClickListener {
            finish() // Go back to the previous activity
        }
    }

    // Inside Reminder Activity (onCreate or some other method where you handle the result)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val reminderItem = data.getSerializableExtra("REMINDER") as? ReminderClass

            reminderItem?.let {
                // Now you can add the reminderItem to the list and update the UI
                remindersList.add(it)
                adapter.notifyDataSetChanged()  // Notify the adapter to update the list
                saveReminders()  // Save the updated list to SharedPreferences
            }
        }
    }

    fun loadReminders() {
        val sharedPreferences = getSharedPreferences("reminders", MODE_PRIVATE)
        val json = sharedPreferences.getString("reminders_list", null)

        // Check if the JSON is null or empty and return an empty list if so
        if (json.isNullOrEmpty()) {
            remindersList.clear()
            adapter.notifyDataSetChanged()
            return
        }

        val gson = Gson()
        val type = object : TypeToken<List<ReminderClass>>() {}.type

        try {
            val loadedReminders: List<ReminderClass> = gson.fromJson(json, type)
            remindersList.clear()
            remindersList.addAll(loadedReminders)
            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            // Handle the case where deserialization fails, e.g., corrupted data
            Log.e("Reminder", "Failed to load reminders: ${e.message}")
            remindersList.clear()
            adapter.notifyDataSetChanged()
        }
    }

    fun saveReminders() {
        val gson = Gson()
        val json = gson.toJson(remindersList)
        val sharedPreferences = getSharedPreferences("reminders", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        try {
            editor.putString("reminders_list", json)
            editor.apply()
        } catch (e: Exception) {
            // Handle any potential errors during saving (e.g., SharedPreferences failure)
            Log.e("Reminder", "Failed to save reminders: ${e.message}")
        }
    }
}
