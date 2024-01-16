package com.example.dailytaskscheduler.admin

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytaskscheduler.R
import com.example.dailytaskscheduler.databinding.ListItemBinding
import com.example.dailytaskscheduler.util.Task
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale

private val taskList = ArrayList<Task>()
class TaskAdapter(private val clickListener: (Task) -> Unit) :
    RecyclerView.Adapter<TaskViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: ListItemBinding = ListItemBinding.inflate(layoutInflater)
        return TaskViewHolder(binding, clickListener)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList[position]
        holder.bind(task)
    }

    fun updateList(newList: List<Task>) {
        taskList.clear()
        taskList.addAll(newList)
        notifyDataSetChanged()
    }

}

class TaskViewHolder(
    private val binding: ListItemBinding,
    private val clickListener: (Task) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private lateinit var db: FirebaseFirestore

    init {
        updateColor()
        binding.cardView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Handle touch down
                    binding.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, R.color.blue_200)
                    )
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Handle touch release or cancel
                    binding.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(binding.root.context, android.R.color.white)
                    )
                    clickListener(taskList[adapterPosition])
                }
            }
            true
        }
    }

    private fun updateColor(){
        val context = binding.root.context

        when (binding.tvComplete.text) {
            "Pending" -> {
                binding.tvComplete.setTextColor(ContextCompat.getColor(context, R.color.yellow_pending))
            }
            "Completed" -> {
                binding.tvComplete.setTextColor(ContextCompat.getColor(context, R.color.dark_green))
            }
            "Reworked" -> {
                binding.tvComplete.setTextColor(ContextCompat.getColor(context, R.color.yellow_rework))
            }
            "Verified" -> {
                binding.tvComplete.setTextColor(ContextCompat.getColor(context, R.color.blue_700))
            }
            else -> {
                binding.tvComplete.setTextColor(ContextCompat.getColor(context, R.color.black))
            }
        }
    }

    fun bind(task: Task) {

        db = FirebaseFirestore.getInstance()
        var username = ""

            val date = task.date.toDate()
            val formattedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)

        db.collection("User").whereEqualTo("userId",task.userId).get()
            .addOnSuccessListener {
                result -> if(result.documents.isNotEmpty()){
                    username = result.documents[0].getString("username").toString()
                Log.d("Task", username)

                binding.tvName.text = username
                }
            }

        binding.tvTitle.text = task.title
        binding.tvDate.text = formattedDate
        binding.tvComplete.text = task.status

        Log.d("Task","${task.title} ${task.date} ${task.status}")
        updateColor()
    }
}
