package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.distance.DistanceCalculator
import com.bobon.mypace.domain.model.GpsPoint
import com.bobon.mypace.domain.pace.PaceCalculator
import com.bobon.mypace.domain.training.modes.TrainingMode

class TrainingMetricsProcessor(
    private val paceCalculator: PaceCalculator,
    private val distanceCalculator: DistanceCalculator
) {
    private var lastGpsPoint: GpsPoint? = null

    fun process(
        point: GpsPoint,
        activityMode: TrainingMode
    ): TrainingMetricsUpdate? {
        val paceUpdate = paceCalculator.calculate(
            speedMetersPerSec = point.speedMetersPerSecond,
            accuracy = point.accuracyMeters,
            maxSpeedMetersPerSec = activityMode.maxSpeedMetersPerSec,
            alphaProvider = activityMode::alphaForAccuracy
        ) ?: return null

        val distanceDelta = calculateDistanceDelta(point)

        return TrainingMetricsUpdate(
            paceUpdate = paceUpdate,
            gpsAccuracyMeters = point.accuracyMeters,
            distanceDeltaMeters = distanceDelta
        )
    }

    fun reset() {
        lastGpsPoint = null
        paceCalculator.reset()
    }

    fun resetPaceOnly() {
        paceCalculator.reset()
    }

    private fun calculateDistanceDelta(point: GpsPoint): Double {
        val previousPoint = lastGpsPoint

        val deltaMeters = if (previousPoint != null) {
            distanceCalculator.distanceBetween(
                from = previousPoint,
                to = point
            )
        } else {
            0.0
        }

        lastGpsPoint = point

        return deltaMeters
    }
}