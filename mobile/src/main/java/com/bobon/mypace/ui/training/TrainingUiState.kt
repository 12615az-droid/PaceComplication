package com.bobon.mypace.ui.training

data class TrainingUiState(
    val paceText: String = "0:00",
    val gpsAccuracyMeters: Float = 0f,
    val trainingTimeMs: Long = 0L,
    val activityModeLabel: String = "",
    val totalDistanceMeters: Double = 0.0,
    val isPaused: Boolean = false,
    val isActive: Boolean = false)