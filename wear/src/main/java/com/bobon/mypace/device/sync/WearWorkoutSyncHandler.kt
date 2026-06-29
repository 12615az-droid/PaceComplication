package com.bobon.mypace.device.sync

import android.content.Context
import android.util.Log
import com.bobon.mypace.presentation.PaceRepository
import com.bobon.mypace.domain.model.WearTrainingState
import com.bobon.mypace.domain.model.WearWorkoutState
import com.bobon.mypace.device.service.WearTrainingServiceController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object WearWorkoutSyncHandler {

    private const val TAG = "WearWorkoutSyncHandler"
    private const val TIMEOUT_MILLIS = 15_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var watchdogJob: Job? = null

    fun handleWorkoutUpdate(
        context: Context,
        paceText: String,
        workoutStateCode: Int
    ) {
        val oldState = PaceRepository.state.value.workoutState
        val newState = WearWorkoutState.fromCode(workoutStateCode)

        PaceRepository.updateState(
            WearTrainingState(
                paceText = paceText,
                workoutState = newState
            )
        )

        WearTrainingServiceController.syncWithWorkoutState(
            context = context,
            oldState = oldState,
            newState = newState
        )

        if (newState == WearWorkoutState.Active) {
            resetWatchdog(context.applicationContext)
        } else {
            stopWatchdog()
        }
    }

    fun handleDisconnect(context: Context) {
        Log.e(TAG, "Связь с телефоном потеряна")

        PaceRepository.updateState(
            WearTrainingState(
                paceText = "0:00",
                workoutState = WearWorkoutState.Idle
            )
        )

        stopWatchdog()
        WearTrainingServiceController.kill(context)
    }

    private fun resetWatchdog(context: Context) {
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            delay(TIMEOUT_MILLIS)
            Log.e(TAG, "Таймаут 15 сек. Телефон пропал!")
            handleDisconnect(context)
        }
    }

    private fun stopWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = null
    }
}