// domain/usecase/workout/ObserveTotalStatsUseCase.kt
package com.bobon.mypace.domain.usecase.workout

import com.bobon.mypace.domain.model.TotalStats
import com.bobon.mypace.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveTotalStatsUseCase(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<TotalStats> {
        return combine(
            workoutRepository.totalDistance,
            workoutRepository.workoutCount,
            workoutRepository.totalTimeMs
        ) { distance, count, timeMs ->
            TotalStats(
                distanceKm = distance / 1000.0,
                workoutCount = count,
                totalHours = timeMs / (1000.0 * 60 * 60)
            )
        }
    }
}