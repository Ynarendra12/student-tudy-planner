package com.example.studentstudyplanner.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studentstudyplanner.database.DocumentEntity
import com.example.studentstudyplanner.databinding.ItemDocumentBinding

class DocumentAdapter(private val onDocClick: (DocumentEntity) -> Unit) :
    ListAdapter<DocumentEntity, DocumentAdapter.DocViewHolder>(DocDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocViewHolder {
        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DocViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DocViewHolder(private val binding: ItemDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(doc: DocumentEntity) {
            binding.tvDocName.text = doc.fileName
            binding.tvDocType.text = "${doc.fileType.uppercase()} Document"
            binding.root.setOnClickListener { onDocClick(doc) }
        }
    }

    class DocDiffCallback : DiffUtil.ItemCallback<DocumentEntity>() {
        override fun areItemsTheSame(oldItem: DocumentEntity, newItem: DocumentEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DocumentEntity, newItem: DocumentEntity): Boolean {
            return oldItem == newItem
        }
    }
}
