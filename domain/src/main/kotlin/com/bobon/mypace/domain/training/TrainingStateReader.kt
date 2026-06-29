package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.modes.TrainingMode
import kotlinx.coroutines.flow.StateFlow

interface TrainingStateReader {
    val currentPaceSecondsPerKm: StateFlow<Double?>
    val workoutState: StateFlow<WorkoutState>
    val activityMode: StateFlow<TrainingMode>
    val totalDistance: StateFlow<Double>
    val currentGPSAccuracy: StateFlow<Float>
    val currentSessionId: StateFlow<String?>
    val startTime: StateFlow<Long?>
    val trainingTimeMs: StateFlow<Long>
}