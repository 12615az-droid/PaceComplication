package com.bobon.mypace.logger

import com.bobon.mypace.logger.LogJson.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

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
    json.encodeToString(entry)