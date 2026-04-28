package com.example.studentstudyplanner.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentstudyplanner.R
import com.example.studentstudyplanner.database.AppDatabase
import com.example.studentstudyplanner.database.DocumentEntity
import com.example.studentstudyplanner.databinding.FragmentDocumentsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class DocumentsFragment : Fragment() {

    private var _binding: FragmentDocumentsBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var adapter: DocumentAdapter

    private val selectFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                saveFileLocally(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDocumentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())
        setupRecyclerView()
        
        binding.fabUploadDocument.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
            }
            selectFileLauncher.launch(intent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            db.appDao().getAllDocuments().collectLatest { docs ->
                adapter.submitList(docs)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = DocumentAdapter { doc ->
            val bundle = Bundle().apply { putString("filePath", doc.filePath) }
            findNavController().navigate(R.id.navigation_pdf_viewer, bundle)
        }
        binding.recyclerViewDocuments.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewDocuments.adapter = adapter
    }

    private fun saveFileLocally(uri: Uri) {
        val fileName = getFileName(uri)
        val file = File(requireContext().filesDir, fileName)
        
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(file).use { output ->
                        input.copyTo(output)
                    }
                }
                db.appDao().insertDocument(DocumentEntity(
                    fileName = fileName, 
                    filePath = file.absolutePath, 
                    fileType = "pdf"
                ))
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error saving file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        return result ?: uri.path?.substringAfterLast('/') ?: "document.pdf"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
