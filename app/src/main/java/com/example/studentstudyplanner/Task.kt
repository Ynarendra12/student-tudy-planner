package com.example.studentstudyplanner

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: String,
    var isCompleted: Boolean = false
)