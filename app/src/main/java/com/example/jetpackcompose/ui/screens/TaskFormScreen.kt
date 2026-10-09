package com.example.jetpackcompose.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpackcompose.data.Priority
import com.example.jetpackcompose.data.TaskStatus
import com.example.jetpackcompose.ui.components.CustomOutlinedTextField
import com.example.jetpackcompose.ui.components.CustomPrimaryButton
import com.example.jetpackcompose.ui.components.CustomSecondaryButton
import com.example.jetpackcompose.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFormScreen(
    viewModel: TaskViewModel,
    taskId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()

    val isEditing = !taskId.isNullOrEmpty()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIA) }
    var selectedStatus by remember { mutableStateOf(TaskStatus.PENDENTE) }
    var dueDate by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoadingForm by remember { mutableStateOf(false) }

    LaunchedEffect(taskId) {
        if (!taskId.isNullOrEmpty()) {
            isLoadingForm = true
            viewModel.getTaskById(taskId)?.let { task ->
                title = task.title
                description = task.description
                selectedCategoryId = task.categoryId
                selectedPriority = task.priority
                selectedStatus = task.status
                dueDate = task.dueDate
            }
            isLoadingForm = false
        } else {
            if (categories.isNotEmpty()) {
                selectedCategoryId = categories.first().id
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Tarefa" else "Nova Tarefa",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Input
            CustomOutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = null
                },
                label = "Título da Tarefa *",
                placeholder = "Ex: Comprar suprimentos",
                leadingIcon = Icons.Default.Title,
                isError = titleError != null,
                errorMessage = titleError
            )

            // Description Input
            CustomOutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descrição",
                placeholder = "Detalhes adicionais sobre a tarefa...",
                leadingIcon = Icons.Default.Description,
                singleLine = false,
                maxLines = 4
            )

            // Category Selection Dropdown
            Column {
                Text(
                    text = "Categoria",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    val currentCatName = categories.find { it.id == selectedCategoryId }?.name
                        ?: if (categories.isEmpty()) "Nenhuma categoria existente" else "Selecione uma categoria"

                    OutlinedTextField(
                        value = currentCatName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecione a Categoria") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        enabled = false
                    )

                    // Overlay transparent click target
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        if (categories.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Nenhuma categoria disponível") },
                                onClick = { dropdownExpanded = false }
                            )
                        } else {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Priority Selection Chips
            Column {
                Text(
                    text = "Prioridade",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Priority.entries.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.label) }
                        )
                    }
                }
            }

            // Status Selection Chips
            Column {
                Text(
                    text = "Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status.label) }
                        )
                    }
                }
            }

            // Due Date Input
            CustomOutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = "Data de Vencimento",
                placeholder = "Ex: 25/12/2025",
                leadingIcon = Icons.Default.Event
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Row (Salvar and Cancelar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "Cancelar" Button - Returns to previous route as explicitly required!
                CustomSecondaryButton(
                    text = "Cancelar",
                    icon = Icons.Default.Cancel,
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                )

                // "Salvar" Button
                CustomPrimaryButton(
                    text = if (isEditing) "Atualizar" else "Salvar",
                    icon = Icons.Default.Save,
                    isLoading = isSaving,
                    enabled = !isSaving && !isLoadingForm,
                    onClick = {
                        if (title.isBlank()) {
                            titleError = "O título é obrigatório"
                        } else {
                            isSaving = true
                            viewModel.saveTask(
                                id = taskId,
                                title = title,
                                description = description,
                                categoryId = selectedCategoryId,
                                priority = selectedPriority,
                                status = selectedStatus,
                                dueDate = dueDate,
                                onComplete = { success ->
                                    isSaving = false
                                    if (success) {
                                        onNavigateBack()
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


