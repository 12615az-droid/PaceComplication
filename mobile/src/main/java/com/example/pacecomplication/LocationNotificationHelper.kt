package com.example.pacecomplication


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

/**
 * LocationNotificationHelper — сборка Foreground-уведомления для LocationService.
 *
 * Назначение:
 * - создаёт NotificationChannel для Android 8+
 * - формирует постоянное (ongoing) уведомление "идёт трекинг"
 * - использует MediaStyle, чтобы уведомление выглядело как "плеер" (как у музыки)
 *
 * Текущее состояние:
 * - кнопки действий (Start/Stop) пока являются заглушками (PendingIntent = null)
 *
 * Важно:
 * - mediaSession создаётся лениво (только при первом показе уведомления)
 * - destroyMediaSession() должен вызываться при остановке сервиса
 */
class LocationNotificationHelper(private val context: Context) {

    // MediaSession нужна для MediaStyle: Android рисует уведомление как "плеер" с кнопками.
    private var mediaSession: MediaSessionCompat? = null


    companion object {
        const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "GPS_CHANNEL"
        private const val CHANNEL_NAME = "Запись трека"


    }

    /**
     * Создаёт канал уведомлений для трекинга локации.
     *
     * Обязателен для Android 8+ (API 26).
     * Используется IMPORTANCE_LOW, чтобы:
     * - не воспроизводить звук
     * - не показывать heads-up уведомление
     *
     * Должен вызываться один раз при запуске сервиса.
     */
    fun createNotificationChannel() {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }


    /**
     * Собирает и возвращает foreground-уведомление трекинга.
     *
     * Что делает:
     * - создаёт MediaSession (если ещё не создана)
     * - формирует MediaStyle уведомление (как у музыкального плеера)
     * - добавляет кнопки управления (пока заглушки)
     *
     * Особенности:
     * - уведомление нельзя смахнуть (ongoing)
     * - обновления не вызывают звук или вибрацию (setOnlyAlertOnce)
     *
     * @return Notification для использования в startForeground()
     */
    fun getNotification(pace: String?): Notification {
        // Клик по уведомлению открывает MainActivity
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val startPendingIntent = createServiceAction("START", 10)

        // 3. Интент для кнопки "СТОП" (шлёт команду STOP в сервис)
        val stopPendingIntent = createServiceAction("STOP", 11)
        // MediaSession создаём один раз (лениво), чтобы не плодить объекты при обновлении уведомления
        if (mediaSession == null) mediaSession = MediaSessionCompat(context, "pace_session")

        // OnlyAlertOnce — не "пикает" при обновлениях.
        // Ongoing — нельзя смахнуть, пока идёт трекинг (foreground service).
        // MediaStyle — отображает кнопки действий как у музыкального плеера.

        val contentText = buildNotificationText(pace)
        return NotificationCompat.Builder(context, CHANNEL_ID).setContentTitle("Pace Tracker")
            .setContentText(contentText).setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent).setOnlyAlertOnce(true).setOngoing(true).setStyle(
                androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(
                        mediaSession?.sessionToken
                    ).setShowActionsInCompactView(0, 1)
            ).addAction(
                android.R.drawable.ic_media_play, "Старт", startPendingIntent
            ).addAction(
                android.R.drawable.ic_media_pause, "Стоп", stopPendingIntent

            ).build()
    }


    fun cancelNotification() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.cancel(NOTIFICATION_ID)
    }


    fun showNotification(pace: String?) {
        val notification = getNotification(pace)
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun createServiceAction(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, LocationService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            context, requestCode, // Важно: разные ID для разных кнопок!
            intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Освобождает MediaSession.
     *
     * Должна вызываться при остановке LocationService,
     * чтобы корректно освободить системные ресурсы.
     */
    fun destroyMediaSession() {
        mediaSession?.release()
        mediaSession = null
    }


}


private fun buildNotificationText(paceMinPerKm: String?): String {
    val paceText = paceMinPerKm?.takeIf { it.isNotBlank() } ?: "--:--"
    return "Темп: $paceText/км"
}



