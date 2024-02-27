package com.example.dailytaskscheduler.admin

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dailytaskscheduler.R

class FileAdapter(
    private val context: Context,
    private val fileUrls: List<String>?
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val pdfView: ImageView = itemView.findViewById(R.id.pdfView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileUrl = fileUrls?.get(position)

        if (fileUrl != null) {
            if (fileUrl.endsWith(".pdf")) {
                holder.pdfView.visibility = View.VISIBLE
                holder.imageView.visibility = View.GONE
            } else if (fileUrl.endsWith(".jpg") || fileUrl.endsWith(".png")) {
                holder.pdfView.visibility = View.GONE
                holder.imageView.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(fileUrl)
                    .into(holder.imageView)
            }

            holder.itemView.setOnClickListener {
                onItemClick(fileUrl)
            }
        }
    }

    private fun onItemClick(fileUrl: String) {
        val fileUri = Uri.parse(fileUrl)

        Log.d("fileClicked", fileUri.toString())

        if (fileUrl.endsWith(".pdf")) {
            openPdfFile(context, fileUri)
        } else if (fileUrl.endsWith(".jpg") || fileUrl.endsWith(".png")) {
            openImageViewer(context, fileUri)
        }
    }

    private fun openPdfFile(context: Context, fileUri: Uri) {
        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(fileUri, "application/pdf")
        pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_GRANT_READ_URI_PERMISSION

        context.startActivity(pdfIntent)
    }

    private fun openImageViewer(context: Context, imageUri: Uri) {
        val imageIntent = Intent(Intent.ACTION_VIEW)
        imageIntent.setDataAndType(imageUri, "image/jpeg")
        imageIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_GRANT_READ_URI_PERMISSION

        context.startActivity(imageIntent)
    }

    override fun getItemCount(): Int {
        return fileUrls?.size ?: 0
    }
}
