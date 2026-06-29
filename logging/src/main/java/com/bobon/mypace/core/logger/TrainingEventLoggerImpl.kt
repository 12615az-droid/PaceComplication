package com.bobon.mypace.core.logger


import com.bobon.mypace.domain.logging.TrainingEventLogger
import com.bobon.mypace.domain.model.WorkoutState

class TrainingEventLoggerImpl(
    private val eventsLog: EventsLog
) : TrainingEventLogger {

    override suspend fun logScreenChanged(
        screenName: String,
        sessionId: String?,
        workoutState: WorkoutState
    ) {
        eventsLog.log(
            type = TypeEvent.SCREEN_CHANGED,
            source = SourceEvent.UI,
            origin = "UI.Navigation",
            sessionId = sessionId,
            data = AppEventData(
                screen = screenName,
                workoutState = workoutState,
                note = "Screen changed"
            )
        )
    }
}