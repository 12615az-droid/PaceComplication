package com.bobon.mypace.ui.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobon.mypace.core.formatter.PaceFormatter
import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.usecase.training.ObserveTrainingStateUseCase
import com.bobon.mypace.domain.usecase.training.ContinueTrainingUseCase
import com.bobon.mypace.domain.usecase.training.PauseTrainingUseCase
import com.bobon.mypace.domain.usecase.training.SaveCurrentTrainingUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrainingViewModel(
    observeTrainingState: ObserveTrainingStateUseCase,
    private val continueTraining: ContinueTrainingUseCase,
    private val pauseTraining: PauseTrainingUseCase,
    private val saveCurrentTraining: SaveCurrentTrainingUseCase,
) : ViewModel() {

    val uiState = observeTrainingState()
        .map { state ->
            TrainingUiState(
                paceText = PaceFormatter.formatPace(state.paceSecondsPerKm ?: 0.0),
                gpsAccuracyMeters = state.gpsAccuracyMeters,
                trainingTimeMs = state.trainingTimeMs,
                activityModeLabel = state.activityMode.label,
                totalDistanceMeters = state.totalDistanceMeters,
                isPaused = state.workoutState == WorkoutState.PAUSED,
                isActive = state.workoutState == WorkoutState.ACTIVE
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrainingUiState()
        )

    fun continueTracking() {
        viewModelScope.launch {
            continueTraining()
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