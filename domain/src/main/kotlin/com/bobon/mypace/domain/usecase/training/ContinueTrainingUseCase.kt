package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.training.TrainingStateReader


class ContinueTrainingUseCase(
    private val trainingStateReader: TrainingStateReader,
    private val startTraining: StartTrainingUseCase
) {
    operator fun invoke() {
        val sessionId = trainingStateReader.currentSessionId.value ?: return
        startTraining(sessionId)
    }
}