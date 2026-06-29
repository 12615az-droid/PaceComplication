package com.bobon.mypace.device.wear

import com.bobon.mypace.core.formatter.PaceFormatter
import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.TrainingSyncSender

class WearTrainingSyncSender(
    private val wearDataSender: WearDataSender
) : TrainingSyncSender {

    override fun sendWorkoutUpdate(
        paceSecondsPerKm: Double?,
        workoutState: WorkoutState
    ) {
        val paceText = PaceFormatter.formatPace(
            paceSecondsPerKm ?: 0.0
        )
        wearDataSender.sendWorkoutUpdate(
            paceString = paceText,
            workoutState = workoutState.code
        )
    }
}
