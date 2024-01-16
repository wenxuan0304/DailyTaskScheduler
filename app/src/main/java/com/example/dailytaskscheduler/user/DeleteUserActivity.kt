package com.example.dailytaskscheduler.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.databinding.ActivityDeleteUserBinding
import com.example.dailytaskscheduler.util.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects

class DeleteUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeleteUserBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeleteUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.userRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)

        userAdapter = UserAdapter { selectedUser: User -> listItemClicked(selectedUser) }
        binding.userRecyclerView.adapter = userAdapter

        getAllUser()
    }

    private fun getAllUser(){
        db.collection("User").get()
            .addOnSuccessListener { result ->
                val userList = result.toObjects(User::class.java)
                userAdapter.updateUserList(userList)
            }
            .addOnFailureListener{
                Log.d("User", "User fail to retrieve")
            }
    }

    private fun listItemClicked(selectedUser: User) {
        deleteUser(selectedUser)
    }

    private fun deleteUser(user: User){
        db.collection("User").document(user.userId).delete()
            .addOnSuccessListener {
                getAllUser()
                Toast.makeText(this, "User deleted successfully",Toast.LENGTH_SHORT).show()
            }
    }
}