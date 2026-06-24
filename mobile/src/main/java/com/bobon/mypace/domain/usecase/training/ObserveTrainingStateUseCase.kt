package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.model.TrainingSnapshot
import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.TrainingStateReader
import com.bobon.mypace.domain.training.modes.TrainingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTrainingStateUseCase(
    private val trainingStateReader: TrainingStateReader
) {
    operator fun invoke(): Flow<TrainingSnapshot> {
        val metrics = combine(
            trainingStateReader.currentPaceSecondsPerKm,
            trainingStateReader.currentGPSAccuracy,
            trainingStateReader.trainingTimeMs,
            trainingStateReader.totalDistance
        ) { paceSecondsPerKm, gpsAccuracy, trainingTimeMs, totalDistance ->
            TrainingMetrics(
                paceSecondsPerKm = paceSecondsPerKm,
                gpsAccuracyMeters = gpsAccuracy,
                trainingTimeMs = trainingTimeMs,
                totalDistanceMeters = totalDistance
            )
        }

        val modeState = combine(
            trainingStateReader.activityMode,
            trainingStateReader.workoutState
        ) { activityMode, workoutState ->
            TrainingModeState(
                activityMode = activityMode,
                workoutState = workoutState
            )
        }

        return combine(metrics, modeState) { metricsState, mode ->
            TrainingSnapshot(
                paceSecondsPerKm = metricsState.paceSecondsPerKm,
                gpsAccuracyMeters = metricsState.gpsAccuracyMeters,
                trainingTimeMs = metricsState.trainingTimeMs,
                totalDistanceMeters = metricsState.totalDistanceMeters,
                activityMode = mode.activityMode,
                workoutState = mode.workoutState
            )
        }
    }

    private data class TrainingMetrics(
        val paceSecondsPerKm: Double?,
        val gpsAccuracyMeters: Float,
        val trainingTimeMs: Long,
        val totalDistanceMeters: Double
    )

    private data class TrainingModeState(
        val activityMode: TrainingMode,
        val workoutState: WorkoutState
    )
}
