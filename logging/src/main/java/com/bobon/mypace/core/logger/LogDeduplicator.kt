package com.bobon.mypace.core.logger

import com.bobon.mypace.core.logger.LogJson.json
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LogDeduplicator(
    private val windowNs: Long = 1_500_000_000L,
    private val maxKeys: Int = 1_000
) {
    private val mutex = Mutex()
    private val lastEventByKey = mutableMapOf<String, Long>()

    suspend fun shouldSkip(entry: EventLogEntry): Boolean {
        val key = buildDedupKey(entry)

        return mutex.withLock {
            val nowNs = entry.tNs
            val lastNs = lastEventByKey[key]
            val isDuplicate = lastNs != null && (nowNs - lastNs) in 0..windowNs

            if (!isDuplicate) {
                lastEventByKey[key] = nowNs
            }

            if (lastEventByKey.size > maxKeys) {
                val cutoffNs = nowNs - windowNs
                lastEventByKey.entries.removeAll { it.value < cutoffNs }
            }

            isDuplicate
        }
    }

    private fun buildDedupKey(entry: EventLogEntry): String {
        val dataJson =
            entry.data?.let { json.encodeToString(EventPayload.serializer(), it) } ?: "null"

        return "${entry.type}|${entry.source}|${entry.origin}|${entry.sessionId}|$dataJson"
    }
}