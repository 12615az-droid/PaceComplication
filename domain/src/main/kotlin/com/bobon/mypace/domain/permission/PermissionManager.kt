package com.bobon.mypace.domain.permission



interface PermissionManager {
    fun getRequiredPermissions(): Array<String>
    fun getCriticalPermissions(): Array<String>
    fun getOptionalPermissions(): Array<String>

    fun hasAllPermissions(): Boolean
    fun hasAnyLocation(): Boolean
    fun hasFineLocation(): Boolean
    fun hasOnlyCoarseLocation(): Boolean
    fun hasNotifications(): Boolean
    fun isLocationEnabled(): Boolean

    fun shouldGoToSettings(): Boolean


    fun markRationaleShown()
    fun incrementDenyCount()
    fun openAppSettings()
    fun openLocationSettings()



    fun resetState()
}