package com.bobon.mypace.device.wear

import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.TrainingSyncSender

class WearTrainingSyncSender(
    private val wearDataSender: WearDataSender
) : TrainingSyncSender {

    override fun sendWorkoutUpdate(
        paceText: String,
        workoutState: WorkoutState
    ) {
        wearDataSender.sendWorkoutUpdate(
            paceString = paceText,
            workoutState = workoutState.toWearState()
        )
    }
}
