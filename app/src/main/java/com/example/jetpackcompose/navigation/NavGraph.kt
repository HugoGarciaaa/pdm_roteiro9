package com.example.jetpackcompose.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jetpackcompose.ui.components.BottomNavigationBar
import com.example.jetpackcompose.ui.screens.CategoryFormScreen
import com.example.jetpackcompose.ui.screens.CategoryListScreen
import com.example.jetpackcompose.ui.screens.TaskFormScreen
import com.example.jetpackcompose.ui.screens.TaskListScreen
import com.example.jetpackcompose.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    data object Tasks : Screen("tasks")
    data object Categories : Screen("categories")
    data object TaskForm : Screen("task_form?taskId={taskId}") {
        fun createRoute(taskId: String? = null): String {
            return if (taskId != null) "task_form?taskId=$taskId" else "task_form"
        }
    }
    data object CategoryForm : Screen("category_form")
}

@Composable
fun MainAppNavigation(
    navController: NavHostController = rememberNavController(),
    taskViewModel: TaskViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Tasks.route

    val isBottomBarVisible = currentRoute == Screen.Tasks.route || currentRoute == Screen.Categories.route

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateToTasks = {
                        if (currentRoute != Screen.Tasks.route) {
                            navController.navigate(Screen.Tasks.route) {
                                popUpTo(Screen.Tasks.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onNavigateToCategories = {
                        if (currentRoute != Screen.Categories.route) {
                            navController.navigate(Screen.Categories.route) {
                                popUpTo(Screen.Tasks.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tasks.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Screen 1: Task List (Accessible via Bottom Bar)
            composable(Screen.Tasks.route) {
                TaskListScreen(
                    viewModel = taskViewModel,
                    onAddTaskClick = {
                        navController.navigate(Screen.TaskForm.createRoute())
                    },
                    onEditTaskClick = { taskId ->
                        navController.navigate(Screen.TaskForm.createRoute(taskId))
                    }
                )
            }

            // Screen 2: Category List (Accessible via Bottom Bar)
            composable(Screen.Categories.route) {
                CategoryListScreen(
                    viewModel = taskViewModel,
                    onAddCategoryClick = {
                        navController.navigate(Screen.CategoryForm.route)
                    }
                )
            }

            // Screen 3: Task Form (Add or Edit Task - Form 1)
            composable(
                route = Screen.TaskForm.route,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId")
                TaskFormScreen(
                    viewModel = taskViewModel,
                    taskId = taskId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Screen 4: Category Form (Add Category - Form 2)
            composable(Screen.CategoryForm.route) {
                CategoryFormScreen(
                    viewModel = taskViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
