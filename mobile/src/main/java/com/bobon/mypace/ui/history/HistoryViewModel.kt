package com.bobon.mypace.ui.history

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bobon.mypace.domain.model.TotalStats
import com.bobon.mypace.domain.usecase.workout.DeleteWorkoutUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveTotalStatsUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveWorkoutsByTypeUseCase
import com.bobon.mypace.domain.usecase.workout.ObserveWorkoutsUseCase
import com.bobon.mypace.ui.model.WorkoutHistoryItem
import com.bobon.mypace.utils.PaceFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val observeWorkouts: ObserveWorkoutsUseCase,
    private val observeWorkoutsByType: ObserveWorkoutsByTypeUseCase,
    private val observeTotalStats: ObserveTotalStatsUseCase,
    private val deleteWorkout: DeleteWorkoutUseCase
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(0)
    val selectedFilter: StateFlow<Int> = _selectedFilter

    @SuppressLint("DefaultLocale")
    val historyList = _selectedFilter
        .flatMapLatest { filter ->
            if (filter == 0) {
                observeWorkouts()
            } else {
                observeWorkoutsByType(filter)
            }
        }
        .map { workouts ->
            workouts.map { workout ->
                WorkoutHistoryItem(
                    id = workout.id,
                    date = PaceFormatter.formatDate(workout.startTime),
                    distance = String.format("%.2f км", workout.totalDistance / 1000.0),
                    duration = PaceFormatter.formatDuration(workout.startTime, workout.endTime),
                    pace = PaceFormatter.calculateAndFormatPace(
                        workout.startTime,
                        workout.endTime,
                        workout.totalDistance
                    ),
                    isRunning = workout.activityType == 1
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalStats = observeTotalStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TotalStats(0.0, 0, 0.0)
        )

    fun selectFilter(index: Int) {
        _selectedFilter.value = index
    }

    fun deleteTrack(id: String) {
        viewModelScope.launch {
            deleteWorkout(id)
        }
    }
}