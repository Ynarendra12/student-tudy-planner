package com.example.studentstudyplanner.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentstudyplanner.R
import com.example.studentstudyplanner.ai.GenerativeAiHelper
import com.example.studentstudyplanner.database.AppDatabase
import com.example.studentstudyplanner.database.TaskEntity
import com.example.studentstudyplanner.database.UserProfileEntity
import com.example.studentstudyplanner.databinding.FragmentTasksBinding
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Calendar

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: AppDatabase
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        setupRecyclerView()
        setupFab()
        observeTasks()
        observePoints()
        addSampleTasksIfEmpty()

        binding.btnAiPlan.setOnClickListener {
            generateAiStudyPlan()
        }
    }

    private fun setupRecyclerView() {
        // Uses TaskAdapter from the same package (ui)
        adapter = TaskAdapter { task, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                if (isChecked && !task.pointsAwarded) {
                    val updatedTask = task.copy(isCompleted = true, pointsAwarded = true)
                    db.appDao().updateTask(updatedTask)
                    awardPointsAndShowMessage()
                } else if (!isChecked && task.pointsAwarded) {
                    val updatedTask = task.copy(isCompleted = false, pointsAwarded = false)
                    db.appDao().updateTask(updatedTask)
                    deductPointsAndShowMessage()
                } else {
                    db.appDao().updateTask(task.copy(isCompleted = isChecked))
                }
            }
        }
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
    }

    private fun awardPointsAndShowMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentProfile = db.appDao().getUserProfile().firstOrNull() ?: UserProfileEntity()
            db.appDao().updateProfile(currentProfile.copy(totalPoints = currentProfile.totalPoints + 50))
            Toast.makeText(requireContext(), "🎉 Incredible! +50 Points!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deductPointsAndShowMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentProfile = db.appDao().getUserProfile().firstOrNull() ?: UserProfileEntity()
            db.appDao().updateProfile(currentProfile.copy(totalPoints = (currentProfile.totalPoints - 50).coerceAtLeast(0)))
            Toast.makeText(requireContext(), "Task unticked. 50 Points deducted.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            db.appDao().getAllTasks().collectLatest { tasks ->
                adapter.updateTasks(tasks)
                updateProgress(tasks)
            }
        }
    }

    private fun observePoints() {
        viewLifecycleOwner.lifecycleScope.launch {
            db.appDao().getUserProfile().collectLatest { profile ->
                binding.tvTotalPoints.text = "${profile?.totalPoints ?: 0} Points"
            }
        }
    }

    private fun updateProgress(tasks: List<TaskEntity>) {
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        if (total > 0) {
            val progress = (completed.toFloat() / total.toFloat() * 100).toInt()
            binding.taskProgress.setProgress(progress, true)
            binding.tvProgressText.text = "$completed/$total tasks completed"
        }
    }

    private fun addSampleTasksIfEmpty() {
        viewLifecycleOwner.lifecycleScope.launch {
            val tasks = db.appDao().getAllTasks().firstOrNull()
            if (tasks.isNullOrEmpty()) {
                db.appDao().insertTask(TaskEntity(subject = "Math", topic = "Calculus", date = "Today", time = "04:00 PM"))
                db.appDao().insertTask(TaskEntity(subject = "AI", topic = "Gemini API", date = "Tomorrow", time = "10:00 AM"))
            }
        }
    }

    private fun generateAiStudyPlan() {
        viewLifecycleOwner.lifecycleScope.launch {
            Toast.makeText(requireContext(), "Gemini is analyzing...", Toast.LENGTH_SHORT).show()
            val plan = GenerativeAiHelper.getExplanation("Create a 3-day study plan.")
            AiResponseBottomSheet("AI Study Plan", plan) { _, _ -> }.show(parentFragmentManager, "AI_PLAN")
        }
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener { showAddTaskDialog() }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val editSubject = dialogView.findViewById<TextInputEditText>(R.id.editTextSubject)
        val editTopic = dialogView.findViewById<TextInputEditText>(R.id.editTextTopic)
        val editDate = dialogView.findViewById<TextInputEditText>(R.id.editTextDate)
        val editTime = dialogView.findViewById<TextInputEditText>(R.id.editTextTime)

        AlertDialog.Builder(requireContext())
            .setTitle("Schedule Task")
            .setView(dialogView)
            .setPositiveButton("Schedule") { _, _ ->
                val subject = editSubject.text.toString()
                val topic = editTopic.text.toString()
                if (subject.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        db.appDao().insertTask(TaskEntity(subject = subject, topic = topic, date = editDate.text.toString(), time = editTime.text.toString()))
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
