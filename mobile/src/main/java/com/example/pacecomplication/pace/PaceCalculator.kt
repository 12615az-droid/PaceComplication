package com.example.pacecomplication.pace

import com.example.pacecomplication.filters.PaceFilter

/**
 * PaceCalculator — инкапсулирует расчёт и форматирование темпа.
 *
 * Отвечает за:
 * - фильтрацию входных данных по точности/скорости
 * - вычисление мгновенного темпа и EMA-сглаживание
 * - форматирование результата в строку "мм:сс"
 */
class PaceCalculator(
    stopThreshold: Float,
    accBadThreshold: Float
) {
    private val paceFilter = PaceFilter(
        stopThreshold = stopThreshold,
        accBadThreshold = accBadThreshold
    )

    fun reset() {
        paceFilter.reset()
    }

    fun calculate(
        speedMetersPerSec: Float,
        accuracy: Float,
        maxSpeedMetersPerSec: Float,
        alphaProvider: (Float) -> Double
    ): PaceUpdate? {
        val filteredPace = paceFilter.apply(
            speedMetersPerSec = speedMetersPerSec,
            accuracy = accuracy,
            maxSpeedMetersPerSec = maxSpeedMetersPerSec,
            alphaProvider = alphaProvider
        ) ?: return null
        return PaceUpdate(
            paceValue = filteredPace,
            paceText = formatPace(filteredPace)
        )
    }

    private fun formatPace(totalSecondsPerKm: Double): String {
        if (totalSecondsPerKm <= 0) return PACE_DEFAULT
        val totalSeconds = totalSecondsPerKm.toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    private companion object {
        const val PACE_DEFAULT = "0:00"
    }
}