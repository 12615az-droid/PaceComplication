package com.example.pacecomplication.presentation

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PaceRepository {
    private val _currentPace = MutableStateFlow("5:48")

    // А это публичная переменная, которую будут читать сервис и приложение
    val currentPace: StateFlow<String> = _currentPace

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()


    private val _workoutState = MutableStateFlow(0)
    val workoutState = _workoutState.asStateFlow()

    fun updateData(newPace: String, isTracking: Boolean, workoutState: Int) {
        _currentPace.value = newPace
        _isTracking.value = isTracking
        _workoutState.value = workoutState
        Log.d("WearData", newPace)
        Log.d("WearData", isTracking.toString())
        Log.d("WearData", workoutState.toString())
    }
}