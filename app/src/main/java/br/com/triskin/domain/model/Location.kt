package br.com.triskin.domain.model

data class LocationInfo(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val adminArea: String? = null,
)
