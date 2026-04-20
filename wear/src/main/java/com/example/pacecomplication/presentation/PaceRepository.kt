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


    private val _workoutState = MutableStateFlow(0)
    val workoutState = _workoutState.asStateFlow()

    fun updateData(context: Context, newPace: String, newWorkoutState: Int) {
        val oldWorkoutState = _workoutState.value

        _currentPace.value = newPace
        _workoutState.value = newWorkoutState

        Log.d("WearData", "Pace: $newPace, State: $newWorkoutState")

        // Логика управления сервисом на основе двух параметров
        if (oldWorkoutState != newWorkoutState) {
            val action = when {
                // Если состояние "Активно" (1), смотрим на флаг трекинга
                newWorkoutState == 1 -> "START"
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