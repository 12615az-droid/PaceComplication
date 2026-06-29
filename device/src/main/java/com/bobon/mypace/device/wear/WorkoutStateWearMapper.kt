package com.bobon.mypace.device.wear

import com.bobon.mypace.domain.model.WorkoutState

fun WorkoutState.toWearState(): Int {
    return when (this) {
        WorkoutState.IDLE -> 0
        WorkoutState.ACTIVE -> 1
        WorkoutState.PAUSED -> 2
        WorkoutState.FINISHED -> 3
    }
}