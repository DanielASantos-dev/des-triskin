package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.Task
import br.com.triskin.domain.repository.TaskRepository
import java.time.LocalDateTime
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(task: Task) {
        repository.updateTask(task.copy(updatedAt = LocalDateTime.now()))
    }
}
