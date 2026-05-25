package br.com.triskin.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "current_weather") val currentWeather: CurrentWeatherDto,
    val hourly: HourlyDto? = null,
)

@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    val temperature: Double,
    @Json(name = "windspeed") val windSpeed: Double,
    @Json(name = "weathercode") val weatherCode: Int,
    val time: String,
)

@JsonClass(generateAdapter = true)
data class HourlyDto(
    val time: List<String>,
    @Json(name = "temperature_2m") val temperature: List<Double>,
    @Json(name = "relative_humidity_2m") val humidity: List<Int>,
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double>,
    @Json(name = "weathercode") val weatherCode: List<Int>,
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    val results: List<GeocodingResultDto>? = emptyList(),
)

@JsonClass(generateAdapter = true)
data class GeocodingResultDto(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    @Json(name = "admin1") val adminArea: String? = null,
)
