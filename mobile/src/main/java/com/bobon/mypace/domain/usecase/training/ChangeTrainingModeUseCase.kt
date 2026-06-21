package com.bobon.mypace.domain.usecase.training


import com.bobon.mypace.domain.training.TrainingCommandController


class ChangeTrainingModeUseCase(
    private val trainingCommandController: TrainingCommandController
) {
    operator fun invoke() {
        trainingCommandController.changeMode()
    }
}