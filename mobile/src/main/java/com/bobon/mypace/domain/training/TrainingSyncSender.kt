package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.model.WorkoutState


interface TrainingSyncSender {
    fun sendWorkoutUpdate(
        paceSecondsPerKm: Double?,
        workoutState: WorkoutState
    )
}