package com.bobon.mypace.device.location

import android.location.Location
import com.bobon.mypace.logger.GPSLog
import android.os.Build
import com.bobon.mypace.logger.GpsRawData
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
            rawGpsData = location.toGpsRawData(),
            paceUpdate = paceUpdate,
            workoutState = trainingStateReader.workoutState.value,
            totalDistance = trainingStateReader.totalDistance.value,
            mode =trainingStateReader.activityMode.value,
            batchSize = 1,
            batchIndex = 0
        )

        return LocationUpdateResult(
            paceSecondsPerKm = trainingStateReader.currentPaceSecondsPerKm.value,
            accuracyMeters = location.accuracy
        )


    }

    private fun Location.toGpsRawData(): GpsRawData =
        GpsRawData(
            accuracyM = accuracy,
            speedMps = speed,
            lat = latitude,
            lon = longitude,
            provider = provider,
            isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isMock else null
        )
}