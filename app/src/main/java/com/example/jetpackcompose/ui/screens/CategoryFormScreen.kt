package com.example.jetpackcompose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jetpackcompose.ui.components.CustomOutlinedTextField
import com.example.jetpackcompose.ui.components.CustomPrimaryButton
import com.example.jetpackcompose.ui.components.CustomSecondaryButton
import com.example.jetpackcompose.viewmodel.TaskViewModel

private val PRESET_COLORS = listOf(
    0xFF2196F3, // Azul
    0xFF9C27B0, // Roxo
    0xFF4CAF50, // Verde
    0xFFFF9800, // Laranja
    0xFFF44336, // Vermelho
    0xFF00BCD4, // Ciano
    0xFF3F51B5  // Indigo
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryFormScreen(
    viewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableLongStateOf(PRESET_COLORS[0]) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nova Categoria",
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
            // Category Name Field
            CustomOutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (it.isNotBlank()) nameError = null
                },
                label = "Nome da Categoria *",
                placeholder = "Ex: Finanças, Compras, Saúde...",
                leadingIcon = Icons.Default.Category,
                isError = nameError != null,
                errorMessage = nameError
            )

            // Category Description Field
            CustomOutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = "Descrição da Categoria",
                placeholder = "Breve detalhamento sobre esta categoria...",
                leadingIcon = Icons.Default.Description,
                singleLine = false,
                maxLines = 3
            )

            // Category Color Selector
            Column {
                Text(
                    text = "Cor da Categoria",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PRESET_COLORS.forEach { colorHex ->
                        val isSelected = selectedColorHex == colorHex
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(colorHex), shape = CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selecionado",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons (Salvar and Cancelar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "Cancelar" button - Returns to previous route
                CustomSecondaryButton(
                    text = "Cancelar",
                    icon = Icons.Default.Cancel,
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f)
                )

                // "Salvar" button
                CustomPrimaryButton(
                    text = "Salvar",
                    icon = Icons.Default.Save,
                    isLoading = isSaving,
                    enabled = !isSaving,
                    onClick = {
                        if (name.isBlank()) {
                            nameError = "O nome é obrigatório"
                        } else {
                            isSaving = true
                            viewModel.saveCategory(
                                name = name,
                                description = description,
                                colorHex = selectedColorHex,
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
        }
    }
}
