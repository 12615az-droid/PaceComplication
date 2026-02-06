package com.example.pacecomplication.modes

/**
 * TrainingMode — интерфейс режима активности.
 *
 * Каждый режим определяет:
 * - допустимую максимальную скорость
 * - коэффициент EMA в зависимости от точности GPS
 */
interface TrainingMode {
    val label: String
    val maxSpeedMetersPerSec: Float

    fun alphaForAccuracy(acc: Float): Double
}