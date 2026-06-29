package com.bobon.mypace.device.location.serviceLocation

import com.bobon.mypace.core.logger.AppEventData
import com.bobon.mypace.core.logger.EventsLog
import com.bobon.mypace.core.logger.SourceEvent
import com.bobon.mypace.core.logger.TypeEvent
import com.bobon.mypace.domain.training.TrainingStateReader


class LocationServiceEventLogger(
    private val eventsLog: EventsLog,
    private val trainingStateReader: TrainingStateReader
) {
    suspend fun logServiceStarted() {
        logServiceEvent(
            type = TypeEvent.SERVICE_STARTED,
            origin = "LocationService.onStartCommand.START",
            note = "Foreground location service started"
        )
    }

    suspend fun logServicePaused() {
        logServiceEvent(
            type = TypeEvent.SERVICE_STOPPED,
            origin = "LocationService.onStartCommand.STOP",
            note = "Foreground location service paused"
        )
    }

    suspend fun logServiceKilled() {
        logServiceEvent(
            type = TypeEvent.SERVICE_STOPPED,
            origin = "LocationService.onStartCommand.KILL",
            note = "Foreground location service killed"
        )
    }

    suspend fun logUnknownAction(action: String?) {
        logServiceEvent(
            type = TypeEvent.ERROR,
            origin = "LocationService.onStartCommand.unknown",
            note = "Unknown action: $action"
        )
    }

    private suspend fun logServiceEvent(
        type: TypeEvent,
        origin: String,
        note: String
    ) {
        eventsLog.log(
            type = type,
            source = SourceEvent.SERVICE,
            origin = origin,
            sessionId = null,
            data = AppEventData(
                workoutState = trainingStateReader.workoutState.value,
                note = note
            )
        )
    }
}