package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.modes.TrainingModes
import com.bobon.mypace.domain.pace.PaceUpdate
import com.bobon.mypace.domain.timer.PaceTimer
import com.bobon.mypace.domain.model.GpsPoint
import com.bobon.mypace.domain.timer.ClockProvider
/**
 * Менеджер активной тренировки. Хранит состояние в памяти.
 */
class TrainingManager(
    private val stateHolder: TrainingStateHolder,
    private val paceTimer: PaceTimer,
    private val metricsProcessor: TrainingMetricsProcessor,
    private val trainingSyncSender: TrainingSyncSender,
    private val clockProvider: ClockProvider
) : ActiveTrainingSession  {
    override val currentPaceSecondsPerKm = stateHolder.currentPaceSecondsPerKm
    override val workoutState = stateHolder.workoutState
    override val activityMode = stateHolder.activityMode
    override val totalDistance = stateHolder.totalDistance
    override val currentGPSAccuracy = stateHolder.currentGPSAccuracy
    override val currentSessionId = stateHolder.currentSessionId
    override val startTime = stateHolder.startTime

    override val trainingTimeMs = paceTimer.trainingTimeMs

    override fun start(sessionId: String) {
        stateHolder.setSessionIdIfEmpty(sessionId)
        stateHolder.setStartTimeIfEmpty(clockProvider.currentTimeMillis())
        stateHolder.setWorkoutState(WorkoutState.ACTIVE)

        paceTimer.start()
        syncTrainingState()
    }
    override fun pause() {
        stateHolder.setWorkoutState(WorkoutState.PAUSED)
        paceTimer.stop()
        syncTrainingState()
    }

    override fun reset() {
        stateHolder.reset()

        paceTimer.reset()
        metricsProcessor.reset()
        syncTrainingState()
    }

    override  fun updatePace(point: GpsPoint): PaceUpdate? {
        val update = metricsProcessor.process(
            point = point,
            activityMode = activityMode.value
        ) ?: return null

        stateHolder.setGpsAccuracy(update.gpsAccuracyMeters)
        stateHolder.setCurrentPaceSecondsPerKm(
            update.paceUpdate.secondsPerKm
        )
        stateHolder.addDistance(update.distanceDeltaMeters)

        syncTrainingState()

        return update.paceUpdate
    }


    override  fun changeMode() {
        if (workoutState.value == WorkoutState.IDLE) {
            stateHolder.setActivityMode(
                TrainingModes.next(activityMode.value)
            )
            metricsProcessor.resetPaceOnly()
        }
    }

    private fun syncTrainingState() {
        trainingSyncSender.sendWorkoutUpdate(
            paceSecondsPerKm = currentPaceSecondsPerKm.value,
            workoutState = workoutState.value
        )
    }
}