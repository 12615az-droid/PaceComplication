package com.bobon.mypace.device.location

data class LocationUpdateResult(
    val paceSecondsPerKm: Double?,
    val accuracyMeters: Float
)