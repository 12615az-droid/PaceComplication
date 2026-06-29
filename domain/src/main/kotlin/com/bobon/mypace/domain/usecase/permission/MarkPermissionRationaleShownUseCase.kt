package com.bobon.mypace.domain.usecase.permission

import com.bobon.mypace.domain.permission.PermissionManager

class MarkPermissionRationaleShownUseCase(
    private val permissionManager: PermissionManager
) {
    operator fun invoke() {
        permissionManager.markRationaleShown()
    }
}