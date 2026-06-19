package com.bobon.mypace.device.location

import android.location.Location
import com.bobon.mypace.domain.model.GpsPoint

fun Location.toGpsPoint(): GpsPoint {
    return GpsPoint(
        latitude = latitude,
        longitude = longitude,
        speedMetersPerSecond = speed,
        accuracyMeters = accuracy,
        timestampMs = time
    )
}