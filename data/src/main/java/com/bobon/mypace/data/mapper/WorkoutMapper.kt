package com.bobon.mypace.data.mapper

import com.bobon.mypace.data.database.WorkoutEntity
import com.bobon.mypace.domain.model.Workout

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