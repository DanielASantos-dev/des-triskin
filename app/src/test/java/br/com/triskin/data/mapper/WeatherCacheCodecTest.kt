package br.com.triskin.data.mapper

import br.com.triskin.domain.model.DataSource
import br.com.triskin.domain.model.HourlyForecast
import br.com.triskin.domain.model.WeatherInfo
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class WeatherCacheCodecTest {

    private val codec = WeatherCacheCodec(
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build(),
    )

    @Test
    fun `round-trip preserves hourly forecast and marks source as CACHE`() {
        val updatedAt = LocalDateTime.of(2026, 5, 22, 9, 0)
        val weather = WeatherInfo(
            temperature = 23.5,
            feelsLike = 22.5,
            humidity = 65,
            windSpeed = 11.0,
            weatherCode = 2,
            description = "Parcialmente nublado",
            locationName = "Cuiabá",
            latitude = -15.6,
            longitude = -56.1,
            hourlyForecast = List(24) { i ->
                HourlyForecast(
                    time = updatedAt.plusHours(i.toLong()),
                    temperature = 22.0 + i * 0.5,
                    humidity = 60 + i,
                    weatherCode = 1,
                    description = "Predominante limpo",
                )
            },
            source = DataSource.LIVE,
            updatedAt = updatedAt,
        )

        val entity = codec.toEntity(weather)
        val recovered = codec.toDomain(entity)

        assertEquals(weather.copy(source = DataSource.CACHE), recovered)
    }
}
