package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.model.WeatherInfo
import br.com.triskin.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository,
) {
    suspend operator fun invoke(location: LocationInfo): Result<WeatherInfo> =
        repository.fetchWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            locationName = location.name,
        )

    suspend fun fromCache(): WeatherInfo? = repository.getCachedWeather()
}
