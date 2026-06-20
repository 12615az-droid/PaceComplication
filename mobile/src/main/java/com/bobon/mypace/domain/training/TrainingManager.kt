package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.modes.RunningMode
import com.bobon.mypace.domain.training.modes.TrainingMode
import com.bobon.mypace.domain.training.modes.TrainingModes
import com.bobon.mypace.domain.pace.PaceCalculator
import com.bobon.mypace.domain.pace.PaceUpdate
import com.bobon.mypace.domain.timer.PaceTimer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.bobon.mypace.domain.distance.DistanceCalculator
import com.bobon.mypace.domain.model.GpsPoint
import com.bobon.mypace.core.formatter.PaceFormatter

/**
 * Менеджер активной тренировки. Хранит состояние в памяти.
 */
class TrainingManager(
    private val paceTimer: PaceTimer,
    private val paceCalculator: PaceCalculator,
    private val trainingSyncSender: TrainingSyncSender,
    private val distanceCalculator: DistanceCalculator
) {
    private val _currentPace = MutableStateFlow("0:00")
    val currentPace = _currentPace.asStateFlow()

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

    private var lastGpsPoint: GpsPoint? = null

    val trainingTimeMs = paceTimer.trainingTimeMs

    fun start(sessionId: String) {
        if (_currentSessionId.value == null) {
            _currentSessionId.value = sessionId
        }
        if (_startTime.value == null) {
            _startTime.value = System.currentTimeMillis()
        }
        _workoutState.value = WorkoutState.ACTIVE
        paceTimer.start()
        syncTrainingState()
    }

    fun pause() {
        _workoutState.value = WorkoutState.PAUSED
        paceTimer.stop()
        syncTrainingState()
    }

    fun reset() {
        _workoutState.value = WorkoutState.IDLE
        _currentSessionId.value = null
        _startTime.value = null
        _totalDistance.value = 0.0
        lastGpsPoint=null
        _currentPace.value = "0:00"
        _currentGPSAccuracy.value = 0f
        paceTimer.reset()
        paceCalculator.reset()
        syncTrainingState()
    }

    fun updatePace(point: GpsPoint): PaceUpdate? {
        _currentGPSAccuracy.value = point.accuracyMeters

        val paceUpdate = paceCalculator.calculate(
            speedMetersPerSec = point.speedMetersPerSecond,
            accuracy = point.accuracyMeters,
            maxSpeedMetersPerSec = _activityMode.value.maxSpeedMetersPerSec,
            alphaProvider = _activityMode.value::alphaForAccuracy
        ) ?: return null

        // ОБНОВЛЕНИЕ: записываем отформатированный темп в StateFlow для UI
        _currentPace.value = PaceFormatter.formatPace(paceUpdate.secondsPerKm)

        updateDistance(point)
        syncTrainingState()
        return paceUpdate
    }

    private fun updateDistance(point: GpsPoint) {
        val previousPoint = lastGpsPoint

        if (previousPoint != null) {
            val deltaMeters = distanceCalculator.distanceBetween(
                from = previousPoint,
                to = point
            )

            _totalDistance.value += deltaMeters
        }

        lastGpsPoint = point
    }

    fun changeMode() {
        if (_workoutState.value == WorkoutState.IDLE) {
            _activityMode.value = TrainingModes.next(_activityMode.value)
            paceCalculator.reset()
        }
    }

    private fun syncTrainingState() {
        trainingSyncSender.sendWorkoutUpdate(
            paceText = _currentPace.value,
            workoutState = _workoutState.value
        )
    }
}