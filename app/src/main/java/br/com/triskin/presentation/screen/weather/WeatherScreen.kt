package br.com.triskin.presentation.screen.weather

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.triskin.R
import br.com.triskin.domain.model.DataSource
import br.com.triskin.domain.model.HourlyForecast
import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.model.WeatherInfo
import br.com.triskin.presentation.theme.Success
import br.com.triskin.presentation.theme.TaskPending
import br.com.triskin.presentation.util.formatHm
import br.com.triskin.presentation.util.formatShortDateTime
import br.com.triskin.presentation.util.weatherIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onIntent(WeatherIntent.UseCurrentLocation)
        else viewModel.onIntent(WeatherIntent.LocationPermissionDenied)
    }

    val requestLocation: () -> Unit = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) viewModel.onIntent(WeatherIntent.UseCurrentLocation)
        else permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    LaunchedEffect(state.cacheChecked) {
        if (state.cacheChecked && state.weather == null && !state.locationPermissionDenied) {
            requestLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.weather_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            SearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onIntent(WeatherIntent.SearchLocation(it)) },
                isSearching = state.isSearching,
                results = state.searchResults,
                onSelectLocation = { viewModel.onIntent(WeatherIntent.SelectLocation(it)) },
                onUseLocation = requestLocation,
                isLocating = state.isLocating,
            )

            Spacer(Modifier.height(16.dp))

            PullToRefreshBox(
                isRefreshing = state.isLoading && state.weather != null,
                onRefresh = { viewModel.onIntent(WeatherIntent.Refresh) },
            ) {
                AnimatedContent(targetState = WeatherBody.from(state), label = "weather-body") { body ->
                    when (body) {
                        WeatherBody.Loading -> CenteredBox { CircularProgressIndicator() }
                        is WeatherBody.Empty -> EmptyHint(
                            message = body.message,
                            locationDenied = state.locationPermissionDenied,
                            isLocating = state.isLocating,
                            onUseLocation = requestLocation,
                        )
                        is WeatherBody.Data -> WeatherContent(
                            weather = body.weather,
                            onRefresh = { viewModel.onIntent(WeatherIntent.Refresh) },
                            isRefreshing = state.isLoading,
                        )
                    }
                }
            }
        }
    }
}

private sealed interface WeatherBody {
    data object Loading : WeatherBody
    data class Empty(val message: String) : WeatherBody
    data class Data(val weather: WeatherInfo) : WeatherBody

    companion object {
        fun from(state: WeatherState): WeatherBody = when {
            state.isLoading && state.weather == null -> Loading
            state.weather != null -> Data(state.weather)
            state.error != null -> Empty(state.error)
            else -> Empty("")
        }
    }
}

@Composable
private fun CenteredBox(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) { content() }
}

@Composable
private fun EmptyHint(
    message: String,
    locationDenied: Boolean,
    isLocating: Boolean,
    onUseLocation: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Icon(
            Icons.Filled.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            when {
                message.isNotBlank() -> message
                locationDenied -> stringResource(R.string.weather_permission_denied)
                else -> stringResource(R.string.weather_empty_hint)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!locationDenied) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onUseLocation,
                enabled = !isLocating,
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isLocating) stringResource(R.string.weather_locating)
                    else stringResource(R.string.weather_use_location),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    results: List<LocationInfo>,
    onSelectLocation: (LocationInfo) -> Unit,
    onUseLocation: () -> Unit,
    isLocating: Boolean,
) {
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.weather_search_hint)) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onUseLocation, enabled = !isLocating) {
                    if (isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            Icons.Filled.MyLocation,
                            contentDescription = stringResource(R.string.weather_use_location),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
        )
        if (results.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(results) { location ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectLocation(location) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(location.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    listOfNotNull(location.adminArea, location.country)
                                        .filter { it.isNotBlank() }
                                        .joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
        if (isSearching) {
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth(), Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun WeatherContent(
    weather: WeatherInfo,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SourceIndicator(
            source = weather.source,
            updatedAt = weather.updatedAt,
            onRefresh = onRefresh,
            isRefreshing = isRefreshing,
        )
        CurrentWeatherCard(weather)
        HourlyForecastSection(forecasts = weather.hourlyForecast)
    }
}

@Composable
private fun SourceIndicator(
    source: DataSource,
    updatedAt: java.time.LocalDateTime,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
) {
    val (color, text) = when (source) {
        DataSource.LIVE -> Success to stringResource(R.string.weather_source_live)
        DataSource.CACHE -> TaskPending to stringResource(
            R.string.weather_source_cache,
            updatedAt.formatShortDateTime(),
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = color)
        }
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onRefresh,
            enabled = !isRefreshing,
            modifier = Modifier.size(36.dp),
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.action_retry),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CurrentWeatherCard(weather: WeatherInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(weather.locationName, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Icon(
                imageVector = weatherIcon(weather.weatherCode),
                contentDescription = weather.description,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                "${weather.temperature.toInt()}°C",
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                weather.description,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Metric(
                    label = stringResource(R.string.weather_detail_feels_like),
                    value = "${weather.feelsLike.toInt()}°C",
                )
                Metric(
                    label = stringResource(R.string.weather_detail_humidity),
                    value = "${weather.humidity}%",
                )
                Metric(
                    label = stringResource(R.string.weather_detail_wind),
                    value = "${weather.windSpeed.toInt()} km/h",
                )
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HourlyForecastSection(forecasts: List<HourlyForecast>) {
    if (forecasts.isEmpty()) return
    Column {
        Text(
            stringResource(R.string.weather_hourly_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(forecasts, key = { it.time.toString() }) { hour ->
                HourCard(hour)
            }
        }
    }
}

@Composable
private fun HourCard(hour: HourlyForecast) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .width(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(hour.time.formatHm(), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(6.dp))
            Icon(
                imageVector = weatherIcon(hour.weatherCode),
                contentDescription = hour.description,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${hour.temperature.toInt()}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
