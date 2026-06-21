package com.bobon.mypace.device.location.serviceLocation

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.bobon.mypace.core.logger.TypeEvent

import com.bobon.mypace.device.location.LocationUpdateHandler
import com.bobon.mypace.device.notification.LocationNotificationHelper
import com.bobon.mypace.device.sensor.SensorLoggingController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocationService : Service() {

    private val notificationHelper: LocationNotificationHelper by inject()
    private val serviceEventLogger: LocationServiceEventLogger by inject()
    private val locationUpdateHandler: LocationUpdateHandler by inject()
    private val sensorLoggingController: SensorLoggingController by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)


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
                sensorLoggingController.start(serviceScope)

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

                serviceScope.launch {
                    serviceEventLogger.logServiceStarted()
                }
            }

            "STOP" -> {
                sensorLoggingController.stop(serviceScope)
                stopLocationUpdates()

                notificationHelper.showNotification("Пауза", 0f)

                serviceScope.launch {
                    serviceEventLogger.logServicePaused()
                }
            }

            "KILL" -> {
                stopLocationUpdates()
                sensorLoggingController.stop(serviceScope)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                notificationHelper.cancelNotification()
                serviceScope.launch {
                    serviceEventLogger.logServiceKilled()
                }
            }

            else -> {
                serviceScope.launch {
                    serviceEventLogger.logUnknownAction(action)
                }
            }
        }
        return START_STICKY
    }





    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationProvider.startUpdates { location ->
            serviceScope.launch {
                val result = locationUpdateHandler.handle(location)

                updateNotification(
                    text = result.paceText,
                    accuracy = result.accuracyMeters
                )
            }
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
        sensorLoggingController.stop(serviceScope)
        notificationHelper.cancelNotification()
        notificationHelper.destroyMediaSession()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
