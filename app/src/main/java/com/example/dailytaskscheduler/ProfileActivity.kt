package com.example.dailytaskscheduler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.dailytaskscheduler.databinding.ActivityChangePasswordBinding
import com.example.dailytaskscheduler.databinding.ActivityProfileBinding
import com.example.dailytaskscheduler.util.Task
import com.example.dailytaskscheduler.util.User
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId").toString()
        Log.d("userId", userId)

        binding.btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        db.collection("User").document(userId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val user = document.toObject(User::class.java)
                Log.d("user",user.toString())
                if (user != null) {
                    binding.tvName.text = user.username
                }
            } else {

            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}