package br.com.triskin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.triskin.data.local.converter.Converters
import br.com.triskin.data.local.dao.CachedWeatherDao
import br.com.triskin.data.local.dao.FieldActivityDao
import br.com.triskin.data.local.dao.TaskDao
import br.com.triskin.data.local.entity.CachedWeatherEntity
import br.com.triskin.data.local.entity.FieldActivityEntity
import br.com.triskin.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class, FieldActivityEntity::class, CachedWeatherEntity::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TriskinDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun fieldActivityDao(): FieldActivityDao
    abstract fun cachedWeatherDao(): CachedWeatherDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN talhao TEXT NOT NULL DEFAULT ''")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS field_activities (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                talhao TEXT NOT NULL,
                startedAt TEXT NOT NULL,
                endedAt TEXT NOT NULL,
                observations TEXT NOT NULL,
                createdAt TEXT NOT NULL,
                isSynced INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_field_activities_startedAt ON field_activities(startedAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_field_activities_type ON field_activities(type)")

        db.execSQL("DROP TABLE IF EXISTS cached_weather")
        db.execSQL(
            """
            CREATE TABLE cached_weather (
                id INTEGER NOT NULL PRIMARY KEY,
                temperature REAL NOT NULL,
                feelsLike REAL NOT NULL,
                humidity INTEGER NOT NULL,
                windSpeed REAL NOT NULL,
                weatherCode INTEGER NOT NULL,
                description TEXT NOT NULL,
                locationName TEXT NOT NULL,
                latitude REAL NOT NULL,
                longitude REAL NOT NULL,
                hourlyJson TEXT NOT NULL,
                cachedAt TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE field_activities ADD COLUMN taskId TEXT")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_field_activities_taskId ON field_activities(taskId)")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE field_activities_new (
                id TEXT NOT NULL PRIMARY KEY,
                type TEXT NOT NULL,
                talhao TEXT NOT NULL,
                startedAt TEXT NOT NULL,
                endedAt TEXT NOT NULL,
                observations TEXT NOT NULL,
                taskIds TEXT NOT NULL DEFAULT '[]',
                createdAt TEXT NOT NULL,
                isSynced INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO field_activities_new
                (id, type, talhao, startedAt, endedAt, observations, taskIds, createdAt, isSynced)
            SELECT
                id, type, talhao, startedAt, endedAt, observations,
                CASE
                    WHEN taskId IS NOT NULL AND taskId <> ''
                    THEN '["' || taskId || '"]'
                    ELSE '[]'
                END,
                createdAt, isSynced
            FROM field_activities
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE field_activities")
        db.execSQL("ALTER TABLE field_activities_new RENAME TO field_activities")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_field_activities_startedAt ON field_activities(startedAt)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_field_activities_type ON field_activities(type)")
    }
}
