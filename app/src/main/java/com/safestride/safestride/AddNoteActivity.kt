package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddNoteActivity : AppCompatActivity() {

    private var notePosition: Int? = null // Store the position of the note being edited or deleted

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_note)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve the note content and position from the intent
        val noteContent = intent.getStringExtra("NOTE_CONTENT")
        notePosition = intent.getIntExtra("NOTE_POSITION", -1) // Get the position of the note

        val noteInput = findViewById<EditText>(R.id.noteInput)
        noteInput.setText(noteContent) // Set the note content in the input field

        // Back Arrow Click Listener
        findViewById<View>(R.id.backArrowIcon).setOnClickListener {
            finish() // Navigate back to NotesActivity
        }

        // Save Icon Click Listener
        findViewById<View>(R.id.saveIcon).setOnClickListener {
            val noteText = noteInput.text.toString()
            if (noteText.isNotEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("NOTE", noteText)
                setResult(RESULT_OK, resultIntent)
                finish() // Return to NotesActivity with the new note
            } else {
                Toast.makeText(this, "Please enter a note", Toast.LENGTH_SHORT).show()
            }
        }

        // Delete Icon Click Listener
        findViewById<View>(R.id.deleteIcon).setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Discard Note?")
            .setMessage("Are you sure you want to discard this note?")
            .setPositiveButton("Discard") { _, _ ->
                val resultIntent = Intent()
                resultIntent.putExtra("DELETE_NOTE", true)
                resultIntent.putExtra(
                    "NOTE_POSITION",
                    notePosition ?: -1
                ) // Ensure notePosition is valid
                setResult(RESULT_OK, resultIntent)
                finish() // Return to NotesActivity with the deleted note
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
