package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.training.TrainingManager

class ChangeTrainingModeUseCase(
    private val trainingManager: TrainingManager
) {
    operator fun invoke() {
        trainingManager.changeMode()
    }
}