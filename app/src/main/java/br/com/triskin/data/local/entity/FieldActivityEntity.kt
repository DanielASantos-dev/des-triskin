package br.com.triskin.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.triskin.domain.model.ActivityType

@Entity(
    tableName = "field_activities",
    indices = [
        Index("startedAt"),
        Index("type"),
    ],
)
data class FieldActivityEntity(
    @PrimaryKey val id: String,
    val type: ActivityType,
    val talhao: String,
    val startedAt: String,
    val endedAt: String,
    val observations: String,
    val taskIds: String,
    val createdAt: String,
    val isSynced: Boolean,
)
