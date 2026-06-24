package com.bobon.mypace.domain.training

import com.bobon.mypace.domain.model.Workout
import com.bobon.mypace.domain.timer.ClockProvider
import com.bobon.mypace.domain.training.modes.TrainingMode

class TrainingSummaryFactory(
    private val clockProvider: ClockProvider
) {
    fun create(
        sessionId: String?,
        startTime: Long?,
        totalDistanceMeters: Double,
        trainingTimeMs: Long,
        activityMode: TrainingMode,
        note: String? = null
    ): Workout? {
        val id = sessionId ?: return null
        val start = startTime ?: return null
        val end = clockProvider.currentTimeMillis()
        val durationHours = trainingTimeMs / 3_600_000.0
        val avgSpeedMetersPerHour =
            if (durationHours > 0.0) {
                totalDistanceMeters / durationHours
            } else {
                0.0
            }

        return Workout(
            id = id,
            startTime = start,
            endTime = end,
            totalDistance = totalDistanceMeters,
            avgSpeed = avgSpeedMetersPerHour,
            caloriesBurned = 0,
            activityType = activityMode.id,
            note = note
        )
    }
}