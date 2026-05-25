package br.com.triskin.domain.repository

import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.model.WeatherInfo

interface WeatherRepository {
    suspend fun fetchWeather(latitude: Double, longitude: Double, locationName: String): Result<WeatherInfo>
    suspend fun searchLocation(query: String): Result<List<LocationInfo>>
    suspend fun getCachedWeather(): WeatherInfo?
}
