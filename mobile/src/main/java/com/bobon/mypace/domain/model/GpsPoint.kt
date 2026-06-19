package com.bobon.mypace.domain.model

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val speedMetersPerSecond: Float,
    val accuracyMeters: Float,
    val timestampMs: Long
)