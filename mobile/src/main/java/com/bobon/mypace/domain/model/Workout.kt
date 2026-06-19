package com.bobon.mypace.domain.model

data class Workout(
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val totalDistance: Double,
    val avgSpeed: Double,
    val caloriesBurned: Int,
    val activityType: Int,
    val note: String?
)
