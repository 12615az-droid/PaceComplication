package com.bobon.mypace.core.sensor

data class SensorData(
    val type: Int,
    val typeName: String,
    val values: FloatArray,
    val timestamp: Long
)