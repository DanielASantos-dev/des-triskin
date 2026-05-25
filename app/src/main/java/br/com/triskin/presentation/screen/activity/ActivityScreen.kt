package br.com.triskin.presentation.screen.activity

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.triskin.R
import br.com.triskin.domain.model.ActivityType
import br.com.triskin.domain.model.FieldActivity
import br.com.triskin.domain.model.Task
import br.com.triskin.presentation.screen.task.TimePickerDialog
import br.com.triskin.presentation.util.formatHm
import br.com.triskin.presentation.util.formatShort
import br.com.triskin.presentation.util.label
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(viewModel: ActivityViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message, state.error) {
        val text = state.message ?: state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.onIntent(ActivityIntent.ConsumeMessage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.activities_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = state.tab.ordinal) {
                ActivityTab.entries.forEach { tab ->
                    Tab(
                        selected = state.tab == tab,
                        onClick = { viewModel.onIntent(ActivityIntent.SelectTab(tab)) },
                        text = {
                            Text(
                                when (tab) {
                                    ActivityTab.REGISTER -> stringResource(R.string.activities_tab_register)
                                    ActivityTab.HISTORY -> stringResource(R.string.activities_tab_history)
                                },
                            )
                        },
                    )
                }
            }

            AnimatedContent(targetState = state.tab, label = "activity-tab") { tab ->
                when (tab) {
                    ActivityTab.REGISTER -> RegisterForm(
                        form = state.form,
                        openTasks = state.openTasks,
                        snapshots = state.linkedTaskSnapshots,
                        onIntent = viewModel::onIntent,
                    )
                    ActivityTab.HISTORY -> HistoryList(
                        activities = state.activities,
                        isLoading = state.isLoading,
                        onViewDetail = { viewModel.onIntent(ActivityIntent.ViewDetail(it)) },
                        onDelete = { viewModel.onIntent(ActivityIntent.Delete(it)) },
                    )
                }
            }

            state.selectedActivity?.let { activity ->
                ActivityDetailDialog(
                    activity = activity,
                    linkedTasks = state.detailLinkedTasks,
                    onDismiss = { viewModel.onIntent(ActivityIntent.DismissDetail) },
                    onDelete = {
                        viewModel.onIntent(ActivityIntent.DismissDetail)
                        viewModel.onIntent(ActivityIntent.Delete(activity.id))
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterForm(
    form: ActivityFormState,
    openTasks: List<Task>,
    snapshots: List<Task>,
    onIntent: (ActivityIntent) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActivityTypeDropdown(
            selected = form.type,
            onSelect = { onIntent(ActivityIntent.ChangeType(it)) },
        )

        OutlinedTextField(
            value = form.talhao,
            onValueChange = { onIntent(ActivityIntent.ChangeTalhao(it)) },
            label = { Text(stringResource(R.string.activities_field_talhao)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
        )

        TimeField(
            label = stringResource(R.string.activities_field_start),
            time = form.startTime,
            onPick = { onIntent(ActivityIntent.ChangeStart(it)) },
        )
        TimeField(
            label = stringResource(R.string.activities_field_end),
            time = form.endTime,
            onPick = { onIntent(ActivityIntent.ChangeEnd(it)) },
        )

        LinkedTasksField(
            selectedIds = form.linkedTaskIds,
            openTasks = openTasks,
            snapshots = snapshots,
            onToggle = { onIntent(ActivityIntent.ToggleLinkedTask(it)) },
            onClear = { onIntent(ActivityIntent.ClearLinkedTasks) },
        )

        if (form.linkedTaskIds.isNotEmpty()) {
            Text(
                stringResource(R.string.activities_finalize_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = form.observations,
            onValueChange = { onIntent(ActivityIntent.ChangeObservations(it)) },
            label = { Text(stringResource(R.string.activities_field_observations)) },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )

        Button(
            onClick = { onIntent(ActivityIntent.Submit) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = form.isValid,
        ) {
            Text(stringResource(R.string.action_register))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun LinkedTasksField(
    selectedIds: List<String>,
    openTasks: List<Task>,
    snapshots: List<Task>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val byId = (openTasks + snapshots).associateBy { it.id }
    val selectedTasks = selectedIds.mapNotNull(byId::get)
    val menuOptions = buildList {
        addAll(openTasks)
        snapshots.forEach { snap ->
            if (openTasks.none { it.id == snap.id }) add(snap)
        }
    }

    val fieldValue = when (selectedTasks.size) {
        0 -> stringResource(R.string.activities_no_linked_task)
        1 -> selectedTasks.first().taskLabel()
        else -> stringResource(R.string.activities_linked_count, selectedTasks.size)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = fieldValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.activities_linked_task)) },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (menuOptions.isEmpty()) {
                    DropdownMenuItem(
                        enabled = false,
                        text = { Text(stringResource(R.string.activities_no_open_tasks)) },
                        onClick = {},
                    )
                }
                menuOptions.forEach { task ->
                    val checked = task.id in selectedIds
                    DropdownMenuItem(
                        text = { Text(task.taskLabel()) },
                        leadingIcon = {
                            if (checked) {
                                Icon(Icons.Filled.Check, contentDescription = null)
                            } else {
                                Spacer(Modifier.size(24.dp))
                            }
                        },
                        onClick = { onToggle(task.id) },
                    )
                }
            }
        }

        if (selectedTasks.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                selectedTasks.forEach { task ->
                    AssistTaskChip(label = task.taskLabel(), onRemove = { onToggle(task.id) })
                }
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.action_clear))
                }
            }
        }
    }
}

@Composable
private fun AssistTaskChip(label: String, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .clickable(onClick = onRemove)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.Filled.Close,
            contentDescription = stringResource(R.string.action_remove),
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

private fun Task.taskLabel(): String =
    if (talhao.isNotBlank()) "$title · $talhao" else title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityTypeDropdown(
    selected: ActivityType,
    onSelect: (ActivityType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.activities_field_type)) },
            trailingIcon = {
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(8.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ActivityType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label()) },
                    onClick = {
                        onSelect(type)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun TimeField(
    label: String,
    time: LocalTime,
    onPick: (LocalTime) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Icon(Icons.Filled.Schedule, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("$label · ${time.formatHm()}")
    }

    if (showPicker) {
        TimePickerDialog(
            initial = time,
            onDismiss = { showPicker = false },
            onConfirm = {
                onPick(it)
                showPicker = false
            },
        )
    }
}

@Composable
private fun HistoryList(
    activities: List<FieldActivity>,
    isLoading: Boolean,
    onViewDetail: (FieldActivity) -> Unit,
    onDelete: (String) -> Unit,
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        activities.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(
                stringResource(R.string.activities_history_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(activities, key = { it.id }) { activity ->
                ActivityCard(
                    activity = activity,
                    onClick = { onViewDetail(activity) },
                    onDelete = { onDelete(activity.id) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: FieldActivity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var askDelete by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    activity.type.label(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        activity.talhao,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
                Text(
                    "${activity.startedAt.toLocalDate().formatShort()} · " +
                        "${activity.startedAt.formatHm()} – ${activity.endedAt.formatHm()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (activity.observations.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        activity.observations,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = { askDelete = true }) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (askDelete) {
        AlertDialog(
            onDismissRequest = { askDelete = false },
            title = { Text(stringResource(R.string.activities_delete_confirm)) },
            confirmButton = {
                Button(onClick = {
                    askDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { askDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun ActivityDetailDialog(
    activity: FieldActivity,
    linkedTasks: List<Task>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    var askDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                stringResource(R.string.activities_detail_title),
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow(
                    label = stringResource(R.string.activities_detail_type),
                    value = activity.type.label(),
                )
                DetailRow(
                    label = stringResource(R.string.activities_detail_talhao),
                    value = activity.talhao,
                )
                DetailRow(
                    label = stringResource(R.string.activities_detail_date),
                    value = activity.startedAt.toLocalDate().formatShort(),
                )
                DetailRow(
                    label = stringResource(R.string.activities_detail_schedule),
                    value = "${activity.startedAt.formatHm()} – ${activity.endedAt.formatHm()}",
                )

                Column {
                    Text(
                        stringResource(R.string.activities_detail_observations),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        activity.observations.ifBlank {
                            stringResource(R.string.activities_detail_no_observations)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (activity.observations.isBlank()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                }

                if (activity.taskIds.isNotEmpty()) {
                    Column {
                        Text(
                            stringResource(R.string.activities_detail_linked_tasks),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(4.dp))
                        if (linkedTasks.isEmpty()) {
                            Text(
                                stringResource(
                                    if (activity.taskIds.size == 1) {
                                        R.string.activities_detail_linked_count
                                    } else {
                                        R.string.activities_detail_linked_count_plural
                                    },
                                    activity.taskIds.size,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            linkedTasks.forEach { task ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        task.taskLabel(),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { askDelete = true }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_close))
                }
            }
        },
    )

    if (askDelete) {
        AlertDialog(
            onDismissRequest = { askDelete = false },
            title = { Text(stringResource(R.string.activities_delete_confirm)) },
            confirmButton = {
                Button(onClick = {
                    askDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { askDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
