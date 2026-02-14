package com.example.pacecomplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pacecomplication.ui.PaceScreen
import com.example.pacecomplication.ui.RunningAppTheme


/**
 * MainActivity — точка входа приложения.
 *
 * Ответственность Activity:
 * - запрос runtime-разрешений (геолокация + уведомления на Android 13+)
 * - запуск/остановка фонового сервиса трекинга (LocationService)
 * - установка Compose UI (Theme -> Surface -> PaceScreen)
 *
 * Важно:
 * - бизнес-состояние трекинга хранится в LocationRepository
 * - UI получает события через callbacks (onStart/onStop/onModeChanged)
 */
class MainActivity : ComponentActivity() {

    // Лаунчер запроса разрешений. Callback вызывается системой после ответа пользователя.
    // Если разрешения получены — запускаем трекинг и сервис.
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
        // Проверяем доступ к точной геолокации
        // + на Android 13+ отдельно проверяем разрешение на уведомления

    ) { permissions ->
        val isFineGranted =
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val isNotificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.getOrDefault(Manifest.permission.POST_NOTIFICATIONS, false)
        } else true

        if (isFineGranted && isNotificationGranted) {
            startPaceService()
        }
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Compose-дерево приложения: тема -> контейнер -> основной экран
        setContent {

            RunningAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Передаём в UI только события управления.
                    // Сама Activity решает: запросить разрешения, запустить/остановить сервис и т.д
                    PaceScreen()



                }
            }
        }


    }

    /**
     * Запрашивает разрешения, необходимые для трекинга.
     * - FINE/COARSE location
     * - POST_NOTIFICATIONS (только Android 13+)
     */
    private fun requestPacePermissions() {
        val permissionsNeeded = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        locationPermissionRequest.launch(permissionsNeeded.toTypedArray())
    }

    /**
     * Запускает трекинг:
     * - включает состояние в LocationRepository
     * - запускает LocationService
     */
    private fun startPaceService() {
        RepositoryProvider.locationRepository.startTracking()
        startService(Intent(this, LocationService::class.java))
    }


    /**
     * Останавливает трекинг:
     * - останавливает LocationService
     * - сбрасывает состояние в LocationRepository
     */
    @SuppressLint("ImplicitSamInstance")
    private fun stopPaceService() {
         RepositoryProvider.locationRepository.stopTracking()
        stopService(Intent(this, LocationService::class.java))
        LocationNotificationHelper(this).showNotification(
            RepositoryProvider.locationRepository.currentPace.value
        )
    }


    private fun savePaceService() {
        RepositoryProvider.locationRepository.saveTracking()
        stopService(Intent(this, LocationService::class.java))
        LocationNotificationHelper(this).cancelNotification()



    }

}