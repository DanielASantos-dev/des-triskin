package br.com.triskin.domain.model

import java.time.LocalDateTime

enum class DataSource { LIVE, CACHE }

data class WeatherInfo(
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val source: DataSource = DataSource.LIVE,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class HourlyForecast(
    val time: LocalDateTime,
    val temperature: Double,
    val humidity: Int,
    val weatherCode: Int,
    val description: String,
)
