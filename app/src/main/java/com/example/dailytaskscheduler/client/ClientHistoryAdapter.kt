package com.example.dailytaskscheduler.client

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dailytaskscheduler.databinding.ListItemHistoryBinding
import com.example.dailytaskscheduler.util.History
import java.text.SimpleDateFormat
import java.util.Locale

class ClientHistoryAdapter: RecyclerView.Adapter<HistoryViewHolder>() {

    private val historyList = ArrayList<History>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding : ListItemHistoryBinding = ListItemHistoryBinding.inflate(layoutInflater)
        return HistoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    fun setList(history: List<History>){
        historyList.clear()
        historyList.addAll(history)
        notifyDataSetChanged()
    }
}

class HistoryViewHolder(val binding: ListItemHistoryBinding): RecyclerView.ViewHolder(binding.root){
    val context = binding.root.context
    fun bind(history: History) {
        val date = history.timestamp?.toDate()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = date?.let { dateFormat.format(it) }

        binding.tvUpdatedField.text = "Updated Field: ${history.updatedField}"
        if (history.updatedField == "File Upload"){
            binding.tvOldValue.text = "File Name: ${history.oldValue}"
            binding.tvNewValue.text = "Action: ${history.newValue}"
        } else {
            binding.tvOldValue.text = "Old Value: ${history.oldValue}"
            binding.tvNewValue.text = "New Value: ${history.newValue}"
        }
        binding.tvTimestamp.text = "Timestamp: $dateString"
    }
}