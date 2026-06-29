package com.bobon.mypace.core.logger

import com.bobon.mypace.core.logger.LogJson.json
import kotlinx.serialization.Serializable

@Serializable
data class EventLogEntry(
    val type: TypeEvent,
    val source: SourceEvent,
    val origin: String? = null,
    val tNs: Long,
    val sessionId: String? = null,
    val data: EventPayload? = null
)

fun toJsonLine(entry: EventLogEntry): String =
    json.encodeToString(EventLogEntry.serializer(), entry)