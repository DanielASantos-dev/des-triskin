package br.com.triskin.presentation.screen.weather

import br.com.triskin.domain.model.DataSource
import br.com.triskin.domain.model.HourlyForecast
import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.model.WeatherInfo
import br.com.triskin.domain.usecase.GetCurrentLocationUseCase
import br.com.triskin.domain.usecase.GetWeatherUseCase
import br.com.triskin.domain.usecase.SearchLocationUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val getWeather: GetWeatherUseCase = mockk()
    private val searchLocation: SearchLocationUseCase = mockk()
    private val getCurrentLocation: GetCurrentLocationUseCase = mockk()
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `selecting a location fetches LIVE weather`() = runTest {
        val weather = sampleWeather(DataSource.LIVE)
        coEvery { getWeather.fromCache() } returns null
        coEvery { getWeather(any()) } returns Result.success(weather)

        val viewModel = WeatherViewModel(getWeather, searchLocation, getCurrentLocation)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(
            WeatherIntent.SelectLocation(
                LocationInfo("Cuiabá", -15.6, -56.1, "Brasil", "MT"),
            ),
        )
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(DataSource.LIVE, viewModel.state.value.weather?.source)
        assertEquals("Cuiabá", viewModel.state.value.weather?.locationName)
    }

    @Test
    fun `UseCurrentLocation success fetches weather for the resolved coordinates`() = runTest {
        val location = LocationInfo("Cuiabá", -15.6, -56.1, "", null)
        val weather = sampleWeather(DataSource.LIVE)
        coEvery { getWeather.fromCache() } returns null
        coEvery { getCurrentLocation() } returns Result.success(location)
        coEvery { getWeather(location) } returns Result.success(weather)

        val viewModel = WeatherViewModel(getWeather, searchLocation, getCurrentLocation)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(WeatherIntent.UseCurrentLocation)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(location, viewModel.state.value.selectedLocation)
        assertEquals(weather, viewModel.state.value.weather)
        assertEquals(false, viewModel.state.value.isLocating)
    }

    @Test
    fun `network failure falls back to cached weather`() = runTest {
        val cached = sampleWeather(DataSource.CACHE)
        coEvery { getWeather.fromCache() } returns cached
        coEvery { getWeather(any()) } returns Result.failure(RuntimeException("offline"))

        val viewModel = WeatherViewModel(getWeather, searchLocation, getCurrentLocation)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(
            WeatherIntent.SelectLocation(
                LocationInfo("Cuiabá", -15.6, -56.1, "Brasil", "MT"),
            ),
        )
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.weather)
        assertEquals(DataSource.CACHE, state.weather?.source)
        assertEquals("offline", state.error)
    }

    private fun sampleWeather(source: DataSource) = WeatherInfo(
        temperature = 22.0,
        feelsLike = 21.0,
        humidity = 60,
        windSpeed = 10.0,
        weatherCode = 1,
        description = "Predominante limpo",
        locationName = "Cuiabá",
        latitude = -15.6,
        longitude = -56.1,
        hourlyForecast = listOf(
            HourlyForecast(
                time = LocalDateTime.of(2026, 5, 22, 9, 0),
                temperature = 22.0,
                humidity = 60,
                weatherCode = 1,
                description = "Predominante limpo",
            ),
        ),
        source = source,
        updatedAt = LocalDateTime.of(2026, 5, 22, 9, 0),
    )
}
