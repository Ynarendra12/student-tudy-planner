package com.example.studentstudyplanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentstudyplanner.ai.GenerativeAiHelper
import com.example.studentstudyplanner.database.AppDatabase
import com.example.studentstudyplanner.database.ChatMessageEntity
import com.example.studentstudyplanner.databinding.FragmentNotesBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChat()
        
        // Observe chat messages reactively
        viewLifecycleOwner.lifecycleScope.launch {
            db.appDao().getAllChatMessages().collectLatest { messages ->
                chatAdapter.setMessages(messages)
                if (messages.isNotEmpty()) {
                    binding.rvChat.post {
                        binding.rvChat.scrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        binding.btnSendAi.setOnClickListener {
            val query = binding.etAiQuery.text.toString()
            if (query.isNotBlank()) {
                sendMessage(query)
            }
        }

        binding.btnClearChat.setOnClickListener {
            clearChat()
        }
    }

    private fun setupChat() {
        chatAdapter = ChatAdapter()
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.adapter = chatAdapter
    }

    private fun sendMessage(query: String) {
        binding.etAiQuery.text.clear()
        
        viewLifecycleOwner.lifecycleScope.launch {
            // 1. Save User Message
            val userMsg = ChatMessageEntity(message = query, isUser = true)
            db.appDao().insertChatMessage(userMsg)

            // 2. Show "Thinking" state
            binding.etAiQuery.hint = "Gemini is typing..."
            binding.btnSendAi.isEnabled = false

            // 3. Get AI Response
            val response = GenerativeAiHelper.getExplanation(query)

            // 4. Save AI Message
            val aiMsg = ChatMessageEntity(message = response, isUser = false)
            db.appDao().insertChatMessage(aiMsg)
            
            binding.etAiQuery.hint = "Message Gemini..."
            binding.btnSendAi.isEnabled = true
        }
    }

    private fun clearChat() {
        viewLifecycleOwner.lifecycleScope.launch {
            db.appDao().clearChat()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
