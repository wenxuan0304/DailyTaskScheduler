package com.example.dailytaskscheduler

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        const val USER_ID_KEY = "user_id"
        const val USERNAME_KEY = "username"
        const val TASK_ID_KEY = "task_id"
    }

    var userId: String
        get() = sharedPreferences.getString(USER_ID_KEY, null) ?: ""
        set(value) {
            editor.putString(USER_ID_KEY, value)
            editor.apply()
        }

    var username: String
        get() = sharedPreferences.getString(USERNAME_KEY, null) ?: ""
        set(value) {
            editor.putString(USERNAME_KEY, value)
            editor.apply()
        }

    var taskId: String
        get() = sharedPreferences.getString(TASK_ID_KEY, null) ?: ""
        set(value) {
            editor.putString(TASK_ID_KEY, value)
            editor.apply()
        }
}