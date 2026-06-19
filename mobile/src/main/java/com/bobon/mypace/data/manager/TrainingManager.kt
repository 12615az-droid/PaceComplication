package com.bobon.mypace.data.manager

import android.location.Location
import com.bobon.mypace.WearDataSender
import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.modes.RunningMode
import com.bobon.mypace.modes.TrainingMode
import com.bobon.mypace.modes.TrainingModes
import com.bobon.mypace.pace.PaceCalculator
import com.bobon.mypace.pace.PaceUpdate
import com.bobon.mypace.timer.PaceTimer
import com.bobon.mypace.utils.PaceFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Менеджер активной тренировки. Хранит состояние в памяти.
 */
class TrainingManager(
    private val paceTimer: PaceTimer,
    private val paceCalculator: PaceCalculator,
    private val wearDataSender: WearDataSender
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

    private var oldLat = 0.0
    private var oldLon = 0.0

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
        syncWithWear()
    }

    fun pause() {
        _workoutState.value = WorkoutState.PAUSED
        paceTimer.stop()
        syncWithWear()
    }

    fun reset() {
        _workoutState.value = WorkoutState.IDLE
        _currentSessionId.value = null
        _startTime.value = null
        _totalDistance.value = 0.0
        oldLat = 0.0
        oldLon = 0.0
        _currentPace.value = "0:00"
        _currentGPSAccuracy.value = 0f
        paceTimer.reset()
        paceCalculator.reset()
        syncWithWear()
    }

    fun updatePace(location: Location): PaceUpdate? {
        if (_workoutState.value != WorkoutState.ACTIVE) return null

        _currentGPSAccuracy.value = location.accuracy
        val paceUpdate = paceCalculator.calculate(
            speedMetersPerSec = location.speed,
            accuracy = location.accuracy,
            maxSpeedMetersPerSec = _activityMode.value.maxSpeedMetersPerSec,
            alphaProvider = _activityMode.value::alphaForAccuracy
        ) ?: return null

        // Форматирование теперь происходит здесь (или во ViewModel), 
        // отделяя расчеты от представления.
        _currentPace.value = PaceFormatter.formatPace(paceUpdate.secondsPerKm)

        updateDistance(location.latitude, location.longitude)
        syncWithWear()
        return paceUpdate
    }

    private fun updateDistance(lat: Double, lon: Double) {
        if (oldLat != 0.0 && oldLon != 0.0) {
            val results = FloatArray(1)
            Location.distanceBetween(oldLat, oldLon, lat, lon, results)
            _totalDistance.value += results[0]
        }
        oldLat = lat
        oldLon = lon
    }

    fun changeMode() {
        if (_workoutState.value == WorkoutState.IDLE) {
            _activityMode.value = TrainingModes.next(_activityMode.value)
            paceCalculator.reset()
        }
    }

    fun syncWithWear() {
        wearDataSender.sendWorkoutUpdate(_currentPace.value, _workoutState.value.intState)
    }
}
