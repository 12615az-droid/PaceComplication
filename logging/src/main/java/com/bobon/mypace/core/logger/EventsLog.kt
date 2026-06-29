package com.bobon.mypace.core.logger

class EventsLog(
    private val storage: StateLogStorage,
    private val deduplicator: LogDeduplicator = LogDeduplicator()
) {
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

        if (deduplicator.shouldSkip(entry)) return

        storage.appendEvent(
            jsonLine = toJsonLine(entry),
            sessionId = sessionId
        )
    }
}