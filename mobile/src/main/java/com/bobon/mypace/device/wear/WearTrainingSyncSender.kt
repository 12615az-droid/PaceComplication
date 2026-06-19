package com.bobon.mypace.device.wear

import com.bobon.mypace.device.wear.WearDataSender
import com.bobon.mypace.domain.training.TrainingSyncSender

class WearTrainingSyncSender(
    private val wearDataSender: WearDataSender
) : TrainingSyncSender {

    override fun sendWorkoutUpdate(
        paceText: String,
        workoutState: Int
    ) {
        wearDataSender.sendWorkoutUpdate(paceText, workoutState)
    }
}