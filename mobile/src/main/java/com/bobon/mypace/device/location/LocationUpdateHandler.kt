package com.bobon.mypace.device.location

import android.location.Location
import com.bobon.mypace.core.logger.GPSLog

import com.bobon.mypace.domain.training.TrainingMetricsUpdater
import com.bobon.mypace.domain.training.TrainingStateReader


class LocationUpdateHandler(
    private val trainingMetricsUpdater: TrainingMetricsUpdater,
    private val trainingStateReader: TrainingStateReader,
    private val gpsLog: GPSLog
) {
    suspend fun handle(location: Location): LocationUpdateResult {
        val gpsPoint = location.toGpsPoint()

        val paceUpdate = trainingMetricsUpdater.updatePace(gpsPoint)

        gpsLog.logLocation(
            sessionId = trainingStateReader.currentSessionId.value,
            location = location,
            paceUpdate = paceUpdate,
            workoutState = trainingStateReader.workoutState.value,
            totalDistance = trainingStateReader.totalDistance.value,
            mode =trainingStateReader.activityMode.value,
            batchSize = 1,
            batchIndex = 0
        )

        return LocationUpdateResult(
            paceText = trainingStateReader.currentPace.value,
            accuracyMeters = location.accuracy
        )
    }
}