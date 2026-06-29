package com.bobon.mypace.core.logger

import kotlinx.serialization.Serializable

@Serializable
data class GpsRawData(
    val accuracyM: Float,
    val speedMps: Float,
    val lat: Double,
    val lon: Double,
    val provider: String? = null,
    val isMock: Boolean? = null
)