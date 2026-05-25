package br.com.triskin.data.mapper

import br.com.triskin.data.local.entity.CachedWeatherEntity
import br.com.triskin.domain.model.DataSource
import br.com.triskin.domain.model.HourlyForecast
import br.com.triskin.domain.model.WeatherInfo
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherCacheCodec @Inject constructor(moshi: Moshi) {

    private val adapter: JsonAdapter<List<HourlySnapshot>> = moshi.adapter(
        Types.newParameterizedType(List::class.java, HourlySnapshot::class.java),
    )

    fun toEntity(weather: WeatherInfo): CachedWeatherEntity = CachedWeatherEntity(
        temperature = weather.temperature,
        feelsLike = weather.feelsLike,
        humidity = weather.humidity,
        windSpeed = weather.windSpeed,
        weatherCode = weather.weatherCode,
        description = weather.description,
        locationName = weather.locationName,
        latitude = weather.latitude,
        longitude = weather.longitude,
        hourlyJson = adapter.toJson(weather.hourlyForecast.map(::HourlySnapshot)),
        cachedAt = weather.updatedAt.format(isoFormatter),
    )

    fun toDomain(entity: CachedWeatherEntity): WeatherInfo {
        val hourly = runCatching { adapter.fromJson(entity.hourlyJson) }
            .getOrNull()
            .orEmpty()
            .map { it.toDomain() }
        return WeatherInfo(
            temperature = entity.temperature,
            feelsLike = entity.feelsLike,
            humidity = entity.humidity,
            windSpeed = entity.windSpeed,
            weatherCode = entity.weatherCode,
            description = entity.description,
            locationName = entity.locationName,
            latitude = entity.latitude,
            longitude = entity.longitude,
            hourlyForecast = hourly,
            source = DataSource.CACHE,
            updatedAt = LocalDateTime.parse(entity.cachedAt, isoFormatter),
        )
    }

    private companion object {
        val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
}

internal data class HourlySnapshot(
    val time: String,
    val temperature: Double,
    val humidity: Int,
    val weatherCode: Int,
    val description: String,
) {
    constructor(forecast: HourlyForecast) : this(
        time = forecast.time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        temperature = forecast.temperature,
        humidity = forecast.humidity,
        weatherCode = forecast.weatherCode,
        description = forecast.description,
    )

    fun toDomain(): HourlyForecast = HourlyForecast(
        time = LocalDateTime.parse(time, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        temperature = temperature,
        humidity = humidity,
        weatherCode = weatherCode,
        description = description,
    )
}
