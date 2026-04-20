package com.example.pacecomplication.logger


import com.example.pacecomplication.WorkoutState
import com.example.pacecomplication.logger.LogJson.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    APP_STARTED, SERVICE_STARTED, SERVICE_STOPPED, WORKOUT_STARTED, WORKOUT_STOPPED, MODE_CHANGED, FILTER_SELECTED, PERMISSION_RESULT, GPS_SIGNAL_CHANGED, ERROR, SCREEN_CHANGED,
}

@Serializable
enum class SourceEvent {
    UI, NOTIFICATION, SERVICE, SYSTEM, WEAR, UNKNOWN,
}


@Serializable
sealed interface EventPayload


@Serializable
@SerialName("app")
data class AppEventData(
    val screen: String? = null,
    val workoutState: WorkoutState? = null,
    val permission: String? = null,
    val granted: Boolean? = null,
    val errorMessage: String? = null,
    val errorStack: String? = null,
    val note: String? = null
) : EventPayload

@Serializable
@SerialName("session")
data class SessionEventData(
    val workoutState: WorkoutState,
    val activityMode: String,
    val paceText: String? = null,
    val trainingTimeMs: Long? = null,
    val gpsAccuracyM: Float? = null,
    val note: String? = null
) : EventPayload

@Serializable
data class EventLogEntry(
    val type: TypeEvent,
    val source: SourceEvent,
    val origin: String? = null,
    val tNs: Long,
    val sessionId: String? = null,
    val data: EventPayload? = null
)

class EventsLog(
    private val storage: StateLogStorage
) {
    private val dedupMutex = Mutex()
    private val dedupWindowNs = 1_500_000_000L
    private val lastEventByKey = mutableMapOf<String, Long>()
    suspend fun log(
        type: TypeEvent,
        source: SourceEvent,
        origin: String? = null,
        sessionId: String? = null,
        data: EventPayload? = null,
        tNs: Long = System.nanoTime()
    ) {
        val entry = EventLogEntry(
            type = type,
            source = source,
            origin = origin,
            tNs = tNs,
            sessionId = sessionId,
            data = data
        )
        val dedupKey = buildDedupKey(entry)
        val shouldSkip = dedupMutex.withLock {
            val nowNs = entry.tNs
            val lastNs = lastEventByKey[dedupKey]
            val isDuplicate = lastNs != null && (nowNs - lastNs) in 0..dedupWindowNs

            if (!isDuplicate) {
                lastEventByKey[dedupKey] = nowNs
            }

            isDuplicate
        }

        if (shouldSkip) return

        val jsonLine = toJsonLine(entry)
        storage.appendEvent(jsonLine, sessionId)
    }

    private fun buildDedupKey(entry: EventLogEntry): String {
        val dataJson =
            entry.data?.let { json.encodeToString(EventPayload.serializer(), it) } ?: "null"
        return "${entry.type}|${entry.source}|${entry.origin}|${entry.sessionId}|$dataJson"
    }
}