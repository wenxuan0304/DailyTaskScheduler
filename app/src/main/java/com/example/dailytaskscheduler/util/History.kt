package com.example.dailytaskscheduler.util

import com.google.firebase.Timestamp

data class History(
    val updatedField: String = "",
    val oldValue: String = "",
    val newValue: String = "",
    val timestamp: Timestamp = Timestamp.now()
)