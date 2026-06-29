package com.bobon.mypace.logger

import com.bobon.mypace.core.sensor.SensorData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class SensorLogEntry(
    val sessionId: String,
    val tNs: Long,
    val sensorTimestampNs: Long,
    val sensorType: Int,
    val sensorTypeName: String,
    val values: List<Float>,
)

class SensorLog(
    private val files: LogFilesManager,
    private val writer: JsonlFileWriter,
    private val json: Json = Json { encodeDefaults = false; explicitNulls = false },
    private val flushBatchSize: Int = 50,
) {
    private val mutex = Mutex()
    private val buffersBySession = mutableMapOf<String, MutableList<String>>()

    suspend fun logSample(
        sessionId: String?,
        sensorData: SensorData,
    ) {
        val actualSessionId = sessionId ?: return
        val encoded = json.encodeToString(
            SensorLogEntry(
                sessionId = actualSessionId,
                tNs = System.nanoTime(),
                sensorTimestampNs = sensorData.timestamp,
                sensorType = sensorData.type,
                sensorTypeName = sensorData.typeName,
                values = sensorData.values.toList(),
            )
        )

        val linesToFlush: List<String>? = mutex.withLock {
            val buffer = buffersBySession.getOrPut(actualSessionId) { mutableListOf() }
            buffer.add(encoded)

            if (buffer.size >= flushBatchSize) {
                val lines = buffer.toList()
                buffer.clear()
                lines
            } else {
                null
            }
        }

        if (!linesToFlush.isNullOrEmpty()) {
            writer.appendLines(files.sensorLogFile(actualSessionId), linesToFlush)
        }
    }



    suspend fun flush() {
        val pending: Map<String, List<String>> = mutex.withLock {
            val snapshot = buffersBySession
                .filterValues { it.isNotEmpty() }
                .mapValues { (_, lines) -> lines.toList() }

            buffersBySession.values.forEach { it.clear() }
            snapshot
        }

        pending.forEach { (sessionId, lines) ->
            writer.appendLines(files.sensorLogFile(sessionId), lines)
        }
    }
}