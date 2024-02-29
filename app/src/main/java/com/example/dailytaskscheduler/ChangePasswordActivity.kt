package com.example.dailytaskscheduler

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.dailytaskscheduler.databinding.ActivityChangePasswordBinding
import com.google.firebase.firestore.FirebaseFirestore

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var db: FirebaseFirestore
    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId").toString()
        Log.d("userId", userId)

        binding.changeBtn.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        db.collection("User").document(userId).get().addOnSuccessListener { result ->
            if (binding.etCurrent.text != null) {
                val password = result.getString("password")
                if (binding.etCurrent.text.toString() == password) {
                    Log.d("Password", "${binding.etCurrent.text} and $password")
                    if (binding.etNew.text.isNotEmpty() && binding.etConfirm.text.isNotEmpty()) {
                        if (binding.etNew.text.toString() == binding.etConfirm.text.toString()) {
                            db.collection("User").document(userId).update(
                                "password", binding.etConfirm.text.toString()
                            )
                            showToast("Password changed")
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        } else {
                            showToast("Password not match")
                        }
                    } else {
                        showToast("Password should not be empty")
                    }
                } else {
                    showToast("Current Password incorrect")
                }
            } else {
                showToast("Password should not be empty")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}