package com.example.dailytaskscheduler.admin

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.dailytaskscheduler.SharedPreferencesHelper
import com.example.dailytaskscheduler.databinding.ActivityAdminTaskProgressBinding
import com.example.dailytaskscheduler.util.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

// Import statements

class AdminTaskProgressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminTaskProgressBinding
    private lateinit var db: FirebaseFirestore
    private var taskId: String = ""
    private var userId: String = ""
    private var currentProgress: Int = 0
    private lateinit var storage:FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminTaskProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = FirebaseStorage.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        db = FirebaseFirestore.getInstance()

        sharedPreferencesHelper = SharedPreferencesHelper(this)
        userId = sharedPreferencesHelper.userId
        taskId = sharedPreferencesHelper.taskId

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

        binding.increaseBtn.setOnClickListener {
            updateProgress(true)
        }

        binding.decreaseBtn.setOnClickListener {
            updateProgress(false)
        }
    }

    private fun retrieveFilesFromFirestore() {
        db.collection("Files").whereEqualTo("taskId", taskId).get()
            .addOnSuccessListener { documents ->
                val fileList = mutableListOf<String>() // List to store file URLs
                Log.d("empty file list", fileList.toString())
                for (document in documents) {
                    // Get file metadata from Firestore document
                    val fileUrl = document.getString("fileUrl")

                    if (fileUrl != null) {
                        // Add file URL to the list
                        fileList.add(fileUrl)
                    }
                }
                Log.d("file list", fileList.toString())

                // Update RecyclerView adapter with the list of file URLs
                val fileAdapter = FileAdapter(this, fileList)
                binding.rvFiles.adapter = fileAdapter

                // After updating the RecyclerView, start downloading the files
                fileList.forEach { fileUrl ->
                    downloadFileFromStorage(fileUrl)
                }
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    private fun downloadFileFromStorage(fileUrl: String) {
        val fileName = getFileNameFromUrl(fileUrl)
        val storageRef = storage.getReferenceFromUrl(fileUrl)
        val localFile = File.createTempFile(fileName, "")

        Log.d("file", localFile.toString())

        storageRef.getFile(localFile)
            .addOnSuccessListener {
                // File downloaded successfully
                Log.d(TAG, "File downloaded: $fileName")
                // You can update the UI or perform any other operations here
            }
            .addOnFailureListener { exception ->
                // Handle unsuccessful download (display error message, retry, etc.)
                Log.e(TAG, "Error downloading file: $fileName", exception)
            }
    }

    private fun getFileNameFromUrl(fileUrl: String): String {
        // Extract the file name from the file URL
        return fileUrl.substringAfterLast("/")
    }




    private fun updateUI(task: Task?) {
        currentProgress = task?.progress ?: 0
        binding.progressBar.progress = currentProgress

        retrieveFilesFromFirestore()
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



