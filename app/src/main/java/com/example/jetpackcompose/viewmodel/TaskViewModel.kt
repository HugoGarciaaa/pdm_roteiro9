package com.example.jetpackcompose.viewmodel

import androidx.lifecycle.ViewModel
import com.example.jetpackcompose.data.Category
import com.example.jetpackcompose.data.Priority
import com.example.jetpackcompose.data.Task
import com.example.jetpackcompose.data.TaskStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TaskViewModel : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<TaskStatus?>(null)
    val selectedStatusFilter: StateFlow<TaskStatus?> = _selectedStatusFilter.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val catTrabalho = Category(
            id = "cat_1",
            name = "Trabalho",
            description = "Tarefas e projetos profissionais",
            colorHex = 0xFF2196F3
        )
        val catEstudos = Category(
            id = "cat_2",
            name = "Estudos",
            description = "Atividades acadêmicas e cursos",
            colorHex = 0xFF9C27B0
        )
        val catPessoal = Category(
            id = "cat_3",
            name = "Pessoal",
            description = "Compromissos do dia a dia e saúde",
            colorHex = 0xFF4CAF50
        )

        _categories.value = listOf(catTrabalho, catEstudos, catPessoal)

        _tasks.value = listOf(
            Task(
                id = "task_1",
                title = "Entregar Projeto Jetpack Compose",
                description = "Desenvolver a aplicação com 4 telas, formulários e listagens.",
                categoryId = "cat_2",
                priority = Priority.ALTA,
                status = TaskStatus.EM_ANDAMENTO,
                dueDate = "15/10/2025"
            ),
            Task(
                id = "task_2",
                title = "Reunião de Alinhamento de Sprint",
                description = "Discutir metas com a equipe de desenvolvimento.",
                categoryId = "cat_1",
                priority = Priority.MEDIA,
                status = TaskStatus.PENDENTE,
                dueDate = "16/10/2025"
            ),
            Task(
                id = "task_3",
                title = "Consulta Médica",
                description = "Check-up anual na clínica central.",
                categoryId = "cat_3",
                priority = Priority.BAIXA,
                status = TaskStatus.CONCLUIDA,
                dueDate = "10/10/2025"
            )
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: TaskStatus?) {
        _selectedStatusFilter.value = status
    }

    fun getTaskById(taskId: String): Task? {
        return _tasks.value.find { it.id == taskId }
    }

    fun getCategoryById(categoryId: String): Category? {
        return _categories.value.find { it.id == categoryId }
    }

    fun saveTask(
        id: String? = null,
        title: String,
        description: String,
        categoryId: String,
        priority: Priority,
        status: TaskStatus,
        dueDate: String
    ): Boolean {
        if (title.isBlank()) return false

        val currentList = _tasks.value.toMutableList()
        if (id.isNullOrEmpty()) {
            val newTask = Task(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                description = description.trim(),
                categoryId = categoryId,
                priority = priority,
                status = status,
                dueDate = dueDate.trim()
            )
            currentList.add(0, newTask)
        } else {
            val index = currentList.indexOfFirst { it.id == id }
            if (index != -1) {
                currentList[index] = Task(
                    id = id,
                    title = title.trim(),
                    description = description.trim(),
                    categoryId = categoryId,
                    priority = priority,
                    status = status,
                    dueDate = dueDate.trim()
                )
            }
        }
        _tasks.value = currentList
        return true
    }



    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
    }

    fun toggleTaskStatus(taskId: String) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                val nextStatus = when (task.status) {
                    TaskStatus.PENDENTE -> TaskStatus.EM_ANDAMENTO
                    TaskStatus.EM_ANDAMENTO -> TaskStatus.CONCLUIDA
                    TaskStatus.CONCLUIDA -> TaskStatus.PENDENTE
                }
                task.copy(status = nextStatus)
            } else {
                task
            }
        }
    }

    fun saveCategory(
        id: String? = null,
        name: String,
        description: String,
        colorHex: Long
    ): Boolean {
        if (name.isBlank()) return false

        val currentList = _categories.value.toMutableList()
        if (id.isNullOrEmpty()) {
            val newCategory = Category(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                description = description.trim(),
                colorHex = colorHex
            )
            currentList.add(newCategory)
        } else {
            val index = currentList.indexOfFirst { it.id == id }
            if (index != -1) {
                currentList[index] = Category(
                    id = id,
                    name = name.trim(),
                    description = description.trim(),
                    colorHex = colorHex
                )
            }
        }
        _categories.value = currentList
        return true
    }

    fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filter { it.id != categoryId }
        // Remove or clear category references from tasks
        _tasks.value = _tasks.value.map { task ->
            if (task.categoryId == categoryId) {
                task.copy(categoryId = "")
            } else {
                task
            }
        }
    }

    fun getTaskCountForCategory(categoryId: String): Int {
        return _tasks.value.count { it.categoryId == categoryId }
    }
}
