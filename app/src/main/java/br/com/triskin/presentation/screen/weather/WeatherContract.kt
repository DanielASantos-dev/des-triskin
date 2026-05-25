package br.com.triskin.presentation.screen.weather

import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.model.WeatherInfo

sealed interface WeatherIntent {
    data class SearchLocation(val query: String) : WeatherIntent
    data class SelectLocation(val location: LocationInfo) : WeatherIntent
    data object UseCurrentLocation : WeatherIntent
    data object LocationPermissionDenied : WeatherIntent
    data object Refresh : WeatherIntent
    data object ConsumeError : WeatherIntent
}

data class WeatherState(
    val weather: WeatherInfo? = null,
    val isLoading: Boolean = false,
    val isLocating: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<LocationInfo> = emptyList(),
    val isSearching: Boolean = false,
    val selectedLocation: LocationInfo? = null,
    val locationPermissionDenied: Boolean = false,
    val cacheChecked: Boolean = false,
)
