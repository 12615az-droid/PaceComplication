package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.service.TrainingServiceController
import com.bobon.mypace.domain.training.TrainingCommandController

class StartTrainingUseCase(
    private val trainingCommandController: TrainingCommandController,
    private val trainingServiceController: TrainingServiceController
){
    operator fun invoke(sessionId: String) {
        trainingCommandController.start(sessionId)
        trainingServiceController.startTrackingService()
    }
}