package com.example.pacecomplication.logger

import kotlinx.serialization.Serializable


@Serializable
data class PaceStreamTick(
    val tNs: Long,
    val dtMs: Long? = null,
    val trainingTimeMs: Long? = null,


    val gpsAccuracyM: Float? = null,
    val speedRawMps: Float? = null, // location.speed


    val paceRawSecPerKm: Float? = null,


    val paceFilteredSecPerKm: Float? = null,


    val alpha: Double? = null,
    val speedClampedMps: Float? = null,


    val isStale: Boolean? = null,
    val isRejected: Boolean? = null,
    val teleport: Boolean? = null,
    val accelLimited: Boolean? = null,

    val note: String? = null
)


class PaceLog {
}