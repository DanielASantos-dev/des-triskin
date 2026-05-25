package br.com.triskin.domain.usecase

import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.repository.LocationProvider
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val provider: LocationProvider,
) {
    suspend operator fun invoke(): Result<LocationInfo> = provider.currentLocation()
}
