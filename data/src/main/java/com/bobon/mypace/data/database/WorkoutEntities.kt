package com.bobon.mypace.data.database


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = false) val id: String,
    val startTime: Long,
    val endTime: Long,
    val totalDistance: Double,
    val avgSpeed: Double,
    val caloriesBurned: Int,
    val activityType: Int,
    val note: String?,
)




