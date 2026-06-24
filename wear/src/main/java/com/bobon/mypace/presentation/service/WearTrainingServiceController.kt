package com.bobon.mypace.presentation.service

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bobon.mypace.presentation.TrainingWearService
import com.bobon.mypace.presentation.model.WearWorkoutState

object WearTrainingServiceController {

    private const val TAG = "WearServiceController"

    fun syncWithWorkoutState(
        context: Context,
        oldState: WearWorkoutState,
        newState: WearWorkoutState
    ) {
        if (oldState == newState) return

        when (newState) {
            WearWorkoutState.Active -> {
                sendAction(context, TrainingWearService.ACTION_START)
            }

            WearWorkoutState.Paused -> {
                sendAction(context, TrainingWearService.ACTION_STOP)
            }

            WearWorkoutState.Idle,
            WearWorkoutState.Finished -> {
                sendAction(context, TrainingWearService.ACTION_KILL)
            }
        }
    }

    fun kill(context: Context) {
        sendAction(context, TrainingWearService.ACTION_KILL)
    }

    private fun sendAction(
        context: Context,
        action: String
    ) {
        val appContext = context.applicationContext

        val intent = Intent(appContext, TrainingWearService::class.java).apply {
            this.action = action
        }

        try {
            if (action == TrainingWearService.ACTION_START) {
                appContext.startForegroundService(intent)
            } else {
                appContext.startService(intent)
            }

            Log.d(TAG, "Sent action to service: $action")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending action to service: $action", e)
        }
    }
}