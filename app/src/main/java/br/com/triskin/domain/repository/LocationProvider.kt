package br.com.triskin.domain.repository

import br.com.triskin.domain.model.LocationInfo

interface LocationProvider {
    suspend fun currentLocation(): Result<LocationInfo>
}
