package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.model.Workout
import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.domain.usecase.workout.SaveWorkoutUseCase

class SaveCurrentTrainingUseCase(
    private val trainingManager: TrainingManager,
    private val saveWorkout: SaveWorkoutUseCase,
    private val finishTraining: FinishTrainingUseCase
) {
    suspend operator fun invoke(): Boolean {
        val sessionId = trainingManager.currentSessionId.value ?: return false
        val start = trainingManager.startTime.value ?: return false
        val end = System.currentTimeMillis()
        val distance = trainingManager.totalDistance.value
        val timeMs = trainingManager.trainingTimeMs.value
        val mode = trainingManager.activityMode.value

        val avgSpeed = if (timeMs > 0) {
            distance / (timeMs / 3_600_000.0)
        } else {
            0.0
        }

        val workout = Workout(
            id = sessionId,
            startTime = start,
            endTime = end,
            totalDistance = distance,
            avgSpeed = avgSpeed,
            caloriesBurned = 0,
            activityType = mode.id,
            note = null
        )

        saveWorkout(workout)
        finishTraining()

        return true
    }
}