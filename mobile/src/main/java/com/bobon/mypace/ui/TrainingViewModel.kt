package com.bobon.mypace.ui

import android.app.Activity

import androidx.lifecycle.viewModelScope
import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.domain.model.Workout
import com.bobon.mypace.history.SessionIdGenerator
import com.bobon.mypace.logger.AppEventData

import com.bobon.mypace.logger.SourceEvent
import com.bobon.mypace.logger.TypeEvent


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.bobon.mypace.device.permission.PermissionManager
import com.bobon.mypace.domain.permission.PermissionResult

import androidx.lifecycle.ViewModel

import com.bobon.mypace.domain.repository.WorkoutRepository
import com.bobon.mypace.logger.EventsLog

import com.bobon.mypace.domain.service.TrainingServiceController
import com.bobon.mypace.domain.usecase.training.ChangeTrainingModeUseCase
import com.bobon.mypace.domain.usecase.training.FinishTrainingUseCase
import com.bobon.mypace.domain.usecase.training.PauseTrainingUseCase
import com.bobon.mypace.domain.usecase.training.StartTrainingUseCase
import com.bobon.mypace.domain.usecase.workout.DeleteWorkoutUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveTotalStatsUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveWorkoutStatsUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveWorkoutsByTypeUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveWorkoutsUseCase
import com.bobon.mypace.domain.usecase.workout.SaveWorkoutUseCase
import com.bobon.mypace.domain.model.TotalStats

