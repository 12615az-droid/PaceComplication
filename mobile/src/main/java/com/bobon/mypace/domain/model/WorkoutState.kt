package com.bobon.mypace.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class WorkoutState(val intState: Int) {
    IDLE(0),
    ACTIVE(1),
    PAUSED(2),
    FINISHED(3);

    companion object {
        fun fromIntState(value: Int): WorkoutState {
            return entries.find { it.intState == value } ?: IDLE
        }
    }
}
