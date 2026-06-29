package com.bobon.mypace.domain.usecase.workout

import com.bobon.mypace.domain.model.Workout
import com.bobon.mypace.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutsByTypeUseCase(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(type: Int): Flow<List<Workout>> {
        return workoutRepository.getByType(type)
    }
}