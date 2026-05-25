package br.com.triskin.data.mapper

import br.com.triskin.domain.model.Task
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class TaskMapperTest {

    @Test
    fun `round-trip preserves all fields`() {
        val source = Task(
            id = "abc-123",
            title = "Plantio de Milho",
            talhao = "T-12",
            description = "Linhas 4 a 12",
            priority = TaskPriority.HIGH,
            status = TaskStatus.IN_PROGRESS,
            dueDate = LocalDateTime.of(2026, 5, 22, 8, 0),
            createdAt = LocalDateTime.of(2026, 5, 21, 12, 0),
            updatedAt = LocalDateTime.of(2026, 5, 21, 13, 0),
            isSynced = false,
            category = "campo-norte",
            weatherConditionAtCreation = "Céu limpo",
        )

        val recovered = source.toEntity().toDomain()

        assertEquals(source, recovered)
    }

    @Test
    fun `null dueDate is preserved`() {
        val source = Task(title = "Sem horário", talhao = "T-7")

        val recovered = source.toEntity().toDomain()

        assertEquals(null, recovered.dueDate)
        assertEquals(source.copy(createdAt = recovered.createdAt, updatedAt = recovered.updatedAt), recovered)
    }
}
