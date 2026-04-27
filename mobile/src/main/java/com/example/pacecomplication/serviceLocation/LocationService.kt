package com.example.pacecomplication.serviceLocation

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
import android.util.Log
import com.example.pacecomplication.LocationNotificationHelper
import com.example.pacecomplication.LocationRepository
import com.example.pacecomplication.logger.AppEventData
import com.example.pacecomplication.logger.EventsLog
import com.example.pacecomplication.logger.SourceEvent
import com.example.pacecomplication.logger.TypeEvent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocationService : Service() {

    private val locationRepository: LocationRepository by inject()
    private val notificationHelper: LocationNotificationHelper by inject()
    private val sensorTracker: SensorTracker by inject()
    private val eventsLog: EventsLog by inject()
    private val gpsLog: GPSLog by inject()
    private val sensorLog: SensorLog by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var sensorLogJob: Job? = null

    // 1. Заменяем жесткую привязку FusedLocationProviderClient на ваш интерфейс
    private lateinit var locationProvider: LocationWrapper

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()

        // 2. Проверяем доступность Google Play Services
        val isGmsAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS

        // 3. Выбираем реализацию "под капотом"
        locationProvider = if (isGmsAvailable) {
            Log.i("LocationService", "GMS доступны. Используем GmsLocation.")
            GmsLocation(this)
        } else {
            Log.i("LocationService", "GMS недоступны. Переход на нативный AndroidLocation.")
            AndroidLocation(this)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (val action = intent?.action) {
            "START" -> {
                locationRepository.forceStartState()
                sensorTracker.startTracking()
                startSensorLogging()

                val notification = notificationHelper.getNotification("0:00", 0f)

                if (Build.VERSION.SDK_INT >= 34) {
                    startForeground(
                        LocationNotificationHelper.Companion.NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } else {
                    startForeground(
                        LocationNotificationHelper.Companion.NOTIFICATION_ID,
                        notification
                    )
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
                manager.notify(
                    LocationNotificationHelper.Companion.NOTIFICATION_ID,
                    pausedNotification
                )

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
                Log.e("Destroy", "kill onDestroy ")
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
                    sessionId = locationRepository.currentSessionId.value, sensorData = sample
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
        type: TypeEvent, origin: String, note: String
    ) {
        serviceScope.launch {
            eventsLog.log(
                type = type,
                source = SourceEvent.SERVICE,
                origin = origin,
                sessionId = null, // <- сервисные события всегда app-log
                data = AppEventData(
                    workoutState = locationRepository.workoutState.value, note = note
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        // Логика из setupLocationLogic переезжает сюда
        locationProvider.startUpdates { location ->
            // Передаем данные в репозиторий
            val paceUpdate = locationRepository.updatePace(location)

            // Пишем логи. Так как LocationWrapper выдает по одной точке (без batch),
            // жестко задаем batchSize = 1, batchIndex = 0.
            serviceScope.launch {
                gpsLog.logLocation(
                    sessionId = locationRepository.currentSessionId.value,
                    location = location,
                    paceUpdate = paceUpdate,
                    workoutState = locationRepository.workoutState.value,
                    mode = locationRepository.activityMode.value,
                    batchSize = 1,
                    batchIndex = 0
                )
            }

            updateNotification(paceUpdate?.paceText, location.accuracy)
        }
    }

    private fun stopLocationUpdates() {
        if (::locationProvider.isInitialized) {
            locationProvider.stopUpdates()
        }
    }

    private fun updateNotification(text: String?, accuracy: Float) {
        if (text.isNullOrBlank()) return
        notificationHelper.showNotification(text, accuracy)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Destroy", "fun onDestroy ")
        stopLocationUpdates()
        stopSensorLogging()
        notificationHelper.cancelNotification()
        notificationHelper.destroyMediaSession()
        locationRepository.destroySave()
        locationRepository.syncWithWear()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}