package com.bobon.mypace.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobon.mypace.LocationRepository
import com.bobon.mypace.database.WorkoutEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

// Koin сам подставит сюда твой готовый LocationRepository
class TrainingViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    val currentPace = repository.currentPace
    val currentGPSAccuracy = repository.currentGPSAccuracy
    val trainingTimeMs = repository.trainingTimeMs
    val activityMode = repository.activityMode
    val workoutState = repository.workoutState
    val isGoalSetupOpen = repository.isGoalSetupOpen
    val totalDistance = repository.totalDistance

    val trainingId = repository.currentSessionId

    val allWorkouts = repository.allWorkouts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    private val _selectedFilter = MutableStateFlow(0)
    val selectedFilter: StateFlow<Int> = _selectedFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val workouts: StateFlow<List<WorkoutEntity>> = _selectedFilter
        .flatMapLatest { filter ->
            when (filter) {
                1 -> repository.getByType(1) // Бег
                2 -> repository.getByType(2) // Ходьба
                else -> repository.allWorkouts
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Общая статистика (для "Все")
    val totalStats = combine(
        repository.totalDistance,
        repository.workoutCount,
        repository.totalTimeMs
    ) { distance, count, timeMs ->
        TotalStats(
            distanceKm = distance / 1000.0,
            workoutCount = count,
            totalHours = timeMs / (1000.0 * 60 * 60)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TotalStats(0.0, 0, 0.0)
    )

    fun selectFilter(index: Int) {
        _selectedFilter.value = index
    }


data class TotalStats(
    val distanceKm: Double,
    val workoutCount: Int,
    val totalHours: Double
)



    fun startTracking() {
        viewModelScope.launch {
            repository.startTracking()
        }
    }

    fun stopTracking() {
        viewModelScope.launch {
            repository.stopTracking()
        }
    }

    fun saveTracking() {
        val id = trainingId.value ?: return
        viewModelScope.launch {
            val speedKmh = if (trainingTimeMs.value > 0) {
                totalDistance.value / (trainingTimeMs.value / 3_600_000.0)
            } else {
                0.0
            }

            val workout = WorkoutEntity(
                id = id,
                startTime = repository.startTime.value ?: return@launch,
                endTime = System.currentTimeMillis(),
                totalDistance = totalDistance.value,
                avgSpeed = speedKmh,
                caloriesBurned = 0,
                activityType = activityMode.value.id,
                note = null,
            )
            repository.insertWorkout(workout)
            repository.saveTracking()
        }
    }

    fun changeMode() {
        repository.changeMode()
    }

    fun openGoalSetupDialog() {
        repository.setTrainingGoalDialogOpen(true)
    }

    fun closeGoalSetupDialog() {
        repository.setTrainingGoalDialogOpen(false)
    }


    fun onModeChanged() = changeMode()

    fun onOpenGoalSetup() = openGoalSetupDialog()

    fun onCloseGoalSetup() = closeGoalSetupDialog()
    fun logScreenChanged(screenName: String) = repository.logScreenChanged(screenName)
}
