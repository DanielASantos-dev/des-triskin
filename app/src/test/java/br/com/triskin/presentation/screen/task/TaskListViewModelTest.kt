package br.com.triskin.presentation.screen.task

import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.usecase.CreateTaskUseCase
import br.com.triskin.domain.usecase.DeleteTaskUseCase
import br.com.triskin.domain.usecase.GetTasksUseCase
import br.com.triskin.domain.usecase.UpdateTaskUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {

    private val getTasks: GetTasksUseCase = mockk()
    private val createTask: CreateTaskUseCase = mockk(relaxed = true)
    private val updateTask: UpdateTaskUseCase = mockk(relaxed = true)
    private val deleteTask: DeleteTaskUseCase = mockk(relaxed = true)

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { getTasks.forDay(any()) } returns flowOf(emptyList())
        every { getTasks.between(any(), any()) } returns flowOf(emptyList())
        every { getTasks.all() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `today filter loads tasks for today`() = runTest {
        val today = LocalDate.now()
        val task = Task(title = "Plantio", talhao = "T-12")
        every { getTasks.forDay(today) } returns flowOf(listOf(task))

        val viewModel = TaskListViewModel(getTasks, createTask, updateTask, deleteTask)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(task), viewModel.state.value.tasks)
    }

    @Test
    fun `Refresh briefly toggles isRefreshing and then clears it`() = runTest {
        val viewModel = TaskListViewModel(getTasks, createTask, updateTask, deleteTask)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TaskListIntent.Refresh)
        dispatcher.scheduler.advanceTimeBy(100)
        assertEquals(true, viewModel.state.value.isRefreshing)

        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(false, viewModel.state.value.isRefreshing)
    }

    @Test
    fun `SubmitForm without existing dialog calls createTask and closes dialog`() = runTest {
        coEvery { createTask(any()) } returns Unit
        val viewModel = TaskListViewModel(getTasks, createTask, updateTask, deleteTask)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TaskListIntent.ShowCreateDialog)
        viewModel.onIntent(
            TaskListIntent.SubmitForm(
                title = "Plantio",
                talhao = "T-12",
                description = "",
                priority = TaskPriority.MEDIUM,
                expectedTime = LocalTime.of(8, 0),
            ),
        )
        dispatcher.scheduler.advanceUntilIdle()

        coVerify { createTask(any()) }
        assertEquals(null, viewModel.state.value.dialog)
    }

    @Test
    fun `SubmitForm in edit mode keeps the existing id and calls updateTask`() = runTest {
        val existing = Task(id = "abc", title = "Plantio", talhao = "T-12")
        val viewModel = TaskListViewModel(getTasks, createTask, updateTask, deleteTask)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(TaskListIntent.ShowEditDialog(existing))
        val captured = slot<Task>()
        coEvery { updateTask(capture(captured)) } returns Unit

        viewModel.onIntent(
            TaskListIntent.SubmitForm(
                title = "Plantio de Milho",
                talhao = "T-13",
                description = "atualizado",
                priority = TaskPriority.HIGH,
                expectedTime = LocalTime.of(9, 0),
            ),
        )
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals("abc", captured.captured.id)
        assertEquals("Plantio de Milho", captured.captured.title)
        assertEquals("T-13", captured.captured.talhao)
        assertEquals(null, viewModel.state.value.dialog)
    }
}
