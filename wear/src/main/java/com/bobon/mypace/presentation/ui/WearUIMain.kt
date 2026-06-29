package com.bobon.mypace.presentation.ui

import MainMenuScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bobon.mypace.presentation.PaceRepository
import com.bobon.mypace.domain.model.WearWorkoutState

@Composable
fun WearUiMain() {
    val state by PaceRepository.state.collectAsState()

    when (state.workoutState) {
        WearWorkoutState.Idle,
        WearWorkoutState.Finished -> {
            MainMenuScreen({})
        }

        WearWorkoutState.Active,
        WearWorkoutState.Paused -> {
            TrainingScreen({})
        }
    }
}