package com.example.pacecomplication.modes

object TrainingModes {
    private val modes: List<TrainingMode> = listOf(RunningMode, WalkingMode)

    fun next(current: TrainingMode): TrainingMode {
        val index = modes.indexOf(current).coerceAtLeast(0)
        val nextIndex = (index + 1) % modes.size
        return modes[nextIndex]
    }
}