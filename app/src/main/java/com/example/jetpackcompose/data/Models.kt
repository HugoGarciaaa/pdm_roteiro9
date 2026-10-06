package com.example.jetpackcompose.data

import androidx.compose.ui.graphics.Color

enum class Priority(val label: String, val colorValue: Long) {
    BAIXA("Baixa", 0xFF4CAF50),
    MEDIA("Média", 0xFFFF9800),
    ALTA("Alta", 0xFFF44336);

    fun getColor(): Color = Color(colorValue)
}

enum class TaskStatus(val label: String) {
    PENDENTE("Pendente"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída")
}

data class Category(
    val id: String,
    val name: String,
    val description: String,
    val colorHex: Long = 0xFF2196F3
)

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val priority: Priority = Priority.MEDIA,
    val status: TaskStatus = TaskStatus.PENDENTE,
    val dueDate: String = ""
)
