package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.model.TrainingSnapshot
import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.domain.training.modes.TrainingMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTrainingStateUseCase(
    private val trainingManager: TrainingManager
) {
    operator fun invoke(): Flow<TrainingSnapshot> {
        val metrics = combine(
            trainingManager.currentPace,
            trainingManager.currentGPSAccuracy,
            trainingManager.trainingTimeMs,
            trainingManager.totalDistance
        ) { paceText, gpsAccuracy, trainingTimeMs, totalDistance ->
            TrainingMetrics(
                paceText = paceText,
                gpsAccuracyMeters = gpsAccuracy,
                trainingTimeMs = trainingTimeMs,
                totalDistanceMeters = totalDistance
            )
        }

        val modeState = combine(
            trainingManager.activityMode,
            trainingManager.workoutState
        ) { activityMode, workoutState ->
            TrainingModeState(
                activityMode = activityMode,
                workoutState = workoutState
            )
        }

        return combine(metrics, modeState) { metricsState, mode ->
            TrainingSnapshot(
                paceText = metricsState.paceText,
                gpsAccuracyMeters = metricsState.gpsAccuracyMeters,
                trainingTimeMs = metricsState.trainingTimeMs,
                totalDistanceMeters = metricsState.totalDistanceMeters,
                activityMode = mode.activityMode,
                workoutState = mode.workoutState
            )
        }
    }

    private data class TrainingMetrics(
        val paceText: String,
        val gpsAccuracyMeters: Float,
        val trainingTimeMs: Long,
        val totalDistanceMeters: Double
    )

    private data class TrainingModeState(
        val activityMode: TrainingMode,
        val workoutState: WorkoutState
    )
}