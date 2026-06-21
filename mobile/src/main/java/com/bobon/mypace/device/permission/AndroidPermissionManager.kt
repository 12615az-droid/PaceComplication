package com.bobon.mypace.device.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.bobon.mypace.domain.permission.PermissionManager


class AndroidPermissionManager(private val context: Context) : PermissionManager {

    companion object {
        private const val PREFS_NAME = "permission_prefs"
        private const val KEY_RATIONALE_SHOWN = "rationale_shown"
        private const val KEY_PERMISSION_DENIED_COUNT = "permission_denied_count"
        private const val MAX_DENIES_BEFORE_SETTINGS = 2
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }


    override fun hasAnyLocation(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse || fine
    }

    override fun hasFineLocation(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun hasOnlyCoarseLocation(): Boolean {
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarse && !fine  // COARSE есть, FINE нет
    }



    // ==================== РАЗРЕШЕНИЯ ====================
    override fun getRequiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions.toTypedArray()
    }

    /** Только критичные — без них тренировка не работает */
    override fun getCriticalPermissions(): Array<String> = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /** Опциональные — удобно, но не обязательно */
    override fun getOptionalPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyArray()
        }
    }

    override fun hasNotifications(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // До Android 13 уведомления не требуют разрешения
        }
    }

    // ... остальные методы ...








    override fun hasAllPermissions(): Boolean {
        return getRequiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }



    /**
     * Android 12+ (API 31): пользователь может выбрать "Приблизительная" локация.
     * Этот метод проверяет, что выбрана именно ТОЧНАЯ.
     */
    fun hasPreciseLocation(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasFineLocation()
        }
    }

    // ==================== RATIONALE ====================





    // ==================== СИСТЕМНЫЕ СЛУЖБЫ ЛОКАЦИИ (GPS) ====================

    /**
     * Проверяет, включены ли системные службы локации (GPS или Network).
     * Это ОТДЕЛЬНО от разрешений — пользователь мог дать разрешение,
     * но выключить GPS в шторке/настройках.
     */
    override fun isLocationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            LocationManagerCompat.isLocationEnabled(locationManager)
        }
    }

    /**
     * Открывает системные настройки локации (шторка "Местоположение").
     */
    override fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ==================== НАСТРОЙКИ ПРИЛОЖЕНИЯ ====================

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    // ==================== СОСТОЯНИЕ ДИАЛОГОВ ====================



    override fun markRationaleShown() {
        prefs.edit().putBoolean(KEY_RATIONALE_SHOWN, true).apply()
    }

    override fun incrementDenyCount() {
        val current = prefs.getInt(KEY_PERMISSION_DENIED_COUNT, 0)
        prefs.edit().putInt(KEY_PERMISSION_DENIED_COUNT, current + 1).apply()
    }

    override fun shouldGoToSettings(): Boolean {
        val denyCount = prefs.getInt(KEY_PERMISSION_DENIED_COUNT, 0)
        return denyCount >= MAX_DENIES_BEFORE_SETTINGS
    }

    override fun resetState() {
        prefs.edit().clear().apply()
    }
}