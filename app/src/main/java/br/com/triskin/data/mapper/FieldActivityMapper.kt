package br.com.triskin.data.mapper

import br.com.triskin.data.local.entity.FieldActivityEntity
import br.com.triskin.domain.model.FieldActivity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

@Singleton
class FieldActivityCodec @Inject constructor(moshi: Moshi) {

    private val adapter: JsonAdapter<List<String>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, String::class.java),
    )

    fun toEntity(activity: FieldActivity): FieldActivityEntity = FieldActivityEntity(
        id = activity.id,
        type = activity.type,
        talhao = activity.talhao,
        startedAt = activity.startedAt.format(isoFormatter),
        endedAt = activity.endedAt.format(isoFormatter),
        observations = activity.observations,
        taskIds = adapter.toJson(activity.taskIds),
        createdAt = activity.createdAt.format(isoFormatter),
        isSynced = activity.isSynced,
    )

    fun toDomain(entity: FieldActivityEntity): FieldActivity = FieldActivity(
        id = entity.id,
        type = entity.type,
        talhao = entity.talhao,
        startedAt = LocalDateTime.parse(entity.startedAt, isoFormatter),
        endedAt = LocalDateTime.parse(entity.endedAt, isoFormatter),
        observations = entity.observations,
        taskIds = runCatching { adapter.fromJson(entity.taskIds) }.getOrNull().orEmpty(),
        createdAt = LocalDateTime.parse(entity.createdAt, isoFormatter),
        isSynced = entity.isSynced,
    )
}
