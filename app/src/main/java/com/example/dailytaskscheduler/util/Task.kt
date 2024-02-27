package com.example.dailytaskscheduler.util

import com.google.firebase.Timestamp


data class Task(
    val taskId: String = "",
    val title: String = "",
    val content: String = "",
    val date: Timestamp = Timestamp.now(),
    val fileList: List<Map<String, String>>? = null,
    val userId: String = "",
    val collaborator: MutableList<String>? = null,
    val status: String = ""
)
