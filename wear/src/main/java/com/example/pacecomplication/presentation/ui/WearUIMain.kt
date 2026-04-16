package com.example.pacecomplication.presentation.ui

import MainMenuScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.pacecomplication.presentation.PaceRepository


@Composable
fun WearUiMain() {

    val workoutState by PaceRepository.workoutState.collectAsState()

    if (workoutState == 0) MainMenuScreen({})
    if (workoutState == 1) TrainingScreen({})

}


