package com.bobon.mypace.ui.model

data class WorkoutHistoryItem(
    val id: String,
    val date: String,
    val distance: String,
    val duration: String,
    val pace: String,
    val isRunning: Boolean
)