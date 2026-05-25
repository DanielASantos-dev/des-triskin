package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.FieldActivity
import br.com.triskin.domain.model.TaskStatus
import br.com.triskin.domain.repository.FieldActivityRepository
import br.com.triskin.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import javax.inject.Inject

class RegisterActivityUseCase @Inject constructor(
    private val activities: FieldActivityRepository,
    private val tasks: TaskRepository,
) {
    suspend operator fun invoke(activity: FieldActivity): Result<Unit> = runCatching {
        require(activity.talhao.isNotBlank()) { "Informe o talhão" }
        require(activity.endedAt.isAfter(activity.startedAt)) {
            "A hora de fim deve ser posterior à hora de início"
        }
        activities.upsert(activity)
        for (taskId in activity.taskIds) {
            completeIfOpen(taskId)
        }
    }

    private suspend fun completeIfOpen(taskId: String) {
        val linked = tasks.getTaskById(taskId).first() ?: return
        if (linked.status == TaskStatus.COMPLETED) return
        tasks.updateTask(linked.copy(status = TaskStatus.COMPLETED, updatedAt = LocalDateTime.now()))
    }
}
