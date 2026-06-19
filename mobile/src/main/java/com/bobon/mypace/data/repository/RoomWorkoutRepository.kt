// data/repository/RoomWorkoutRepository.kt
package com.bobon.mypace.data.repository

import com.bobon.mypace.data.mapper.toDomain
import com.bobon.mypace.data.mapper.toEntity
import com.bobon.mypace.database.WorkoutDao
import com.bobon.mypace.domain.model.Workout
import com.bobon.mypace.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomWorkoutRepository(
    private val workoutDao: WorkoutDao
) : WorkoutRepository {

    override val allWorkouts: Flow<List<Workout>> =
        workoutDao.observeAllWorkouts()
            .map { list -> list.map { it.toDomain() } }

    override fun getByType(type: Int): Flow<List<Workout>> =
        workoutDao.observeAllWorkoutsByType(type)
            .map { list -> list.map { it.toDomain() } }

    override val totalTimeMs: Flow<Long> =
        workoutDao.observeTotalTime()

    override val totalDistance: Flow<Float> =
        workoutDao.observeTotalDistance()

    override val workoutCount: Flow<Int> =
        workoutDao.observeWorkoutsCount()

    override fun getTotalTimeByType(type: Int): Flow<Long> =
        workoutDao.observeTotalTimeByType(type)

    override suspend fun insertWorkout(workout: Workout) {
        workoutDao.insertWorkout(workout.toEntity())
    }

    override suspend fun deleteWorkout(id: String) {
        workoutDao.deleteWorkoutById(id)
    }
}