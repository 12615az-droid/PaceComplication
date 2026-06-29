package com.bobon.mypace.domain.usecase.permission

sealed interface PermissionRequestResult {
    data object Granted : PermissionRequestResult
    data object ShowRetryDialog : PermissionRequestResult
    data object ShowSettingsDialog : PermissionRequestResult
}