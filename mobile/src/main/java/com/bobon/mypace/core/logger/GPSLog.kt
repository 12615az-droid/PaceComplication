package com.bobon.mypace.core.logger

import android.location.Location
import android.os.Build
import com.bobon.mypace.domain.model.WorkoutState
import com.bobon.mypace.domain.training.modes.TrainingMode
import com.bobon.mypace.domain.pace.PaceUpdate
import com.bobon.mypace.core.formatter.PaceFormatter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class RawGpsData(
    val accuracyM: Float,
    val speedMps: Float,
    val lat: Double,
    val lon: Double,
    val provider: String? = null,
    val isMock: Boolean? = null,
)

@Serializable
private data class ProcessedGpsData(
    val paceText: String? = null,
    val paceSecPerKm: Double? = null,
    val workoutState: WorkoutState,
    val totalDistance: Double,
    val mode: String,
)

@Serializable
private data class GpsLogEntry(
    val sessionId: String,
    val tNs: Long,
    val batchSize: Int,
    val batchIndex: Int,
    val raw: RawGpsData,
    val processed: ProcessedGpsData,
)

class GPSLog(
    private val files: LogFilesManager,
    private val writer: JsonlFileWriter,
    private val json: Json = Json { encodeDefaults = false; explicitNulls = false }
) {

    suspend fun logLocation(
        sessionId: String?,
        location: Location,
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
            raw = location.toRawGpsData(),
            processed = ProcessedGpsData(
                paceText = paceUpdate?.let { PaceFormatter.formatPace(it.secondsPerKm) },
                paceSecPerKm = paceUpdate?.secondsPerKm,
                workoutState = workoutState,
                totalDistance = totalDistance,
                mode = mode.label
            )
        )

        writer.appendLine(
            files.gpsLogFile(actualSessionId),
            json.encodeToString(entry)
        )
    }

    private fun Location.toRawGpsData(): RawGpsData = RawGpsData(
        accuracyM = accuracy,
        speedMps = speed,
        lat = latitude,
        lon = longitude,
        provider = provider,
        isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isMock else null,
    )
}
