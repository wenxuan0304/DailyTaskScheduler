package com.example.dailytaskscheduler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.dailytaskscheduler.admin.AdminMainActivity
import com.example.dailytaskscheduler.client.ClientMainActivity
import com.example.dailytaskscheduler.databinding.ActivityMainBinding
import com.example.dailytaskscheduler.user.AddUserActivity
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private var userID = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        val adminIntent = Intent(this, AdminMainActivity::class.java)
        val clientIntent = Intent(this, ClientMainActivity::class.java)

        binding.btnSubmit.setOnClickListener {
            loginUser(adminIntent, clientIntent)
        }

        binding.btnAddUser.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(adminIntent: Intent, clientIntent: Intent) {
        val username = binding.username.text.toString()
        val password = binding.password.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Username or Password should not be empty")
        } else {
            db.collection("User").whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { result ->
                    if (result.documents.isNotEmpty()) {
                        val storedPassword = result.documents[0].getString("password")

                        if (password == storedPassword) {
                            userID = result.documents[0].id
                            val role = result.documents[0].getString("role")
                            when (role) {
                                "Admin" -> {
                                    adminIntent.putExtra("userId", userID)
                                    startActivity(adminIntent)
                                    finish()
                                }
                                "Client" -> {
                                    clientIntent.putExtra("userId", userID)
                                    startActivity(clientIntent)
                                    finish()
                                }
                                else -> showToast("Invalid Role")
                            }
                        } else {
                            showToast("Password not matched")
                        }
                    } else {
                        showToast("User not found")
                    }
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}