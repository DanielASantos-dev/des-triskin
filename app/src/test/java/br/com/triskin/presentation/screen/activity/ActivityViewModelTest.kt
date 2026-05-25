package br.com.triskin.presentation.screen.activity

import br.com.triskin.domain.model.ActivityType
import br.com.triskin.domain.usecase.DeleteActivityUseCase
import br.com.triskin.domain.usecase.GetActivitiesUseCase
import br.com.triskin.domain.usecase.GetTasksUseCase
import br.com.triskin.domain.usecase.RegisterActivityUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    private val getActivities: GetActivitiesUseCase = mockk()
    private val getTasks: GetTasksUseCase = mockk()
    private val registerActivity: RegisterActivityUseCase = mockk()
    private val deleteActivity: DeleteActivityUseCase = mockk(relaxed = true)

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        every { getActivities() } returns flowOf(emptyList())
        every { getTasks.open() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `register success clears form and switches to history tab`() = runTest {
        coEvery { registerActivity(any()) } returns Result.success(Unit)

        val viewModel = ActivityViewModel(getActivities, getTasks, registerActivity, deleteActivity)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(ActivityIntent.ChangeTalhao("T-12"))
        viewModel.onIntent(ActivityIntent.ChangeStart(LocalTime.of(8, 0)))
        viewModel.onIntent(ActivityIntent.ChangeEnd(LocalTime.of(11, 0)))
        viewModel.onIntent(ActivityIntent.ChangeType(ActivityType.COLHEITA))
        viewModel.onIntent(ActivityIntent.Submit)
        dispatcher.scheduler.advanceUntilIdle()

        val after = viewModel.state.value
        assertEquals(ActivityTab.HISTORY, after.tab)
        assertEquals("", after.form.talhao)
        assertEquals("Atividade registrada", after.message)
        coVerify(exactly = 1) { registerActivity(any()) }
    }

    @Test
    fun `register failure surfaces error message`() = runTest {
        coEvery { registerActivity(any()) } returns Result.failure(IllegalArgumentException("Informe o talhão"))

        val viewModel = ActivityViewModel(getActivities, getTasks, registerActivity, deleteActivity)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(ActivityIntent.Submit)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.error?.contains("talhão") == true)
    }
}
