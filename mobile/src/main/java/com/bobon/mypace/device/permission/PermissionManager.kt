package com.bobon.mypace.device.permission

import android.app.Activity

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

    fun shouldShowRationale(activity: Activity): Boolean
    fun shouldShowFineLocationRationale(activity: Activity): Boolean
    fun shouldGoToSettings(): Boolean
    fun shouldExplainBeforeRequest(activity: Activity): Boolean

    fun markRationaleShown()
    fun incrementDenyCount()
    fun openAppSettings()
    fun openLocationSettings()

    fun getPermissionRationaleText(): String
    fun getLocationDisabledText(): String
    fun getPermissionsBlockedText(): String
    fun getPreciseLocationRequiredText(): String

    fun resetState()
}