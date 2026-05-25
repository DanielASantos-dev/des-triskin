package br.com.triskin.presentation.screen.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.usecase.GetCurrentLocationUseCase
import br.com.triskin.domain.usecase.GetWeatherUseCase
import br.com.triskin.domain.usecase.SearchLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeather: GetWeatherUseCase,
    private val searchLocation: SearchLocationUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherState())
    val state: StateFlow<WeatherState> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadFromCache()
    }

    fun onIntent(intent: WeatherIntent) {
        when (intent) {
            is WeatherIntent.SearchLocation -> queueSearch(intent.query)
            is WeatherIntent.SelectLocation -> selectLocation(intent.location)
            WeatherIntent.UseCurrentLocation -> fetchByGps()
            WeatherIntent.LocationPermissionDenied ->
                _state.update { it.copy(locationPermissionDenied = true) }
            WeatherIntent.Refresh -> _state.value.selectedLocation?.let(::fetch)
            WeatherIntent.ConsumeError -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadFromCache() {
        viewModelScope.launch {
            val cached = getWeather.fromCache()
            _state.update {
                if (cached == null) {
                    it.copy(cacheChecked = true)
                } else {
                    it.copy(
                        weather = cached,
                        selectedLocation = LocationInfo(
                            name = cached.locationName,
                            latitude = cached.latitude,
                            longitude = cached.longitude,
                            country = "",
                        ),
                        cacheChecked = true,
                    )
                }
            }
        }
    }

    private fun queueSearch(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.length < 3) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            _state.update { it.copy(isSearching = true) }
            searchLocation(query)
                .onSuccess { results ->
                    _state.update { it.copy(searchResults = results, isSearching = false) }
                }
                .onFailure {
                    _state.update { it.copy(isSearching = false) }
                }
        }
    }

    private fun selectLocation(location: LocationInfo) {
        _state.update {
            it.copy(
                selectedLocation = location,
                searchQuery = "",
                searchResults = emptyList(),
            )
        }
        fetch(location)
    }

    private fun fetchByGps() {
        viewModelScope.launch {
            _state.update { it.copy(isLocating = true, error = null, locationPermissionDenied = false) }
            getCurrentLocation()
                .onSuccess { location ->
                    _state.update {
                        it.copy(
                            isLocating = false,
                            selectedLocation = location,
                        )
                    }
                    fetch(location)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLocating = false, error = e.message) }
                }
        }
    }

    private fun fetch(location: LocationInfo) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getWeather(location)
                .onSuccess { weather ->
                    _state.update { it.copy(weather = weather, isLoading = false) }
                }
                .onFailure { e ->
                    val cached = getWeather.fromCache()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            weather = cached ?: it.weather,
                            error = e.message,
                        )
                    }
                }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
