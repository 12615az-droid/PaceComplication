package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.permission.PermissionManager

class CheckStartWorkoutAvailabilityUseCase(
    private val permissionManager: PermissionManager
) {
    operator fun invoke(): StartWorkoutAvailability {
        return when {
            !permissionManager.hasAnyLocation() -> {
                StartWorkoutAvailability.NoLocationPermission
            }

            permissionManager.hasOnlyCoarseLocation() -> {
                StartWorkoutAvailability.OnlyCoarseLocation
            }

            !permissionManager.isLocationEnabled() -> {
                StartWorkoutAvailability.LocationDisabled
            }

            else -> {
                StartWorkoutAvailability.Available
            }
        }
    }
}