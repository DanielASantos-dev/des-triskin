package br.com.triskin.presentation.screen.task

import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class DateFilter { TODAY, WEEK, ALL }

sealed interface TaskListIntent {
    data class FilterByDate(val filter: DateFilter) : TaskListIntent
    data class FilterByStatus(val status: TaskStatus?) : TaskListIntent
    data class FilterByPriority(val priority: TaskPriority?) : TaskListIntent
    data class DeleteTask(val taskId: String) : TaskListIntent
    data class UpdateStatus(val taskId: String, val status: TaskStatus) : TaskListIntent
    data object Refresh : TaskListIntent
    data object ShowCreateDialog : TaskListIntent
    data class ShowEditDialog(val task: Task) : TaskListIntent
    data object HideDialog : TaskListIntent
    data class SubmitForm(
        val title: String,
        val talhao: String,
        val description: String,
        val priority: TaskPriority,
        val expectedDate: LocalDate,
        val expectedTime: LocalTime,
    ) : TaskListIntent
}

data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val filterDate: DateFilter = DateFilter.TODAY,
    val filterStatus: TaskStatus? = null,
    val filterPriority: TaskPriority? = null,
    val dialog: TaskDialog? = null,
)

sealed interface TaskDialog {
    data object Create : TaskDialog
    data class Edit(val task: Task) : TaskDialog
}

internal fun TaskListIntent.SubmitForm.applyTo(
    existing: Task?,
): Task {
    val dueDate = LocalDateTime.of(expectedDate, expectedTime)
    return existing?.copy(
        title = title.trim(),
        talhao = talhao.trim(),
        description = description.trim(),
        priority = priority,
        dueDate = dueDate,
    ) ?: Task(
        title = title.trim(),
        talhao = talhao.trim(),
        description = description.trim(),
        priority = priority,
        dueDate = dueDate,
    )
}
