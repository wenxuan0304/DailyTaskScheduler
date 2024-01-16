package com.example.dailytaskscheduler.admin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytaskscheduler.databinding.ListItemUserBinding

class AdminColAdapter(private val userList: List<String>, private val clickListener: (String) -> Unit)
    : RecyclerView.Adapter<CollaboratorViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollaboratorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding : ListItemUserBinding = ListItemUserBinding.inflate(layoutInflater)
        return CollaboratorViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: CollaboratorViewHolder, position: Int) {
        holder.bind(userList[position], clickListener)
    }

    fun getList(): MutableList<String> {
        return userList.toMutableList()
    }
}

class CollaboratorViewHolder(val binding: ListItemUserBinding): RecyclerView.ViewHolder(binding.root){

    fun bind(user: String, clickListener: (String) -> Unit) {
        binding.tvUser.text = user
        binding.btnDelete.setOnClickListener {
            clickListener(user)
        }
    }
}