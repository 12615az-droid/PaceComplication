package com.example.pacecomplication

import GPSLog
import SensorLog
import SensorTracker
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import com.example.pacecomplication.logger.AppEventData
import com.example.pacecomplication.logger.EventsLog
import com.example.pacecomplication.logger.SourceEvent
import com.example.pacecomplication.logger.TypeEvent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocationService : Service() {

    // 1. ИНЪЕКЦИЯ: Берем готовые инструменты через Koin
    // (Убедись, что LocationNotificationHelper добавлен в AppModule)
    private val locationRepository: LocationRepository by inject()
    private val notificationHelper: LocationNotificationHelper by inject()
    private val sensorTracker: SensorTracker by inject()
    private val eventsLog: EventsLog by inject()
    private val gpsLog: GPSLog by inject()
    private val sensorLog: SensorLog by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var sensorLogJob: Job? = null


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Настраиваем логику callback (но пока не запускаем)
        setupLocationLogic()
    }

    /**
     * ГЛАВНЫЙ ПУЛЬТ УПРАВЛЕНИЯ
     * Сюда приходят команды из LocationRepository: "START" или "STOP"
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (val action = intent?.action) {
            "START" -> {
                locationRepository.forceStartState()
                sensorTracker.startTracking()
                startSensorLogging()

                val notification = notificationHelper.getNotification("0:00", 0f)

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

                logServiceEvent(
                    type = TypeEvent.SERVICE_STARTED,
                    origin = "LocationService.onStartCommand.START",
                    note = "Foreground location service started"
                )
            }

            "STOP" -> {
                locationRepository.forceStopState()
                sensorTracker.stopTracking()
                stopLocationUpdates()
                stopSensorLogging()

                val pausedNotification = notificationHelper.getNotification("Пауза", 0f)
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(LocationNotificationHelper.NOTIFICATION_ID, pausedNotification)

                logServiceEvent(
                    type = TypeEvent.SERVICE_STOPPED,
                    origin = "LocationService.onStartCommand.STOP",
                    note = "Foreground location service paused"
                )
            }

            "KILL" -> {
                stopLocationUpdates()
                sensorTracker.stopTracking()
                stopSensorLogging()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                notificationHelper.cancelNotification()

                logServiceEvent(
                    type = TypeEvent.SERVICE_STOPPED,
                    origin = "LocationService.onStartCommand.KILL",
                    note = "Foreground location service killed"
                )
            }

            else -> {
                serviceScope.launch {
                    eventsLog.log(
                        type = TypeEvent.ERROR,
                        source = SourceEvent.SYSTEM, // тут можно SYSTEM, но я бы оставил SERVICE
                        origin = "LocationService.onStartCommand.unknown",
                        sessionId = null,
                        data = AppEventData(
                            workoutState = locationRepository.workoutState.value,
                            note = "Unknown action: $action"
                        )
                    )
                }
            }
        }
        return START_STICKY
    }

    private fun startSensorLogging() {
        sensorLogJob?.cancel()
        sensorLogJob = serviceScope.launch {
            sensorTracker.sensorDataFlow.collect { sample ->
                sensorLog.logSample(
                    sessionId = locationRepository.currentSessionId.value,
                    sensorData = sample
                )
            }
        }
    }

    private fun stopSensorLogging() {
        val job = sensorLogJob ?: return
        sensorLogJob = null

        serviceScope.launch {
            job.cancelAndJoin()
            sensorLog.flush()
        }
    }


    private fun logServiceEvent(
        type: TypeEvent,
        origin: String,
        note: String
    ) {
        serviceScope.launch {
            eventsLog.log(
                type = type,
                source = SourceEvent.SERVICE,
                origin = origin,
                sessionId = null, // <- сервисные события всегда app-log
                data = AppEventData(
                    workoutState = locationRepository.workoutState.value,
                    note = note
                )
            )
        }
    }

    private fun setupLocationLogic() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val locations = locationResult.locations
                val batchSize = locations.size
                locations.forEachIndexed { index, location ->
                    // Передаем данные в репозиторий
                    val paceUpdate = locationRepository.updatePace(location)

                    // Пишем логи
                    serviceScope.launch {
                        gpsLog.logLocation(
                            sessionId = locationRepository.currentSessionId.value,
                            location = location,
                            paceUpdate = paceUpdate,
                            workoutState = locationRepository.workoutState.value,
                            isTracking = locationRepository.isTracking.value,
                            mode = locationRepository.activityMode.value,
                            batchSize = batchSize,
                            batchIndex = index
                        )
                    }

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

        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ФИНАЛЬНАЯ ЗАЧИСТКА
        stopLocationUpdates()
        stopSensorLogging()
        notificationHelper.cancelNotification() // Убираем уведомление совсем
        notificationHelper.destroyMediaSession()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}