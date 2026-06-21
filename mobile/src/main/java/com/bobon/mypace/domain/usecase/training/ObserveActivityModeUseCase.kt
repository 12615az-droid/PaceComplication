package com.bobon.mypace.domain.usecase.training

import com.bobon.mypace.domain.training.TrainingStateReader

class ObserveActivityModeUseCase(
    private val   trainingStateReader: TrainingStateReader
) {
    operator fun invoke() =   trainingStateReader.activityMode
}