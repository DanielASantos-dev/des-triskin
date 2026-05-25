package br.com.triskin.data.mapper

import br.com.triskin.data.remote.dto.GeocodingResultDto
import br.com.triskin.data.remote.dto.HourlyDto
import br.com.triskin.data.remote.dto.WeatherResponse
import br.com.triskin.domain.model.DataSource
import br.com.triskin.domain.model.HourlyForecast
import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.model.WeatherInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val openMeteoTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

fun WeatherResponse.toDomain(
    locationName: String,
    latitude: Double,
    longitude: Double,
    now: LocalDateTime = LocalDateTime.now(),
): WeatherInfo {
    val truncated = now.truncatedToHour()
    val currentIdx = hourly?.time?.indexOfFirst { LocalDateTime.parse(it, openMeteoTimeFormatter) >= truncated }
        ?.takeIf { it >= 0 }
    val feelsLike = currentIdx?.let { hourly.apparentTemperature.getOrNull(it) }
        ?: currentWeather.temperature
    val humidity = currentIdx?.let { hourly.humidity.getOrNull(it) } ?: 0

    return WeatherInfo(
        temperature = currentWeather.temperature,
        feelsLike = feelsLike,
        humidity = humidity,
        windSpeed = currentWeather.windSpeed,
        weatherCode = currentWeather.weatherCode,
        description = describeWeatherCode(currentWeather.weatherCode),
        locationName = locationName,
        latitude = latitude,
        longitude = longitude,
        hourlyForecast = hourly?.toForecasts(from = truncated, count = 24).orEmpty(),
        source = DataSource.LIVE,
        updatedAt = now,
    )
}

fun GeocodingResultDto.toDomain() = LocationInfo(
    name = name,
    latitude = latitude,
    longitude = longitude,
    country = country,
    adminArea = adminArea,
)

private fun HourlyDto.toForecasts(from: LocalDateTime, count: Int): List<HourlyForecast> {
    val startIdx = time.indexOfFirst { LocalDateTime.parse(it, openMeteoTimeFormatter) >= from }
    if (startIdx < 0) return emptyList()
    val endIdx = (startIdx + count).coerceAtMost(time.size)
    val rangeSize = minOf(temperature.size, humidity.size, weatherCode.size)
    return (startIdx until endIdx.coerceAtMost(rangeSize)).map { i ->
        HourlyForecast(
            time = LocalDateTime.parse(time[i], openMeteoTimeFormatter),
            temperature = temperature[i],
            humidity = humidity[i],
            weatherCode = weatherCode[i],
            description = describeWeatherCode(weatherCode[i]),
        )
    }
}

private fun LocalDateTime.truncatedToHour(): LocalDateTime =
    withMinute(0).withSecond(0).withNano(0)

fun describeWeatherCode(code: Int): String = when (code) {
    0 -> "Céu limpo"
    1 -> "Predominante limpo"
    2 -> "Parcialmente nublado"
    3 -> "Encoberto"
    in 45..48 -> "Neblina"
    in 51..55 -> "Garoa"
    in 56..57 -> "Garoa congelante"
    in 61..65 -> "Chuva"
    in 66..67 -> "Chuva congelante"
    in 71..75 -> "Neve"
    77 -> "Granizo de neve"
    in 80..82 -> "Pancadas de chuva"
    in 85..86 -> "Pancadas de neve"
    95 -> "Tempestade"
    in 96..99 -> "Tempestade com granizo"
    else -> "Indeterminado"
}
