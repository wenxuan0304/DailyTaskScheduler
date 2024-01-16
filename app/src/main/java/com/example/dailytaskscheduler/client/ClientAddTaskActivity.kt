package com.example.dailytaskscheduler.client

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.admin.AdminColAdapter
import com.example.dailytaskscheduler.databinding.ActivityClientAddTaskBinding
import com.example.dailytaskscheduler.util.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class ClientAddTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClientAddTaskBinding
    private lateinit var db: FirebaseFirestore

    private var userId: String = ""
    private lateinit var usernameList: MutableList<String>
    private lateinit var rvNames: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>

    private lateinit var selectedDate: LocalDate
    private lateinit var adminColAdapter: AdminColAdapter
    private lateinit var taskId: String
    private lateinit var timestamp: Timestamp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("userId").toString()
        Log.d("admin", userId)

        usernameList = mutableListOf()
        rvNames = mutableListOf()
        taskId = generateUniqueId()

        db = FirebaseFirestore.getInstance()
        selectedDate = LocalDate.now()
        val instant = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        timestamp = Timestamp(instant.toEpochMilli() / 1000, (instant.toEpochMilli() % 1000 * 1000000).toInt())

        initRecyclerView()
        calendar()

        binding.btnSave.setOnClickListener{
            createTask()
        }
        binding.btCancel.setOnClickListener{
            val intent = Intent(this@ClientAddTaskActivity, ClientMainActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            finish()
        }
    }

    private fun initRecyclerView(){
        db.collection("Task").document(taskId).get()
            .addOnSuccessListener { result ->
                if(result.exists()){
                    val collaboratorList = result.get("collaborator") as MutableList<String>
                    if(collaboratorList!=null){
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
            LinearLayoutManager(this@ClientAddTaskActivity, LinearLayoutManager.VERTICAL, false)
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
                            }
                    }
            }
        }

        val intent = Intent(this@ClientAddTaskActivity, ClientMainActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
        finish()
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

    private fun generateUniqueId(): String{
        return UUID.randomUUID().toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}