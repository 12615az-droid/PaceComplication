package com.bobon.mypace.domain.usecase.logging

import com.bobon.mypace.core.logger.AppEventData
import com.bobon.mypace.core.logger.EventsLog
import com.bobon.mypace.core.logger.SourceEvent
import com.bobon.mypace.core.logger.TypeEvent
import com.bobon.mypace.domain.training.TrainingStateReader

class LogScreenChangedUseCase(
    private val eventsLog: EventsLog,
    private val trainingStateReader: TrainingStateReader
) {
    suspend operator fun invoke(screenName: String) {
        eventsLog.log(
            type = TypeEvent.SCREEN_CHANGED,
            source = SourceEvent.UI,
            origin = "UI.Navigation",
            sessionId = trainingStateReader.currentSessionId.value,
            data = AppEventData(
                screen = screenName,
                workoutState = trainingStateReader.workoutState.value,
                note = "Screen changed"
            )
        )
    }
}
