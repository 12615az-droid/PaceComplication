package com.example.pacecomplication.Logger

import com.example.pacecomplication.WorkoutState
import com.example.pacecomplication.modes.TrainingMode
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

    // optional context
    val screen: String? = null,
    val workoutState: WorkoutState? = null,

    // permissions
    val permission: String? = null,
    val granted: Boolean? = null,

    // errors
    val errorMessage: String? = null,
    val errorStack: String? = null,

    // any extra, compact info
    val note: String? = null
) : EventData

@Serializable
@SerialName("session")
data class SessionEventData(
    override val tNs: Long,
    override val sessionId: String,

    val workoutState: WorkoutState,
    val isTracking: Boolean,
    val activityMode: TrainingMode,

    // UI snapshot (event-level)
    val paceText: String? = null,        // строка как у тебя сейчас
    val trainingTimeMs: Long? = null,    // _trainingTimeMs.value на момент события

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

class EventsLog {

    fun log(type: TypeEvent, source: SourceEvent, origin: String, data: EventData) {

    }


}