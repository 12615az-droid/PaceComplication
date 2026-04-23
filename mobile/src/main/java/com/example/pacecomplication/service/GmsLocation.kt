package com.example.pacecomplication.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class GmsLocation(context: Context) : LocationWrapper {
    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)
    private var internalCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun startUpdates(callback: (Location) -> Unit) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500).build()

        internalCallback = object : LocationCallback() {
            override fun onLocationResult(res: LocationResult) {
                res.locations.forEach { callback(it) }
            }
        }
        fusedClient.requestLocationUpdates(request, internalCallback!!, Looper.getMainLooper())
    }

    override fun stopUpdates() {
        internalCallback?.let { fusedClient.removeLocationUpdates(it) }
    }
}