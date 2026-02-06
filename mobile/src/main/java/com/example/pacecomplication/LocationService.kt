package com.example.pacecomplication

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*

import com.google.android.gms.location.*




/**
 * LocationService — foreground service для трекинга локации.
 *
 * Назначение:
 * - запускается из MainActivity после получения разрешений
 * - запрашивает обновления геолокации через FusedLocationProviderClient
 * - обновляет расчёт темпа/статуса через LocationRepository
 * - держит постоянное уведомление (foreground notification), чтобы сервис не был убит системой
 *
 * Зависимости:
 * - LocationRepository: расчёт темпа и хранение состояния
 * - LocationNotificationHelper: канал и уведомление
 * - TelemetryLogger: отладочная запись телеметрии
 *
 * Требования:
 * - разрешения на геолокацию должны быть выданы до запуска сервиса
 * - сервис запускается как foreground и показывает ongoing уведомление
 */
class LocationService : Service() {

    // Google Fused Location Provider — источник локации
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Callback, куда приходят обновления локации
    private lateinit var locationCallback: LocationCallback

    // Сборка уведомления foreground-сервиса
    private lateinit var notificationHelper: LocationNotificationHelper

    // Отладочный логгер телеметрии (Logcat + файл)
    private lateinit var logger: TelemetryLogger








    /**
     * Инициализация сервиса.
     *
     * Здесь создаём все зависимости:
     * - notification helper + канал уведомлений
     * - logger
     * - init репозитория (контекст)
     * - fusedLocationClient
     * - callback логика обработки локации
     */
    override fun onCreate() {
        super.onCreate()
        notificationHelper = LocationNotificationHelper(this)
        logger = TelemetryLogger(this)
        LocationRepository.init(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        notificationHelper.createNotificationChannel()
        // Подготовка callback-логики до старта updates
        setupLocationLogic()
    }


    /**
     * Настраивает обработку входящих координат (LocationCallback).
     *
     * Логика:
     * - для каждой пришедшей Location обновляем темп через LocationRepository
     * - пишем отладочную телеметрию (темп + точность)
     */
    private fun setupLocationLogic() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val paceTwo = LocationRepository.updatePace(location)

                    logger.log("SmoothPACE: ${paceTwo?.paceValue} | ACC: ${location.accuracy}")
                    updateForegroundNotification(paceTwo?.paceText)
                }
            }
        }
    }

    private fun updateForegroundNotification(pace: String?) {
         if (pace.isNullOrBlank()) return
        val notification = notificationHelper.getNotification(pace)
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(LocationNotificationHelper.NOTIFICATION_ID, notification)
    }

    /**
     * Запуск сервиса.
     *
     * Что происходит:
     * - формируем уведомление и поднимаем сервис в foreground
     * - запускаем обновления геолокации
     *
     * Возвращает START_STICKY:
     * - система может перезапустить сервис после убийства процесса
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = notificationHelper.getNotification("0:00")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(LocationNotificationHelper.NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(LocationNotificationHelper.NOTIFICATION_ID, notification)
        }
        startLocationUpdates()
        return START_STICKY
    }



    /**
     * Запускает запрос обновлений геолокации.
     *
     * Используем высокий приоритет и период 1 сек (1000 мс).
     * Требует выданных разрешений на геолокацию (иначе SecurityException).
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    /**
     * Освобождение ресурсов при остановке сервиса.
     *
     * Важно:
     * - обязательно снять подписку на обновления локации
     * - освободить MediaSession в уведомлении
     */
    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_DETACH)
        super.onDestroy()
        // Останавливаем обновления координат
        fusedLocationClient.removeLocationUpdates(locationCallback)
        // Освобождаем MediaSession, чтобы не держать системные ресурсы
        notificationHelper.destroyMediaSession()


    }



    /**
     * Binding не поддерживается — сервис запускается только через startService().
     */
    override fun onBind(intent: Intent?): IBinder? = null
}




