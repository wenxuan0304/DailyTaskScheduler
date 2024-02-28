package com.example.dailytaskscheduler.admin

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dailytaskscheduler.R
import java.net.URLDecoder

class FileAdapter(
    private val context: Context,
    private val fileUrls: List<String>?
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val pdfView: ImageView = itemView.findViewById(R.id.pdfView)
        val zipView: ImageView = itemView.findViewById(R.id.zipFile)
        val fileView: ImageView = itemView.findViewById(R.id.file)
        val videoView: ImageView = itemView.findViewById(R.id.mp4file)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileUrl = fileUrls?.get(position)

        if (fileUrl != null) {
            val isImage = isImageUrl(fileUrl.toString())

            val path = fileUrl.substringBefore("?")
            val extension = path.substringAfterLast('.', "")

            if(isImage){
                holder.pdfView.visibility = View.GONE
                holder.zipView.visibility = View.GONE
                holder.fileView.visibility = View.GONE
                holder.videoView.visibility = View.GONE
                holder.imageView.visibility = View.VISIBLE
                Glide.with(holder.itemView.context)
                    .load(fileUrl)
                    .into(holder.imageView)
            }else if(extension == "pdf"){
                holder.videoView.visibility = View.GONE
                holder.pdfView.visibility = View.VISIBLE
                holder.zipView.visibility = View.GONE
                holder.fileView.visibility = View.GONE
                holder.imageView.visibility = View.GONE
            }else if(extension == "zip"){
                holder.videoView.visibility = View.GONE
                holder.pdfView.visibility = View.GONE
                holder.zipView.visibility = View.VISIBLE
                holder.fileView.visibility = View.GONE
                holder.imageView.visibility = View.GONE
            }else if(extension == "mp4"){
                holder.videoView.visibility = View.VISIBLE
                holder.pdfView.visibility = View.GONE
                holder.zipView.visibility = View.GONE
                holder.fileView.visibility = View.GONE
                holder.imageView.visibility = View.GONE
            }else{
                holder.videoView.visibility = View.GONE
                holder.pdfView.visibility = View.GONE
                holder.zipView.visibility = View.GONE
                holder.fileView.visibility = View.VISIBLE
                holder.imageView.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                onItemClick(fileUrl)
            }
        }
    }

    private fun onItemClick(fileUrl: String) {
        val fileUri = Uri.parse(fileUrl)
        val path = fileUrl.substringBefore("?")
        val extension = path.substringAfterLast('.', "")

        if(fileUri != null){
            openFile(context,fileUri,extension)
        }
    }

    private fun isImageUrl(url: String): Boolean {
        val imageExtensions = arrayOf("jpg", "jpeg", "png", "gif", "bmp", "webp") // Add more if needed
        val path = url.substringBefore("?")
        val uri = path.substringAfterLast('.', "")
        for (extension in imageExtensions) {
            if (uri.endsWith(extension, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun openFile(context: Context, fileUri: Uri, fileExtension: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(fileUri, getMimeType(fileExtension))
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION

            context.startActivity(intent)
        } catch (e: Exception) {
            val errorMessage = if (fileExtension.equals("zip", ignoreCase = true)) {
                "Zip files cannot be opened directly. Please use a file manager to extract its contents."
            } else {
                "Your file type is inaccessible"
            }
            AlertDialog.Builder(context)
                .setMessage(errorMessage)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
            Log.e("Error", "Error: $e")
        }
    }


    private fun getMimeType(fileExtension: String): String {
        return when (fileExtension.lowercase()) {
            "txt" -> "text/plain"
            "html", "htm" -> "text/html"
            "xml" -> "text/xml"
            "css" -> "text/css"
            "js" -> "text/javascript"
            "jpeg", "jpg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "svg" -> "image/svg+xml"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            "flac" -> "audio/flac"
            "mp4" -> "video/mp4"
            "webm" -> "video/webm"
            "ogv" -> "video/ogg"
            "avi" -> "video/x-msvideo"
            "pdf" -> "application/pdf"
            "doc", "docx" -> "application/msword"
            "xls", "xlsx" -> "application/vnd.ms-excel"
            "ppt", "pptx" -> "application/vnd.ms-powerpoint"
            "odt" -> "application/vnd.oasis.opendocument.text"
            "zip" -> "application/zip"
            "rar" -> "application/x-rar-compressed"
            "7z" -> "application/x-7z-compressed"
            "tar" -> "application/x-tar"
            "gz" -> "application/gzip"
            else -> "application/octet-stream"
        }
    }


    override fun getItemCount(): Int {
        return fileUrls?.size ?: 0
    }
}
