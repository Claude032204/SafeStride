package com.safestride.safestride

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(private val notificationList: List<Notif>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    // ViewHolder to hold views for each notification item
    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.notificationTitle)
        val detailsTextView: TextView = itemView.findViewById(R.id.notificationDetails)
        val timestampTextView: TextView = itemView.findViewById(R.id.notificationTimestamp)
    }

    // Inflate the layout for each notification item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)  // Use your item layout file
        return NotificationViewHolder(view)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.titleTextView.text = notification.title
        holder.detailsTextView.text = notification.details
        holder.timestampTextView.text = notification.timestamp
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }
}
