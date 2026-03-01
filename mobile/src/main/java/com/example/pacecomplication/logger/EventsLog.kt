package com.example.pacecomplication.logger

import com.example.pacecomplication.logger.LogJson.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object LogJson {
    val json = Json {
        encodeDefaults = false
        explicitNulls = false
        ignoreUnknownKeys = true
        classDiscriminator = "kind" // в JSON появится "kind": "app" или "session"
    }
}


fun toJsonLine(entry: EventLogEntry): String =
    json.encodeToString(EventLogEntry.serializer(), entry)

@Serializable
enum class TypeEvent {
    APP_STARTED,
    SERVICE_STARTED,
    SERVICE_STOPPED,
    WORKOUT_STARTED,
    WORKOUT_STOPPED,
    MODE_CHANGED,
    FILTER_SELECTED,
    PERMISSION_RESULT,
    GPS_SIGNAL_CHANGED,
    ERROR,
}

@Serializable
enum class SourceEvent {
    UI,
    NOTIFICATION,
    SERVICE,
    SYSTEM,
    WEAR,
    UNKNOWN,
}

@Serializable
sealed interface EventData {
    val tNs: Long
    val sessionId: String?
}

@Serializable
@SerialName("app")
data class AppEventData(
    override val tNs: Long,
    override val sessionId: String? = null,

    val screen: String? = null,
    val workoutState: String? = null,   // <-- было WorkoutState?

    val permission: String? = null,
    val granted: Boolean? = null,

    val errorMessage: String? = null,
    val errorStack: String? = null,

    val note: String? = null
) : EventData

@Serializable
@SerialName("session")
data class SessionEventData(
    override val tNs: Long,
    override val sessionId: String,

    val workoutState: String,           // <-- было WorkoutState
    val isTracking: Boolean,
    val activityMode: String,           // <-- было TrainingMode

    val paceText: String? = null,
    val trainingTimeMs: Long? = null,
    val gpsAccuracyM: Float? = null,

    val note: String? = null
) : EventData

@Serializable
data class EventLogEntry(
    val type: TypeEvent,
    val source: SourceEvent,
    val origin: String? = null,

    // дублируем в “шапке” для удобства фильтрации без парсинга data
    val tNs: Long,
    val sessionId: String? = null,

    val data: EventData? = null,
)

class EventsLog(
    private val storage: StateLogStorage
) {
    /**
     * Единая точка записи event log.
     * sessionId берём из data (если есть) или можно передать отдельно при желании.
     */
    suspend fun log(
        type: TypeEvent,
        source: SourceEvent,
        origin: String? = null,
        data: EventData? = null
    ) {
        val tNs = data?.tNs ?: System.nanoTime()
        val sessionId = data?.sessionId

        val entry = EventLogEntry(
            type = type,
            source = source,
            origin = origin,
            tNs = tNs,
            sessionId = sessionId,
            data = data
        )

        val jsonLine = toJsonLine(entry)
        storage.appendEvent(jsonLine, sessionId)
    }
}