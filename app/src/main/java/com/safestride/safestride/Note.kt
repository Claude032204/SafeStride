package com.safestride.safestride

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class Note : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotesAdapter
    private val notesList = mutableListOf<String>() // Empty list, no sample notes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_note)

        // Ensure the ID matches the one in your XML layout
        val noteLayout = findViewById<RelativeLayout>(R.id.note)

        // Apply Window Insets listener
        ViewCompat.setOnApplyWindowInsetsListener(noteLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    recyclerView = findViewById(R.id.recyclerNotes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotesAdapter(notesList)
        recyclerView.adapter = adapter

        // Back Arrow Click Listener
        findViewById<View>(R.id.backArrowIcon).setOnClickListener {
            finish() // Close the current activity and return to the previous one
        }

        // Add swipe-to-delete functionality
        val itemTouchHelperCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false // We don't need dragging
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        // Delete the swiped note
                        notesList.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        Toast.makeText(this@Note, "Note deleted", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onChildDraw(
                    c: android.graphics.Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )

                    // Only swipe left (dX < 0) for delete icon
                    if (dX < 0) {
                        val deleteIcon = ContextCompat.getDrawable(
                            viewHolder.itemView.context,
                            R.drawable.delete
                        ) // Use itemView context
                        deleteIcon?.let {
                            val itemView = viewHolder.itemView
                            val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                            val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                            val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                            val iconRight = itemView.right - iconMargin
                            val iconBottom = iconTop + it.intrinsicHeight

                            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            it.draw(c)
                        }
                    }
                }
            }

        // Attach ItemTouchHelper to RecyclerView
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        val fabAddNote = findViewById<FloatingActionButton>(R.id.fabAddNote)
        fabAddNote.setOnClickListener {
            // When the FAB is clicked, navigate to AddNoteActivity
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivityForResult(
                intent,
                1
            )  // This will start AddNoteActivity and expects a result
        }

        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val child = rv.findChildViewUnder(e.x, e.y)
                if (child != null) {
                    val position = rv.getChildAdapterPosition(child)
                    val noteContent = notesList[position]

                    val intent = Intent(this@Note, AddNoteActivity::class.java)
                    intent.putExtra("NOTE_CONTENT", noteContent)
                    intent.putExtra(
                        "NOTE_POSITION",
                        position
                    ) // Pass the position to edit or delete the note
                    startActivityForResult(intent, 2) // Open AddNoteActivity for editing
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Add new note from AddNoteActivity
            val newNote = data?.getStringExtra("NOTE")
            if (!newNote.isNullOrEmpty()) {
                notesList.add(newNote) // Add new note to the list
                adapter.notifyDataSetChanged() // Update RecyclerView
            }
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            // Handle the updated note from AddNoteActivity
            val updatedNote = data?.getStringExtra("UPDATED_NOTE")
            val position = data?.getIntExtra("NOTE_POSITION", -1) // Get the position to update
            if (!updatedNote.isNullOrEmpty() && position != null && position >= 0) {
                if (position < notesList.size) { // Ensure position is within bounds
                    notesList[position] = updatedNote // Update the note at that position
                    adapter.notifyItemChanged(position) // Notify RecyclerView to update that item
                }
            }

            // Handle deletion result
            if (data?.getBooleanExtra("DELETE_NOTE", false) == true) {
                val position = data?.getIntExtra("NOTE_POSITION", -1) ?: -1
                if (position >= 0 && position < notesList.size) { // Check if position is valid
                    notesList.removeAt(position) // Remove the note at the position
                    adapter.notifyItemRemoved(position) // Notify the adapter that the note has been removed
                    Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}