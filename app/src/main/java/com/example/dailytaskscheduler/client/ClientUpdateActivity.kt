package com.example.dailytaskscheduler.client

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.SharedPreferencesHelper
import com.example.dailytaskscheduler.databinding.ActivityUpdateClientBinding
import com.example.dailytaskscheduler.util.File
import com.example.dailytaskscheduler.util.History
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class ClientUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateClientBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var adapter: ClientFileAdapter

    private var taskId: String = ""
    private var userId: String = ""
    private var selectedStatus: String = ""
    private var orgTitle: String = ""
    private var orgContent: String = ""
    private var orgStatus: String = ""
    private var orgFileList = mutableListOf<File>()
    private var fileName: String = ""
    private var curFile: Uri? = null
    private var retrieveFileList = listOf<Map<String, String>>()
    private var fileList = mutableListOf<File>()
    private var historyList = mutableListOf<History>()
    private var retrieveHistoryList = listOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init database
        db = FirebaseFirestore.getInstance()

        //init values
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        userId = sharedPreferencesHelper.userId
        taskId = intent.getStringExtra("taskId").toString()

        //retrieve firestore values
        db.collection("Task").document(taskId).get()
            .addOnSuccessListener { result ->
                if (result.exists()) {
                    runOnUiThread {
                        binding.etTitle.setText(result.get("title").toString())
                        binding.etContent.setText(result.get("content").toString())
                        selectedStatus = result.get("status").toString()
                        retrieveFileList = result.get("fileList") as List<Map<String, String>>
                        fileList = retrieveFileList.map { map ->
                            val fileUrl = map["fileUrl"] ?: ""
                            val fileName = map["fileName"] ?: ""
                            File(fileUrl, fileName)
                        }.toMutableList()
                        val retrieveHistoryList = result.get("history") as List<Map<String, Any>>?
                        if (retrieveHistoryList != null){
                            historyList = retrieveHistoryList.map { map ->
                                val updatedField = map["updatedField"] as String
                                val oldValue = map["oldValue"] as String
                                val newValue = map["newValue"] as String
                                val timestamp = map["timestamp"] as com.google.firebase.Timestamp
                                History(updatedField, oldValue, newValue, timestamp)
                            }.toMutableList()
                        }
                        val timestamp = result.getTimestamp("date")
                        if (timestamp != null) {
                            val date = timestamp.toDate()
                            val formattedDate =
                                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)
                            binding.tvDate2.text = formattedDate
                        }

                        orgTitle = result.get("title").toString()
                        orgContent = result.get("content").toString()
                        orgStatus = result.get("status").toString()
                        orgFileList = retrieveFileList.map { map ->
                            val fileUrl = map["fileUrl"] ?: ""
                            val fileName = map["fileName"] ?: ""
                            File(fileUrl, fileName)
                        }.toMutableList()

                        //init spinner
                        initSpinner()

                        //init recycler view
                        initRecyclerView()
                    }
                }
            }

        binding.btUpload.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "*/*"
                getResult.launch(it)
            }
        }

        binding.btUpdate.setOnClickListener {
            update()
        }

        binding.btnDelete.setOnClickListener {
            delete()
        }

        binding.btHistory.setOnClickListener {
            val intent = Intent(this@ClientUpdateActivity, ClientHistoryActivity::class.java)
            intent.putExtra("taskId", taskId)
            startActivity(intent)
        }
    }

    private fun initSpinner() {
        val status = resources.getStringArray(R.array.task_status)
        if (binding.spStatus != null){
            val adapter = ArrayAdapter(this@ClientUpdateActivity, android.R.layout.simple_spinner_dropdown_item, status)
            binding.spStatus.adapter = adapter

            val initialIndex = status.indexOf(selectedStatus)
            binding.spStatus.setSelection(initialIndex)
        }

        binding.spStatus.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                selectedStatus = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Toast.makeText(applicationContext, "Please select a status.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initRecyclerView() {
        binding.rvFiles.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = ClientFileAdapter(fileList) { selectedItem: File -> listItemClicked(selectedItem) }
        binding.rvFiles.adapter = adapter
    }

    private fun listItemClicked(file: File) {
        fileList.remove(file)
        adapter.notifyDataSetChanged()
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        try {
            if (uri.scheme == "content") {
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (displayNameIndex != -1) {
                            val displayName = it.getString(displayNameIndex)
                            result = displayName
                        }
                    }
                }
            } else if (uri.scheme == "file") {
                result = uri.lastPathSegment
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    private val getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it?.data?.let {
                curFile = it.data
                fileName = curFile?.let { it1 -> getFileName(it1) }.toString()
                if (fileName != null) {
                    if (containDuplicateFileName(fileName)) {
                        Toast.makeText(
                            this@ClientUpdateActivity,
                            "Contain duplicate file names. Please rename the file or upload another file",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        fileList.add(File(
                            curFile.toString(),
                            fileName
                        ))
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun update() {
        binding.apply {
            if (!etTitle.text.isEmpty() && !etContent.text.isEmpty()){
                val userInputTitle = etTitle.text.toString()
                val userInputContent = etContent.text.toString()
                val userInputStatus = selectedStatus
                val team = mutableListOf<String>()

                if (userInputTitle == orgTitle &&
                    userInputContent == orgContent &&
                    userInputStatus == orgStatus &&
                    checkIfListsHaveSameValues(fileList, orgFileList)) {

                    Toast.makeText(this@ClientUpdateActivity,
                        "No changes made.",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    val fileMap = fileList.map { file ->
                        mapOf(
                            "fileUrl" to file.fileUrl,
                            "fileName" to file.fileName
                        )
                    }

                    addHistory(userInputTitle, userInputContent, userInputStatus)

                    val historyMap = historyList.map { history ->
                        mapOf(
                            "updatedField" to history.updatedField,
                            "oldValue" to history.oldValue,
                            "newValue" to history.newValue,
                            "timestamp" to history.timestamp
                        )
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

                                    val updates = hashMapOf(
                                        "title" to userInputTitle,
                                        "content" to userInputContent,
                                        "fileList" to fileMap,
                                        "collaborator" to team,
                                        "status" to userInputStatus,
                                        "history" to historyMap
                                    )

                                    db.collection("Task").document(taskId).update(updates)
                                        .addOnSuccessListener {
                                            Toast.makeText(
                                                this@ClientUpdateActivity,
                                                "Task Updated Successfully.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this@ClientUpdateActivity,
                                                "Error: ${it.message}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                }
                        }

                    etTitle.text.clear()
                    etContent.text.clear()

                    val intent = Intent(this@ClientUpdateActivity, ClientMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                Toast.makeText(this@ClientUpdateActivity, "Please fill in the empty field.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addHistory(title: String, content: String, status: String) {
        if (title != orgTitle) {
            historyList.add(History(
                updatedField = "Title",
                oldValue = orgTitle,
                newValue = title
            ))
        }

        if (content != orgContent) {
            historyList.add(History(
                updatedField = "Content",
                oldValue = orgContent,
                newValue = content
            ))
        }

        if (status != orgStatus) {
            historyList.add(History(
                updatedField = "Status",
                oldValue = orgStatus,
                newValue = status
            ))
        }

        val removedElements = orgFileList.map { it.fileName }
            .filterNot { orgFileName -> fileList.map { it.fileName }.contains(orgFileName) }
        val addedElements = fileList.map { it.fileName }
            .filterNot { fileName -> orgFileList.map { it.fileName }.contains(fileName) }

        removedElements.forEach{fileName ->
            Log.d("history", "remove element $fileName")
            historyList.add(History(
                updatedField = "File Upload",
                oldValue = fileName,
                newValue = "Removed"
            ))
        }

        addedElements.forEach{fileName ->
            Log.d("history", "add element $fileName")
            historyList.add(History(
                updatedField = "File Upload",
                oldValue = fileName,
                newValue = "Added"
            ))
        }
    }

    private fun delete() {
        db.collection("Task").document(taskId).delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this@ClientUpdateActivity,
                    "Task Deleted Successfully.",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@ClientUpdateActivity,
                    "Error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        val intent = Intent(this@ClientUpdateActivity, ClientMainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun containDuplicateFileName(fileName: String): Boolean {
        return fileList.any { it.fileName == fileName }
    }

    private fun checkIfListsHaveSameValues(list1: MutableList<File>, list2: MutableList<File>): Boolean {
        if (list1.size != list2.size) {
            return false
        }

        val fileNameList1 = mutableListOf<String>()
        val fileNameList2 = mutableListOf<String>()

        for (file in list1) {
            fileNameList1.add(file.fileName)
        }

        for (file in list2) {
            fileNameList2.add(file.fileName)
        }

        val sortedList1 = fileNameList1.sorted()
        val sortedList2 = fileNameList2.sorted()

        for (i in sortedList1.indices) {
            if (sortedList1[i] != sortedList2[i]) {
                return false
            }
        }

        return true
    }
}