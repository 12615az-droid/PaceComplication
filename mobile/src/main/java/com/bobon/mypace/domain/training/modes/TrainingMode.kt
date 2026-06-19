package com.bobon.mypace.domain.training.modes

/**
 * TrainingMode — интерфейс режима активности.
 *
 * Каждый режим определяет:
 * - допустимую максимальную скорость
 * - коэффициент EMA в зависимости от точности GPS
 */
interface TrainingMode {
    val id: Int
    val label: String
    val maxSpeedMetersPerSec: Float

    fun alphaForAccuracy(acc: Float): Double
}