package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.modes.RunningMode
import com.bobon.mypace.domain.training.modes.TrainingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrainingStateHolder {

    private val _currentPaceSecondsPerKm = MutableStateFlow<Double?>(null)
    val currentPaceSecondsPerKm = _currentPaceSecondsPerKm.asStateFlow()

    private val _workoutState = MutableStateFlow(WorkoutState.IDLE)
    val workoutState = _workoutState.asStateFlow()

    private val _activityMode = MutableStateFlow<TrainingMode>(RunningMode)
    val activityMode = _activityMode.asStateFlow()

    private val _totalDistance = MutableStateFlow(0.0)
    val totalDistance = _totalDistance.asStateFlow()

    private val _currentGPSAccuracy = MutableStateFlow(0f)
    val currentGPSAccuracy = _currentGPSAccuracy.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId = _currentSessionId.asStateFlow()

    private val _startTime = MutableStateFlow<Long?>(null)
    val startTime = _startTime.asStateFlow()

    fun setCurrentPaceSecondsPerKm(value: Double) {
        _currentPaceSecondsPerKm.value = value
    }

    fun setWorkoutState(value: WorkoutState) {
        _workoutState.value = value
    }

    fun setActivityMode(value: TrainingMode) {
        _activityMode.value = value
    }

    fun addDistance(deltaMeters: Double) {
        _totalDistance.value += deltaMeters
    }

    fun setGpsAccuracy(value: Float) {
        _currentGPSAccuracy.value = value
    }

    fun setSessionIdIfEmpty(sessionId: String) {
        if (_currentSessionId.value == null) {
            _currentSessionId.value = sessionId
        }
    }

    fun setStartTimeIfEmpty(startTime: Long) {
        if (_startTime.value == null) {
            _startTime.value = startTime
        }
    }

    fun reset() {
        _currentPaceSecondsPerKm.value = null
        _workoutState.value = WorkoutState.IDLE
        _activityMode.value = RunningMode
        _totalDistance.value = 0.0
        _currentGPSAccuracy.value = 0f
        _currentSessionId.value = null
        _startTime.value = null
    }
}