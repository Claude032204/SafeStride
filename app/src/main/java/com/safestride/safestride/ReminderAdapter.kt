package com.safestride.safestride

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private val remindersList: MutableList<ReminderClass>,
    private val saveReminders: () -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reminderTitle: TextView = itemView.findViewById(R.id.reminderTitle)
        val reminderDate: TextView = itemView.findViewById(R.id.reminderDate)
        val reminderTime: TextView = itemView.findViewById(R.id.reminderTime)
        val doneButton: Button = itemView.findViewById(R.id.doneButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.reminder_item, parent, false)
        return ReminderViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = remindersList[position]
        holder.reminderTitle.text = reminder.title
        holder.reminderDate.text = reminder.date
        holder.reminderTime.text = reminder.time

        // Initially hide the "Done" button
        holder.doneButton.visibility = View.GONE

        // Handle item click to toggle the "Done" button visibility
        holder.itemView.setOnClickListener {
            // Toggle the visibility of the "Done" button
            if (holder.doneButton.visibility == View.VISIBLE) {
                holder.doneButton.visibility = View.GONE
            } else {
                holder.doneButton.visibility = View.VISIBLE
            }
        }

        // Handle "Done" button click to delete the reminder
        holder.doneButton.setOnClickListener {
            if (position >= 0 && position < remindersList.size) {
                remindersList.removeAt(position) // Remove the reminder from the list
                saveReminders() // Save the updated list to SharedPreferences
                notifyItemRemoved(position) // Notify the adapter about the change
            }
        }
    }

    override fun getItemCount(): Int {
        return remindersList.size
    }
}
