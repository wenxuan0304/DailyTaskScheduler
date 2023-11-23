package com.example.dailytaskscheduler.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.util.Task
import com.example.dailytaskscheduler.util.TaskDatabase
import com.example.dailytaskscheduler.util.TaskRepository
import com.example.dailytaskscheduler.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: TaskDatabase
    lateinit var taskViewModel: TaskViewModel
    lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        taskViewModel = ViewModelProvider(this,TaskViewModelFactory(application)
        ).get(TaskViewModel::class.java)

        initRecyclerView()

        taskViewModel.allTasks.observe(this){
            list -> list?.let {
                adapter.updateList(list)
            }
        }
    }

    private fun initRecyclerView() {
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        adapter = TaskAdapter({selectedItem: Task -> listItemClicked(selectedItem)})
        binding.recyclerView.adapter = adapter
    }

    private fun listItemClicked(task: Task){
        Toast.makeText(this,"Name : ${task.title}", Toast.LENGTH_LONG).show()
    }
}