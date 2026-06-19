package com.bobon.mypace.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat

interface PermissionManager {
    fun getRequiredPermissions(): Array<String>
    fun getCriticalPermissions(): Array<String>
    fun getOptionalPermissions(): Array<String>

    fun hasAllPermissions(): Boolean
    fun hasAnyLocation(): Boolean      // ← COARSE или FINE
    fun hasFineLocation(): Boolean     // ← именно FINE
    fun hasOnlyCoarseLocation(): Boolean // ← COARSE есть, FINE нет
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
    fun getPreciseLocationRequiredText(): String  // ← новый текст
    fun resetState()
}
sealed class PermissionResult {
    object RequestPermissions : PermissionResult() // ← новый
    object AllGranted : PermissionResult()
    object RationaleRequired : PermissionResult()
    object DeniedPermanently : PermissionResult()
    object LocationDisabled : PermissionResult()
}

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

    override fun getPreciseLocationRequiredText(): String {
        return buildString {
            appendLine("⚠️ Выбрана приблизительная геолокация")
            appendLine()
            appendLine("Для точного расчёта расстояния и темпа нужна ТОЧНАЯ геолокация.")
            appendLine()
            appendLine("Текущий режим «Приблизительная» дает большую погрешность:")
            appendLine("• Расстояние может отличаться на 10-100 метров")
            appendLine("• Темп будет прыгать")
            appendLine("• Маршрут на карте будет неточным")
            appendLine()
            appendLine("Пожалуйста, выберите «Разрешить точную геолокацию» в настройках.")
        }
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

    override fun getPermissionRationaleText(): String {
        return buildString {
            appendLine("⚠️ Для записи тренировки необходимы разрешения:")
            appendLine()
            appendLine("📍 Геолокация — ОБЯЗАТЕЛЬНО")
            appendLine("   • Определение маршрута и расстояния")
            appendLine("   • Расчёт темпа и скорости")
            appendLine()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appendLine("🔔 Уведомления — рекомендуется")
                appendLine("   • Показ прогресса в шторке уведомлений")
                appendLine("   • Управление тренировкой без входа в приложение")
                appendLine()
            }
            appendLine("Без геолокации тренировка не может быть записана.")
        }
    }

    override fun getLocationDisabledText(): String {
        return "Для записи тренировки нужно включить GPS в настройках системы.\n\n" +
                "Без этого мы не определим ваш маршрут, расстояние и темп."
    }

    override fun getPermissionsBlockedText(): String {
        return buildString {
            appendLine("Доступ к разрешениям заблокирован.")
            appendLine()
            appendLine("Для работы приложения нужны:")
            appendLine("• Геолокация — запись маршрута")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appendLine("• Уведомления — показ прогресса")
            }
            appendLine()
            appendLine("Включите разрешения вручную в настройках приложения.")
        }
    }




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

    override fun shouldShowRationale(activity: Activity): Boolean {
        return getRequiredPermissions().any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    override fun shouldShowFineLocationRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }



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

    override fun shouldExplainBeforeRequest(activity: Activity): Boolean {
        if (prefs.getBoolean(KEY_RATIONALE_SHOWN, false)) return false
        return shouldShowRationale(activity)
    }

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