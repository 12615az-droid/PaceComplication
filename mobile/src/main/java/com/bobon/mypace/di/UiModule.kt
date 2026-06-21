package com.bobon.mypace.di

import com.bobon.mypace.ui.training.TrainingViewModel
import com.bobon.mypace.ui.history.HistoryViewModel
import com.bobon.mypace.ui.main.MainViewModel
import com.bobon.mypace.ui.trainingSetup.TrainingSetupViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    viewModel {
        TrainingViewModel(
            observeTrainingState = get(),
            continueTraining = get(),
            pauseTraining = get(),
            saveCurrentTraining = get()
        )
    }

    viewModel {
        MainViewModel(
            observeWorkoutState = get(),
            logScreenChangedUseCase = get()
        )
    }

    viewModel {
        TrainingSetupViewModel(
            observeActivityMode = get(),
            changeTrainingMode = get(),
            checkStartWorkoutAvailability = get(),
            startTraining = get(),
            handlePermissionResult = get(),
            getRequiredPermissionsUseCase = get(),
            markPermissionRationaleShown = get(),
            shouldGoToSettings = get(),
            isLocationEnabled = get()
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