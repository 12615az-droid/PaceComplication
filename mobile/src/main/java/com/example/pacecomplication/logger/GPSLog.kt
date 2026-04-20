import android.location.Location
import android.os.Build
import com.example.pacecomplication.WorkoutState
import com.example.pacecomplication.logger.JsonlFileWriter
import com.example.pacecomplication.logger.LogFilesManager
import com.example.pacecomplication.modes.TrainingMode
import com.example.pacecomplication.pace.PaceUpdate
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
                paceText = paceUpdate?.paceText,
                paceSecPerKm = paceUpdate?.paceValue,
                workoutState = workoutState,
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