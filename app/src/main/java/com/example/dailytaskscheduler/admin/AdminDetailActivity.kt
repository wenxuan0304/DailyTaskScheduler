package com.example.dailytaskscheduler.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.databinding.ActivityAdminDetailBinding
import com.example.dailytaskscheduler.databinding.ListItemUserBinding
import com.example.dailytaskscheduler.util.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale


class AdminDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var usernameList: MutableList<String>
    private lateinit var rvNames: MutableList<String>
    private lateinit var adminColAdapter: AdminColAdapter
    private lateinit var adapter: ArrayAdapter<String>

    private var taskId: String = ""
    private var userId: String = ""
    private var currentStatus: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        taskId = intent.getStringExtra("taskId").toString()
        userId = intent.getStringExtra("userId").toString()
        usernameList = mutableListOf()
        rvNames = mutableListOf()

        db.collection("Task").document(taskId).get()
            .addOnSuccessListener { result ->
                if (result.exists()) {
                    val collaboratorList = result.get("collaborator") as MutableList<String>
                    if (collaboratorList != null) {
                        rvNames.addAll(collaboratorList.mapNotNull { it })
                    }
                    runOnUiThread {
                        binding.tvTitle.text = result.get("title").toString()
                        binding.tvContent.text = result.get("content").toString()
                        val userId = result.getString("userId") ?: ""
                        db.collection("User").document(userId).get()
                            .addOnSuccessListener { userResult ->
                                val username = userResult.getString("username") ?: ""
                                binding.tvName.text = username
                            }
                            .addOnFailureListener {
                                Log.d("User", "Failed to retrieve username")
                            }
                        val timestamp = result.getTimestamp("date")
                        if (timestamp != null) {
                            val date = timestamp.toDate()
                            val formattedDate =
                                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
                            binding.tvDate.text = formattedDate
                        }
                        binding.rvTeam.layoutManager =
                            LinearLayoutManager(
                                this@AdminDetailActivity,
                                LinearLayoutManager.VERTICAL,
                                false
                            )
                        adminColAdapter = AdminColAdapter(rvNames) { selectedItem: String ->
                            if (!rvNames.contains(selectedItem)) {
                                rvNames.add(selectedItem)
                                adminColAdapter.notifyDataSetChanged()
                                usernameList.remove(selectedItem)
                                adapter.notifyDataSetChanged()
                            } else {
                                rvNames.remove(selectedItem)
                                adminColAdapter.notifyDataSetChanged()
                                usernameList.add(selectedItem)
                                adapter.notifyDataSetChanged()
                            }
                        }
                        binding.rvTeam.adapter = adminColAdapter
                    }

                }
            }

        spinnerTeam()
        spinnerOption()

        binding.taskProgress.setOnClickListener {
            val intent = Intent(this@AdminDetailActivity, AdminTaskProgressActivity::class.java)
            intent.putExtra("taskId", taskId)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }

    private fun spinnerTeam() {
        val spinnerTeam = binding.spinnerTeam

        db.collection("User").get()
            .addOnSuccessListener { result ->
                val userList = result.toObjects(User::class.java)
                for (user in userList) {
                    if (user.userId != userId && user.username.isNotBlank()) {
                        if (!rvNames.contains(user.username)) {
                            usernameList.add(user.username)
                        }
                    }
                }

                adapter = ArrayAdapter(
                    this@AdminDetailActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    usernameList
                )
                usernameList.add(0, "Select Member")
                adapter.notifyDataSetChanged()
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTeam.adapter = adapter
            }
            .addOnFailureListener {
                Log.d("User", "User fail to retrieve")
            }

        spinnerTeam.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedName = parent?.getItemAtPosition(position).toString()
                if (selectedName != "Select Member") {
                    rvNames.add(selectedName)
                    adminColAdapter.notifyDataSetChanged()
                    usernameList.remove(selectedName)
                    adapter.notifyDataSetChanged()
                    spinnerTeam.setSelection(0)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun spinnerOption() {
        val spinnerOption = binding.spinnerOption

        ArrayAdapter.createFromResource(
            this@AdminDetailActivity,
            R.array.task_status,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerOption.adapter = adapter

            db.collection("Task").document(taskId).get()
                .addOnSuccessListener { result ->
                    if (result.exists()) {
                        currentStatus = result.get("status") as String
                        val position = adapter.getPosition(currentStatus)
                        spinnerOption.setSelection(position)
                    }
                }

            spinnerOption.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedStatus = parent?.getItemAtPosition(position).toString()
                    binding.btnDone.setOnClickListener {
                        db.collection("Task").document(taskId).update(
                            "status", selectedStatus,
                            "collaborator", rvNames
                        ).addOnSuccessListener {
                            Toast.makeText(
                                applicationContext,
                                "User update successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        val intent = Intent(this@AdminDetailActivity, AdminMainActivity::class.java)
                        intent.putExtra("userId", userId)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }
    }
}