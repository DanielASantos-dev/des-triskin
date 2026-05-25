package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskStatus
import br.com.triskin.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    fun all(): Flow<List<Task>> = repository.getAllTasks()

    fun forDay(date: LocalDate): Flow<List<Task>> = repository.getTasksDueOn(date)

    fun between(from: LocalDate, to: LocalDate): Flow<List<Task>> =
        repository.getTasksDueBetween(from, to)

    fun open(): Flow<List<Task>> = repository.getAllTasks().map { tasks ->
        tasks.filter { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }
    }

    fun byId(id: String): Flow<Task?> = repository.getTaskById(id)
}
