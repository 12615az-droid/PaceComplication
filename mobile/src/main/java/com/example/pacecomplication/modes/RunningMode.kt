package com.example.pacecomplication.modes

object RunningMode : TrainingMode {
    override val label: String = "БЕГ"
    override val maxSpeedMetersPerSec: Float = 8.5f

    override fun alphaForAccuracy(acc: Float): Double {
        return when {
            acc > 25f -> 0.10
            acc > 10f -> 0.30
            else -> 0.65
        }
    }
}