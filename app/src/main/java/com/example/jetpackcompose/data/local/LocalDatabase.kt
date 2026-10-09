package com.example.jetpackcompose.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.jetpackcompose.data.Category
import com.example.jetpackcompose.data.Priority
import com.example.jetpackcompose.data.Task
import com.example.jetpackcompose.data.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val colorHex: Long
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    description = description,
    colorHex = colorHex
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val categoryId: String,
    val priority: Priority,
    val status: TaskStatus,
    val dueDate: String
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    priority = priority,
    status = status,
    dueDate = dueDate
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    categoryId = categoryId,
    priority = priority,
    status = status,
    dueDate = dueDate
)

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(value: String): Priority = try {
        Priority.valueOf(value)
    } catch (e: Exception) {
        Priority.MEDIA
    }

    @TypeConverter
    fun fromTaskStatus(status: TaskStatus): String = status.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = try {
        TaskStatus.valueOf(value)
    } catch (e: Exception) {
        TaskStatus.PENDENTE
    }
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    fun deleteCategoryById(id: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    fun deleteTaskById(id: String)
}

@Database(
    entities = [CategoryEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jetpack_compose_tasks_db"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialDatabase(database.categoryDao(), database.taskDao())
                    }
                }
            }
        }

        private suspend fun populateInitialDatabase(categoryDao: CategoryDao, taskDao: TaskDao) {
            val catTrabalho = CategoryEntity(
                id = "cat_1",
                name = "Trabalho",
                description = "Tarefas e projetos profissionais",
                colorHex = 0xFF2196F3
            )
            val catEstudos = CategoryEntity(
                id = "cat_2",
                name = "Estudos",
                description = "Atividades acadêmicas e cursos",
                colorHex = 0xFF9C27B0
            )
            val catPessoal = CategoryEntity(
                id = "cat_3",
                name = "Pessoal",
                description = "Compromissos do dia a dia e saúde",
                colorHex = 0xFF4CAF50
            )

            categoryDao.insertCategory(catTrabalho)
            categoryDao.insertCategory(catEstudos)
            categoryDao.insertCategory(catPessoal)

            taskDao.insertTask(
                TaskEntity(
                    id = "task_1",
                    title = "Entregar Projeto Jetpack Compose com Room",
                    description = "Desenvolver a aplicação com banco de dados local Room, listagens e formulários.",
                    categoryId = "cat_2",
                    priority = Priority.ALTA,
                    status = TaskStatus.EM_ANDAMENTO,
                    dueDate = "15/10/2025"
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    id = "task_2",
                    title = "Reunião de Alinhamento de Sprint",
                    description = "Discutir metas com a equipe de desenvolvimento.",
                    categoryId = "cat_1",
                    priority = Priority.MEDIA,
                    status = TaskStatus.PENDENTE,
                    dueDate = "16/10/2025"
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    id = "task_3",
                    title = "Consulta Médica",
                    description = "Check-up anual no hospital de Albert Einstein.",
                    categoryId = "cat_3",
                    priority = Priority.BAIXA,
                    status = TaskStatus.CONCLUIDA,
                    dueDate = "10/10/2025"
                )
            )
        }
    }
}
