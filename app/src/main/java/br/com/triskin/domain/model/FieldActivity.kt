package br.com.triskin.domain.model

import java.time.LocalDateTime
import java.util.UUID

enum class ActivityType { PLANTIO, COLHEITA, ADUBACAO, IRRIGACAO, PULVERIZACAO, OUTRO }

data class FieldActivity(
    val id: String = UUID.randomUUID().toString(),
    val type: ActivityType,
    val talhao: String,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val observations: String = "",
    val taskIds: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isSynced: Boolean = false,
)
