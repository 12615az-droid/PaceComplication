package com.bobon.mypace.domain.usecase.permission

import com.bobon.mypace.domain.permission.PermissionManager

class IsLocationEnabledUseCase(
    private val permissionManager: PermissionManager
) {
    operator fun invoke(): Boolean {
        return permissionManager.isLocationEnabled()
    }
}