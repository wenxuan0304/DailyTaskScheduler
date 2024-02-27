package com.example.dailytaskscheduler.client

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.dailytaskscheduler.databinding.ActivityHistoryClientBinding
import com.example.dailytaskscheduler.util.History
import com.google.firebase.firestore.FirebaseFirestore
import java.sql.Timestamp

class ClientHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryClientBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ClientHistoryAdapter

    private var taskId: String = ""
    private var historyList = mutableListOf<History>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init database
        db = FirebaseFirestore.getInstance()

        //init values
        taskId = intent.getStringExtra("taskId").toString()

        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rvHistory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ClientHistoryAdapter()
        binding.rvHistory.adapter = adapter
        displayHistory()
    }

    private fun displayHistory() {
        db.collection("Task").document(taskId).get()
            .addOnSuccessListener { result ->
                val retrieveHistoryList = result.get("history") as List<Map<String, Any>>?
                if (retrieveHistoryList != null){
                    historyList = retrieveHistoryList.map { map ->
                        val updatedField = map["updatedField"] as String
                        val oldValue = map["oldValue"] as String
                        val newValue = map["newValue"] as String
                        val timestamp = map["timestamp"] as com.google.firebase.Timestamp
                        History(updatedField, oldValue, newValue, timestamp)
                    }.toMutableList()
                    adapter.setList(historyList)
                } else {
                    binding.tvNull.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@ClientHistoryActivity,
                    "Error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}