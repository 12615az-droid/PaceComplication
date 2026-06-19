// domain/repository/WorkoutRepository.kt
package com.bobon.mypace.domain.repository

import com.bobon.mypace.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {

    val allWorkouts: Flow<List<Workout>>

    fun getByType(type: Int): Flow<List<Workout>>

    val totalTimeMs: Flow<Long>

    val totalDistance: Flow<Float>

    val workoutCount: Flow<Int>

    fun getTotalTimeByType(type: Int): Flow<Long>

    suspend fun insertWorkout(workout: Workout)

    suspend fun deleteWorkout(id: String)
}