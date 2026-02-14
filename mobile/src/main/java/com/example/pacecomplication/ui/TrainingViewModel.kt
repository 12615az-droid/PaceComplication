package com.example.pacecomplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pacecomplication.LocationRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Koin сам подставит сюда твой готовый LocationRepository
class TrainingViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    // 1. Прокидываем данные из Репозитория (чтобы экран их видел)
    // Предполагаю, что у тебя в репозитории есть эти Flow.
    // Если они называются иначе — поправь названия тут.
    val currentPace = repository.currentPace
    val currentGPSAccuracy = repository.currentGPSAccuracy
    val trainingTimeMs = repository.trainingTimeMs
    val activityMode = repository.activityMode
    val workoutState = repository.workoutState
    val isGoalSetupOpen = repository.isGoalSetupOpen
    // val currentMode = repository.currentMode // раскомментируй, если есть

    val isTracking = repository.isTracking

    fun startTracking() = repository.startTracking()

    fun stopTracking() = repository.stopTracking()

    fun saveTracking() = repository.saveTracking()

    fun changeMode() = repository.changeMode()

    fun openGoalSetupDialog() = repository.setTrainingGoalDialogOpen(true)

    fun closeGoalSetupDialog() = repository.setTrainingGoalDialogOpen(false)

    fun onStopClick() = stopTracking()

    fun onSaveClick() = saveTracking()

    fun onModeChanged() = changeMode()

    fun onOpenGoalSetup() = openGoalSetupDialog()

    fun onCloseGoalSetup() = closeGoalSetupDialog()
}
