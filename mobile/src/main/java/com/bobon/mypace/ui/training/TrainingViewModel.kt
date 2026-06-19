package com.bobon.mypace.ui.training


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.bobon.mypace.domain.training.TrainingManager

import com.bobon.mypace.domain.usecase.training.PauseTrainingUseCase
import com.bobon.mypace.domain.usecase.training.SaveCurrentTrainingUseCase
import com.bobon.mypace.domain.usecase.training.StartTrainingUseCase


import kotlinx.coroutines.launch
import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.usecase.logging.LogScreenChangedUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

private data class TrainingMetricsUi(
    val paceText: String,
    val gpsAccuracyMeters: Float,
    val trainingTimeMs: Long,
    val totalDistanceMeters: Double
)

private data class TrainingModeUi(
    val activityModeLabel: String,
    val isPaused: Boolean,
    val isActive: Boolean
)

class TrainingViewModel(
    private val trainingManager: TrainingManager,
    private val startTraining: StartTrainingUseCase,
    private val pauseTraining: PauseTrainingUseCase,
    private val saveCurrentTraining: SaveCurrentTrainingUseCase,
    private val logScreenChangedUseCase: LogScreenChangedUseCase
) : ViewModel(){


    private val trainingMetrics = combine(
        trainingManager.currentPace,
        trainingManager.currentGPSAccuracy,
        trainingManager.trainingTimeMs,
        trainingManager.totalDistance
    ) { pace, gpsAccuracy, timeMs, distance ->
        TrainingMetricsUi(
            paceText = pace,
            gpsAccuracyMeters = gpsAccuracy,
            trainingTimeMs = timeMs,
            totalDistanceMeters = distance
        )
    }

    private val trainingModeState = combine(
        trainingManager.activityMode,
        trainingManager.workoutState
    ) { mode, state ->
        TrainingModeUi(
            activityModeLabel = mode.label,
            isPaused = state == WorkoutState.PAUSED,
            isActive = state == WorkoutState.ACTIVE
        )
    }

    val uiState = combine(
        trainingMetrics,
        trainingModeState
    ) { metrics, modeState ->
        TrainingUiState(
            paceText = metrics.paceText,
            gpsAccuracyMeters = metrics.gpsAccuracyMeters,
            trainingTimeMs = metrics.trainingTimeMs,
            activityModeLabel = modeState.activityModeLabel,
            totalDistanceMeters = metrics.totalDistanceMeters,
            isPaused = modeState.isPaused,
            isActive = modeState.isActive
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TrainingUiState()
    )

    fun continueTracking() {
        viewModelScope.launch {
            val sessionId = trainingManager.currentSessionId.value ?: return@launch
            startTraining(sessionId)
        }
    }

    fun stopTracking() {
        viewModelScope.launch {
            pauseTraining()
        }
    }

    fun saveTracking() {
        viewModelScope.launch {
            saveCurrentTraining()
        }
    }


}