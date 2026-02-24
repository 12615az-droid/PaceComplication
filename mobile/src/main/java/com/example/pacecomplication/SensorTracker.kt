import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Класс для хранения данных (будущий кусок пакета)
data class SensorData(
    val type: Int,
    val values: FloatArray,
    val timestamp: Long
)

class SensorTracker(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val linearAccel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
    private val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // SharedFlow с буфером, чтобы не терять данные при записи пакетов
    private val _sensorDataFlow = MutableSharedFlow<SensorData>(extraBufferCapacity = 100)
    val sensorDataFlow = _sensorDataFlow.asSharedFlow()

    fun startTracking() {
        linearAccel?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
        gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    fun stopTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            // Клонируем values, иначе Android переиспользует массив, и в логах будет мусор
            val data = SensorData(it.sensor.type, it.values.clone(), it.timestamp)
            _sensorDataFlow.tryEmit(data)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Для гироскопа и акселерометра обычно не нужно
    }
}