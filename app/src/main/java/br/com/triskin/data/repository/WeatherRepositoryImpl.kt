package br.com.triskin.data.repository

import br.com.triskin.data.local.dao.CachedWeatherDao
import br.com.triskin.data.mapper.WeatherCacheCodec
import br.com.triskin.data.mapper.toDomain
import br.com.triskin.data.remote.api.GeocodingApiService
import br.com.triskin.data.remote.api.WeatherApiService
import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.model.WeatherInfo
import br.com.triskin.domain.repository.WeatherRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApiService,
    private val geocodingApi: GeocodingApiService,
    private val cachedWeatherDao: CachedWeatherDao,
    private val cacheCodec: WeatherCacheCodec,
) : WeatherRepository {

    override suspend fun fetchWeather(
        latitude: Double,
        longitude: Double,
        locationName: String,
    ): Result<WeatherInfo> = runCatching {
        val response = weatherApi.getWeather(latitude = latitude, longitude = longitude)
        response.toDomain(
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
        )
    }.onSuccess { weather ->
        cachedWeatherDao.upsert(cacheCodec.toEntity(weather))
    }

    override suspend fun searchLocation(query: String): Result<List<LocationInfo>> = runCatching {
        geocodingApi.searchLocation(query).results?.map { it.toDomain() }.orEmpty()
    }

    override suspend fun getCachedWeather(): WeatherInfo? =
        cachedWeatherDao.getCached()?.let(cacheCodec::toDomain)
}
