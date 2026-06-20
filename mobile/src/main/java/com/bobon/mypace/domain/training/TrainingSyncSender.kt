package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.model.WorkoutState


interface TrainingSyncSender {
    fun sendWorkoutUpdate(
        paceText: String,
        workoutState: WorkoutState
    )
}