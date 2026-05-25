package br.com.triskin.domain.model

import java.time.LocalDateTime
import java.util.UUID

enum class TaskPriority { LOW, MEDIUM, HIGH }

enum class TaskStatus { PENDING, IN_PROGRESS, COMPLETED, CANCELLED }

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val talhao: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val isSynced: Boolean = false,
    val category: String = "",
    val weatherConditionAtCreation: String? = null,
)
