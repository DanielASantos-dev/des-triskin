package br.com.triskin.data.mapper

import br.com.triskin.data.remote.dto.CurrentWeatherDto
import br.com.triskin.data.remote.dto.HourlyDto
import br.com.triskin.data.remote.dto.WeatherResponse
import br.com.triskin.domain.model.DataSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class WeatherMapperTest {

    @Test
    fun `maps current weather and slices the next 24 hourly forecasts`() {
        val now = LocalDateTime.of(2026, 5, 22, 9, 30)
        val response = WeatherResponse(
            currentWeather = CurrentWeatherDto(
                temperature = 21.5,
                windSpeed = 12.0,
                weatherCode = 2,
                time = "2026-05-22T09:00",
            ),
            hourly = HourlyDto(
                time = (0..47).map { hour ->
                    LocalDateTime.of(2026, 5, 22, 0, 0)
                        .plusHours(hour.toLong())
                        .toString().substring(0, 16)
                },
                temperature = (0..47).map { 15.0 + it },
                humidity = (0..47).map { 60 + it },
                apparentTemperature = (0..47).map { 14.0 + it },
                weatherCode = (0..47).map { 1 },
            ),
        )

        val weather = response.toDomain(
            locationName = "Cuiabá",
            latitude = -15.6,
            longitude = -56.1,
            now = now,
        )

        assertEquals("Cuiabá", weather.locationName)
        assertEquals(21.5, weather.temperature, 0.0)
        assertEquals(23.0, weather.feelsLike, 0.0)
        assertEquals(69, weather.humidity)
        assertEquals(24, weather.hourlyForecast.size)
        assertEquals(LocalDateTime.of(2026, 5, 22, 9, 0), weather.hourlyForecast.first().time)
        assertEquals(LocalDateTime.of(2026, 5, 23, 8, 0), weather.hourlyForecast.last().time)
        assertEquals(24.0, weather.hourlyForecast.first().temperature, 0.0)
        assertEquals(DataSource.LIVE, weather.source)
    }

    @Test
    fun `weather codes get descriptive labels`() {
        assertEquals("Céu limpo", describeWeatherCode(0))
        assertEquals("Tempestade", describeWeatherCode(95))
        assertEquals("Pancadas de chuva", describeWeatherCode(80))
        assertEquals("Neve", describeWeatherCode(73))
        assertTrue(describeWeatherCode(999) == "Indeterminado")
    }
}
