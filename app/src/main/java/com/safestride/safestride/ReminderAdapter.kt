package com.safestride.safestride

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private val remindersList: MutableList<ReminderClass>,
    private val deleteReminder: (ReminderClass) -> Unit
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

        // Show "Done" button when clicked
        holder.itemView.setOnClickListener {
            holder.doneButton.visibility = if (holder.doneButton.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        // Delete reminder on "Done" button click
        holder.doneButton.setOnClickListener {
            deleteReminder(reminder)
        }
    }

    override fun getItemCount(): Int = remindersList.size
}
