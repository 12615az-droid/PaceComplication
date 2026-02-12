package com.example.pacecomplication


import android.content.Context
import android.location.Location
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.example.pacecomplication.modes.TrainingMode
import com.example.pacecomplication.modes.TrainingModes
import com.example.pacecomplication.modes.RunningMode
import com.example.pacecomplication.pace.PaceCalculator
import com.example.pacecomplication.pace.PaceUpdate
import com.example.pacecomplication.pace.WearPaceSender
import com.example.pacecomplication.timer.PaceTimer


enum class WorkoutState {
    IDLE, ACTIVE
}

/**
 * LocationRepository — центральное хранилище состояния трекинга и логики расчёта темпа.
 *
 * Назначение:
 * - хранит состояние для UI через StateFlow (темп, точность, режим, флаг трекинга)
 * - принимает Location из LocationService
 * - фильтрует шумные значения GPS и скорости
 * - считает темп (сек/км) и сглаживает его EMA-фильтром
 * - отправляет итоговый темп на Wear OS через DataClient (если инициализирован)
 *
 * Использование:
 * 1) init(context) — один раз при старте (обычно в Service)
 * 2) startTracking()/stopTracking() — управление трекингом
 * 3) updatePace(location) — вызывается на каждом обновлении координат
 *
 * Важно:
 * - объект singleton: живёт в процессе приложения
 * - UI подписывается на currentPace/activityMode/isTracking/currentGPSAccuracy
 */
object LocationRepository {

    private const val TAG = "PACE_DEBUG"

    // Пороги (теперь это константы, а не переменные)
    // STOP_THRESHOLD — ниже этой скорости считаем, что стоим
    // ACC_BAD_THRESHOLD — точность хуже этого значения считаем шумом (значение отбрасываем)
    // PACE_DEFAULT — значение темпа при отсутствии данных/сигнала
    private const val STOP_THRESHOLD = 0.5f
    private const val ACC_BAD_THRESHOLD = 35f
    private const val PACE_DEFAULT = "0:00"

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


     private val paceTimer = PaceTimer()

    val trainingTimeMs = paceTimer.trainingTimeMs

    private val paceCalculator = PaceCalculator(
        stopThreshold = STOP_THRESHOLD, accBadThreshold = ACC_BAD_THRESHOLD
    )


    private val wearPaceSender = WearPaceSender()

    // --- УПРАВЛЕНИЕ ---

    /**
     * Инициализация репозитория.
     *
     * Нужна для подключения Wear OS DataClient.
     * Важно использовать applicationContext, чтобы избежать утечек памяти.
     *
     * Вызывать один раз при старте сервиса/приложения.
     *
     * @param context любой Context, внутри будет взят applicationContext
     */
    fun init(context: Context) {
        // Используем applicationContext, чтобы не было утечек памяти
        wearPaceSender.init(context)
    }

    /**
     * Включает режим трекинга.
     *
     * Что делает:
     * - ставит isTracking = true
     * - сбрасывает EMA-фильтр, чтобы новый трек начинался "с нуля"
     */
    fun startTracking() {
        _isTracking.value = true
        _workoutState.value = WorkoutState.ACTIVE
        paceTimer.start()
        paceCalculator.reset()
    }

    /**
     * Останавливает трекинг.
     *
     * Что делает:
     * - ставит isTracking = false
     * - сбрасывает текущую точность GPS в 0 (нет сигнала/неактивно)
     */
    fun stopTracking() {
        _isTracking.value = false
        _currentGPSAccuracy.value = 0f
        paceTimer.stop()
    }

    fun saveTracking() {
        stopTracking()
         paceTimer.reset()
        _workoutState.value = WorkoutState.IDLE

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


