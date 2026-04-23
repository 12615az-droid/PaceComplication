package com.example.pacecomplication.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager

class AndroidLocation(context: Context) : LocationWrapper, android.location.LocationListener {
    @SuppressLint("ServiceCast")
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var onLocation: ((Location) -> Unit)? = null

    @SuppressLint("MissingPermission")
    override fun startUpdates(callback: (Location) -> Unit) {
        onLocation = callback
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this)
    }

    override fun onLocationChanged(location: Location) {
        onLocation?.invoke(location)
    }

    override fun stopUpdates() {
        locationManager.removeUpdates(this)
    }
}