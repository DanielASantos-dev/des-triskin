package br.com.triskin.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.com.triskin.R
import br.com.triskin.domain.model.ActivityType
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.model.TaskStatus

@Composable
fun TaskStatus.label(): String = stringResource(
    when (this) {
        TaskStatus.PENDING -> R.string.tasks_status_pending
        TaskStatus.IN_PROGRESS -> R.string.tasks_status_in_progress
        TaskStatus.COMPLETED -> R.string.tasks_status_completed
        TaskStatus.CANCELLED -> R.string.tasks_status_cancelled
    },
)

@Composable
fun TaskPriority.label(): String = stringResource(
    when (this) {
        TaskPriority.LOW -> R.string.tasks_priority_low
        TaskPriority.MEDIUM -> R.string.tasks_priority_medium
        TaskPriority.HIGH -> R.string.tasks_priority_high
    },
)

@Composable
fun ActivityType.label(): String = stringResource(
    when (this) {
        ActivityType.PLANTIO -> R.string.activities_type_plantio
        ActivityType.COLHEITA -> R.string.activities_type_colheita
        ActivityType.ADUBACAO -> R.string.activities_type_adubacao
        ActivityType.IRRIGACAO -> R.string.activities_type_irrigacao
        ActivityType.PULVERIZACAO -> R.string.activities_type_pulverizacao
        ActivityType.OUTRO -> R.string.activities_type_outro
    },
)
