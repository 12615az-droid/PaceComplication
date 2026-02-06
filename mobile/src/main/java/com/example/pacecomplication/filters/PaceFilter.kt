package com.example.pacecomplication.filters

/**
 * PaceFilter — фильтр и сглаживание темпа.
 *
 * Отвечает за:
 * - фильтрацию входных данных по точности/скорости
 * - вычисление мгновенного темпа
 * - EMA-сглаживание
 */
class PaceFilter(
    private val stopThreshold: Float,
    private val accBadThreshold: Float
) {
    private var emaPace: Double = 0.0

    fun reset() {
        emaPace = 0.0
    }

    fun apply(
        speedMetersPerSec: Float,
        accuracy: Float,
        maxSpeedMetersPerSec: Float,
        alphaProvider: (Float) -> Double
    ): Double? {
        val instantPace = processSpeed(speedMetersPerSec, accuracy, maxSpeedMetersPerSec)
            ?: return null
        return applyEmaFilter(instantPace, accuracy, alphaProvider)
    }

    private fun processSpeed(
        speedMetersPerSec: Float,
        accuracy: Float,
        maxSpeedMetersPerSec: Float
    ): Double? {
        return when {
            accuracy > accBadThreshold -> null
            speedMetersPerSec < stopThreshold -> 0.0
            speedMetersPerSec > maxSpeedMetersPerSec -> null
            else -> 1000.0 / speedMetersPerSec
        }
    }

    private fun applyEmaFilter(
        instantPace: Double,
        accuracy: Float,
        alphaProvider: (Float) -> Double
    ): Double {
        if (instantPace <= 0.0) return 0.0
        if (emaPace <= 0.0) {
            emaPace = instantPace
            return instantPace
        }
        val alpha = alphaProvider(accuracy)
        emaPace = (alpha * instantPace) + (1.0 - alpha) * emaPace
        return emaPace
    }
}