package br.com.triskin.presentation.screen.activity

import br.com.triskin.domain.model.ActivityType
import br.com.triskin.domain.model.FieldActivity
import br.com.triskin.domain.model.Task
import java.time.LocalTime
import java.util.UUID

enum class ActivityTab { REGISTER, HISTORY }

sealed interface ActivityIntent {
    data class SelectTab(val tab: ActivityTab) : ActivityIntent
    data class ChangeType(val type: ActivityType) : ActivityIntent
    data class ChangeTalhao(val talhao: String) : ActivityIntent
    data class ChangeStart(val time: LocalTime) : ActivityIntent
    data class ChangeEnd(val time: LocalTime) : ActivityIntent
    data class ChangeObservations(val text: String) : ActivityIntent
    data class ToggleLinkedTask(val taskId: String) : ActivityIntent
    data object ClearLinkedTasks : ActivityIntent
    data class ViewDetail(val activity: FieldActivity) : ActivityIntent
    data object DismissDetail : ActivityIntent
    data object Submit : ActivityIntent
    data class Delete(val id: String) : ActivityIntent
    data object ConsumeMessage : ActivityIntent
}

data class ActivityFormState(
    val type: ActivityType = ActivityType.PLANTIO,
    val talhao: String = "",
    val startTime: LocalTime = LocalTime.of(8, 0),
    val endTime: LocalTime = LocalTime.of(11, 0),
    val observations: String = "",
    val linkedTaskIds: List<String> = emptyList(),
) {
    val isValid: Boolean get() = talhao.isNotBlank() && endTime > startTime

    fun toDomain(now: java.time.LocalDate = java.time.LocalDate.now()): FieldActivity = FieldActivity(
        id = UUID.randomUUID().toString(),
        type = type,
        talhao = talhao.trim(),
        startedAt = java.time.LocalDateTime.of(now, startTime),
        endedAt = java.time.LocalDateTime.of(now, endTime),
        observations = observations.trim(),
        taskIds = linkedTaskIds,
    )
}

data class ActivityScreenState(
    val tab: ActivityTab = ActivityTab.REGISTER,
    val form: ActivityFormState = ActivityFormState(),
    val activities: List<FieldActivity> = emptyList(),
    val openTasks: List<Task> = emptyList(),
    val linkedTaskSnapshots: List<Task> = emptyList(),
    val selectedActivity: FieldActivity? = null,
    val detailLinkedTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val error: String? = null,
)