class TrainingViewModel(
    private val trainingManager: TrainingManager,
    private val trainingServiceController: TrainingServiceController,
    private val eventsLog: EventsLog,
    private val permissionManager: PermissionManager,
    private val startTraining: StartTrainingUseCase,
    private val pauseTraining: PauseTrainingUseCase,
    private val finishTraining: FinishTrainingUseCase,
    private val changeTrainingMode: ChangeTrainingModeUseCase,
    private val observeWorkouts: ObserveWorkoutsUseCase,
    private val observeWorkoutsByType: ObserveWorkoutsByTypeUseCase,
    private val observeWorkoutStats: ObserveWorkoutStatsUseCase,
    private val saveWorkout: SaveWorkoutUseCase,
    private val observeTotalStats: ObserveTotalStatsUseCase,
    private val deleteWorkout: DeleteWorkoutUseCase
) : ViewModel(){

    val currentPace = trainingManager.currentPace
    val currentGPSAccuracy = trainingManager.currentGPSAccuracy
    val trainingTimeMs = trainingManager.trainingTimeMs
    val activityMode = trainingManager.activityMode
    val workoutState = trainingManager.workoutState
    val totalDistance = trainingManager.totalDistance

    private val _isGoalSetupOpen = MutableStateFlow(false)
    val isGoalSetupOpen = _isGoalSetupOpen.asStateFlow()

    private val _selectedFilter = MutableStateFlow(0)
    val selectedFilter: StateFlow<Int> = _selectedFilter.asStateFlow()

    // === Flow для разрешений (UI подписывается и показывает диалоги) ===
    private val _permissionEvent = MutableSharedFlow<PermissionResult>()
    val permissionEvent = _permissionEvent.asSharedFlow()

    // Состояние для показа диалогов из ViewModel
    private val _showRationaleDialog = MutableStateFlow(false)
    val showRationaleDialog = _showRationaleDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog = _showSettingsDialog.asStateFlow()

    private val _showLocationServicesDialog = MutableStateFlow(false)
    val showLocationServicesDialog = _showLocationServicesDialog.asStateFlow()

    private val _showRetryDialog = MutableStateFlow(false)
    val showRetryDialog = _showRetryDialog.asStateFlow()


    // === Снэкбар сообщения (Toast замена) ===
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()
    private val _launchPermissionRequest = MutableStateFlow(false)
    val launchPermissionRequest = _launchPermissionRequest.asStateFlow()
    private val _showPreciseLocationDialog = MutableStateFlow(false)
    val showPreciseLocationDialog = _showPreciseLocationDialog.asStateFlow()

    fun onDismissPreciseLocationDialog() {
        _showPreciseLocationDialog.value = false
    }

    fun onOpenAppSettingsForPrecise() {
        _showPreciseLocationDialog.value = false
        permissionManager.openAppSettings()
    }
    fun getPreciseLocationRequiredText(): String = permissionManager.getPreciseLocationRequiredText()
    // === Флаг что тренировка не может стартовать ===
    private val _canStartWorkout = MutableStateFlow(false)
    val canStartWorkout = _canStartWorkout.asStateFlow()
    fun onStartClick(activity: Activity) {
        when {
            // 1. Нет вообще никакой локации
            !permissionManager.hasAnyLocation() -> {
                handlePermissionsCheck(activity)
            }

            // 2. Есть только приблизительная, точной нет
            permissionManager.hasOnlyCoarseLocation() -> {
                _showPreciseLocationDialog.value = true  // ← новый диалог
            }

            // 3. Точная локация есть, но GPS выключен в системе
            !permissionManager.isLocationEnabled() -> {
                _showLocationServicesDialog.value = true
            }

            // 4. Всё ок
            else -> {
                startTracking()
            }
        }
    }

    /** Проверяет только критичные разрешения (локация) */
    fun hasCriticalPermissions(): Boolean = permissionManager.hasFineLocation()

    fun getLocationDisabledText(): String = permissionManager.getLocationDisabledText()
    fun getPermissionsBlockedText(): String = permissionManager.getPermissionsBlockedText()

    fun onPermissionRequestLaunched() {
        _launchPermissionRequest.value = false
    }

    private fun handlePermissionsCheck(activity: Activity) {
        when {
            permissionManager.shouldGoToSettings() -> {
                _showSettingsDialog.value = true
            }
            permissionManager.shouldExplainBeforeRequest(activity) -> {
                _showRationaleDialog.value = true
            }
            else -> {
                _launchPermissionRequest.value = true // ← UI увидит и запустит launcher
            }
        }
    }

    /**
     * Обработка результата запроса разрешений.
     */
    fun onPermissionResult(
        activity: Activity,
        permissions: Map<String, Boolean>
    ) {
        when {
            // ✅ Все даны
            permissionManager.hasAllPermissions() -> {
                checkLocationServices()
            }

            // ❌ Можно показать rationale ещё раз
            permissionManager.shouldShowFineLocationRationale(activity) -> {
                permissionManager.incrementDenyCount()
                _showRetryDialog.value = true
            }

            // ❌ "Больше не спрашивать"
            else -> {
                permissionManager.incrementDenyCount()
                _showSettingsDialog.value = true
            }
        }
    }

    // === ДЕЙСТВИЯ ПОЛЬЗОВАТЕЛЯ ===

    fun onRationaleConfirm() {
        _showRationaleDialog.value = false
        permissionManager.markRationaleShown()
    }

    fun onOpenSettings() {
        _showSettingsDialog.value = false
        permissionManager.openAppSettings()
    }

    fun onOpenLocationSettings() {
        _showLocationServicesDialog.value = false
        permissionManager.openLocationSettings()
    }

    fun onRetryRequest() {
        _showRetryDialog.value = false
    }

    /** Пользователь закрыл диалог GPS — просто скрываем, НЕ выходим из приложения */
    fun onDismissLocationDialog() {
        _showLocationServicesDialog.value = false
    }

    /** Пользователь закрыл диалог настроек — просто скрываем */
    fun onDismissSettingsDialog() {
        _showSettingsDialog.value = false
    }

    /** Пользователь закрыл retry диалог — просто скрываем */
    fun onDismissRetryDialog() {
        _showRetryDialog.value = false
    }

    /** Пользователь закрыл rationale — просто скрываем */
    fun onDismissRationaleDialog() {
        _showRationaleDialog.value = false
    }

    /** Сброс всех диалогов (onResume) */
    fun dismissDialogs() {
        _showRationaleDialog.value = false
        _showSettingsDialog.value = false
        _showLocationServicesDialog.value = false
        _showRetryDialog.value = false
        _showPreciseLocationDialog.value = false  // ← добавить
    }

    // === ПРОКСИ-МЕТОДЫ ===

    fun getRequiredPermissions(): Array<String> = permissionManager.getRequiredPermissions()
    fun getPermissionRationaleText(): String = permissionManager.getPermissionRationaleText()





    private fun checkLocationServices() {
        if (!permissionManager.isLocationEnabled()) {
            _showLocationServicesDialog.value = true
        } else {
            startTracking()
        }
    }



    fun selectFilter(index: Int) {
        _selectedFilter.value = index
    }



    fun startTracking() {

        viewModelScope.launch {
            val sessionId = SessionIdGenerator.newId()
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
            val sessionId = trainingManager.currentSessionId.value ?: return@launch
            val start = trainingManager.startTime.value ?: return@launch
            val end = System.currentTimeMillis()
            val distance = trainingManager.totalDistance.value
            val timeMs = trainingManager.trainingTimeMs.value
            val mode = trainingManager.activityMode.value

            val avgSpeed = if (timeMs > 0) (distance / (timeMs / 3600000.0)) else 0.0

            val workout = Workout(
                id = sessionId,
                startTime = start,
                endTime = end,
                totalDistance = distance,
                avgSpeed = avgSpeed,
                caloriesBurned = 0,
                activityType = mode.id, // Используем ID напрямую из режима
                note = null
            )

            saveWorkout(workout)


            finishTraining()
        }
    }



    fun changeMode() {
        changeTrainingMode()
    }

    fun openGoalSetupDialog() {
        _isGoalSetupOpen.value = true
    }

    fun closeGoalSetupDialog() {
        _isGoalSetupOpen.value = false
    }

    fun logScreenChanged(screenName: String) {
        viewModelScope.launch {
            eventsLog.log(
                type = TypeEvent.SCREEN_CHANGED,
                source = SourceEvent.UI,
                origin = "UI.Navigation",
                sessionId = null,
                data = AppEventData(
                    screen = screenName,
                    workoutState = trainingManager.workoutState.value,
                    note = "Screen changed"
                )
            )
        }
    }
}
