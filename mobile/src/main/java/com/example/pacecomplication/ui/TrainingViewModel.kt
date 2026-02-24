package com.example.pacecomplication.ui

import androidx.lifecycle.ViewModel
import com.example.pacecomplication.LocationRepository

// Koin сам подставит сюда твой готовый LocationRepository
class TrainingViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    val currentPace = repository.currentPace
    val currentGPSAccuracy = repository.currentGPSAccuracy
    val trainingTimeMs = repository.trainingTimeMs
    val activityMode = repository.activityMode
    val workoutState = repository.workoutState
    val isGoalSetupOpen = repository.isGoalSetupOpen


    val isTracking = repository.isTracking

    fun startTracking() = repository.startTracking()

    fun stopTracking() = repository.stopTracking()

    fun saveTracking() = repository.saveTracking()

    fun changeMode() = repository.changeMode()

    fun openGoalSetupDialog() = repository.setTrainingGoalDialogOpen(true)

    fun closeGoalSetupDialog() = repository.setTrainingGoalDialogOpen(false)


    fun onModeChanged() = changeMode()

    fun onOpenGoalSetup() = openGoalSetupDialog()

    fun onCloseGoalSetup() = closeGoalSetupDialog()
}
