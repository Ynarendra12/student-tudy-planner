package com.example.studentstudyplanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.studentstudyplanner.databinding.BottomSheetAiResponseBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AiResponseBottomSheet(
    private val query: String,
    private val response: String,
    private val onSaveRequested: (String, String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAiResponseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAiResponseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.tvAiQuery.text = "Topic: $query"
        binding.tvAiResponse.text = response
        
        binding.btnSaveToNotes.setOnClickListener {
            onSaveRequested(query, response)
            dismiss()
        }
        
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AiResponseBottomSheet"
    }
}
