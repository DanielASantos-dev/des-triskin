package br.com.triskin.presentation.screen.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskStatus
import br.com.triskin.domain.usecase.CreateTaskUseCase
import br.com.triskin.domain.usecase.DeleteTaskUseCase
import br.com.triskin.domain.usecase.GetTasksUseCase
import br.com.triskin.domain.usecase.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasks: GetTasksUseCase,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val deleteTask: DeleteTaskUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    init {
        observeTasks()
    }

    fun onIntent(intent: TaskListIntent) {
        when (intent) {
            is TaskListIntent.FilterByDate -> _state.update { it.copy(filterDate = intent.filter) }
            is TaskListIntent.FilterByStatus -> _state.update { it.copy(filterStatus = intent.status) }
            is TaskListIntent.FilterByPriority -> _state.update { it.copy(filterPriority = intent.priority) }
            is TaskListIntent.DeleteTask -> viewModelScope.launch { deleteTask(intent.taskId) }
            is TaskListIntent.UpdateStatus -> updateTaskStatus(intent.taskId, intent.status)
            TaskListIntent.Refresh -> refresh()
            TaskListIntent.ShowCreateDialog -> _state.update { it.copy(dialog = TaskDialog.Create) }
            is TaskListIntent.ShowEditDialog -> _state.update { it.copy(dialog = TaskDialog.Edit(intent.task)) }
            TaskListIntent.HideDialog -> _state.update { it.copy(dialog = null) }
            is TaskListIntent.SubmitForm -> submit(intent)
        }
    }

    private fun submit(intent: TaskListIntent.SubmitForm) {
        val current = _state.value.dialog
        viewModelScope.launch {
            runCatching {
                when (current) {
                    is TaskDialog.Edit -> updateTask(intent.applyTo(current.task))
                    TaskDialog.Create, null -> createTask(intent.applyTo(null))
                }
            }
                .onSuccess { _state.update { it.copy(dialog = null) } }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            delay(REFRESH_FEEDBACK_MS)
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun observeTasks() {
        val sourceFlow: Flow<List<Task>> = _state
            .map { it.filterDate }
            .distinctUntilChanged()
            .flatMapLatest { tasksFor(it) }

        viewModelScope.launch {
            combine(
                sourceFlow,
                _state.map { it.filterStatus }.distinctUntilChanged(),
                _state.map { it.filterPriority }.distinctUntilChanged(),
            ) { tasks, status, priority ->
                tasks.filter { task ->
                    (status == null || task.status == status) &&
                        (priority == null || task.priority == priority)
                }
            }.catch { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }.collect { filtered ->
                _state.update { it.copy(tasks = filtered, isLoading = false, error = null) }
            }
        }
    }

    private fun tasksFor(filter: DateFilter): Flow<List<Task>> = when (filter) {
        DateFilter.TODAY -> getTasks.forDay(LocalDate.now())
        DateFilter.WEEK -> {
            val today = LocalDate.now()
            getTasks.between(today, today.plusDays(7))
        }
        DateFilter.ALL -> getTasks.all()
    }

    private fun updateTaskStatus(id: String, status: TaskStatus) {
        viewModelScope.launch {
            val task = _state.value.tasks.firstOrNull { it.id == id } ?: return@launch
            updateTask(task.copy(status = status))
        }
    }

    private companion object {
        const val REFRESH_FEEDBACK_MS = 350L
    }
}
