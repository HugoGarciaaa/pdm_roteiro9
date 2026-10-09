package com.example.jetpackcompose.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackcompose.data.Category
import com.example.jetpackcompose.data.Priority
import com.example.jetpackcompose.data.Task
import com.example.jetpackcompose.data.TaskStatus
import com.example.jetpackcompose.data.local.AppDatabase
import com.example.jetpackcompose.data.local.toDomain
import com.example.jetpackcompose.data.local.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val taskDao = database.taskDao()
    private val categoryDao = database.categoryDao()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val categories: StateFlow<List<Category>> = categoryDao.getAllCategories()
        .map { list -> list.map { it.toDomain() } }
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .map { list -> list.map { it.toDomain() } }
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<TaskStatus?>(null)
    val selectedStatusFilter: StateFlow<TaskStatus?> = _selectedStatusFilter.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: TaskStatus?) {
        _selectedStatusFilter.value = status
    }

    suspend fun getTaskById(taskId: String): Task? = withContext(Dispatchers.IO) {
        taskDao.getTaskById(taskId)?.toDomain()
    }

    suspend fun getCategoryById(categoryId: String): Category? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(categoryId)?.toDomain()
    }

    fun saveTask(
        id: String? = null,
        title: String,
        description: String,
        categoryId: String,
        priority: Priority,
        status: TaskStatus,
        dueDate: String,
        onComplete: (Boolean) -> Unit
    ) {
        if (title.isBlank()) {
            onComplete(false)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val taskId = if (id.isNullOrEmpty()) UUID.randomUUID().toString() else id
            val task = Task(
                id = taskId,
                title = title.trim(),
                description = description.trim(),
                categoryId = categoryId,
                priority = priority,
                status = status,
                dueDate = dueDate.trim()
            )
            taskDao.insertTask(task.toEntity())
            _isLoading.value = false
            withContext(Dispatchers.Main) {
                onComplete(true)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            taskDao.deleteTaskById(taskId)
            _isLoading.value = false
        }
    }

    fun toggleTaskStatus(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val taskEntity = taskDao.getTaskById(taskId) ?: return@launch
            val task = taskEntity.toDomain()
            val nextStatus = when (task.status) {
                TaskStatus.PENDENTE -> TaskStatus.EM_ANDAMENTO
                TaskStatus.EM_ANDAMENTO -> TaskStatus.CONCLUIDA
                TaskStatus.CONCLUIDA -> TaskStatus.PENDENTE
            }
            val updatedTask = task.copy(status = nextStatus)
            taskDao.insertTask(updatedTask.toEntity())
        }
    }

    fun saveCategory(
        id: String? = null,
        name: String,
        description: String,
        colorHex: Long,
        onComplete: (Boolean) -> Unit
    ) {
        if (name.isBlank()) {
            onComplete(false)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val categoryId = if (id.isNullOrEmpty()) UUID.randomUUID().toString() else id
            val category = Category(
                id = categoryId,
                name = name.trim(),
                description = description.trim(),
                colorHex = colorHex
            )
            categoryDao.insertCategory(category.toEntity())
            _isLoading.value = false
            withContext(Dispatchers.Main) {
                onComplete(true)
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            categoryDao.deleteCategoryById(categoryId)
            _isLoading.value = false
        }
    }

    fun getTaskCountForCategory(categoryId: String, currentTasks: List<Task>): Int {
        return currentTasks.count { it.categoryId == categoryId }
    }
}
