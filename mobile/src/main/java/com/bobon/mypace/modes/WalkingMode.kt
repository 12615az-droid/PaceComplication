package com.bobon.mypace.modes

object WalkingMode : TrainingMode {
    override val id: Int = 2
    override val label: String = "ХОДЬБА"
    override val maxSpeedMetersPerSec: Float = 3.5f

    override fun alphaForAccuracy(acc: Float): Double {
        return when {
            acc > 25f -> 0.05
            acc > 10f -> 0.15
            else -> 0.40
        }
    }
}