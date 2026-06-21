package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.training.TrainingStateReader


class ObserveWorkoutStateUseCase(
    private val trainingStateReader: TrainingStateReader
) {
    operator fun invoke() = trainingStateReader.workoutState
}