package br.com.triskin.data.mapper

import br.com.triskin.domain.model.ActivityType
import br.com.triskin.domain.model.FieldActivity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class FieldActivityMapperTest {

    private val codec = FieldActivityCodec(
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build(),
    )

    @Test
    fun `round-trip preserves all fields including multiple linked taskIds`() {
        val source = FieldActivity(
            id = "ac-1",
            type = ActivityType.PULVERIZACAO,
            talhao = "T-3",
            startedAt = LocalDateTime.of(2026, 5, 22, 6, 30),
            endedAt = LocalDateTime.of(2026, 5, 22, 9, 0),
            observations = "Linhas 1 a 6",
            taskIds = listOf("task-a", "task-b", "task-c"),
            createdAt = LocalDateTime.of(2026, 5, 22, 9, 5),
            isSynced = false,
        )

        val recovered = codec.toDomain(codec.toEntity(source))

        assertEquals(source, recovered)
    }

    @Test
    fun `empty taskIds round-trips as empty list`() {
        val source = FieldActivity(
            type = ActivityType.PLANTIO,
            talhao = "T-1",
            startedAt = LocalDateTime.of(2026, 5, 22, 6, 0),
            endedAt = LocalDateTime.of(2026, 5, 22, 7, 0),
        )

        val recovered = codec.toDomain(codec.toEntity(source))

        assertEquals(emptyList<String>(), recovered.taskIds)
    }
}
