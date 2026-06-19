package com.bobon.mypace.domain.usecase.workout

import com.bobon.mypace.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutStatsUseCase(
    private val workoutRepository: WorkoutRepository
) {
    val totalTimeMs: Flow<Long> = workoutRepository.totalTimeMs
    val totalDistance: Flow<Float> = workoutRepository.totalDistance
    val workoutCount: Flow<Int> = workoutRepository.workoutCount

    fun getTotalTimeByType(type: Int): Flow<Long> {
        return workoutRepository.getTotalTimeByType(type)
    }
}