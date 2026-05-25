package br.com.triskin.domain.usecase

import br.com.triskin.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(taskId: String) {
        repository.deleteTask(taskId)
    }
}
