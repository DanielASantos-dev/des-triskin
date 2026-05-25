package br.com.triskin.domain.repository

import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksDueOn(date: LocalDate): Flow<List<Task>>
    fun getTasksDueBetween(from: LocalDate, to: LocalDate): Flow<List<Task>>
    fun getTasksByStatus(status: TaskStatus): Flow<List<Task>>
    fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>>
    fun getTaskById(id: String): Flow<Task?>
    suspend fun createTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(id: String)
}
