package com.example.dailytaskscheduler.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.ChangePasswordActivity
import com.example.dailytaskscheduler.MainActivity
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.databinding.ActivityMainAdminBinding
import com.example.dailytaskscheduler.util.Task
import com.google.firebase.firestore.FirebaseFirestore

class AdminMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainAdminBinding
    private lateinit var adapter: TaskAdapter
    private lateinit var db: FirebaseFirestore

    private var userId: String = ""
    private var currentOption: String = ""
    private var selectedOption: String = ""
    private lateinit var userIds: List<String>
    private lateinit var userNames: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId").toString()
        Log.d("userId", userId)

        binding.btnAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

//        binding.btnLogout.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
//            finish()
//        }

        spinnerOption()

        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout,binding.toolbarAdmin, R.string.open, R.string.close
        )

        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this,R.color.white)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.nav_profile ->
                {
                    val intent = Intent(this, ChangePasswordActivity::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                }
                R.id.nav_change_password ->
                {
                    val intent = Intent(this, ChangePasswordActivity::class.java)
                    intent.putExtra("userId", userId)
                    startActivity(intent)
                }
                R.id.nav_logout ->
                {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

    }

    private fun search(newText: String?, taskList: MutableList<Task>) {
        val searchTask = if(newText.isNullOrBlank()){
            taskList
        }else{
            taskList.filter { task: Task ->
                task.title.contains(newText, ignoreCase = true) ||  task.date.toString().contains(newText, ignoreCase = true) ||
                        task.status.contains(newText, ignoreCase = true) || task.collaborator?.any { it.contains(newText, ignoreCase = true) } == true
            }
        }
        adapter.updateList(searchTask)
    }

    private fun initRecyclerView(option: String) {
        binding.adminRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = TaskAdapter { selectedItem: Task -> listItemClicked(selectedItem) }
        binding.adminRecyclerView.adapter = adapter

        db.collection("User").document(userId).get()
            .addOnSuccessListener { userResult ->
                val username = userResult.getString("username")
                userNames = mutableListOf()
                if (username != null) {
                    db.collection("Task").whereArrayContains("collaborator", username).get()
                        .addOnSuccessListener { result ->
                            val taskList = result.toObjects(Task::class.java)
                            userIds = taskList.map { it.userId }
                            when (option) {
                                "date" -> {
                                    taskList.sortByDescending { it.date }
                                }
                                "status" -> {
                                    val sortByThis = listOf("Request", "Pending", "Completed", "Reworked", "Verified")
                                    taskList.sortBy { sortByThis.indexOf(it.status) }
                                }
                                "name" -> {
                                    getNameById(taskList)
                                }
                                else -> {
                                    taskList.sortByDescending { it.title.lowercase() }
                                }
                            }
                            adapter.updateList(taskList)

                            binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    return false
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    search(newText, taskList)
                                    return true
                                }

                            })
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

    private fun listItemClicked(task: Task) {
        val intent = Intent(this, AdminDetailActivity::class.java)
        intent.putExtra("taskId", task.taskId)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun getNameById(taskList: MutableList<Task>) {
        db.collection("User")
            .whereIn("userId", userIds)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val usernamesMap = mutableMapOf<String, String>()
                for (document in querySnapshot) {
                    val userId = document.getString("userId")
                    val username = document.getString("username")

                    if (userId != null && username != null) {
                        usernamesMap[userId] = username
                    }
                }

                taskList.sortByDescending { usernamesMap[it.userId] }

                adapter.updateList(taskList)
            }
            .addOnFailureListener { e ->
                Log.d("User", "Error getting usernames: $e")
            }
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
                initRecyclerView(selectedOption.lowercase())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }
}