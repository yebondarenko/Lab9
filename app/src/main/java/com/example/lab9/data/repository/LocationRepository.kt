package com.example.lab9.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.lab9.data.local.AppDatabase
import com.example.lab9.data.local.entity.LocationEntity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationRepository(
    private val context: Context
) {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private val dao = AppDatabase.getInstance(context).locationDao()

    fun observeCachedLocations(): Flow<List<LocationEntity>> = dao.observeAll()

    @Suppress("MissingPermission")
    suspend fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { location ->
                    cont.resume(location)
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

    @Suppress("MissingPermission")
    suspend fun requestCurrentLocation(): Location? {
        if (!hasLocationPermission()) return null

        return suspendCancellableCoroutine { cont ->
            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                0
            ).build()

            val callback = object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                    val location = result.lastLocation
                    fusedClient.removeLocationUpdates(this)
                    cont.resume(location)
                }
            }

            fusedClient.requestLocationUpdates(locationRequest, callback, android.os.Looper.getMainLooper())
                .addOnFailureListener {
                    fusedClient.removeLocationUpdates(callback)
                    cont.resume(null)
                }
        }
    }

    suspend fun saveLocationToDb(location: Location) {
        dao.insert(
            LocationEntity(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy,
                provider = location.provider,
                timestamp = location.time
            )
        )
    }

    suspend fun clearCache() = dao.clear()

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
}