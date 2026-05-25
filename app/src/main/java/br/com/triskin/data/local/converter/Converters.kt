package br.com.triskin.data.local.converter

import androidx.room.TypeConverter
import br.com.triskin.domain.model.ActivityType
import br.com.triskin.domain.model.TaskPriority
import br.com.triskin.domain.model.TaskStatus

class Converters {
    @TypeConverter fun fromTaskPriority(value: TaskPriority): String = value.name
    @TypeConverter fun toTaskPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter fun fromTaskStatus(value: TaskStatus): String = value.name
    @TypeConverter fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter fun fromActivityType(value: ActivityType): String = value.name
    @TypeConverter fun toActivityType(value: String): ActivityType = ActivityType.valueOf(value)
}
