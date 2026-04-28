package com.example.studentstudyplanner.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studentstudyplanner.database.ChatMessageEntity
import com.example.studentstudyplanner.databinding.ItemChatBubbleBinding

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var messages = mutableListOf<ChatMessageEntity>()

    fun setMessages(newMessages: List<ChatMessageEntity>) {
        messages = newMessages.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBubbleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class ChatViewHolder(private val binding: ItemChatBubbleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chat: ChatMessageEntity) {
            if (chat.isUser) {
                binding.cardUser.visibility = View.VISIBLE
                binding.cardAi.visibility = View.GONE
                binding.tvUserMessage.text = chat.message
            } else {
                binding.cardUser.visibility = View.GONE
                binding.cardAi.visibility = View.VISIBLE
                binding.tvAiMessage.text = chat.message
            }
        }
    }
}
