package com.example.pacecomplication

import SensorTracker
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.koin.android.ext.android.inject

class LocationService : Service() {

    // 1. ИНЪЕКЦИЯ: Берем готовые инструменты через Koin
    // (Убедись, что LocationNotificationHelper добавлен в AppModule)
    private val locationRepository: LocationRepository by inject()
    private val notificationHelper: LocationNotificationHelper by inject()
    private val sensorTracker: SensorTracker by inject()

    // Если Logger простой и нужен Context, можно оставить создание руками,
    // но лучше тоже через inject, если он в модуле. Пока оставим так:
    private lateinit var logger: TelemetryLogger

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        logger = TelemetryLogger(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Настраиваем логику callback (но пока не запускаем)
        setupLocationLogic()
    }

    /**
     * ГЛАВНЫЙ ПУЛЬТ УПРАВЛЕНИЯ
     * Сюда приходят команды из LocationRepository: "START" или "STOP"
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            "START" -> {
                // Синхронизируем состояние (на случай, если старт был из шторки, а не из UI)
                // Это безопасно, так как мы убрали "return" в репозитории
                locationRepository.forceStartState()
                sensorTracker.startTracking()
                

                val notification = notificationHelper.getNotification("0:00", 0f)

                // Запускаем Foreground
                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(
                        LocationNotificationHelper.NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } else {
                    startForeground(LocationNotificationHelper.NOTIFICATION_ID, notification)
                }

                startLocationUpdates()
            }

            "STOP" -> {
                locationRepository.forceStopState()
                sensorTracker.stopTracking()
                stopLocationUpdates()
                val pausedNotification = notificationHelper.getNotification("Пауза", 0f)
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(LocationNotificationHelper.NOTIFICATION_ID, pausedNotification)

            }

            "KILL" -> {
                // --- РЕЖИМ ВЫХОДА (Save) ---
                // Вот теперь убиваем всё
                stopLocationUpdates()
                sensorTracker.stopTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                notificationHelper.cancelNotification()
                // Или showFinalNotification, если хочешь оставить результат
            }
        }
        return START_STICKY
    }

    private fun setupLocationLogic() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // Передаем данные в репозиторий
                    val paceUpdate = locationRepository.updatePace(location)

                    // Пишем логи
                    logger.log("Pace: ${paceUpdate?.paceValue} | Acc: ${location.accuracy}")

                    // Обновляем уведомление (то, что было в stopPaceService)
                    updateNotification(paceUpdate?.paceText, location.accuracy)
                }
            }
        }
    }

    private fun updateNotification(text: String?, accuracy: Float) {
        if (text.isNullOrBlank()) return
        notificationHelper.showNotification(text, accuracy) // Используем метод хелпера
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMinUpdateIntervalMillis(500).build()

        fusedLocationClient.requestLocationUpdates(
            request, locationCallback, Looper.getMainLooper()


        )
    }

    private fun stopLocationUpdates() {
        // Важно: отписываемся от GPS, чтобы не жрать батарею
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ФИНАЛЬНАЯ ЗАЧИСТКА
        stopLocationUpdates()
        notificationHelper.cancelNotification() // Убираем уведомление совсем
        notificationHelper.destroyMediaSession()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}