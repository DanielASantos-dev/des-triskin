package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.repository.WeatherRepository
import javax.inject.Inject

class SearchLocationUseCase @Inject constructor(
    private val repository: WeatherRepository,
) {
    suspend operator fun invoke(query: String): Result<List<LocationInfo>> {
        if (query.isBlank()) return Result.success(emptyList())
        return repository.searchLocation(query)
    }
}
