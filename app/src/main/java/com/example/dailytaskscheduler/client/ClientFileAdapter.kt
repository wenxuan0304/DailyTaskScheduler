package com.example.dailytaskscheduler.client

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytaskscheduler.databinding.ListItemFileBinding
import com.example.dailytaskscheduler.util.File
import com.example.dailytaskscheduler.util.Task

class ClientFileAdapter(private val filesList: List<File>, private val clickListener: (File) -> Unit)
    : RecyclerView.Adapter<MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemFileBinding.inflate(layoutInflater)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return filesList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(filesList[position], clickListener)
    }
}

class MyViewHolder(val binding: ListItemFileBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(file: File, clickListener: (File) -> Unit) {
        binding.tvFileName.text = file.fileName
        binding.btDelete.setOnClickListener{
            clickListener(file)
        }
    }
}