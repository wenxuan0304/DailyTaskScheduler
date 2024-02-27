package com.example.dailytaskscheduler.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.dailytaskscheduler.databinding.ActivityAdminTaskProgressBinding
import com.example.dailytaskscheduler.util.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

// Import statements

class AdminTaskProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminTaskProgressBinding
    private lateinit var db: FirebaseFirestore
    private var taskId: String = ""
    private var userId: String = ""
    private var currentProgress: Int = 0
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminTaskProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        db = FirebaseFirestore.getInstance()

        taskId = intent.getStringExtra("taskId").toString()
        userId = intent.getStringExtra("userId").toString()

        db.collection("Task").document(taskId).get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val task = document.toObject(Task::class.java)
                Log.d("task",task.toString())
                updateUI(task)
            } else {

            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.historyBtn.setOnClickListener {
            // Handle history button click
        }

        // Set onClickListener for increase button
        binding.increaseBtn.setOnClickListener {
            updateProgress(true)
        }

        // Set onClickListener for decrease button
        binding.decreaseBtn.setOnClickListener {
            updateProgress(false)
        }
    }

    private fun updateUI(task: Task?) {
        currentProgress = task?.progress ?: 0
        binding.progressBar.progress = currentProgress

        val fileAdapter = FileAdapter(this,task?.fileUrls)
        binding.rvFiles.adapter = fileAdapter

        binding.editTextTextMultiLine.setText(task?.notes)


    }

    private fun updateProgress(increase: Boolean) {
        if (increase && currentProgress < 100) {
            currentProgress += 10
        } else if (!increase && currentProgress > 0) {
            currentProgress -= 10
        }

        binding.progressBar.progress = currentProgress
    }
}



