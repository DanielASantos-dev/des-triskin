package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.Task
import br.com.triskin.domain.repository.TaskRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CreateTaskUseCaseTest {

    private val repository: TaskRepository = mockk(relaxed = true)
    private val useCase = CreateTaskUseCase(repository)

    @Test
    fun `delegates to repository when title is valid`() = runTest {
        val task = Task(title = "Plantio de Milho", talhao = "T-12")

        useCase(task)

        coVerify(exactly = 1) { repository.createTask(task) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rejects blank title`() = runTest {
        useCase(Task(title = "   ", talhao = "T-12"))
    }
}
