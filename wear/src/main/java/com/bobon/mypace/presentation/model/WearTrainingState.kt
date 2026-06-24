package com.bobon.mypace.presentation.model

data class WearTrainingState(
    val paceText: String = "0:00",
    val workoutState: WearWorkoutState = WearWorkoutState.Idle
)