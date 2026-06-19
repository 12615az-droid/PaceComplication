package com.bobon.mypace.ui.trainingSetup

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobon.mypace.device.permission.PermissionManager
import com.bobon.mypace.domain.permission.PermissionResult
import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.domain.usecase.training.ChangeTrainingModeUseCase
import com.bobon.mypace.domain.usecase.training.CheckStartWorkoutAvailabilityUseCase
import com.bobon.mypace.domain.usecase.training.StartTrainingUseCase
import com.bobon.mypace.domain.usecase.training.StartWorkoutAvailability
import com.bobon.mypace.domain.session.SessionIdGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TrainingSetupViewModel(
    private val trainingManager: TrainingManager,
    private val changeTrainingMode: ChangeTrainingModeUseCase,
    private val permissionManager: PermissionManager,
    private val checkStartWorkoutAvailability: CheckStartWorkoutAvailabilityUseCase,
    private val startTraining: StartTrainingUseCase
) : ViewModel() {

    val activityMode = trainingManager.activityMode

    private val _isGoalSetupOpen = MutableStateFlow(false)
    val isGoalSetupOpen = _isGoalSetupOpen.asStateFlow()

    private val _permissionEvent = MutableSharedFlow<PermissionResult>()
    val permissionEvent = _permissionEvent.asSharedFlow()

    private val _showRationaleDialog = MutableStateFlow(false)
    val showRationaleDialog = _showRationaleDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog = _showSettingsDialog.asStateFlow()

    private val _showLocationServicesDialog = MutableStateFlow(false)
    val showLocationServicesDialog = _showLocationServicesDialog.asStateFlow()

    private val _showRetryDialog = MutableStateFlow(false)
    val showRetryDialog = _showRetryDialog.asStateFlow()

    private val _showPreciseLocationDialog = MutableStateFlow(false)
    val showPreciseLocationDialog = _showPreciseLocationDialog.asStateFlow()

    private val _launchPermissionRequest = MutableStateFlow(false)
    val launchPermissionRequest = _launchPermissionRequest.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    fun changeMode() {
        changeTrainingMode()
    }

    fun openGoalSetupDialog() {
        _isGoalSetupOpen.value = true
    }

    fun closeGoalSetupDialog() {
        _isGoalSetupOpen.value = false
    }

    fun onStartClick(activity: Activity) {
        when (checkStartWorkoutAvailability()) {
            StartWorkoutAvailability.NoLocationPermission -> {
                handlePermissionsCheck(activity)
            }

            StartWorkoutAvailability.OnlyCoarseLocation -> {
                _showPreciseLocationDialog.value = true
            }

            StartWorkoutAvailability.LocationDisabled -> {
                _showLocationServicesDialog.value = true
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

    private fun handlePermissionsCheck(activity: Activity) {
        when {
            permissionManager.shouldGoToSettings() -> {
                _showSettingsDialog.value = true
            }

            permissionManager.shouldExplainBeforeRequest(activity) -> {
                _showRationaleDialog.value = true
            }

            else -> {
                _launchPermissionRequest.value = true
            }
        }
    }

    fun onPermissionResult(
        activity: Activity,
        permissions: Map<String, Boolean>
    ) {
        when {
            permissionManager.hasAllPermissions() -> {
                checkLocationServices()
            }

            permissionManager.shouldShowFineLocationRationale(activity) -> {
                permissionManager.incrementDenyCount()
                _showRetryDialog.value = true
            }

            else -> {
                permissionManager.incrementDenyCount()
                _showSettingsDialog.value = true
            }
        }
    }

    private fun checkLocationServices() {
        if (!permissionManager.isLocationEnabled()) {
            _showLocationServicesDialog.value = true
        } else {
            startTracking()
        }
    }

    fun getRequiredPermissions(): Array<String> =
        permissionManager.getRequiredPermissions()

    fun getPermissionRationaleText(): String =
        permissionManager.getPermissionRationaleText()

    fun getLocationDisabledText(): String =
        permissionManager.getLocationDisabledText()

    fun getPermissionsBlockedText(): String =
        permissionManager.getPermissionsBlockedText()

    fun getPreciseLocationRequiredText(): String =
        permissionManager.getPreciseLocationRequiredText()

    fun onPermissionRequestLaunched() {
        _launchPermissionRequest.value = false
    }

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

    fun onOpenAppSettingsForPrecise() {
        _showPreciseLocationDialog.value = false
        permissionManager.openAppSettings()
    }

    fun onRetryRequest() {
        _showRetryDialog.value = false
    }

    fun onDismissRationaleDialog() {
        _showRationaleDialog.value = false
    }

    fun onDismissSettingsDialog() {
        _showSettingsDialog.value = false
    }

    fun onDismissLocationDialog() {
        _showLocationServicesDialog.value = false
    }

    fun onDismissRetryDialog() {
        _showRetryDialog.value = false
    }

    fun onDismissPreciseLocationDialog() {
        _showPreciseLocationDialog.value = false
    }

    fun dismissDialogs() {
        _showRationaleDialog.value = false
        _showSettingsDialog.value = false
        _showLocationServicesDialog.value = false
        _showRetryDialog.value = false
        _showPreciseLocationDialog.value = false
    }
}