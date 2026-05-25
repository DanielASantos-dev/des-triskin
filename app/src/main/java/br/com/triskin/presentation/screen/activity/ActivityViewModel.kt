package br.com.triskin.presentation.screen.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.triskin.domain.model.FieldActivity
import br.com.triskin.domain.model.Task
import br.com.triskin.domain.usecase.DeleteActivityUseCase
import br.com.triskin.domain.usecase.GetActivitiesUseCase
import br.com.triskin.domain.usecase.GetTasksUseCase
import br.com.triskin.domain.usecase.RegisterActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val getActivities: GetActivitiesUseCase,
    private val getTasks: GetTasksUseCase,
    private val registerActivity: RegisterActivityUseCase,
    private val deleteActivity: DeleteActivityUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityScreenState())
    val state: StateFlow<ActivityScreenState> = _state.asStateFlow()

    private var linkedTaskJob: Job? = null

    init {
        observeHistory()
        observeOpenTasks()
    }

    fun onIntent(intent: ActivityIntent) {
        when (intent) {
            is ActivityIntent.SelectTab -> _state.update { it.copy(tab = intent.tab) }
            is ActivityIntent.ChangeType -> updateForm { copy(type = intent.type) }
            is ActivityIntent.ChangeTalhao -> updateForm { copy(talhao = intent.talhao) }
            is ActivityIntent.ChangeStart -> updateForm { copy(startTime = intent.time) }
            is ActivityIntent.ChangeEnd -> updateForm { copy(endTime = intent.time) }
            is ActivityIntent.ChangeObservations -> updateForm { copy(observations = intent.text) }
            is ActivityIntent.ToggleLinkedTask -> toggleLinkedTask(intent.taskId)
            ActivityIntent.ClearLinkedTasks -> {
                updateForm { copy(linkedTaskIds = emptyList()) }
                refreshSnapshots()
            }
            is ActivityIntent.ViewDetail -> showDetail(intent.activity)
            ActivityIntent.DismissDetail -> _state.update { it.copy(selectedActivity = null, detailLinkedTasks = emptyList()) }
            ActivityIntent.Submit -> submit()
            is ActivityIntent.Delete -> viewModelScope.launch { deleteActivity(intent.id) }
            ActivityIntent.ConsumeMessage -> _state.update { it.copy(message = null, error = null) }
        }
    }

    private fun updateForm(transform: ActivityFormState.() -> ActivityFormState) {
        _state.update { it.copy(form = it.form.transform()) }
    }

    private fun toggleLinkedTask(taskId: String) {
        val currentIds = _state.value.form.linkedTaskIds
        val nextIds = if (taskId in currentIds) currentIds - taskId else currentIds + taskId
        val task = _state.value.openTasks.firstOrNull { it.id == taskId }
        updateForm {
            copy(
                linkedTaskIds = nextIds,
                talhao = if (talhao.isBlank() && taskId !in currentIds && task != null) {
                    task.talhao
                } else {
                    talhao
                },
            )
        }
        refreshSnapshots()
    }

    private fun refreshSnapshots() {
        linkedTaskJob?.cancel()
        val ids = _state.value.form.linkedTaskIds
        if (ids.isEmpty()) {
            _state.update { it.copy(linkedTaskSnapshots = emptyList()) }
            return
        }
        linkedTaskJob = viewModelScope.launch {
            val resolved = ids.mapNotNull { id -> getTasks.byId(id).first() }
            _state.update { it.copy(linkedTaskSnapshots = resolved) }
        }
    }

    private fun submit() {
        val form = _state.value.form
        viewModelScope.launch {
            registerActivity(form.toDomain())
                .onSuccess {
                    linkedTaskJob?.cancel()
                    _state.update {
                        it.copy(
                            form = ActivityFormState(),
                            linkedTaskSnapshots = emptyList(),
                            message = "Atividade registrada",
                            tab = ActivityTab.HISTORY,
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    private fun showDetail(activity: FieldActivity) {
        _state.update { it.copy(selectedActivity = activity) }
        if (activity.taskIds.isEmpty()) return
        viewModelScope.launch {
            val tasks = activity.taskIds.mapNotNull { id -> getTasks.byId(id).first() }
            _state.update { it.copy(detailLinkedTasks = tasks) }
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            getActivities()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { list ->
                    _state.update { it.copy(activities = list, isLoading = false) }
                }
        }
    }

    private fun observeOpenTasks() {
        viewModelScope.launch {
            getTasks.open()
                .catch { /* ignore — dropdown stays empty */ }
                .collect { tasks: List<Task> ->
                    _state.update { it.copy(openTasks = tasks) }
                }
        }
    }
}
