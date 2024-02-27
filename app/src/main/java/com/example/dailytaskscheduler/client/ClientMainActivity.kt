package com.example.dailytaskscheduler.client

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.admin.AdminColAdapter
import com.example.dailytaskscheduler.admin.AdminDetailActivity
import com.example.dailytaskscheduler.admin.TaskAdapter
import com.example.dailytaskscheduler.databinding.ActivityMainClientBinding
import com.example.dailytaskscheduler.util.Task
import com.google.firebase.firestore.FirebaseFirestore

class ClientMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainClientBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var userId: String = ""
    private var currentOption: String = ""
    private var selectedOption: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init database
        db = FirebaseFirestore.getInstance()

        //init values
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        userId = sharedPreferencesHelper.userId

        binding.btnAddTask.setOnClickListener{
            val intent = Intent(this, ClientAddActivity::class.java)
            startActivity(intent)
        }

        initRecyclerView()
        spinnerOption()
    }

    private fun initRecyclerView() {
        binding.clientRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        adapter = TaskAdapter { selectedItem: Task -> listItemClicked(selectedItem) }
        binding.clientRecyclerView.adapter = adapter
        displayTask()
    }

    private fun displayTask() {
        db.collection("User").document(userId).get()
            .addOnSuccessListener { userResult ->
                val username = userResult.getString("username")
                if (username != null) {
                    // fetch tasks where the collaborator list contains the username
                    db.collection("Task").whereArrayContains("collaborator", username).get()
                        .addOnSuccessListener { result ->
                            val taskList = result.toObjects(Task::class.java)
                            taskList.sortByDescending { it.date }
                            Log.d("Task", "Task List: $taskList")
                            adapter.updateList(taskList)
                        }
                        .addOnFailureListener { e ->
                            Log.d("Task", "Error getting tasks: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.d("User", "Error getting user: $e")
            }
    }

    private fun listItemClicked(task: Task){
        val intent = Intent(this, ClientUpdateActivity::class.java)
        intent.putExtra("taskId", task.taskId)
        startActivity(intent)
    }

    private fun spinnerOption() {
        ArrayAdapter.createFromResource(
            this,
            R.array.option,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.option.adapter = adapter

            val position = adapter.getPosition(currentOption)
            binding.option.setSelection(position)
        }

        binding.option.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedOption = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }
}