package br.com.triskin.data.mapper

import br.com.triskin.data.local.entity.TaskEntity
import br.com.triskin.domain.model.Task
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    talhao = talhao,
    description = description,
    priority = priority,
    status = status,
    dueDate = dueDate?.format(isoFormatter),
    createdAt = createdAt.format(isoFormatter),
    updatedAt = updatedAt.format(isoFormatter),
    isSynced = isSynced,
    category = category,
    weatherConditionAtCreation = weatherConditionAtCreation,
)

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    talhao = talhao,
    description = description,
    priority = priority,
    status = status,
    dueDate = dueDate?.let { LocalDateTime.parse(it, isoFormatter) },
    createdAt = LocalDateTime.parse(createdAt, isoFormatter),
    updatedAt = LocalDateTime.parse(updatedAt, isoFormatter),
    isSynced = isSynced,
    category = category,
    weatherConditionAtCreation = weatherConditionAtCreation,
)
