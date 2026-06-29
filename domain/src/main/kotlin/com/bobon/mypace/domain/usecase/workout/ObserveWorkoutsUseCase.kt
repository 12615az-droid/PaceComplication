package com.bobon.mypace.domain.usecase.workout

import com.bobon.mypace.domain.model.Workout
import com.bobon.mypace.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutsUseCase(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(): Flow<List<Workout>> {
        return workoutRepository.allWorkouts
    }
}