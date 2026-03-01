package com.example.pacecomplication


import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.example.pacecomplication.history.SessionIdGenerator
import com.example.pacecomplication.logger.AppEventData
import com.example.pacecomplication.logger.EventsLog
import com.example.pacecomplication.logger.SessionEventData
import com.example.pacecomplication.logger.SourceEvent
import com.example.pacecomplication.logger.TypeEvent
import com.example.pacecomplication.modes.RunningMode
import com.example.pacecomplication.modes.TrainingMode
import com.example.pacecomplication.modes.TrainingModes
import com.example.pacecomplication.pace.PaceCalculator
import com.example.pacecomplication.pace.PaceUpdate
import com.example.pacecomplication.pace.WearPaceSender
import com.example.pacecomplication.timer.PaceTimer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
enum class WorkoutState {
    IDLE, ACTIVE
}

private const val STOP_THRESHOLD = 0.5f
private const val ACC_BAD_THRESHOLD = 35f
private const val PACE_DEFAULT = "0:00"

class LocationRepository(
    private val paceTimer: PaceTimer = PaceTimer(),
    private val paceCalculator: PaceCalculator = PaceCalculator(
        stopThreshold = STOP_THRESHOLD, accBadThreshold = ACC_BAD_THRESHOLD
    ),
    private val wearPaceSender: WearPaceSender = WearPaceSender(),
    private val context: Context,
    private val eventsLog: EventsLog
) {

    private val TAG = "PACE_DEBUG"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Состояние репозитория (наблюдаемое UI через StateFlow):
    // - currentPace: строка темпа для экрана тренировки
    // - activityMode: текущий режим (WALKING/RUNNING)
    // - isTracking: флаг активного трекинга
    // - currentGPSAccuracy: текущая точность GPS (метры)
    private val _currentPace = MutableStateFlow(PACE_DEFAULT)
    val currentPace = _currentPace.asStateFlow()

    private val _isGoalSetupOpen = MutableStateFlow(false)
    val isGoalSetupOpen = _isGoalSetupOpen.asStateFlow()

    private val _activityMode = MutableStateFlow<TrainingMode>(RunningMode)
    val activityMode = _activityMode.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val _workoutState = MutableStateFlow(WorkoutState.IDLE)
    val workoutState = _workoutState.asStateFlow()

    private val _currentGPSAccuracy = MutableStateFlow(0f)
    val currentGPSAccuracy = _currentGPSAccuracy.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId = _currentSessionId.asStateFlow()


    val trainingTimeMs = paceTimer.trainingTimeMs


    // --- УПРАВЛЕНИЕ ---


    // Внутри LocationRepository

    fun startTracking() {

        // 1. Сразу меняем UI, не ждем сервиса
        forceStartState()

        // 2. Запускаем сервис
        sendStartCommand()
    }

    fun forceStartState() {
        // Убираем проверку "if (ACTIVE) return", она мешает, если сервис перезапускается
        if (_currentSessionId.value == null) {
            _currentSessionId.value = SessionIdGenerator.newId()
        }

        _isTracking.value = true
        _workoutState.value = WorkoutState.ACTIVE
        paceTimer.start()
        if (_workoutState.value == WorkoutState.IDLE) {
            paceCalculator.reset()
        }
        logStateEvent(
            type = TypeEvent.WORKOUT_STARTED,
            source = SourceEvent.SERVICE,
            origin = "LocationRepository.forceStartState"
        )
    }

    private fun logStateEvent(
        type: TypeEvent,
        source: SourceEvent,
        origin: String,
        note: String? = null
    ) {
        val sessionId = _currentSessionId.value
        val workoutStateName = _workoutState.value
        val isTrackingNow = _isTracking.value
        val activityModeLabel = _activityMode.value.label
        val paceTextNow = _currentPace.value
        val trainingTimeNow = trainingTimeMs.value
        val gpsAccuracyNow = _currentGPSAccuracy.value

        scope.launch {
            if (!sessionId.isNullOrBlank()) {
                // Тренировка активна -> session log, payload = session
                eventsLog.log(
                    type = type,
                    source = source,
                    origin = origin,
                    sessionId = sessionId,
                    data = SessionEventData(
                        workoutState = workoutStateName,
                        isTracking = isTrackingNow,
                        activityMode = activityModeLabel,
                        paceText = paceTextNow,
                        trainingTimeMs = trainingTimeNow,
                        gpsAccuracyM = gpsAccuracyNow,
                        note = note
                    )
                )
            } else {
                // Тренировки нет -> app log, payload = app
                eventsLog.log(
                    type = type,
                    source = source,
                    origin = origin,
                    sessionId = null,
                    data = AppEventData(
                        workoutState = workoutStateName,
                        note = note
                    )
                )
            }
        }
    }

    private fun sendStartCommand() {
        val intent = Intent(context, LocationService::class.java)
        intent.action = "START"

        context.startForegroundService(intent)
    }

// --- СТОП ---

    fun stopTracking() {

        // 1. Сразу гасим UI
        forceStopState()

        // 2. Шлем команду сервису
        sendStopCommand()
    }

    fun forceStopState() {
        _isTracking.value = false
        _currentGPSAccuracy.value = 0f
        paceTimer.stop()

        logStateEvent(
            type = TypeEvent.WORKOUT_STOPPED,
            source = SourceEvent.SERVICE,
            origin = "LocationRepository.forceStopState"
        )
    }

    private fun sendStopCommand() {
        val intent = Intent(context, LocationService::class.java)
        intent.action = "STOP"

        // ВАЖНО: Используем startService, чтобы доставить команду "STOP" в onStartCommand!
        // Если вызвать stopService, то onStartCommand НЕ СРАБОТАЕТ.
        context.startForegroundService(intent)
    }

    fun saveTracking() {

        // 1. УБИВАЕМ СЕРВИС (Первым делом!)
        // Шлем команду "STOP", чтобы он отписался от GPS и убрал уведомление.
        val intent = Intent(context, LocationService::class.java)
        intent.action = "KILL" // <--- НОВАЯ КОМАНДА
        // Используем startService для доставки команды (как мы обсуждали)
        context.startForegroundService(intent)

        // 2. ОСТАНАВЛИВАЕМ (но не стираем) таймер
        // Чтобы данные не менялись, пока мы "сохраняем"
        paceTimer.stop()
        _isTracking.value = false

        // --- (Здесь будет код сохранения в БД, когда ты его напишешь) ---
        // val finalDistance = paceCalculator.totalDistance
        // database.save(finalDistance, paceTimer.elapsedTime)

        // 3. ЗАЧИСТКА (Сброс в ноль)
        // Делаем это ПОСЛЕ того, как всё остановили
        paceCalculator.reset()
        paceTimer.reset()
        _currentGPSAccuracy.value = 0f

        // 4. ПЕРЕКЛЮЧАЕМ РЕЖИМ (Финал)
        // Только теперь говорим UI: "Всё, покажи стартовый экран"
        _workoutState.value = WorkoutState.IDLE
        _currentSessionId.value = null
        Log.d("ID", _currentSessionId.value.toString())

        logStateEvent(
            type = TypeEvent.SERVICE_STOPPED,
            source = SourceEvent.UI,
            origin = "LocationRepository.saveTracking",
            note = "Tracking saved and repository reset"
        )
    }


    fun setTrainingGoalDialogOpen(isOpen: Boolean) {
        _isGoalSetupOpen.value = isOpen


    }


    /**
     * Переключает режим активности (RUNNING <-> WALKING).
     *
     * Ограничение:
     * - переключение разрешено только когда трекинг НЕ идёт,
     *   чтобы не ломать фильтрацию во время активной записи.
     *
     * При смене режима:
     * - сбрасывается EMA-фильтр (сглаживание начинается заново)
     */
    fun changeMode() {
        if (_workoutState.value == WorkoutState.IDLE) {
            _activityMode.value = TrainingModes.next(_activityMode.value)
            paceCalculator.reset()
            Log.d(TAG, "Режим изменен на: ${_activityMode.value}")

        }
        logStateEvent(
            type = TypeEvent.MODE_CHANGED,
            source = SourceEvent.UI,
            origin = "LocationRepository.changeMode"
        )


    }


    /**
     * Принимает очередную Location и обновляет темп/точность.
     *
     * Алгоритм:
     * 1) если трекинг выключен — игнорируем входящие точки
     * 2) обновляем currentGPSAccuracy
     * 3) переводим Location.speed в мгновенный темп (сек/км) + фильтруем шум
     * 4) сглаживаем темп EMA-фильтром (коэффициент зависит от точности и режима)
     * 5) обновляем currentPace (строка) и отправляем темп на часы (Wear OS)
     *
     * @param location новая GPS-точка от FusedLocationProvider
     */
    fun updatePace(location: Location): PaceUpdate? {

        // Игнорируем точки, если запись не активна
        if (!_isTracking.value) return null

        _currentGPSAccuracy.value = location.accuracy

        val paceUpdate = paceCalculator.calculate(
            speedMetersPerSec = location.speed,
            accuracy = location.accuracy,
            maxSpeedMetersPerSec = _activityMode.value.maxSpeedMetersPerSec,
            alphaProvider = _activityMode.value::alphaForAccuracy
        ) ?: return null
        _currentPace.value = paceUpdate.paceText
        wearPaceSender.sendPace(paceUpdate.paceText)

        Log.d(TAG, "SPD: ${"%.2f".format(location.speed)} | PACE: ${paceUpdate.paceText}")

        // 1) для лога число (тут выбрал emaPace, но можешь вернуть instantPace)
        // 2) для уведомления строка
        return paceUpdate
    }
}


