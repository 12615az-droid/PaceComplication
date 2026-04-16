package com.example.pacecomplication.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import com.example.pacecomplication.R

class TrainingWearService : Service() {

    companion object {
        private const val TAG = "TRAINING_WEAR_SERVICE"
        private const val NOTIFICATION_ID = 101
        private const val CHANNEL_ID = "sync_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "onCreate: service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand: action=$action, startId=$startId")

        when (action) {
            "START" -> {
                Log.d(TAG, "START: starting foreground with Ongoing Activity")

                val notificationIntent = Intent(this, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                // 1. Сначала создаем BUILDER, а не само уведомление
                val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Тренировка активна")
                    .setContentText("Синхронизация...")
                    .setSmallIcon(R.drawable.ic_launcher_foreground) // Твоя иконка для строки состояния
                    .setOngoing(true)
                    .setCategory(NotificationCompat.CATEGORY_WORKOUT)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(pendingIntent)

                // 2. Создаем OngoingActivity, передавая туда именно BUILDER
                val ongoingActivity = OngoingActivity.Builder(
                    applicationContext, NOTIFICATION_ID, notificationBuilder
                )
                    .setStaticIcon(R.drawable.ic_launcher_foreground) // Иконка, которая будет внизу циферблата
                    .setTouchIntent(pendingIntent)
                    .build()

                // 3. ПРИМЕНЯЕМ настройки Ongoing к билдеру
                ongoingActivity.apply(applicationContext)

                // 4. И только ТЕПЕРЬ собираем уведомление из модифицированного билдера
                val finalNotification = notificationBuilder.build()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        finalNotification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                    )
                } else {
                    startForeground(NOTIFICATION_ID, finalNotification)
                }

                startHeartRateMonitoring()
            }

            "STOP" -> {
                Log.d(TAG, "STOP: updating notification to paused")
                updateNotificationPaused()
            }

            "KILL" -> {
                Log.d(TAG, "KILL: stopping foreground and self")
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            else -> {
                Log.w(TAG, "UNKNOWN ACTION: $action")
            }
        }

        return START_STICKY
    }

    private fun updateNotificationPaused() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Тренировка на паузе")
            .setContentText("Нажмите, чтобы вернуться")
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun startHeartRateMonitoring() {
        Log.d(TAG, "startHeartRateMonitoring: ready to read heart rate")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Sync Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT // Повысил важность до DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
        Log.d(TAG, "createNotificationChannel: channel created")
    }
}