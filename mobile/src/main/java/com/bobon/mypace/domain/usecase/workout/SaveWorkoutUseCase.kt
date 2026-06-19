package com.bobon.mypace.domain.usecase.workout

import com.bobon.mypace.domain.model.Workout
import com.bobon.mypace.domain.repository.WorkoutRepository

class SaveWorkoutUseCase(
    private val workoutRepository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout) {
        workoutRepository.insertWorkout(workout)
    }
}