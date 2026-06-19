package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.service.TrainingServiceController
import com.bobon.mypace.domain.training.TrainingManager

class FinishTrainingUseCase(
    private val trainingManager: TrainingManager,
    private val trainingServiceController: TrainingServiceController
) {
    operator fun invoke() {
        trainingServiceController.killTrackingService()
        trainingManager.reset()
    }
}