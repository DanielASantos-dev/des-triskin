package br.com.triskin.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import br.com.triskin.domain.model.LocationInfo
import br.com.triskin.domain.repository.LocationProvider
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class FusedLocationProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient,
) : LocationProvider {

    override suspend fun currentLocation(): Result<LocationInfo> = runCatching {
        check(hasLocationPermission()) { "Permissão de localização não concedida" }
        val location = fetchLocation() ?: error("Não foi possível obter a localização")
        val name = reverseGeocode(location) ?: "Localização atual"
        LocationInfo(
            name = name,
            latitude = location.latitude,
            longitude = location.longitude,
            country = "",
            adminArea = null,
        )
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private suspend fun fetchLocation(): Location? = suspendCancellableCoroutine { cont ->
        val cts = CancellationTokenSource()
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_COARSE)
            .setDurationMillis(LOCATION_TIMEOUT_MS)
            .build()

        client.getCurrentLocation(request, cts.token)
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }

        cont.invokeOnCancellation { cts.cancel() }
    }

    private suspend fun reverseGeocode(location: Location): String? = withContext(Dispatchers.IO) {
        if (!Geocoder.isPresent()) return@withContext null
        runCatching {
            @Suppress("DEPRECATION")
            val matches = Geocoder(context, Locale("pt", "BR"))
                .getFromLocation(location.latitude, location.longitude, 1)
            matches?.firstOrNull()?.let { address ->
                address.subAdminArea ?: address.locality ?: address.adminArea
            }
        }.recoverCatching { e ->
            if (e is IOException) null else throw e
        }.getOrNull()
    }

    private companion object {
        const val LOCATION_TIMEOUT_MS = 15_000L
    }
}
