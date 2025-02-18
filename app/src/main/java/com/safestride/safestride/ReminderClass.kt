package com.safestride.safestride

import java.io.Serializable

data class ReminderClass(
    val title: String,
    val date: String,
    val time: String
) : Serializable // Make it Serializable
