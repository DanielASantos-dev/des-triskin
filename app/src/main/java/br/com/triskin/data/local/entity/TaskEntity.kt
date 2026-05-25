package br.com.triskin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.model.TaskStatus

@Entity(
    tableName = "tasks",
    indices = [
        Index("status"),
        Index("priority"),
        Index("dueDate"),
        Index("isSynced"),
    ],
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val talhao: String,
    val description: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val dueDate: String?,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean,
    val category: String,
    val weatherConditionAtCreation: String?,
)
