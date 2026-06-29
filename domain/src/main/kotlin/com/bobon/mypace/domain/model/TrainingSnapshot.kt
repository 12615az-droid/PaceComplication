package com.bobon.mypace.domain.model

import com.bobon.mypace.domain.training.modes.TrainingMode

data class TrainingSnapshot(
    val paceSecondsPerKm: Double?,
    val gpsAccuracyMeters: Float,
    val trainingTimeMs: Long,
    val totalDistanceMeters: Double,
    val activityMode: TrainingMode,
    val workoutState: WorkoutState
)