package com.bobon.mypace.pace

import com.bobon.mypace.filters.PaceFilter

/**
 * PaceCalculator — инкапсулирует расчёт темпа.
 *
 * Отвечает за:
 * - фильтрацию входных данных по точности/скорости
 * - вычисление мгновенного темпа и EMA-сглаживание
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

    /**
     * Рассчитывает темп на основе скорости и точности.
     * Возвращает PaceUpdate с сырым значением секунд на километр.
     */
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
        
        return PaceUpdate(secondsPerKm = filteredPace)
    }
}
