package com.example.pacecomplication

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * PaceTimer — инкапсулированный "счетчик" для отслеживания времени тренировки.
 *
 * Ответственность:
 * - Запускать, останавливать и сбрасывать отсчёт времени.
 * - Управлять собственной корутиной, избегая утечек.
 * - Использовать существующий WorkoutTimer для преобразования секунд в строку.
 * - Предоставлять наружу StateFlow с уже отформатированным временем.
 */
class PaceTimer {

    private val _trainingTimeMs = MutableStateFlow(0L)
    val trainingTimeMs = _trainingTimeMs.asStateFlow()
    private var timerJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val timer = WorkoutTimer()



    /**
     * Запускает таймер, если он еще не запущен.
     * Немедленно обновляет UI, чтобы показать 00:00:00 при старте с нуля.
     */
    fun start() {
        if (timerJob?.isActive == true) return
        timer.startTimer(SystemClock.elapsedRealtime())
        publishNow()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                publishNow()

            }
        }
    }

    /**
     * Останавливает таймер, отменяя корутину.
     */
    fun stop() {
        val now = SystemClock.elapsedRealtime()
        timer.onStop(now)
        publish(now)
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * Сбрасывает таймер в исходное состояние.
     * Сначала останавливает таймер, затем обнуляет счетчик и StateFlow.
     */
    fun reset() {
        stop()
        timer.reset()
        _trainingTimeMs.value = 0
    }

     fun shutdown() {
        timerJob?.cancel()
        timerJob = null
        scope.cancel()
    }

    private fun publishNow() = publish(SystemClock.elapsedRealtime())

    private fun publish(nowMs: Long) {
        val ms = timer.currentMs(nowMs)
        _trainingTimeMs.value = ms
    }
}