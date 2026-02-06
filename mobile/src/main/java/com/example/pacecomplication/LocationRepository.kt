package com.example.pacecomplication

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.DataClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


import com.example.pacecomplication.modes.TrainingMode
import com.example.pacecomplication.modes.TrainingModes
import com.example.pacecomplication.modes.RunningMode

data class PaceUpdate(
    val paceValue: Double,   // для лога (например instant или ema)
    val paceText: String     // для уведомления
)

enum class WorkoutState {
    IDLE,
    ACTIVE
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

    private val _activityMode = MutableStateFlow<TrainingMode>(RunningMode)
    val activityMode = _activityMode.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    private val _workoutState = MutableStateFlow(WorkoutState.IDLE)
    val workoutState = _workoutState.asStateFlow()

    private val _currentGPSAccuracy = MutableStateFlow(0f)
    val currentGPSAccuracy = _currentGPSAccuracy.asStateFlow()


    val MyTimer = PaceTimer()

    val trainingTimeMs = MyTimer.trainingTimeMs

    private var emaPace: Double = 0.0


    // Клиент Wear OS (инициализируем лениво, чтобы не передавать контекст в каждый метод)
    @SuppressLint("StaticFieldLeak")
    private var dataClient: DataClient? = null

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
        dataClient = Wearable.getDataClient(context.applicationContext)
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
        MyTimer.start()
        emaPace = 0.0
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
        MyTimer.stop()
    }

    fun saveTracking() {
        stopTracking()
        MyTimer.reset()
        _workoutState.value = WorkoutState.IDLE

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
    fun changeMod() {
        if (_workoutState.value == WorkoutState.IDLE) {
            _activityMode.value = TrainingModes.next(_activityMode.value)
            emaPace = 0.0
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

        val instantPace = processLocationToPace(location) ?: return null

        emaPace = applyEmaFilter(instantPace, location.accuracy)
        val paceString = formatPace(emaPace)

        _currentPace.value = paceString
        sendPaceToWatch(paceString)




        Log.d(TAG, "SPD: ${"%.2f".format(location.speed)} | PACE: $paceString")

        // 1) для лога число (тут выбрал emaPace, но можешь вернуть instantPace)
        // 2) для уведомления строка
        return PaceUpdate(
            paceValue = emaPace,
            paceText = paceString
        )
    }


    /**
     * Преобразует скорость из Location в мгновенный темп (сек/км) с фильтрацией.
     *
     * Фильтры:
     * - слишком плохая точность -> отбрасываем
     * - скорость ниже STOP_THRESHOLD -> считаем "стоим" (0.0)
     * - скорость выше maxSpeed для режима -> отбрасываем как скачок
     *
     * @return мгновенный темп (сек/км) или null, если точку нужно отбросить
     */
    private fun processLocationToPace(location: Location): Double? {
        val speed = location.speed
        val acc = location.accuracy

        // Динамический лимит скорости
        val maxSpeed = _activityMode.value.maxSpeedMetersPerSec

        return when {
            acc > ACC_BAD_THRESHOLD -> null // Слишком шумно
            speed < STOP_THRESHOLD -> 0.0    // Стоим
            speed > maxSpeed -> null         // Нефизичный скачок для выбранного режима
            else -> 1000.0 / speed           // Секунд на км
        }
    }

    /**
     * Сглаживает мгновенный темп EMA-фильтром.
     *
     * Правила:
     * - если темп 0 (стоим) -> возвращаем 0
     * - если предыдущего emaPace ещё нет -> берём instantPace как старт
     * - иначе применяем EMA: alpha*instant + (1-alpha)*prev
     */
    private fun applyEmaFilter(instantPace: Double, acc: Float): Double {
        if (instantPace <= 0.0) return 0.0
        if (emaPace <= 0.0) return instantPace

        val alpha = calculateAlpha(acc)
        return (alpha * instantPace) + (1.0 - alpha) * emaPace
    }

    /**
     * Подбирает коэффициент EMA (alpha) по точности GPS и режиму активности.
     *
     * Идея:
     * - чем хуже точность (acc больше), тем меньше alpha -> сильнее сглаживание
     * - для бега допускаем более "живой" темп (alpha выше), чем для ходьбы
     *
     * @return alpha в диапазоне (0..1)
     */
    private fun calculateAlpha(acc: Float): Double = _activityMode.value.alphaForAccuracy(acc)


    /**
     * Форматирует темп (сек/км) в строку "мин:сек".
     * При 0 или отрицательных значениях возвращает PACE_DEFAULT.
     */
    private fun formatPace(totalSecondsPerKm: Double): String {
        if (totalSecondsPerKm <= 0) return PACE_DEFAULT
        val totalSeconds = totalSecondsPerKm.toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }


    /**
     * Отправляет текущий темп на Wear OS через DataClient (Data Layer API).
     *
     * Важно:
     * - dataClient может быть null, если init() ещё не вызывался
     * - timestamp добавляется, чтобы данные считались "новыми" и обновлялись на часах
     *
     * @param paceString темп в формате "мин:сек"
     */
    @SuppressLint("VisibleForTests")
    private fun sendPaceToWatch(paceString: String) {
        dataClient?.let { client ->
            val putDataReq = PutDataMapRequest.create("/pace_updates").apply {
                dataMap.putString("pace_key", paceString)
                dataMap.putLong("timestamp", System.currentTimeMillis())
            }.asPutDataRequest()
            client.putDataItem(putDataReq)
        }
    }


}