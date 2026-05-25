package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.ActivityType
import br.com.triskin.domain.model.FieldActivity
import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskStatus
import br.com.triskin.domain.repository.FieldActivityRepository
import br.com.triskin.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class RegisterActivityUseCaseTest {

    private val activities: FieldActivityRepository = mockk(relaxed = true)
    private val tasks: TaskRepository = mockk(relaxed = true)
    private val useCase = RegisterActivityUseCase(activities, tasks)

    @Test
    fun `persists activity when valid`() = runTest {
        val start = LocalDateTime.of(2026, 5, 22, 8, 0)
        val activity = FieldActivity(
            type = ActivityType.PLANTIO,
            talhao = "T-12",
            startedAt = start,
            endedAt = start.plusHours(3),
        )

        val result = useCase(activity)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { activities.upsert(activity) }
        coVerify(exactly = 0) { tasks.updateTask(any()) }
    }

    @Test
    fun `fails when talhao is blank`() = runTest {
        val start = LocalDateTime.of(2026, 5, 22, 8, 0)

        val result = useCase(
            FieldActivity(
                type = ActivityType.PLANTIO,
                talhao = "  ",
                startedAt = start,
                endedAt = start.plusHours(1),
            ),
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `fails when end is not after start`() = runTest {
        val start = LocalDateTime.of(2026, 5, 22, 8, 0)

        val result = useCase(
            FieldActivity(
                type = ActivityType.COLHEITA,
                talhao = "T-1",
                startedAt = start,
                endedAt = start,
            ),
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `finalizes every linked task that is not already completed`() = runTest {
        val taskA = Task(id = "a", title = "Plantio", talhao = "T-12", status = TaskStatus.PENDING)
        val taskB = Task(id = "b", title = "Adubação", talhao = "T-13", status = TaskStatus.IN_PROGRESS)
        every { tasks.getTaskById("a") } returns flowOf(taskA)
        every { tasks.getTaskById("b") } returns flowOf(taskB)

        val start = LocalDateTime.of(2026, 5, 22, 8, 0)
        val activity = FieldActivity(
            type = ActivityType.PLANTIO,
            talhao = "T-12",
            startedAt = start,
            endedAt = start.plusHours(4),
            taskIds = listOf("a", "b"),
        )

        val result = useCase(activity)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { tasks.updateTask(match { it.id == "a" && it.status == TaskStatus.COMPLETED }) }
        coVerify(exactly = 1) { tasks.updateTask(match { it.id == "b" && it.status == TaskStatus.COMPLETED }) }
    }

    @Test
    fun `skips tasks already completed`() = runTest {
        val task = Task(id = "a", title = "Plantio", talhao = "T-12", status = TaskStatus.COMPLETED)
        every { tasks.getTaskById("a") } returns flowOf(task)

        val start = LocalDateTime.of(2026, 5, 22, 8, 0)
        val activity = FieldActivity(
            type = ActivityType.PLANTIO,
            talhao = "T-12",
            startedAt = start,
            endedAt = start.plusHours(2),
            taskIds = listOf("a"),
        )

        val result = useCase(activity)

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { tasks.updateTask(any()) }
    }
}
