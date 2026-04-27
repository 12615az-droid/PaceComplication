package com.example.pacecomplication.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object PaceRepository {
    private val _currentPace = MutableStateFlow("0:00")
    val currentPace: StateFlow<String> = _currentPace

    private val _workoutState = MutableStateFlow(0)
    val workoutState = _workoutState.asStateFlow()

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var watchdogJob: Job? = null
    private const val TIMEOUT_MILLIS = 15_000L

    // Переменная для хранения контекста
    private var appContext: Context? = null

    fun updateData(context: Context, newPace: String, newWorkoutState: Int) {
        // Инициализируем appContext при первом обновлении данных.
        // Используем applicationContext, чтобы избежать утечек памяти!
        if (appContext == null) {
            appContext = context.applicationContext
        }

        val oldWorkoutState = _workoutState.value

        _currentPace.value = newPace
        _workoutState.value = newWorkoutState
        Log.d("WearData", "_workoutState=${_workoutState.value}")
        Log.d("WearData", "Pace: $newPace, State: $newWorkoutState")

        if (newWorkoutState == 1) {
            resetWatchdogTimer()
        } else {
            stopWatchdogTimer()
        }

        // Логика управления сервисом на основе двух параметров
        if (oldWorkoutState != newWorkoutState) {
            val action = when {
                newWorkoutState == 1 -> "START"
                newWorkoutState == 0 || newWorkoutState == 3 -> "KILL"
                newWorkoutState == 2 -> "STOP"
                else -> null
            }

            action?.let {
                val intent = Intent(context, TrainingWearService::class.java).apply {
                    this.action = it
                }
                try {
                    if (it == "START") {
                        context.startForegroundService(intent)
                    } else {
                        intent.action?.let { msg -> Log.e("WearData", msg) }
                        context.startService(intent)
                    }
                    Log.d("WearData", "Sent action to service: $it")
                } catch (e: Exception) {
                    Log.e("WearData", "Error starting service with action $it", e)
                }
            }
        }
    }

    fun killWorkoutDueToDisconnect() {
        // Теперь appContext не null (если updateData вызывался хотя бы раз)
        appContext?.let { context ->
            Log.e("WearData", "Связь потеряна, убиваем тренировку")
            // Мы убрали _workoutState.value = 0 отсюда.
            // Просто передаем параметры в updateData, и он сам все сделает:
            // 1. Поменяет стейт на 0
            // 2. Увидит, что старый стейт был 1, а новый 0
            // 3. Отправит команду KILL в сервис
            updateData(context, "0:00", 0)
        } ?: Log.e("WearData", "Ошибка: appContext равен null. Невозможно завершить тренировку.")
    }

    private fun resetWatchdogTimer() {
        watchdogJob?.cancel()
        watchdogJob = repositoryScope.launch {
            delay(TIMEOUT_MILLIS)
            Log.e("WearData", "Таймаут 15 сек. Телефон пропал!")
            killWorkoutDueToDisconnect()
        }
    }

    private fun stopWatchdogTimer() {
        watchdogJob?.cancel()
        watchdogJob = null
        Log.e("WearData", "Таймаут сбросился!")
    }
}