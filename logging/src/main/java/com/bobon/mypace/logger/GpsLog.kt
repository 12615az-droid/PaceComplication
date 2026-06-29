package com.bobon.mypace.logger

import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.modes.TrainingMode
import com.bobon.mypace.domain.pace.PaceUpdate
import com.bobon.mypace.core.formatter.PaceFormatter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
private data class ProcessedGpsData(
    val paceText: String? = null,
    val paceSecPerKm: Double? = null,
    @SerialName("workoutStateCode")
    val workoutStateCode: Int,
    val totalDistance: Double,
    val mode: String,
)

@Serializable
private data class GpsLogEntry(
    val sessionId: String,
    val tNs: Long,
    val batchSize: Int,
    val batchIndex: Int,
    val raw: GpsRawData,
    val processed: ProcessedGpsData,
)

class GPSLog(
    private val files: LogFilesManager,
    private val writer: JsonlFileWriter,
    private val json: Json = Json { encodeDefaults = false; explicitNulls = false }
) {

    suspend fun logLocation(
        sessionId: String?,
        rawGpsData: GpsRawData,
        paceUpdate: PaceUpdate?,
        workoutState: WorkoutState,
        totalDistance: Double,
        mode: TrainingMode,
        batchSize: Int,
        batchIndex: Int,
    ) {
        val actualSessionId = sessionId ?: return
        val entry = GpsLogEntry(
            sessionId = actualSessionId,
            tNs = System.nanoTime(),
            batchSize = batchSize,
            batchIndex = batchIndex,
            raw = rawGpsData,
            processed = ProcessedGpsData(
                paceText = paceUpdate?.let { PaceFormatter.formatPace(it.secondsPerKm) },
                paceSecPerKm = paceUpdate?.secondsPerKm,
                workoutStateCode = workoutState.code,
                totalDistance = totalDistance,
                mode = mode.label
            )
        )

        writer.appendLine(
            files.gpsLogFile(actualSessionId),
            json.encodeToString(entry)
        )
    }

}
