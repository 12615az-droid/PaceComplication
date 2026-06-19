package com.bobon.mypace.domain.usecase.logging

import com.bobon.mypace.domain.training.TrainingManager
import com.bobon.mypace.core.logger.AppEventData
import com.bobon.mypace.core.logger.EventsLog
import com.bobon.mypace.core.logger.SourceEvent
import com.bobon.mypace.core.logger.TypeEvent

class LogScreenChangedUseCase(
    private val eventsLog: EventsLog,
    private val trainingManager: TrainingManager
) {
    suspend operator fun invoke(screenName: String) {
        eventsLog.log(
            type = TypeEvent.SCREEN_CHANGED,
            source = SourceEvent.UI,
            origin = "UI.Navigation",
            sessionId = null,
            data = AppEventData(
                screen = screenName,
                workoutState = trainingManager.workoutState.value,
                note = "Screen changed"
            )
        )
    }
}