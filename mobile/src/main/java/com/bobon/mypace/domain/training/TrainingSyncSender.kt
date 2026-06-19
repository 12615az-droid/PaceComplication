package com.bobon.mypace.domain.training

interface TrainingSyncSender {
    fun sendWorkoutUpdate(
        paceText: String,
        workoutState: Int
    )
}