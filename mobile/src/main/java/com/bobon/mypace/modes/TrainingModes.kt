package com.bobon.mypace.modes

object TrainingModes {
    private val modes: List<TrainingMode> = listOf(RunningMode, WalkingMode)

    fun fromId(id: Int): TrainingMode {
        return modes.find { it.id == id } ?: WalkingMode
    }

    fun toId(mode: TrainingMode): Int = mode.id

    fun next(current: TrainingMode): TrainingMode {
        val index = modes.indexOf(current).coerceAtLeast(0)
        val nextIndex = (index + 1) % modes.size
        return modes[nextIndex]
    }
}