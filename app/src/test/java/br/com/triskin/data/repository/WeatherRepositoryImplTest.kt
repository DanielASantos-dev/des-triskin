package br.com.triskin.data.repository

import br.com.triskin.data.local.dao.CachedWeatherDao
import br.com.triskin.data.mapper.WeatherCacheCodec
import br.com.triskin.data.remote.api.GeocodingApiService
import br.com.triskin.data.remote.api.WeatherApiService
import br.com.triskin.data.remote.dto.CurrentWeatherDto
import br.com.triskin.data.remote.dto.HourlyDto
import br.com.triskin.data.remote.dto.WeatherResponse
import br.com.triskin.domain.model.DataSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class WeatherRepositoryImplTest {

    private val weatherApi: WeatherApiService = mockk()
    private val geocodingApi: GeocodingApiService = mockk(relaxed = true)
    private val cachedWeatherDao: CachedWeatherDao = mockk(relaxed = true)
    private val codec = WeatherCacheCodec(Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build())

    private val repository = WeatherRepositoryImpl(
        weatherApi = weatherApi,
        geocodingApi = geocodingApi,
        cachedWeatherDao = cachedWeatherDao,
        cacheCodec = codec,
    )

    @Test
    fun `fetch on success caches the result and tags source as LIVE`() = runTest {
        coEvery { weatherApi.getWeather(any(), any(), any(), any(), any(), any()) } returns sampleResponse()
        coEvery { cachedWeatherDao.getCached() } returns null

        val result = repository.fetchWeather(-15.6, -56.1, "Cuiabá")

        assertTrue(result.isSuccess)
        assertEquals(DataSource.LIVE, result.getOrThrow().source)
        assertEquals("Cuiabá", result.getOrThrow().locationName)
        coVerify { cachedWeatherDao.upsert(any()) }
    }

    @Test
    fun `fetch propagates network failures`() = runTest {
        coEvery { weatherApi.getWeather(any(), any(), any(), any(), any(), any()) } throws IOException("offline")

        val result = repository.fetchWeather(-15.6, -56.1, "Cuiabá")

        assertTrue(result.isFailure)
    }

    private fun sampleResponse() = WeatherResponse(
        currentWeather = CurrentWeatherDto(
            temperature = 21.5,
            windSpeed = 10.0,
            weatherCode = 1,
            time = "2026-05-22T09:00",
        ),
        hourly = HourlyDto(
            time = (0..47).map { "2026-05-${22 + it / 24}T${(it % 24).toString().padStart(2, '0')}:00" },
            temperature = (0..47).map { 20.0 + it * 0.1 },
            humidity = (0..47).map { 55 },
            apparentTemperature = (0..47).map { 19.5 + it * 0.1 },
            weatherCode = (0..47).map { 1 },
        ),
    )
}
