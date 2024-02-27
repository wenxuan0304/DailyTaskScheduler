package com.example.dailytaskscheduler.client

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dailytaskscheduler.databinding.ActivityAddClientBinding
import com.example.dailytaskscheduler.util.File
import com.example.dailytaskscheduler.util.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ClientAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddClientBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var adapter: ClientFileAdapter
    private lateinit var db: FirebaseFirestore

    private var userId: String = ""
//    private val calendar = Calendar.getInstance()
//    private var date = LocalDateTime.now()
    private var fileName: String = ""
    private var curFile: Uri? = null
    private var fileList = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init database
        db = FirebaseFirestore.getInstance()

        //init values
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        userId = sharedPreferencesHelper.userId

        //init calendar view
//        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
//        val formattedDate = formatter.format(date)
//        binding.etDate.inputType = InputType.TYPE_NULL
//        binding.etDate.setText(formattedDate)
//        binding.etDate.setOnClickListener {
//            showCalendarPicker()
//        }

        //init recycler view
        initRecyclerView()

        binding.btUpload.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "*/*"
                getResult.launch(it)
            }
        }

        binding.btSave.setOnClickListener {
            save()
        }

        binding.btCancel.setOnClickListener {
            cancel()
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
                            this@ClientAddActivity,
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

//    private fun showCalendarPicker() {
//        val dialogPickerDialog = DatePickerDialog(this, { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
//            val selectedDate = Calendar.getInstance()
//            selectedDate.set(year, monthOfYear, dayOfMonth)
//
//            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
//            val formattedDate = formatter.format(selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
//
//            date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
//            binding.etDate.setText(formattedDate)
//        },
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH)
//        )
//        dialogPickerDialog.show()
//    }

    private fun save() {
        binding.apply {
            if (!etTitle.text.isEmpty() && !etContent.text.isEmpty()){
                val userInputTitle = etTitle.text.toString()
                val userInputContent = etContent.text.toString()
//                val userInputDate = convertDate(date)
                val status = "Pending"
                val team = mutableListOf<String>()
                val taskId = generateUniqueId()
                val fileMap = fileList.map { file ->
                    mapOf(
                        "fileUrl" to file.fileUrl,
                        "fileName" to file.fileName
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

                                val task = Task(
                                    taskId = taskId,
                                    title = userInputTitle,
                                    content = userInputContent,
//                                    date = userInputDate,
                                    fileList = fileMap,
                                    userId = userId,
                                    collaborator = team,
                                    status = status,
                                )

                                db.collection("Task").document(taskId).set(task)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this@ClientAddActivity,
                                            "Task Added Successfully.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this@ClientAddActivity,
                                            "Error: ${it.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            }
                    }

                etTitle.text.clear()
                etContent.text.clear()

                val intent = Intent(this@ClientAddActivity, ClientMainActivity::class.java)
                startActivity(intent)
                finish()

            } else {
                Toast.makeText(
                    this@ClientAddActivity,
                    "Please fill in the empty field.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun cancel() {
        finish()
    }

    private fun generateUniqueId(): String{
        return UUID.randomUUID().toString()
    }

//    private fun convertDate(localDateTime: LocalDateTime): Timestamp {
//        val instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant()
//        return Timestamp(instant.toEpochMilli() / 1000, (instant.toEpochMilli() % 1000 * 1000000).toInt())
//    }

    private fun containDuplicateFileName(fileName: String): Boolean {
        return fileList.any { it.fileName == fileName }
    }
}