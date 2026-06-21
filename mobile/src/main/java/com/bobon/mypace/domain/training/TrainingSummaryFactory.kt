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

        val avgSpeed = if (trainingTimeMs > 0) {
            totalDistanceMeters / (trainingTimeMs / 3_600_000.0)
        } else {
            0.0
        }

        return Workout(
            id = id,
            startTime = start,
            endTime = end,
            totalDistance = totalDistanceMeters,
            avgSpeed = avgSpeed,
            caloriesBurned = 0,
            activityType = activityMode.id,
            note = note
        )
    }
}