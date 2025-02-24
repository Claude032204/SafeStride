package com.safestride.safestride

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val message = intent.getStringExtra("REMINDER_MESSAGE") ?: "Reminder Alert!"
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val db = FirebaseFirestore.getInstance()
        val notification = hashMapOf(
            "title" to "Reminder Alert!",
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        // 🔹 Store the reminder **only when it is due**
        db.collection("notifications").document(userId)
            .collection("reminders").add(notification)
            .addOnSuccessListener {
                Toast.makeText(context, "Reminder: $message", Toast.LENGTH_SHORT).show()
            }
    }
}
