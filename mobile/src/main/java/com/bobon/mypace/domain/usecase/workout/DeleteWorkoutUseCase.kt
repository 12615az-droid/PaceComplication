package com.bobon.mypace.domain.usecase.workout

import com.bobon.mypace.domain.repository.WorkoutRepository

class DeleteWorkoutUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(id: String) {
        workoutRepository.deleteWorkout(id)
    }
}