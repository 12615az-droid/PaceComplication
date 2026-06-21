package com.bobon.mypace.domain.usecase.permission

import com.bobon.mypace.domain.permission.PermissionManager

class HandlePermissionResultUseCase(
    private val permissionManager: PermissionManager
) {
    operator fun invoke(
        shouldShowFineLocationRationale: Boolean
    ): PermissionRequestResult {
        return when {
            permissionManager.hasAllPermissions() -> {
                PermissionRequestResult.Granted
            }

            shouldShowFineLocationRationale -> {
                permissionManager.incrementDenyCount()
                PermissionRequestResult.ShowRetryDialog
            }

            else -> {
                permissionManager.incrementDenyCount()
                PermissionRequestResult.ShowSettingsDialog
            }
        }
    }
}