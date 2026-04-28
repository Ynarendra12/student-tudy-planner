package com.example.studentstudyplanner.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.studentstudyplanner.ai.GenerativeAiHelper
import com.example.studentstudyplanner.database.AppDatabase
import com.example.studentstudyplanner.database.NoteEntity
import com.example.studentstudyplanner.databinding.FragmentPdfViewerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PdfViewerFragment : Fragment() {

    private var _binding: FragmentPdfViewerBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPdfViewerBinding.inflate(inflater, container, false)
        db = Room.databaseBuilder(requireContext(), AppDatabase::class.java, "study-db").build()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val filePath = arguments?.getString("filePath") ?: return
        val file = File(filePath)

        if (file.exists()) {
            binding.pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .load()
        }

        binding.btnAskAi.setOnClickListener {
            val query = binding.etAiQuery.text.toString()
            if (query.isNotEmpty()) {
                askAi(query)
                binding.etAiQuery.text.clear()
            }
        }
    }

    private fun askAi(query: String) {
        lifecycleScope.launch {
            binding.btnAskAi.isEnabled = false
            binding.etAiQuery.hint = "Gemini is analyzing..."
            
            val explanation = GenerativeAiHelper.getExplanation(query)
            
            binding.btnAskAi.isEnabled = true
            binding.etAiQuery.hint = "Ask AI about this page..."

            val bottomSheet = AiResponseBottomSheet(query, explanation) { q, r ->
                saveNote(q, r)
            }
            bottomSheet.show(parentFragmentManager, AiResponseBottomSheet.TAG)
        }
    }

    private fun saveNote(topic: String, explanation: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.appDao().insertNote(
                NoteEntity(
                    docId = 0,
                    originalText = topic,
                    explanation = explanation
                )
            )
            launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Insight saved to Notes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
