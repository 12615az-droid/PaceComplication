package com.bobon.mypace.domain.usecase.logging


import com.bobon.mypace.domain.logging.TrainingEventLogger
import com.bobon.mypace.domain.training.TrainingStateReader

class LogScreenChangedUseCase(
    private val trainingEventLogger: TrainingEventLogger,
    private val trainingStateReader: TrainingStateReader
) {
    suspend operator fun invoke(screenName: String) {
        trainingEventLogger.logScreenChanged(
            screenName = screenName,
            sessionId = trainingStateReader.currentSessionId.value,
            workoutState = trainingStateReader.workoutState.value
        )
    }
}