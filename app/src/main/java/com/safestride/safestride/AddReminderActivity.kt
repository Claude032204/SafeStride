package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddReminderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_reminder)

        findViewById<LinearLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val reminderMessageInput = findViewById<EditText>(R.id.reminderMessageInput)
        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val addReminderButton = findViewById<Button>(R.id.addReminderButton)
        val toggleDateTimeButton = findViewById<ImageView>(R.id.toggleDateTimeButton) // New button to toggle visibility

        // Set TimePicker to 24-hour format
        timePicker.setIs24HourView(true)

        // Initially hide the DatePicker and TimePicker
        datePicker.visibility = View.GONE
        timePicker.visibility = View.GONE

        // Back Arrow Click Listener
        findViewById<ImageView>(R.id.backArrowIcon).setOnClickListener {
            finish()
        }

        // Toggle Date and Time Pickers Visibility
        toggleDateTimeButton.setOnClickListener {
            if (datePicker.visibility == View.GONE) {
                datePicker.visibility = View.VISIBLE
                timePicker.visibility = View.VISIBLE
            } else {
                datePicker.visibility = View.GONE
                timePicker.visibility = View.GONE
            }
        }

        // Inside AddReminderActivity
        addReminderButton.setOnClickListener {
            val reminderMessage = reminderMessageInput.text.toString()
            val day = datePicker.dayOfMonth
            val month = datePicker.month + 1 // Month is 0-based
            val year = datePicker.year
            val hour = timePicker.hour
            val minute = timePicker.minute

            if (reminderMessage.isNotEmpty()) {
                // Format the date and time
                val date = "$day/$month/$year"
                val time = "$hour:$minute"

                // Create the ReminderItem object
                val reminderItem = ReminderClass(reminderMessage, date, time)

                // Pass the ReminderItem back to the Reminder Activity
                val resultIntent = Intent()
                resultIntent.putExtra("REMINDER", reminderItem) // Pass the object
                setResult(RESULT_OK, resultIntent)
                finish() // Close the AddReminderActivity
            } else {
                // Show a Toast if reminder message is empty
                Toast.makeText(this, "Please enter a reminder message", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
