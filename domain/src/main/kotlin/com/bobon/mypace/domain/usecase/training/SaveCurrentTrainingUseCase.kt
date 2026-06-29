package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.training.TrainingStateReader
import com.bobon.mypace.domain.training.TrainingSummaryFactory
import com.bobon.mypace.domain.usecase.workout.SaveWorkoutUseCase

class SaveCurrentTrainingUseCase(
    private val trainingStateReader: TrainingStateReader,
    private val trainingSummaryFactory: TrainingSummaryFactory,
    private val saveWorkout: SaveWorkoutUseCase,
    private val finishTraining: FinishTrainingUseCase
) {
    suspend operator fun invoke(): Boolean {
        val workout = trainingSummaryFactory.create(
            sessionId = trainingStateReader.currentSessionId.value,
            startTime = trainingStateReader.startTime.value,
            totalDistanceMeters = trainingStateReader.totalDistance.value,
            trainingTimeMs = trainingStateReader.trainingTimeMs.value,
            activityMode = trainingStateReader.activityMode.value
        ) ?: return false

        saveWorkout(workout)
        finishTraining()

        return true
    }
}
