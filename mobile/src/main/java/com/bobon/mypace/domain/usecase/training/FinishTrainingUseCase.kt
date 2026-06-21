package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.service.TrainingServiceController
import com.bobon.mypace.domain.training.TrainingCommandController

class FinishTrainingUseCase(
    private val  trainingCommandController: TrainingCommandController,
    private val trainingServiceController: TrainingServiceController
) {
    operator fun invoke() {
        trainingServiceController.killTrackingService()
        trainingCommandController.reset()
    }
}