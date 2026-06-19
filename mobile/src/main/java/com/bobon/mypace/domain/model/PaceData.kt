package com.bobon.mypace.domain.model

data class PaceData(
    val paceText: String,
    val currentSpeed: Float,
    val accuracy: Float,
    val isStopped: Boolean = false
)
