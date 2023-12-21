package com.example.dailytaskscheduler.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytaskscheduler.util.Task
import com.example.dailytaskscheduler.databinding.ListItemBinding

class TaskAdapter(private val clickListener:(Task)->Unit):
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>(){

    private val taskList = ArrayList<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding : ListItemBinding = ListItemBinding.inflate(layoutInflater)
        return TaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position], clickListener)
    }

    fun updateList(newList: List<Task>){
        taskList.clear()
        taskList.addAll(newList)
        notifyDataSetChanged()
    }

    class TaskViewHolder(val binding: ListItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(task: Task, clickListener: (Task) -> Unit){
            binding.tvTitle.text = task.title
            binding.tvDate.text = task.date.toString()
            binding.cardLayout.setOnClickListener{
                clickListener(task)
            }
        }
    }


}