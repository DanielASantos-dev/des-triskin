package br.com.triskin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey val id: Int = 1,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val hourlyJson: String,
    val cachedAt: String,
)
