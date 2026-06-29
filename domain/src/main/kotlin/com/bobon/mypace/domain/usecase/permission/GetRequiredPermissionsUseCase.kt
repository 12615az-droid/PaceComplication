package com.bobon.mypace.domain.usecase.permission

import com.bobon.mypace.domain.permission.PermissionManager

class GetRequiredPermissionsUseCase(
    private val permissionManager: PermissionManager
) {
    operator fun invoke(): Array<String> {
        return permissionManager.getRequiredPermissions()
    }
}