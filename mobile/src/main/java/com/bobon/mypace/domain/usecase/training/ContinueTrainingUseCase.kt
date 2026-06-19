package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.training.TrainingManager

class ContinueTrainingUseCase(
    private val trainingManager: TrainingManager,
    private val startTraining: StartTrainingUseCase
) {
    operator fun invoke() {
        val sessionId = trainingManager.currentSessionId.value ?: return
        startTraining(sessionId)
    }
}