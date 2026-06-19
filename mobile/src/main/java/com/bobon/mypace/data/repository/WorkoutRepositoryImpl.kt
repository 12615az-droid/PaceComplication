package com.bobon.mypace.data.repository

import com.bobon.mypace.database.WorkoutDao
import com.bobon.mypace.database.WorkoutEntity
import com.bobon.mypace.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Репозиторий для работы с тренировками.
 * Маппит сущности БД (WorkoutEntity) в доменные модели (Workout).
 */
class WorkoutRepository(
    private val workoutDao: WorkoutDao
) {
    val allWorkouts: Flow<List<Workout>> = workoutDao.observeAllWorkouts().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getByType(type: Int): Flow<List<Workout>> = workoutDao.observeAllWorkoutsByType(type).map { entities ->
        entities.map { it.toDomain() }
    }

    val totalTimeMs: Flow<Long> = workoutDao.observeTotalTime()
    val totalDistanceDB: Flow<Float> = workoutDao.observeTotalDistance()
    val workoutCount: Flow<Int> = workoutDao.observeWorkoutsCount()

    fun getTotalTimeByType(type: Int): Flow<Long> = workoutDao.observeTotalTimeByType(type)

    suspend fun insertWorkout(workout: Workout) {
        workoutDao.insertWorkout(workout.toEntity())
    }

    suspend fun deleteWorkout(id: String) {
        workoutDao.deleteWorkoutById(id)
    }
}

// Мапперы вынесены как extension-функции для чистоты
fun WorkoutEntity.toDomain(): Workout = Workout(
    id = id,
    startTime = startTime,
    endTime = endTime,
    totalDistance = totalDistance,
    avgSpeed = avgSpeed,
    caloriesBurned = caloriesBurned,
    activityType = activityType,
    note = note
)

fun Workout.toEntity(): WorkoutEntity = WorkoutEntity(
    id = id,
    startTime = startTime,
    endTime = endTime,
    totalDistance = totalDistance,
    avgSpeed = avgSpeed,
    caloriesBurned = caloriesBurned,
    activityType = activityType,
    note = note
)
