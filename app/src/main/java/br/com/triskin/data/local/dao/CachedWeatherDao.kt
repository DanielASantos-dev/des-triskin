package br.com.triskin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.triskin.data.local.entity.CachedWeatherEntity

@Dao
interface CachedWeatherDao {
    @Query("SELECT * FROM cached_weather WHERE id = 1")
    suspend fun getCached(): CachedWeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weather: CachedWeatherEntity)

    @Query("DELETE FROM cached_weather")
    suspend fun clear()
}
