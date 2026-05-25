package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.Task
import br.com.triskin.domain.repository.TaskRepository
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(task: Task) {
        require(task.title.isNotBlank()) { "O nome da tarefa não pode estar em branco" }
        repository.createTask(task)
    }
}
