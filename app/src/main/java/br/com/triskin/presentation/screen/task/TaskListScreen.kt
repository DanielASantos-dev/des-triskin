package br.com.triskin.presentation.screen.task

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.triskin.R
import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.model.TaskStatus
import br.com.triskin.presentation.theme.PriorityHigh
import br.com.triskin.presentation.theme.PriorityLow
import br.com.triskin.presentation.theme.PriorityMedium
import br.com.triskin.presentation.theme.TaskCancelled
import br.com.triskin.presentation.theme.TaskCompleted
import br.com.triskin.presentation.theme.TaskInProgress
import br.com.triskin.presentation.theme.TaskPending
import br.com.triskin.presentation.util.formatHm
import br.com.triskin.presentation.util.formatShort
import br.com.triskin.presentation.util.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(viewModel: TaskListViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tasks_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onIntent(TaskListIntent.ShowCreateDialog) },
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.tasks_fab_new))
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Filters(
                state = state,
                onDate = { viewModel.onIntent(TaskListIntent.FilterByDate(it)) },
                onStatus = { viewModel.onIntent(TaskListIntent.FilterByStatus(it)) },
                onPriority = { viewModel.onIntent(TaskListIntent.FilterByPriority(it)) },
            )

            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.onIntent(TaskListIntent.Refresh) },
            ) {
                AnimatedContent(
                    targetState = TaskBody.from(state),
                    label = "task-body",
                ) { body ->
                    when (body) {
                        TaskBody.Loading -> CenteredBox { CircularProgressIndicator() }
                        is TaskBody.Error -> CenteredBox {
                            Text(body.message, color = MaterialTheme.colorScheme.error)
                        }
                        TaskBody.Empty -> EmptyState()
                        is TaskBody.Loaded -> TaskList(
                            tasks = body.tasks,
                            onEdit = { viewModel.onIntent(TaskListIntent.ShowEditDialog(it)) },
                            onDelete = { viewModel.onIntent(TaskListIntent.DeleteTask(it)) },
                            onStatusChange = { id, status ->
                                viewModel.onIntent(TaskListIntent.UpdateStatus(id, status))
                            },
                        )
                    }
                }
            }
        }

        when (val dialog = state.dialog) {
            TaskDialog.Create -> TaskFormDialog(
                initial = null,
                onDismiss = { viewModel.onIntent(TaskListIntent.HideDialog) },
                onSubmit = { viewModel.onIntent(it) },
            )
            is TaskDialog.Edit -> TaskFormDialog(
                initial = dialog.task,
                onDismiss = { viewModel.onIntent(TaskListIntent.HideDialog) },
                onSubmit = { viewModel.onIntent(it) },
            )
            null -> Unit
        }
    }
}

private sealed interface TaskBody {
    data object Loading : TaskBody
    data object Empty : TaskBody
    data class Loaded(val tasks: List<Task>) : TaskBody
    data class Error(val message: String) : TaskBody

    companion object {
        fun from(state: TaskListState): TaskBody = when {
            state.isLoading && state.tasks.isEmpty() -> Loading
            state.error != null -> Error(state.error)
            state.tasks.isEmpty() -> Empty
            else -> Loaded(state.tasks)
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.tasks_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 80.dp),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Filters(
    state: TaskListState,
    onDate: (DateFilter) -> Unit,
    onStatus: (TaskStatus?) -> Unit,
    onPriority: (TaskPriority?) -> Unit,
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filterDate == filter,
                    onClick = { onDate(filter) },
                    label = {
                        Text(
                            when (filter) {
                                DateFilter.TODAY -> stringResource(R.string.tasks_filter_today)
                                DateFilter.WEEK -> stringResource(R.string.tasks_filter_week)
                                DateFilter.ALL -> stringResource(R.string.tasks_filter_all)
                            },
                        )
                    },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        StatusFilterDropdown(
            selected = state.filterStatus,
            onSelect = onStatus,
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.filterPriority == null,
                onClick = { onPriority(null) },
                label = { Text(stringResource(R.string.tasks_filter_priority_all)) },
            )
            TaskPriority.entries.forEach { priority ->
                FilterChip(
                    selected = state.filterPriority == priority,
                    onClick = { onPriority(priority) },
                    label = { Text(priority.label()) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusFilterDropdown(
    selected: TaskStatus?,
    onSelect: (TaskStatus?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = selected?.label() ?: stringResource(R.string.tasks_filter_all)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.tasks_filter_all)) },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(8.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.tasks_filter_all)) },
                onClick = {
                    onSelect(null)
                    expanded = false
                },
            )
            TaskStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label()) },
                    onClick = {
                        onSelect(status)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TaskList(
    tasks: List<Task>,
    onEdit: (Task) -> Unit,
    onDelete: (String) -> Unit,
    onStatusChange: (String, TaskStatus) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tasks, key = { it.id }) { task ->
            TaskCard(
                task = task,
                onEdit = { onEdit(task) },
                onDelete = { onDelete(task.id) },
                onStatusChange = { onStatusChange(task.id, it) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val priorityColor = when (task.priority) {
        TaskPriority.LOW -> PriorityLow
        TaskPriority.MEDIUM -> PriorityMedium
        TaskPriority.HIGH -> PriorityHigh
    }
    val statusColor = when (task.status) {
        TaskStatus.PENDING -> TaskPending
        TaskStatus.IN_PROGRESS -> TaskInProgress
        TaskStatus.COMPLETED -> TaskCompleted
        TaskStatus.CANCELLED -> TaskCancelled
    }
    val animatedStatusColor by animateColorAsState(statusColor, label = "status-color")
    val isEditable = task.status != TaskStatus.COMPLETED

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isEditable) Modifier.clickable(onClick = onEdit) else Modifier),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 6.dp, height = 56.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(priorityColor),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusBadge(
                        label = task.status.label(),
                        color = animatedStatusColor,
                        onPick = onStatusChange,
                    )
                    if (task.talhao.isNotBlank()) {
                        InlineIconLabel(icon = Icons.Filled.Place, text = task.talhao)
                    }
                    task.dueDate?.let { dueDate ->
                        InlineIconLabel(
                            icon = Icons.Filled.Schedule,
                            text = "${dueDate.toLocalDate().formatShort()} · ${dueDate.formatHm()}",
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(
    label: String,
    color: Color,
    onPick: (TaskStatus) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.2f))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TaskStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label()) },
                    onClick = {
                        onPick(status)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun InlineIconLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
