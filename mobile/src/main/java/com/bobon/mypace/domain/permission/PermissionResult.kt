package com.bobon.mypace.domain.permission

sealed class PermissionResult {
    data object RequestPermissions : PermissionResult()
    data object AllGranted : PermissionResult()
    data object RationaleRequired : PermissionResult()
    data object DeniedPermanently : PermissionResult()
    data object LocationDisabled : PermissionResult()
}