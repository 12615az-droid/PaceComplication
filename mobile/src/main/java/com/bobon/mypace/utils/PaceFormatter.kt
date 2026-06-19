package com.bobon.mypace.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PaceFormatter {
    private const val PACE_DEFAULT = "0:00"

    /**
     * Форматирует темп (секунды на км) в строку "М:СС"
     */
    fun formatPace(totalSecondsPerKm: Double): String {
        if (totalSecondsPerKm <= 0 || totalSecondsPerKm.isInfinite() || totalSecondsPerKm.isNaN()) {
            return PACE_DEFAULT
        }
        val totalSeconds = totalSecondsPerKm.toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    /**
     * Форматирует длительность тренировки
     */
    fun formatDuration(startTime: Long, endTime: Long): String {
        val durationMs = endTime - startTime
        if (durationMs <= 0) return "00:00"
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Вычисляет и форматирует темп на основе дистанции и времени
     */
    fun calculateAndFormatPace(startTime: Long, endTime: Long, distanceMeters: Double): String {
        if (distanceMeters <= 0) return "0:00 мин/км"
        val durationMinutes = (endTime - startTime) / 60000.0
        val distanceKm = distanceMeters / 1000.0
        val paceMinPerKm = durationMinutes / distanceKm

        val minutes = paceMinPerKm.toInt()
        val seconds = ((paceMinPerKm - minutes) * 60).toInt()
        return String.format(Locale.getDefault(), "%d:%02d мин/км", minutes, seconds)
    }

    /**
     * Форматирует дату начала тренировки
     */
    fun formatDate(timeInMillis: Long): String {
        val formatter = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        return formatter.format(Date(timeInMillis))
    }
}
