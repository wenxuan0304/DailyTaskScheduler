package com.example.dailytaskscheduler.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.dailytaskscheduler.MainActivity
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.databinding.ActivityAddUserBinding
import com.example.dailytaskscheduler.util.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class AddUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddUserBinding
    private lateinit var role: String
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        spinner()

        binding.btnAddUser.setOnClickListener {
            addUser(role)
        }

        binding.btnDelete.setOnClickListener{
            val intent = Intent(this, DeleteUserActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun spinner() {
        val spinner = binding.role

        ArrayAdapter.createFromResource(
            this@AddUserActivity,
            R.array.role,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                role = parent!!.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle nothing selected if needed
            }
        }
    }

    private fun addUser(role:String){

        binding.apply {
            if (!binding.username.text.isNullOrEmpty()) {
                val userName = binding.username.text.toString()
                var password = ""
                if (role == "Client") {
                    password = userName + "123c"
                } else {
                    password = userName + "123a"
                }

                val userId = generateUniqueId()
                val user = User(userId,userName,password,role)

                db.collection("User")
                    .document(userId)
                    .set(user)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@AddUserActivity,
                            "User added successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this@AddUserActivity,
                            "Error ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                val intent = Intent(
                    this@AddUserActivity,
                    MainActivity::class.java
                )
                startActivity(intent)
                finish()
            }

            else{
                Toast.makeText(
                    this@AddUserActivity,
                    "Username is null",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun generateUniqueId(): String{
        return UUID.randomUUID().toString()
    }
}