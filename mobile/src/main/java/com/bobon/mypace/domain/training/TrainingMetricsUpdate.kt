package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.pace.PaceUpdate

data class TrainingMetricsUpdate(
    val paceUpdate: PaceUpdate,
    val gpsAccuracyMeters: Float,
    val distanceDeltaMeters: Double
)