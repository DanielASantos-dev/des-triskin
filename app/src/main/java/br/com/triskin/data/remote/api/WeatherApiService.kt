package br.com.triskin.data.remote.api

import br.com.triskin.data.remote.dto.GeocodingResponse
import br.com.triskin.data.remote.dto.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = HOURLY_FIELDS,
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 2,
    ): WeatherResponse

    companion object {
        const val HOURLY_FIELDS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,weathercode"
    }
}

interface GeocodingApiService {
    @GET("search")
    suspend fun searchLocation(
        @Query("name") query: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "pt",
        @Query("format") format: String = "json",
    ): GeocodingResponse
}
