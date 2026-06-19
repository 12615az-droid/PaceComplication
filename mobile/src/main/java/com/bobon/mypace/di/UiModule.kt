package com.bobon.mypace.di

import com.bobon.mypace.ui.training.TrainingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.bobon.mypace.ui.history.HistoryViewModel
import com.bobon.mypace.ui.main.MainViewModel
import com.bobon.mypace.ui.trainingSetup.TrainingSetupViewModel

val uiModule = module {

    viewModel {
        TrainingViewModel(
            observeTrainingState = get(),
            continueTraining = get(),
            pauseTraining = get(),
            saveCurrentTraining = get(),
        )

    }
    viewModel {
        MainViewModel(
            trainingManager = get(),
            logScreenChangedUseCase = get()
        )
    }
    viewModel {
        TrainingSetupViewModel(
            trainingManager = get(),
            changeTrainingMode = get(),
            permissionManager = get(),
            checkStartWorkoutAvailability = get(),
            startTraining = get()
        )
    }
    viewModel {
        HistoryViewModel(
            observeWorkouts = get(),
            observeWorkoutsByType = get(),
            observeTotalStats = get(),
            deleteWorkout = get()
        )
    }
}