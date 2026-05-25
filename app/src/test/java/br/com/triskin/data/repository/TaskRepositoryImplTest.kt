package br.com.triskin.data.repository

import app.cash.turbine.test
import br.com.triskin.data.local.dao.TaskDao
import br.com.triskin.data.mapper.toEntity
import br.com.triskin.domain.model.Task
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class TaskRepositoryImplTest {

    private val dao: TaskDao = mockk(relaxed = true)
    private val repository = TaskRepositoryImpl(dao)

    @Test
    fun `getTasksDueOn passes start-of-day range to DAO`() = runTest {
        every { dao.getTasksDueBetween(any(), any()) } returns flowOf(emptyList())

        repository.getTasksDueOn(LocalDate.of(2026, 5, 22)).test {
            awaitItem()
            awaitComplete()
        }

        coVerify {
            dao.getTasksDueBetween(
                from = "2026-05-22T00:00:00",
                to = "2026-05-23T00:00:00",
            )
        }
    }

    @Test
    fun `getAllTasks maps entities to domain`() = runTest {
        val task = Task(
            id = "1",
            title = "Plantio",
            talhao = "T-12",
            createdAt = LocalDateTime.of(2026, 5, 22, 8, 0),
            updatedAt = LocalDateTime.of(2026, 5, 22, 8, 0),
        )
        every { dao.getAllTasks() } returns flowOf(listOf(task.toEntity()))

        repository.getAllTasks().test {
            assertEquals(listOf(task), awaitItem())
            awaitComplete()
        }
    }
}
