package com.bobon.mypace.domain.logging

import com.bobon.mypace.domain.model.WorkoutState

interface TrainingEventLogger {
    suspend fun logScreenChanged(
        screenName: String,
        sessionId: String?,
        workoutState: WorkoutState
    )
}