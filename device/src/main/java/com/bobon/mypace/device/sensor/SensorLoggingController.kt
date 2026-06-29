package com.bobon.mypace.device.sensor


import com.bobon.mypace.logger.SensorLog
import com.bobon.mypace.domain.training.TrainingStateReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

class SensorLoggingController(
    private val sensorTracker: SensorTracker,
    private val sensorLog: SensorLog,
    private val  trainingStateReader: TrainingStateReader
) {
    private var sensorLogJob: Job? = null

    fun start(scope: CoroutineScope) {
        sensorTracker.startTracking()

        sensorLogJob?.cancel()
        sensorLogJob = scope.launch {
            sensorTracker.sensorDataFlow.collect { sample ->
                sensorLog.logSample(
                    sessionId = trainingStateReader.currentSessionId.value,
                    sensorData = sample
                )
            }
        }
    }

    fun stop(scope: CoroutineScope) {
        sensorTracker.stopTracking()

        val job = sensorLogJob ?: return
        sensorLogJob = null

        scope.launch {
            job.cancelAndJoin()
            sensorLog.flush()
        }
    }
}