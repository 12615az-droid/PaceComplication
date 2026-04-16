package com.example.pacecomplication.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PaceRepository {
    private val _currentPace = MutableStateFlow("0:00")
    val currentPace: StateFlow<String> = _currentPace

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val _workoutState = MutableStateFlow(0)
    val workoutState = _workoutState.asStateFlow()

    fun updateData(context: Context, newPace: String, newIsTracking: Boolean, newWorkoutState: Int) {
        val oldIsTracking = _isTracking.value
        val oldWorkoutState = _workoutState.value

        _currentPace.value = newPace
        _isTracking.value = newIsTracking
        _workoutState.value = newWorkoutState

        Log.d("WearData", "Pace: $newPace, Tracking: $newIsTracking, State: $newWorkoutState")

        // Логика управления сервисом на основе двух параметров
        if (oldWorkoutState != newWorkoutState || oldIsTracking != newIsTracking) {
            val action = when {
                // Если состояние "Активно" (1), смотрим на флаг трекинга
                newWorkoutState == 1 -> {
                    if (newIsTracking) "START" else "STOP"
                }
                // Если состояние "IDLE/KILL" (0) или "FINISHED" (3)
                newWorkoutState == 0 || newWorkoutState == 3 -> "KILL"
                // Если пришла явная команда паузы (2)
                newWorkoutState == 2 -> "STOP"
                else -> null
            }

            action?.let {
                val intent = Intent(context, TrainingWearService::class.java).apply {
                    this.action = it
                }
                try {
                    if (it == "START") {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                    Log.d("WearData", "Sent action to service: $it")
                } catch (e: Exception) {
                    Log.e("WearData", "Error starting service with action $it", e)
                }
            }
        }
    }
}