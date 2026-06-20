package com.bobon.mypace.device.location.serviceLocation



import SensorLog
import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.bobon.mypace.device.notification.LocationNotificationHelper
import com.bobon.mypace.device.location.toGpsPoint
import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.core.logger.AppEventData
import com.bobon.mypace.core.logger.EventsLog
import com.bobon.mypace.core.logger.GPSLog
import com.bobon.mypace.core.logger.SourceEvent
import com.bobon.mypace.core.logger.TypeEvent
import com.bobon.mypace.device.sensor.SensorTracker
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

    private val trainingManager: TrainingManager by inject()
    private val notificationHelper: LocationNotificationHelper by inject()
    private val sensorTracker: SensorTracker by inject()
    private val eventsLog: EventsLog by inject()
    private val gpsLog: GPSLog by inject()
    private val sensorLog: SensorLog by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var sensorLogJob: Job? = null

    private lateinit var locationProvider: LocationWrapper

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel()

        val isGmsAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(applicationContext) == ConnectionResult.SUCCESS

        locationProvider = if (isGmsAvailable) {
            Log.i("LocationService", "GMS доступны. Используем GmsLocation.")
            GmsLocation(applicationContext)
        } else {
            Log.i("LocationService", "GMS недоступны. Переход на нативный AndroidLocation.")
            AndroidLocation(applicationContext)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (val action = intent?.action) {
            "START" -> {
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
                    startForeground(
                        LocationNotificationHelper.NOTIFICATION_ID,
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
                sensorTracker.stopTracking()
                stopLocationUpdates()
                stopSensorLogging()

                notificationHelper.showNotification("Пауза", 0f)

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
                        source = SourceEvent.SERVICE,
                        origin = "LocationService.onStartCommand.unknown",
                        sessionId = null,
                        data = AppEventData(
                            workoutState = trainingManager.workoutState.value,
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
                    sessionId = trainingManager.currentSessionId.value, sensorData = sample
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
                sessionId = null,
                data = AppEventData(
                    workoutState = trainingManager.workoutState.value, note = note
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationProvider.startUpdates { location ->
            val paceUpdate = trainingManager.updatePace(location.toGpsPoint())
            
            // Получаем отформатированный темп из менеджера, так как он его уже посчитал и обновил StateFlow
            val formattedPace = trainingManager.currentPace.value

            serviceScope.launch {
                gpsLog.logLocation(
                    sessionId = trainingManager.currentSessionId.value,
                    location = location,
                    paceUpdate = paceUpdate,
                    workoutState = trainingManager.workoutState.value,
                    totalDistance = trainingManager.totalDistance.value,
                    mode = trainingManager.activityMode.value,
                    batchSize = 1,
                    batchIndex = 0
                )
            }

            updateNotification(formattedPace.toString(), location.accuracy)
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
        stopLocationUpdates()
        stopSensorLogging()
        notificationHelper.cancelNotification()
        notificationHelper.destroyMediaSession()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
