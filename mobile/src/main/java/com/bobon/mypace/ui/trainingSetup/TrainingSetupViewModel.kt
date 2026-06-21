package com.bobon.mypace.ui.trainingSetup


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobon.mypace.domain.usecase.training.ObserveActivityModeUseCase
import com.bobon.mypace.domain.usecase.training.ChangeTrainingModeUseCase
import com.bobon.mypace.domain.usecase.training.CheckStartWorkoutAvailabilityUseCase
import com.bobon.mypace.domain.usecase.training.StartTrainingUseCase
import com.bobon.mypace.domain.usecase.training.StartWorkoutAvailability
import com.bobon.mypace.domain.session.SessionIdGenerator
import com.bobon.mypace.domain.usecase.permission.GetRequiredPermissionsUseCase
import com.bobon.mypace.domain.usecase.permission.HandlePermissionResultUseCase
import com.bobon.mypace.domain.usecase.permission.IsLocationEnabledUseCase
import com.bobon.mypace.domain.usecase.permission.MarkPermissionRationaleShownUseCase
import com.bobon.mypace.domain.usecase.permission.PermissionRequestResult
import com.bobon.mypace.domain.usecase.permission.ShouldGoToSettingsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrainingSetupViewModel(
    observeActivityMode: ObserveActivityModeUseCase,
    private val changeTrainingMode: ChangeTrainingModeUseCase,
    private val handlePermissionResult: HandlePermissionResultUseCase,
    private val getRequiredPermissionsUseCase: GetRequiredPermissionsUseCase,
    private val markPermissionRationaleShown: MarkPermissionRationaleShownUseCase,
    private val checkStartWorkoutAvailability: CheckStartWorkoutAvailabilityUseCase,
    private val startTraining: StartTrainingUseCase,
    private val shouldGoToSettings: ShouldGoToSettingsUseCase,
    private val isLocationEnabled: IsLocationEnabledUseCase,
) : ViewModel() {




    val activityMode = observeActivityMode()

    private val _activeDialog = MutableStateFlow<TrainingSetupDialog?>(null)
    val activeDialog = _activeDialog.asStateFlow()



    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()


    private val _effect = MutableSharedFlow<TrainingSetupEffect>()
    val effect = _effect.asSharedFlow()
    fun changeMode() {
        changeTrainingMode()
    }

    fun openGoalSetupDialog() {
        _activeDialog.value = TrainingSetupDialog.GoalSetup
    }


    fun dismissDialog() {
        _activeDialog.value = null
    }

    fun onStartClick(shouldShowRationale: Boolean) {
        when (checkStartWorkoutAvailability()) {
            StartWorkoutAvailability.NoLocationPermission -> {
                handlePermissionsCheck(shouldShowRationale)
            }

            StartWorkoutAvailability.OnlyCoarseLocation -> {
                _activeDialog.value = TrainingSetupDialog.PreciseLocationRequired
            }

            StartWorkoutAvailability.LocationDisabled -> {
                _activeDialog.value = TrainingSetupDialog.LocationDisabled
            }

            StartWorkoutAvailability.Available -> {
                startTracking()
            }
        }
    }

    private fun startTracking() {
        viewModelScope.launch {
            val sessionId = SessionIdGenerator.newId()
            startTraining(sessionId)
        }
    }

    private fun handlePermissionsCheck(shouldShowRationale: Boolean) {
        when {
            shouldGoToSettings() -> {
                _activeDialog.value = TrainingSetupDialog.PermissionSettings
            }

            shouldShowRationale -> {
                _activeDialog.value = TrainingSetupDialog.PermissionRationale
            }

            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun requestLocationPermission() {
        viewModelScope.launch {
            _effect.emit(TrainingSetupEffect.RequestLocationPermission)
        }
    }

    fun onPermissionResult(
        shouldShowFineLocationRationale: Boolean
    ) {
        when (handlePermissionResult(shouldShowFineLocationRationale)) {
            PermissionRequestResult.Granted -> {
                checkLocationServices()
            }

            PermissionRequestResult.ShowRetryDialog -> {
                _activeDialog.value = TrainingSetupDialog.PermissionRetry
            }

            PermissionRequestResult.ShowSettingsDialog -> {
                _activeDialog.value = TrainingSetupDialog.PermissionSettings
            }
        }
    }

    private fun checkLocationServices() {
        if (!isLocationEnabled()) {
            _activeDialog.value = TrainingSetupDialog.LocationDisabled
        } else {
            startTracking()
        }
    }

    fun getRequiredPermissions(): Array<String> =
        getRequiredPermissionsUseCase()




    fun onRationaleConfirm() {
        _activeDialog.value = null
        markPermissionRationaleShown()
        requestLocationPermission()
    }

    fun onOpenSettings() {
        _activeDialog.value = null

        viewModelScope.launch {
            _effect.emit(TrainingSetupEffect.OpenAppSettings)
        }
    }

    fun onOpenLocationSettings() {
        _activeDialog.value = null

        viewModelScope.launch {
            _effect.emit(TrainingSetupEffect.OpenLocationSettings)
        }
    }

    fun onOpenAppSettingsForPrecise() {
        _activeDialog.value = null

        viewModelScope.launch {
            _effect.emit(TrainingSetupEffect.OpenAppSettings)
        }
    }



    fun onDismissPreciseLocationDialog() {
        _activeDialog.value = null
    }


}