package com.example.dailytaskscheduler.admin

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.SharedPreferencesHelper
import com.example.dailytaskscheduler.databinding.ActivityAddTaskBinding
import com.example.dailytaskscheduler.user.UserAdapter
import com.example.dailytaskscheduler.util.Task
import com.example.dailytaskscheduler.util.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class AddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTaskBinding
    private lateinit var db: FirebaseFirestore

    private var userId: String = ""
    private lateinit var usernameList: MutableList<String>
    private lateinit var rvNames: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var selectedDate: LocalDate
    private lateinit var adminColAdapter: AdminColAdapter
    private lateinit var taskId: String
    private lateinit var timestamp: Timestamp
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesHelper = SharedPreferencesHelper(this)
        userId = sharedPreferencesHelper.userId

        usernameList = mutableListOf()
        rvNames = mutableListOf()
        taskId = generateUniqueId()

        db = FirebaseFirestore.getInstance()
        selectedDate = LocalDate.now()
        val instant = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        timestamp = Timestamp(
            instant.toEpochMilli() / 1000,
            (instant.toEpochMilli() % 1000 * 1000000).toInt()
        )

        calendar()

        spinner()
        initRecyclerView()
        binding.btnSave.setOnClickListener {
            createTask()
        }
        binding.btCancel.setOnClickListener {
            val intent = Intent(this@AddTaskActivity, AdminMainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun spinner() {
        val spinner = binding.spinnerTeam

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
                    this@AddTaskActivity,
                    android.R.layout.simple_spinner_dropdown_item,
                    usernameList
                )
                usernameList.add(0, "Select Member")
                adapter.notifyDataSetChanged()
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }.addOnFailureListener {
                Log.d("User", "User fail to retrieve")
            }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                    spinner.setSelection(0)
                }

                Log.d("rvNames", rvNames.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
    }

    private fun initRecyclerView() {
        db.collection("Task").document(taskId).get()
            .addOnSuccessListener { result ->
                if (result.exists()) {
                    val collaboratorList = result.get("collaborator") as MutableList<String>
                    if (collaboratorList != null) {
                        rvNames.addAll(collaboratorList.mapNotNull { it })
                    }
                }
            }

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

        binding.rvTeam.layoutManager =
            LinearLayoutManager(this@AddTaskActivity, LinearLayoutManager.VERTICAL, false)
        binding.rvTeam.adapter = adminColAdapter
    }

    private fun createTask() {
        when {
            binding.etTitle.text.isNullOrEmpty() -> showToast("Enter the title")
            binding.etContent.text.isNullOrEmpty() -> showToast("Enter the content")

            else -> {
                val title = binding.etTitle.text.toString()
                val content = binding.etContent.text.toString()
                val date = timestamp
                val team: MutableList<String> = mutableListOf()

                for(name in rvNames){
                    team.add(name)
                }

                db.collection("User").whereEqualTo("userId", userId).get()
                    .addOnSuccessListener { result ->
                        for (currentUser in result.documents) {
                            val currentUserUsername = currentUser.getString("username")
                            if (currentUserUsername != null && !team.contains(currentUserUsername)) {
                                team.add(currentUserUsername)
                            }
                        }

                        db.collection("User").whereEqualTo("role", "Admin").get()
                            .addOnSuccessListener { adminResult ->
                                for (userAdmin in adminResult.documents) {
                                    val adminUsername = userAdmin.getString("username")
                                    if (adminUsername != null && !team.contains(adminUsername)) {
                                        team.add(adminUsername)
                                    }
                                }

                                val task = Task(
                                    taskId = taskId,
                                    title = title,
                                    content = content,
                                    date = date,
                                    userId = userId,
                                    collaborator = team,
                                    status = "Pending"
                                )
                                db.collection("Task").document(taskId).set(task)
                                    .addOnSuccessListener {
                                        showToast("Task Added Successfully")
                                    }
                                    .addOnFailureListener {
                                        showToast("Error: ${it.message}")
                                    }


                                val intent = Intent(this@AddTaskActivity, AdminMainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
            }
        }
    }

    private fun calendar() {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
        val formattedDate = formatter.format(selectedDate)

        val editTextDate = binding.etDate
        editTextDate.setText(formattedDate)

        editTextDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this, { _, year, monthOfYear, dayOfMonth ->
                    selectedDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.getDefault())
                    val formattedDate = formatter.format(selectedDate)

                    val editTextDate = binding.etDate
                    editTextDate.setText(formattedDate)
                },
                selectedDate.year,
                selectedDate.monthValue - 1,
                selectedDate.dayOfMonth
            )

            datePickerDialog.show()
        }
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}